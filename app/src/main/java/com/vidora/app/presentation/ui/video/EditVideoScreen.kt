package com.vidora.app.presentation.ui.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.vidora.app.presentation.video.VideoUiState
import com.vidora.app.presentation.video.VideoViewModel
import com.vidora.app.utils.Constants
import com.vidora.app.utils.toRequestBody
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVideoScreen(
    navController: NavHostController,
    videoId: String,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.detail.collectAsState()
    val mainState by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackHost = remember { SnackbarHostState() }

    // 1) Local form state, start empty
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedThumbnailUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("public") }
    var isAgeRestricted by remember { mutableStateOf(false) }

    val thumbnailPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> selectedThumbnailUri = uri }

    LaunchedEffect(videoId) {
        viewModel.showVideo(videoId)
    }

    LaunchedEffect(state, mainState) {
        when (state) {
            is VideoUiState.DetailLoaded -> {
                val vid = (state as VideoUiState.DetailLoaded).video
                title = vid.title
                description = vid.description
                categories = vid.categories.joinToString(",")
                tags = vid.tags.joinToString(",")
                visibility = vid.visibility
                isAgeRestricted = vid.isAgeRestricted
                selectedThumbnailUri = null
            }
            else -> {}
        }

        // Handle update success from main state
        if (mainState is VideoUiState.UpdateSuccess) {
            scope.launch {
                snackHost.showSnackbar("Video updated!")
                // Clear state before navigation
                viewModel.clearState()
                navController.popBackStack()
            }
        }

        // Handle errors from main state
        if (mainState is VideoUiState.Error) {
            scope.launch {
                snackHost.showSnackbar((mainState as VideoUiState.Error).message)
                viewModel.clearState()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Video") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackHost) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Thumbnail Card (same pattern) ---
            Text("Thumbnail", style = MaterialTheme.typography.titleMedium)
            Card(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { thumbnailPicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    selectedThumbnailUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        val thumbRaw = if (state is VideoUiState.DetailLoaded)
                            (state as VideoUiState.DetailLoaded).video.thumbnailUrl
                        else null

                        val thumbSigned = CachedSignedUrl(viewModel, thumbRaw)
                        AsyncImage(
                            model = thumbSigned ?: Constants.DEFAULT_BANNER_IMG,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Text Fields ---
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
                label = { Text("Categories*") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // --- Visibility & Age ---
            Text("Visibility", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = visibility=="public", onClick={visibility="public"})
                Text("Public")
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = visibility=="private", onClick={visibility="private"})
                Text("Private")
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isAgeRestricted, onCheckedChange = { isAgeRestricted = it })
                Text("Age Restricted")
            }

            Spacer(Modifier.height(16.dp))

            // --- Save Button ---
            Button(
                onClick = {
                    // build form map
                    val data = mapOf(
                        "title" to title,
                        "description" to description,
                        "categories" to categories,
                        "tags" to tags,
                        "visibility" to visibility,
                        "ageRestricted" to isAgeRestricted.toString()
                    )
                    // toRequestBody
                    val parts = data.mapValues { it.value }
                    val thumbPart = selectedThumbnailUri
                        ?.toMultipart("thumbnailImage", context)

                    viewModel.updateVideo(
                        id = videoId,
                        data = parts,
                        thumbnail = thumbPart
                    )

                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is VideoUiState.Loading
            ) {
                if (state is VideoUiState.Loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Saving…")
                } else {
                    Text("Save changes")
                }
            }
        }
    }
}

@Composable
fun CachedSignedUrl(viewModel: VideoViewModel, rawUrl: String?): String? {
    // Only re-run when rawUrl changes
    return produceState<String?>(initialValue = null, key1 = rawUrl) {
        if (!rawUrl.isNullOrBlank()) {
            // call the suspend helper you already have
            value = viewModel.signedUrlForSingle(rawUrl)
        }
    }.value
}
