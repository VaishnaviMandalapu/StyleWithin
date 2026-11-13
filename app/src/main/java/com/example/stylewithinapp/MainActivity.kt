package com.example.stylewithinapp

import android.os.Bundle
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.LaunchedEffect


private val Cream = Color(0xFFFAF6F3)
private val Plum = Color(0xFF704B68)
private val MutedText = Color(0xFF756B69)

// Represents one item saved in the StyleWithin wardrobe.
data class ClothingItem(
    val id: Long = 0,
    val name: String,
    val category: String,
    val colour: String,
    val photoPath: String? = null,
    val style: String = "Casual"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                StyleWithinApp()
            }
        }
    }
}

@Composable
fun StyleWithinApp() {

    // Get the Android context so we can open our local database.
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        addSamplePhotos(context)
    }

    // Create the database once while the app is running.
    val database = remember { WardrobeDatabase(context) }

    var showLogin by rememberSaveable { mutableStateOf(false) }
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var showAddClothing by rememberSaveable { mutableStateOf(false) }
    var showRecommendations by rememberSaveable { mutableStateOf(false) }

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var submitted by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Load saved clothing from SQLite when the screen starts.
    var clothingItems by remember {
        mutableStateOf(
            database.getAllClothing().map { record ->
                ClothingItem(
                    id = record.id,
                    name = record.name,
                    category = record.category,
                    colour = record.colour,
                    photoPath = record.photoPath
                )
            }
        )
    }

    // Reload the wardrobe after saving a new item.
    fun refreshClothing() {
        clothingItems = database.getAllClothing().map { record ->
            ClothingItem(
                id = record.id,
                name = record.name,
                category = record.category,
                colour = record.colour,
                photoPath = record.photoPath,
                style = record.style
            )
        }
    }

    val validEmail = email.matches(
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    )

    val validPassword =
        password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }

    if (isLoggedIn) {

        if (showRecommendations) {
            OutfitRecommendationScreen(
                items = clothingItems,
                onBack = {
                    showRecommendations = false
                }
            )

        } else if (showAddClothing) {
            AddClothingScreen(
                category = selectedCategory,
                onBack = {
                    showAddClothing = false
                },
                onSave = { item ->

                    // Store the clothing details in the database.
                    database.addClothing(
                        name = item.name,
                        category = item.category,
                        colour = item.colour,
                        photoPath = item.photoPath,
                        style = item.style
                    )

                    // Refresh the screen so the new item appears.
                    refreshClothing()
                    showAddClothing = false
                }
            )

        } else if (
            selectedCategory.isNotEmpty() &&
            selectedCategory != "WARDROBE"
        ) {

            CategoryScreen(
                category = selectedCategory,
                items = clothingItems.filter {
                    it.category == selectedCategory
                },
                onBack = {
                    selectedCategory = "WARDROBE"
                },
                onAddClothing = {
                    showAddClothing = true
                },
                onLogout = {
                    isLoggedIn = false
                    selectedCategory = ""
                    showAddClothing = false
                    showLogin = true
                    fullName = ""
                    email = ""
                    password = ""
                    message = "Successfully logged out!"
                }
            )

        } else if (selectedCategory == "WARDROBE" &&
            !showAddClothing && !showRecommendations
            ) {

            WardrobeScreen(onOpenRecommendations = {
                showRecommendations = true
            },
                onCategorySelected = {
                    selectedCategory = it
                },
                onLogout = {
                    isLoggedIn = false
                    selectedCategory = ""
                    showLogin = true
                    fullName = ""
                    email = ""
                    password = ""
                    message = "Successfully logged out!"
                }
            )

        } else {

            WelcomeScreen(
                onOpenWardrobe = {},
                onLogout = {
                    isLoggedIn = false
                    showLogin = true
                    fullName = ""
                    email = ""
                    password = ""
                    message = "Successfully logged out!"
                },
                onOpenWardrobeActual = {
                    selectedCategory = "WARDROBE"
                }
            )
        }

    } else {

        AuthenticationScreen(
            showLogin = showLogin,
            fullName = fullName,
            email = email,
            password = password,
            message = message,
            submitted = submitted,
            onFullNameChange = {
                fullName = it
                message = ""
            },
            onEmailChange = {
                email = it
                message = ""
            },
            onPasswordChange = {
                password = it
                message = ""
            },
            onToggleLogin = {
                showLogin = !showLogin
                submitted = false
                message = ""
                password = ""
            },
            onSubmit = {
                submitted = true
                message = ""

                if (
                    validEmail &&
                    validPassword &&
                    (showLogin || fullName.isNotBlank())
                ) {
                    if (showLogin) {
                        isLoggedIn = true
                    } else {
                        message =
                            "Registration details look valid. Please login to continue."
                    }
                }
            },
            nameError = submitted && !showLogin && fullName.isBlank(),
            emailError = submitted && !validEmail,
            passwordError = submitted && !validPassword
        )
    }
}


@Composable
fun AuthenticationScreen(
    showLogin: Boolean,
    fullName: String,
    email: String,
    password: String,
    message: String,
    submitted: Boolean,
    nameError: Boolean,
    emailError: Boolean,
    passwordError: Boolean,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleLogin: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (showLogin) "Welcome Back" else "Create Account",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Plum
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (showLogin && message.isNotEmpty()) {
                Text(message, color = Plum)
            }

            Text(
                if (showLogin)
                    "Login to your StyleWithin account."
                else
                    "Create an account to get started.",
                color = MutedText
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!showLogin) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Full name") },
                    singleLine = true,
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (nameError) {
                    ErrorText("Please enter your full name.")
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                isError = emailError,
                modifier = Modifier.fillMaxWidth()
            )

            if (emailError) {
                ErrorText("Please enter a valid email address.")
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                isError = passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            if (passwordError) {
                ErrorText(
                    "Use 8+ characters with uppercase, lowercase, a number and a special character."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Plum
                )
            ) {
                Text(if (showLogin) "Login" else "Create Account")
            }

            if (message.isNotEmpty() && !showLogin) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(message, color = Plum)
            }

            TextButton(onClick = onToggleLogin) {
                Text(
                    if (showLogin)
                        "Don't have an account? Register"
                    else
                        "Already have an account? Login",
                    color = Plum
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onOpenWardrobe: () -> Unit,
    onLogout: () -> Unit,
    onOpenWardrobeActual: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onLogout) {
                    Text("↪ Logout", color = Plum)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Welcome to StyleWithin!",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = Plum
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(
                    id = R.drawable.stylewithin_logo
                ),
                contentDescription = "StyleWithin logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Discover the style within you.",
                color = MutedText
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onOpenWardrobeActual,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Plum
                )
            ) {
                Text("My Wardrobe")
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun WardrobeScreen(
    onCategorySelected: (String) -> Unit,
    onLogout: () -> Unit,
    onOpenRecommendations: () -> Unit
) {
    val categories = listOf(
        "Tops",
        "Bottoms",
        "Dresses",
        "Shoes",
        "Bags",
        "Jewellery",
        "Outerwear",
        "Other Accessories"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "My Wardrobe",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Plum
                )

                TextButton(onClick = onLogout) {
                    Text("Logout", color = Plum)
                }
            }

            Text(
                "Your clothes, your style.",
                color = MutedText
            )
            Button(
                onClick = onOpenRecommendations,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Plum
                )
            ) {
                Text("✨ Style Me – Get Outfit Ideas")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.height(24.dp))

            categories.forEach { category ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            onCategorySelected(category)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        category,
                        modifier = Modifier.padding(20.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Plum
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(
    category: String,
    items: List<ClothingItem>,
    onBack: () -> Unit,
    onAddClothing: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Plum)
                }

                TextButton(onClick = onLogout) {
                    Text("Logout", color = Plum)
                }
            }

            Text(
                category,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Plum
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Your $category collection",
                color = MutedText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddClothing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Plum
                )
            ) {
                Text("+ Add Clothing")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (items.isEmpty()) {
                Text(
                    "No clothing items added yet.",
                    color = MutedText
                )
            } else {
                items.forEach { item ->
                    ClothingCard(item)
                }
            }
        }
    }
}

@Composable
fun AddClothingScreen(
    category: String,
    onBack: () -> Unit,
    onSave: (ClothingItem) -> Unit
) {
    val context = LocalContext.current

    var itemName by rememberSaveable { mutableStateOf("") }
    var colour by rememberSaveable { mutableStateOf("") }
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var selectedMethod by rememberSaveable { mutableStateOf("Manual Entry") }
    var permissionMessage by remember { mutableStateOf("") }

    // Copy an image into the app's private files directory.
    fun saveImageToApp(bitmap: Bitmap): String? {
        return try {
            val imageFolder = File(context.filesDir, "wardrobe_photos")
            if (!imageFolder.exists()) {
                imageFolder.mkdirs()
            }

            val imageFile = File(
                imageFolder,
                "clothing_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(imageFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }

            imageFile.absolutePath
        } catch (exception: Exception) {
            permissionMessage = "Could not save the photo."
            null
        }
    }

    // Gallery selection. No broad storage permission is needed.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        if (uri != null) {
            try {
                val imageFolder = File(context.filesDir, "wardrobe_photos")
                if (!imageFolder.exists()) {
                    imageFolder.mkdirs()
                }

                val imageFile = File(
                    imageFolder,
                    "gallery_${System.currentTimeMillis()}.jpg"
                )

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (imageFile.exists() && imageFile.length() > 0) {
                    photoPath = imageFile.absolutePath
                    permissionMessage = ""
                } else {
                    permissionMessage = "Could not read the selected photo."
                }

            } catch (exception: Exception) {
                permissionMessage = "Could not save the selected photo."
            }
        }
    }

    // The camera preview returns a Bitmap.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->

        if (bitmap != null) {
            photoPath = saveImageToApp(bitmap)
            permissionMessage = ""
        } else {
            permissionMessage = "No photo was taken."
        }
    }

    // Ask Android for camera permission at runtime.
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                cameraLauncher.launch(null)
            } else {
                permissionMessage =
                    "Camera permission was denied. You can still add clothing manually or use the gallery."
            }
        }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Plum)
                }
            }

            Text(
                "Add Clothing",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Plum
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Category: $category", color = MutedText)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Choose how to add your item",
                fontWeight = FontWeight.SemiBold,
                color = Plum
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    selectedMethod = "Manual Entry"
                    photoPath = null
                    permissionMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (selectedMethod == "Manual Entry") Plum
                        else MutedText
                )
            ) {
                Text("Manual Entry")
            }

            Button(
                onClick = {
                    selectedMethod = "Gallery"
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (selectedMethod == "Gallery") Plum
                        else MutedText
                )
            ) {
                Text("Upload from Gallery")
            }

            Button(
                onClick = {
                    selectedMethod = "Camera"

                    // Check whether camera access has already been granted.
                    if (
                        context.checkSelfPermission(
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (selectedMethod == "Camera") Plum
                        else MutedText
                )
            ) {
                Text("Take a Photo")
            }

            if (permissionMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    permissionMessage,
                    color = Plum,
                    fontSize = 13.sp
                )
            }


            if (photoPath != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Photo selected", color = Plum)
                ClothingPhoto(photoPath = photoPath)
                Spacer(modifier = Modifier.height(16.dp))
            }

// Show name and colour fields only for manual entry.
            if (selectedMethod == "Manual Entry") {

                OutlinedTextField(
                    value = itemName,
                    onValueChange = {
                        itemName = it
                        showError = false
                    },
                    label = { Text("Clothing name") },
                    placeholder = { Text("e.g. White T-shirt") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = colour,
                    onValueChange = {
                        colour = it
                        showError = false
                    },
                    label = { Text("Colour") },
                    placeholder = { Text("e.g. White") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text(
                        "Please enter the clothing name and colour.",
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (
                        selectedMethod == "Manual Entry" &&
                        (itemName.isBlank() || colour.isBlank())
                    ) {
                        showError = true
                    } else {
                        onSave(
                            ClothingItem(
                                name = itemName.ifBlank { "Gallery Item" },
                                category = category,
                                colour = colour.ifBlank { "Not specified" },
                                photoPath = photoPath
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Plum
                )
            ) {
                Text("Save Clothing")
            }
        }
    }
}


@Composable
fun ClothingCard(item: ClothingItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Show the saved photo if this item has one.
            if (item.photoPath != null) {
                ClothingPhoto(photoPath = item.photoPath)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                item.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Plum
            )

            Text(
                "Colour: ${item.colour}",
                color = MutedText
            )
        }
    }
}

@Composable
fun ClothingPhoto(photoPath: String?) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            if (photoPath != null) {
                imageView.setImageURI(Uri.fromFile(File(photoPath)))
            } else {
                imageView.setImageDrawable(null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    )
}

@Composable
fun ErrorText(message: String) {
    Text(
        text = message,
        color = Color(0xFF9C27B0),
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}

@Composable
fun SampleClothingImages(category: String) {

    // Select sample images based on the wardrobe category.
    val sampleImages = when (category) {
        "Tops" -> listOf(
            R.drawable.white_shirt
        )

        "Outerwear" -> listOf(
            R.drawable.red_jacket
        )

        else -> emptyList()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (sampleImages.isNotEmpty()) {
            Text(
                "Sample Clothing",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Plum
            )

            sampleImages.forEach { image ->
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Sample clothing image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
fun addSamplePhotos(context: android.content.Context) {
    val photos = listOf(
        R.drawable.white_shirt to "white_shirt.jpg",
        R.drawable.red_jacket to "red_jacket.jpg",
        R.drawable.blue_gymcloth to "blue_gymcloth.jpg"
    )

    val prefs = context.getSharedPreferences("StyleWithin", 0)
    if (prefs.getBoolean("photosAdded", false)) return

    var success = true

    photos.forEach { (image, name) ->
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/StyleWithin"
                )
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: throw Exception("Could not create image")

            context.contentResolver.openOutputStream(uri)?.use { output ->
                context.resources.openRawResource(image).use { input ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Could not save image")

        } catch (e: Exception) {
            success = false
        }
    }

    if (success) {
        prefs.edit().putBoolean("photosAdded", true).apply()
    }
}


@Composable
fun OutfitRecommendationScreen(
    items: List<ClothingItem>,
    onBack: () -> Unit
) {
    val occasions = listOf(
        "College", "Work", "Outing", "Dinner", "Date",
        "Movie", "Party", "Wedding", "Gym",
        "Walking", "Travel", "Casual Day"
    )

    var selectedOccasion by rememberSaveable {
        mutableStateOf("College")
    }
    var expanded by remember {
        mutableStateOf(false)
    }
    var recommendedItems by remember {
        mutableStateOf<List<ClothingItem>>(emptyList())
    }
    var message by remember {
        mutableStateOf("")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back", color = Plum)
            }

            Text(
                "Style Me",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Plum
            )

            Text(
                "Discover an outfit from your wardrobe.",
                color = MutedText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Choose your occasion",
                color = Plum,
                fontWeight = FontWeight.SemiBold
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedOccasion)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    occasions.forEach { occasion ->
                        DropdownMenuItem(
                            text = { Text(occasion) },
                            onClick = {
                                selectedOccasion = occasion
                                expanded = false
                                recommendedItems = emptyList()
                                message = ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val tops = items.filter { it.category.equals("Tops", true) }
                    val bottoms = items.filter { it.category.equals("Bottoms", true) }
                    val dresses = items.filter { it.category.equals("Dresses", true) }
                    val shoes = items.filter { it.category.equals("Shoes", true) }

                    val outfit = mutableListOf<ClothingItem>()

                    // Gym and walking outfits should use comfortable separates.
                    if (selectedOccasion == "Gym") {

                        // Only select sports bottoms for Gym.
                        val gymBottoms = bottoms.filter {
                            val name = it.name.lowercase()
                            name.contains("leggings") ||
                                    name.contains("joggers") ||
                                    name.contains("gym shorts") ||
                                    name.contains("sports shorts") ||
                                    name.contains("track pants")
                        }

                        if (tops.isNotEmpty() && gymBottoms.isNotEmpty()) {
                            outfit.add(tops.first())
                            outfit.add(gymBottoms.first())
                            shoes.firstOrNull()?.let { outfit.add(it) }
                        }

                    } else if (selectedOccasion == "Walking") {

                        if (tops.isNotEmpty() && bottoms.isNotEmpty()) {
                            outfit.add(tops.first())
                            outfit.add(bottoms.first())
                            shoes.firstOrNull()?.let { outfit.add(it) }
                        }

                    } else {
                        // Formal occasions can use a dress or a top and bottom.
                        if (
                            selectedOccasion in listOf(
                                "Work", "Dinner", "Date", "Party", "Wedding"
                            ) && dresses.isNotEmpty()
                        ) {
                            outfit.add(dresses.first())
                        } else if (tops.isNotEmpty() && bottoms.isNotEmpty()) {
                            outfit.add(tops.first())
                            outfit.add(bottoms.first())
                        } else if (dresses.isNotEmpty()) {
                            outfit.add(dresses.first())
                        }

                        shoes.firstOrNull()?.let { outfit.add(it) }

                        // Add accessories only when appropriate.
                        items.firstOrNull { it.category.equals("Bags", true) }
                            ?.let { outfit.add(it) }

                        items.firstOrNull { it.category.equals("Jewellery", true) }
                            ?.let { outfit.add(it) }

                        items.firstOrNull { it.category.equals("Outerwear", true) }
                            ?.let { outfit.add(it) }
                    }

                    recommendedItems = outfit

                    message = if (outfit.isNotEmpty()) {
                        "Your $selectedOccasion outfit is ready!"
                    } else {
                        "Add suitable clothing to your wardrobe to generate an outfit."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Plum
                )
            ) {
                Text("Generate Outfit")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (message.isNotEmpty()) {
                Text(
                    message,
                    color = Plum,
                    fontWeight = FontWeight.SemiBold
                )
            }

            recommendedItems.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            item.name,
                            color = Plum,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text("Category: ${item.category}", color = MutedText)
                        Text("Colour: ${item.colour}", color = MutedText)
                    }
                }
            }
        }
    }
}
