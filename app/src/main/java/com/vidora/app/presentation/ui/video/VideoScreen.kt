package com.vidora.app.presentation.ui.video

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.vidora.app.R
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.data.repository.file.FileRepository
import com.vidora.app.presentation.navigation.main.Screen
import com.vidora.app.presentation.video.VideoUiState
import com.vidora.app.presentation.video.VideoViewModel
import com.vidora.app.utils.Constants
import com.vidora.app.utils.toRelativeTime
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MyVideosScreen(
    navHostController: NavHostController,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedVideo by remember { mutableStateOf<Video?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val pullState = rememberPullRefreshState(
        refreshing = state is VideoUiState.Loading,
        onRefresh  = { viewModel.refresh() }
    )

    LaunchedEffect(Unit) { viewModel.listVideos() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Your Videos") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navHostController.navigate(Screen.UploadVideoScreen.route) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)
            .pullRefresh(pullState)) {
            when (state) {
                is VideoUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is VideoUiState.Error -> {
                    val message = (state as VideoUiState.Error).message
                    LaunchedEffect(message) {
                        snackbarHostState.showSnackbar(message)
                    }
                }

                is VideoUiState.Lists -> {
                    val videos = (state as VideoUiState.Lists).videos

                    if (videos.isEmpty()) {
                        EmptyVideosIllustration()
                    } else {
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(videos) { vid ->
                                VideoCard(
                                    video = vid,
                                    viewModel = viewModel,             // ← pass the same VM
                                    onMore    = {
                                        selectedVideo = vid
                                        showBottomSheet = true
                                    },
                                    onClick = { navHostController.navigate("videoDetail/${vid.id}") }
                                )
                            }
                        }
                    }
                }

                else -> Unit
            }
            PullRefreshIndicator(
                refreshing = state is VideoUiState.Loading,
                state = pullState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
        if (showBottomSheet && selectedVideo != null) {
            VideoActionsBottomSheet(
                onDismiss = { showBottomSheet = false },
                onEdit    = {
                    navHostController.navigate("editVideo/${selectedVideo!!.id}")
                    showBottomSheet = false
                },
                onDelete  = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Deleted")
                    }
                    viewModel.deleteVideo(selectedVideo!!.id)
                    showBottomSheet = false
                },
                onShare   = { /* … */ },
                onSaveToPlaylist = { Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show() }
            )
        }
    }
}

// Utility to format seconds as mm:ss
private fun formatDuration(seconds: Double): String {
    val mins = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    return "%d:%02d".format(mins, secs)
}

@Composable
fun VideoCard(
    video: Video,
    viewModel: VideoViewModel,
    onMore: () -> Unit,
    onClick: () -> Unit
) {
    // ① signed thumbnail
    val thumbSigned by viewModel
        .signedUrlFor(video.thumbnailUrl)
        .collectAsState(initial = null)

    // ② signed original (if you need for preview)
    val origSigned by viewModel
        .signedUrlFor(video.videoUrls.original)
        .collectAsState(initial = null)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box {
            // thumbnail image
            AsyncImage(
                model = thumbSigned ?: Constants.DEFAULT_BANNER_IMG,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // duration badge
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    formatDuration(video.duration),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                )
            }

            // more‑menu button
            IconButton(
                onClick = onMore,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            }
        }

        Column(Modifier.padding(12.dp)) {
            Text(
                video.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (video.visibility == "public") Icons.Default.Person else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${video.views} views • ${video.createdAt?.toRelativeTime()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("${video.likes.size}", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.width(16.dp))

                Icon(
                    painter = painterResource(R.drawable.comment_icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("${video.comments.size}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoActionsBottomSheet(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onSaveToPlaylist: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column {
            ListItem(headlineContent = { Text("Edit") }, leadingContent = {
                Icon(Icons.Default.Edit, contentDescription = null)
            }, modifier = Modifier.clickable { onEdit(); onDismiss() })

            ListItem(headlineContent = { Text("Share") }, leadingContent = {
                Icon(Icons.Default.Share, contentDescription = null)
            }, modifier = Modifier.clickable { onShare(); onDismiss() })

            ListItem(headlineContent = { Text("Save to Playlist") }, leadingContent = {
                Icon(painter = painterResource(R.drawable.saved_icon), contentDescription = null)
            }, modifier = Modifier.clickable { onSaveToPlaylist(); onDismiss() })

            ListItem(headlineContent = { Text("Delete") }, leadingContent = {
                Icon(Icons.Default.Delete, contentDescription = null)
            }, modifier = Modifier.clickable { onDelete(); onDismiss() })
        }
    }
}

@Composable
fun EmptyVideosIllustration() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.video),
            contentDescription = "Create one",
            modifier = Modifier.size(220.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("No videos uploaded yet", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Start sharing your content now!", style = MaterialTheme.typography.bodySmall)
    }
}
