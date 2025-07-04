package com.vidora.app.presentation.ui.channel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.vidora.app.R
import com.vidora.app.presentation.channel.ChannelState
import com.vidora.app.presentation.channel.ChannelViewModel
import com.vidora.app.presentation.ui.channel.components.BottomSheet
import com.vidora.app.utils.Constants
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EditChannelScreen(
    channelId: String,
    navController: NavHostController,
    viewModel: ChannelViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load channel data once
    LaunchedEffect(channelId) {
        viewModel.loadChannel(channelId)
    }

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var twitter by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var tagList by remember {
        mutableStateOf(listOf<String>())
    }

    // Image URIs
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }

    // Which picker to show? Pair<"banner"|"profile", isVisible>
    var showPicker by remember { mutableStateOf("" to false) }
    val tmpFile = remember { mutableStateOf<File?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            when (showPicker.first) {
                "banner" -> bannerUri = it
                "profile" -> profileUri = it
            }
            showPicker = "" to false
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tmpFile.value != null) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tmpFile.value!!
            )
            when (showPicker.first) {
                "banner" -> bannerUri = uri
                "profile" -> profileUri = uri
            }
        }
        showPicker = "" to false
    }

    // Pull‑to‑refresh
    val pullState = rememberPullRefreshState(
        refreshing = uiState is ChannelState.Loading,
        onRefresh = { viewModel.loadChannel(channelId) }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Channel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullState)
        ) {
            when (uiState) {
                is ChannelState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is ChannelState.Error -> {
                    Toast.makeText(
                        context,
                        (uiState as ChannelState.Error).message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ChannelState.Success -> {
                    val (channel, signedProfile, signedBanner) = uiState as ChannelState.Success

                    // Initialize form once
                    LaunchedEffect(channel) {
                        name = channel.name
                        description = channel.description.orEmpty()
                        location = channel.location.orEmpty()
                        tags = channel.tags
                            ?.takeIf { it.isNotEmpty() }
                            ?.joinToString(", ")
                            ?: ""
                        tagList      = channel.tags ?: emptyList()
                        contactEmail = channel.contactEmail.orEmpty()
                        facebook = channel.socialLinks?.get("facebook").orEmpty()
                        twitter = channel.socialLinks?.get("twitter").orEmpty()
                        instagram = channel.socialLinks?.get("instagram").orEmpty()
                    }

                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Banner picker
                        Box(Modifier
                            .fillMaxWidth()
                            .height(160.dp)) {
                            val bannerModel = remember(bannerUri, signedBanner) {
                                bannerUri ?: signedBanner ?: Constants.DEFAULT_BANNER_IMG
                            }
                            AsyncImage(
                                model = bannerModel,
                                contentDescription = "Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { showPicker = "banner" to true },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit banner")
                            }
                        }

                        // Profile picker
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .clickable { showPicker = "profile" to true }
                            ) {
                                val profileModel = remember(profileUri, signedProfile) {
                                    profileUri ?: signedProfile ?: Constants.DEFAULT_CHANNEL_IMG
                                }
                                AsyncImage(
                                    model = profileModel,
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            OutlinedButton(onClick = { showPicker = "profile" to true }) {
                                Text("Change Photo")
                            }
                        }

                        // Text fields
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Channel Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // 2️⃣ The text field now updates both the raw string and the parsed list:
                        OutlinedTextField(
                            value = tags,
                            onValueChange = {
                                tags = it
                                tagList = it
                                    .split(",")
                                    .map { t -> t.trim() }
                                    .filter { t -> t.isNotBlank() }
                            },
                            label = { Text("Tags (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 3️⃣ Beneath it, render your chips:
                        if (tagList.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tagList.forEach { tag ->
                                    AssistChip(
                                        onClick = { /* optional: allow editing this tag */ },
                                        label = { Text(tag) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove $tag",
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        // remove this single tag:
                                                        tagList = tagList.filterNot { it == tag }
                                                        // also keep your raw `tags` string in sync:
                                                        tags = tagList.joinToString(", ")
                                                    }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Contact Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Social links
                        Text("Social Links", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = facebook,
                            onValueChange = { facebook = it },
                            label = { Text("Facebook URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = twitter,
                            onValueChange = { twitter = it },
                            label = { Text("Twitter URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = instagram,
                            onValueChange = { instagram = it },
                            label = { Text("Instagram URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                // image parts (as before)…
                                val bannerPart =
                                    bannerUri?.toFileSafely(context)?.asMultipart("bannerImage")
                                val profilePart =
                                    profileUri?.toFileSafely(context)?.asMultipart("profileImage")

                                // collect socials
                                val socialMap = mapOf(
                                    "facebook" to facebook,
                                    "twitter" to twitter,
                                    "instagram" to instagram
                                ).filterValues { it.isNotBlank() }

                                viewModel.updateChannel(
                                    channelId = channel.id,
                                    name = name,
                                    description = description,
                                    location = location,
                                    tagsCsv = tags,
                                    contactEmail = contactEmail,
                                    socialLinks = socialMap,
                                    profileImagePart = profilePart,
                                    bannerImagePart = bannerPart
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Changes")
                        }
                    }
                }

                else -> println()
            }

            // Picker bottom sheet
            if (showPicker.second) {
                BottomSheet(
                    showBottomSheet = mutableStateOf(true),
                    text1 = "Take Photo",
                    text2 = "Choose from Gallery",
                    icon1 = R.drawable.camera,
                    icon2 = R.drawable.photo_library,
                    onClick1 = {
                        val f = File.createTempFile("tmp", ".jpg", context.cacheDir)
                        tmpFile.value = f
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            f
                        )
                        cameraLauncher.launch(uri)
                    },
                    onClick2 = {
                        galleryLauncher.launch("image/*")
                    }
                )
            }

            // Pull‑to‑refresh indicator
            PullRefreshIndicator(
                refreshing = uiState is ChannelState.Loading,
                state = pullState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}

// Uri → File
private fun Uri.toFileSafely(context: Context): File? {
    return try {
        val input = context.contentResolver.openInputStream(this) ?: return null
        val tmp = File.createTempFile("pick", ".jpg", context.cacheDir)
        tmp.outputStream().use { input.copyTo(it) }
        tmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


// File → MultipartBody.Part
private fun File.asMultipart(field: String): MultipartBody.Part {
    val req = this.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(field, name, req)
}
