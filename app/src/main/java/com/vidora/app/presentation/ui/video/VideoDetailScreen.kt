package com.vidora.app.presentation.ui.video

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.vidora.app.R
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.presentation.video.VideoUiState
import com.vidora.app.presentation.video.VideoViewModel
import com.vidora.app.utils.Constants
import com.vidora.app.utils.toRelativeTime
import kotlinx.coroutines.delay

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    navController: NavHostController,
    videoId: String,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val detailState by viewModel.detail.collectAsState()
    val upNextState by viewModel.state.collectAsState()
    val generalState by viewModel.state.collectAsState()
    var isFullscreen by remember { mutableStateOf(false) }

    // Handle back press in fullscreen
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    // 1️⃣ Load details, then Up Next (channel) and generalMyVideos
    LaunchedEffect(videoId) {
        viewModel.getVideo(videoId)
        delay(200)
        viewModel.listVideos()
    }

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    title = { Text("Watch") },
                    navigationIcon = {
                        IconButton({ navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .then(if (!isFullscreen) Modifier.padding(padding) else Modifier)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val st = detailState) {
                VideoUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                is VideoUiState.Error -> Text(
                    "Failed to load video",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                is VideoUiState.Success -> {
                    val video = st.video
                    var signedUrl by remember { mutableStateOf<String?>(null) }
                    var signedUrlLoading by remember { mutableStateOf(true) }

                    LaunchedEffect(video.videoUrls.original) {
                        signedUrlLoading = true
                        signedUrl = try {
                            viewModel.signedUrlForSingle(video.videoUrls.original)
                        } catch (e: Exception) {
                            null
                        } finally {
                            signedUrlLoading = false
                        }
                    }

                    Column(Modifier.fillMaxSize()) {
                        // — Video player + fullscreen toggle —
                        if (signedUrl != null) {
                            PlayerWithFullscreenToggle(
                                url = signedUrl!!,
                                isFullscreen = isFullscreen,
                                onToggle = { isFullscreen = !isFullscreen }
                            )
                        } else if (signedUrlLoading) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(240.dp), Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .background(Color.Black),
                                Alignment.Center
                            ) {
                                Text("Failed to load video", color = Color.White)
                            }
                        }

                        // — Scrollable content below player when not fullscreen —
                        if (!isFullscreen) {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item { VideoMetadata(video) }
                                item { VideoActionRow(video, viewModel) }
                                item { SectionTitle("Up next") }
                                item { UpNextCarousel(viewModel, upNextState, navController) }
                                item { SectionTitle("More videos") }
                                item { RelatedVideosList(viewModel, generalState, navController) }
                            }
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlayerWithFullscreenToggle(
    url: String,
    isFullscreen: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create player with proper lifecycle handling
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("Player", "Playback error: ${error.message}")
                }
            })
        }
    }

    // Player lifecycle handling
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    player.play()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    player.pause()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    player.release()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    // Player content handling
    LaunchedEffect(url) {
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player.prepare()
        player.playWhenReady = true
    }

    // Fullscreen UI handling
    LaunchedEffect(isFullscreen) {
        activity?.window?.apply {
            if (isFullscreen) {
                // Enter fullscreen mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setDecorFitsSystemWindows(false)
                    insetsController?.hide(WindowInsets.Type.systemBars())
                    insetsController?.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            )
                }
            } else {
                // Exit fullscreen mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setDecorFitsSystemWindows(true)
                    insetsController?.show(WindowInsets.Type.systemBars())
                } else {
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.height(240.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setFullscreenButtonClickListener { onToggle() }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun VideoMetadata(video: Video) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(video.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "${video.views} views • ${video.createdAt?.toRelativeTime()}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun VideoActionRow(
    video: Video,
    viewModel: VideoViewModel
) {
    val context = LocalContext.current
    var liked by remember { mutableStateOf(false) }
    var disliked by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton({
            liked = !liked
            if (liked) viewModel.likeVideo(video.id) else viewModel.dislikeVideo(video.id)
            Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
        }) {
            Icon(
                painter = painterResource(R.drawable.thumb_up), contentDescription = "Like",
                tint = if (liked) Color.Blue else Color.Gray
            )
        }

        IconButton({
            disliked = !disliked
            if (disliked) viewModel.dislikeVideo(video.id) else viewModel.likeVideo(video.id)
            Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show()
        }) {
            Icon(
                painterResource(R.drawable.thumb_down), contentDescription = "Dislike",
                tint = if (disliked) Color.Red else Color.Gray
            )
        }

        IconButton({
            shareVideo(context, video)
        }) { Icon(Icons.Filled.Share, "Share") }

        IconButton({
            // Download via DownloadManager
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val req = DownloadManager.Request(Uri.parse(video.videoUrls.original)).apply {
                setTitle(video.title)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, "${video.title}.mp4"
                )
            }
            dm.enqueue(req)
        }) { Icon(painterResource(R.drawable.download_icon), "Download") }

        IconButton({ Toast.makeText(context, "Added to Playlist", Toast.LENGTH_SHORT).show() }) {
            Icon(painterResource(R.drawable.playlist_add), "Add to Playlist")
        }
        IconButton({ Toast.makeText(context, "Added to watch later", Toast.LENGTH_SHORT).show() }) {
            Icon(painterResource(R.drawable.history_icon), "Watch Later")
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun UpNextCarousel(
    viewModel: VideoViewModel,
    state: VideoUiState,
    navController: NavHostController
) {
    when (state) {
        VideoUiState.Loading -> Box(Modifier.fillMaxWidth(), Alignment.Center) {
            CircularProgressIndicator()
        }

        is VideoUiState.Lists -> LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.videos, key = { it.id }) { v ->
                UpNextItem(viewModel, v) {
                    navController.navigate("videoDetail/${v.id}")
                }
            }
        }

        else -> Unit
    }
}

@Composable
fun UpNextItem(viewModel: VideoViewModel, video: Video, onClick: () -> Unit) {
    val thumbnailUrl by produceState<String?>(null, video.thumbnailUrl) {
        value = if (!video.thumbnailUrl.isNullOrEmpty()) {
            viewModel.signedUrlForSingle(video.thumbnailUrl)
        } else {
            Constants.DEFAULT_BANNER_IMG
        }
    }

    Card(
        Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(Modifier.height(90.dp)) {
            AsyncImage(
                model = thumbnailUrl ?: Constants.DEFAULT_BANNER_IMG,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = .7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                val m = (video.duration / 60).toInt()
                val s = (video.duration % 60).toInt()
                Text(
                    "%d:%02d".format(m, s),
                    color = Color.White, style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Column(Modifier.padding(8.dp)) {
            Text(video.title, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("${video.views} views", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun RelatedVideosList(
    viewModel: VideoViewModel,
    state: VideoUiState,
    navController: NavHostController
) {
    when (state) {
        VideoUiState.Loading -> Box(Modifier.fillMaxWidth(), Alignment.Center) {
            CircularProgressIndicator()
        }

        is VideoUiState.Lists -> Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.videos.forEach { video ->
                RelatedVideoItem(viewModel, video) {
                    navController.navigate("videoDetail/${video.id}")
                }
            }
        }

        is VideoUiState.Error -> Text(
            "Failed to load more videos",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )

        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelatedVideoItem(
    viewModel: VideoViewModel,
    video: Video,
    onClick: () -> Unit
) {
    val thumbnailUrl by produceState<String?>(null, video.thumbnailUrl) {
        value = if (!video.thumbnailUrl.isNullOrEmpty()) {
            viewModel.signedUrlForSingle(video.thumbnailUrl)
        } else {
            Constants.DEFAULT_BANNER_IMG
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp, 70.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        video.title,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Channel info
                    Text(
                        video.channelName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Stats row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Views
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.eye_icon2),
                                contentDescription = "Views",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${video.views}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        // Likes
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.like_icon),
                                contentDescription = "Likes",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${video.likes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        // Comments
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.comment_icon),
                                contentDescription = "Comments",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${video.comments}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Divider
        Divider(
            modifier = Modifier.padding(horizontal = 12.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        // Action buttons
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show() }) {
                Icon(
                    painter = painterResource(R.drawable.like_icon),
                    contentDescription = "Like",
                    tint = Color.Gray
                )
            }

            IconButton(onClick = { Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show() }) {
                Icon(
                    painter = painterResource(R.drawable.comment_icon),
                    contentDescription = "Comment",
                    tint = Color.Gray
                )
            }

            IconButton(onClick = {
                shareVideo(context, video)
            }) {
                Icon(
                    painter = painterResource(R.drawable.share),
                    contentDescription = "Share",
                    tint = Color.Gray
                )
            }

            IconButton(onClick = { Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show() }) {
                Icon(
                    painter = painterResource(R.drawable.saved_icon),
                    contentDescription = "Save",
                    tint = Color.Gray
                )
            }
        }
    }
}

fun shareVideo(context: Context, video: Video) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Check out this video: ${video.title}\n${Constants.BASE_URL}/${video.id}"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
}