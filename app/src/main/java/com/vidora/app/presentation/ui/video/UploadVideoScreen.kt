package com.vidora.app.presentation.ui.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.vidora.app.R
import com.vidora.app.data.remote.models.auth.UserProfile
import com.vidora.app.presentation.navigation.main.Screen
import com.vidora.app.presentation.ui.profile.ProfileViewModel
import com.vidora.app.presentation.video.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVideoScreen(
    navController: NavHostController,
    viewModel: VideoViewModel = hiltViewModel(),
    profileVm: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val uiState by profileVm.uiState.collectAsState()

    var selectedVideoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedThumbnailUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("public") }
    var isAgeRestricted by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1) Handle back: clear picks first, else pop
    BackHandler {
        when {
            selectedThumbnailUri != null -> selectedThumbnailUri = null
            selectedVideoUri     != null -> selectedVideoUri = null
            else                         -> navController.popBackStack()
        }
    }

    // 2) While profile loading
    if (uiState.loading && uiState.profile == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    // 3) On error loading profile
    uiState.error?.let { /* show full‑screen error if desired */ }

    // 4) If user has no channel → block
    val profile = uiState.profile
    if (profile != null && profile.channelId == null) {
        AlertDialog(
            onDismissRequest = { /* no-op */ },
            title   = { Text("Create a channel first") },
            text    = { Text("You need a channel before uploading videos.") },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigate(Screen.CreateChannelScreen.route)
                }) { Text("Create Channel") }
            },
            dismissButton = {
                TextButton(onClick = {
                    navController.popBackStack()
                }) { Text("Go Back") }
            }
        )
        return
    }


    LaunchedEffect(state) {
        when (state) {
            is VideoUiState.Success -> {
                Toast
                    .makeText(context, "Upload successful!", Toast.LENGTH_SHORT)
                    .show()
                viewModel.clearState()          // reset so we don't fire again
                navController.popBackStack()
            }
            is VideoUiState.Error -> {
                val msg = (state as VideoUiState.Error).message
                Toast
                    .makeText(context, "Upload failed: $msg", Toast.LENGTH_LONG)
                    .show()
                viewModel.clearState()          // clear the error so it doesn’t loop
            }
            else -> Unit
        }
    }

    val videoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedVideoUri = uri
        }

    val thumbnailPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedThumbnailUri = uri
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Upload Video") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Video Picker Card ---
            Text("Video", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable {
                        if (selectedVideoUri != null) {
                            selectedVideoUri = null
                        } else {
                            videoPicker.launch("video/*")
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEEEEEE)
                )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (selectedVideoUri != null) {
                        val thumbnail = remember(selectedVideoUri) {
                            generateBitmapFromVideoUri(context, selectedVideoUri!!)
                        }
                        if (thumbnail != null) {
                            Image(
                                bitmap = thumbnail.asImageBitmap(),
                                contentDescription = "Video Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Overlay remove and preview buttons
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                                IconButton(onClick = { selectedVideoUri = null }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color.White
                                    )
                                }
                                IconButton(onClick = {
                                    selectedVideoUri?.let {
                                        navController.navigate("videoPreview/${Uri.encode(it.toString())}")
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Preview",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Tap to select video", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Thumbnail Picker Card ---
            Text("Thumbnail", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable {
                        thumbnailPicker.launch("image/*")
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEEEEEE)
                )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (selectedThumbnailUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedThumbnailUri),
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            "Tap to pick thumbnail (optional)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Fields ---
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title*") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = categories,
                onValueChange = { categories = it },
                label = { Text("Categories (comma separated)*") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // --- Visibility ---
            Text("Visibility", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = visibility == "public", onClick = { visibility = "public" })
                Text("Public")
                Spacer(Modifier.width(16.dp))
                RadioButton(
                    selected = visibility == "private",
                    onClick = { visibility = "private" })
                Text("Private")
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isAgeRestricted, onCheckedChange = { isAgeRestricted = it })
                Text("Age Restricted")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agreedToTerms, onCheckedChange = { agreedToTerms = it })
                Text("I agree to the Terms and Conditions")
            }

            Spacer(Modifier.height(16.dp))

            // --- Upload Button ---
            Button(
                onClick = {
                    if (title.isBlank() || selectedVideoUri == null || categories.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill in all required fields.")
                        }
                        return@Button
                    }

                    if (!agreedToTerms) {
                        scope.launch {
                            snackbarHostState.showSnackbar("You must agree to the terms.")
                        }
                        return@Button
                    }

                    val videoPart = selectedVideoUri?.toMultipart("videoFile", context)
                    var thumbnailPart = selectedThumbnailUri?.toMultipart("thumbnailImage", context)

                    if (thumbnailPart == null && selectedVideoUri != null) {
                        // Try to auto-generate thumbnail
                        generateThumbnailFromVideo(selectedVideoUri!!, context)?.let {
                            thumbnailPart = it
                        }
                    }

                    if (videoPart != null) {
                        viewModel.uploadVideo(
                            title,
                            description,
                            categories,
                            tags,
                            visibility,
                            videoPart,
                            thumbnailPart
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is VideoUiState.Loading
            ) {
                if (state is VideoUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Text("Upload")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPreviewScreen(videoUri: Uri) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview Video") },
                navigationIcon = {
                    IconButton(onClick = { (context as? android.app.Activity)?.onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    setVideoURI(videoUri)
                    setOnPreparedListener {
                        it.isLooping = true
                        start()
                    }
                }
            }
        )
    }
}

fun Uri.toMultipart(partName: String, context: Context): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(this) ?: throw IOException("Can't open URI")
    val bytes = inputStream.readBytes()
    val mimeType = contentResolver.getType(this) ?: "application/octet-stream"
    val fileName = getFileNameFromUri(context, this) ?: "video.mp4"
    val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, fileName, requestFile)
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) {
            return it.getString(nameIndex)
        }
    }
    return null
}

fun generateThumbnailFromVideo(videoUri: Uri, context: Context): MultipartBody.Part? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, videoUri)
        val bitmap: Bitmap? = retriever.frameAtTime   // Grab a frame from 0th millisecond

        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val byteArray = stream.toByteArray()

            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(
                name = "thumbnailImage",
                filename = "autogenerated_thumbnail.jpg",
                body = requestBody
            )
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        retriever.release()
    }
}

fun generateBitmapFromVideoUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val frame = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        frame
    } catch (e: Exception) {
        null
    }
}

