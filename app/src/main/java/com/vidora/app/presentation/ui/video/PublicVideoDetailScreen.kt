package com.vidora.app.presentation.ui.video


import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.vidora.app.data.remote.models.comment.Comment
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.presentation.channel.ChannelState
import com.vidora.app.presentation.channel.ChannelViewModel
import com.vidora.app.presentation.subscription.SubscriptionUIState
import com.vidora.app.presentation.subscription.SubscriptionViewModel
import com.vidora.app.presentation.video.VideoUiState
import com.vidora.app.presentation.video.VideoViewModel
import com.vidora.app.utils.Constants
import com.vidora.app.utils.toRelativeTime
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicVideoDetailScreen(
    navController: NavHostController,
    videoId: String,
    videoViewModel: VideoViewModel = hiltViewModel(),
    channelViewModel: ChannelViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
) {
    val detailState by videoViewModel.detail.collectAsState()
    val interactionState by videoViewModel.state.collectAsState()
    val savedState by videoViewModel.state.collectAsState()
    val subs by subscriptionViewModel.subscribedIds.collectAsState()
    var isFullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 1️⃣ Load video details
    LaunchedEffect(videoId) {
        videoViewModel.addWatchHistory(videoId)
        videoViewModel.getVideo(videoId)
        videoViewModel.addViews(videoId)
    }

    Scaffold(
        topBar = {
            detailState.let { st ->
                TopAppBar(
                    title = {
                        Text(
                            text = (st as? VideoUiState.Success)?.video?.title
                                ?: "Loading…",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val st = detailState) {
                VideoUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is VideoUiState.Error -> {
                    Text("Failed to load video", color = MaterialTheme.colorScheme.error)
                }

                is VideoUiState.Success -> {
                    val video = st.video
                    var signedUrl by remember { mutableStateOf<String?>(null) }
                    var signedUrlLoading by remember { mutableStateOf(true) }

                    // Once we have the detail, get the channel’s profile
                    LaunchedEffect(video.channelId) {
                        channelViewModel.loadChannel(video.channelId)
                    }

                    LaunchedEffect(video.videoUrls.original) {
                        signedUrlLoading = true
                        signedUrl = try {
                            videoViewModel.signedUrlForSingle(video.videoUrls.original)
                        } catch (e: Exception) {
                            null
                        } finally {
                            signedUrlLoading = false
                        }
                    }

                    LaunchedEffect(video.channelId, video.id) {
                        videoViewModel.loadUpNext(
                            channelId = video.channelId,
                            excludeId = video.id
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())) {
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

                        val channelState by channelViewModel.state.collectAsState()
                        if (channelState is ChannelState.Success) {
                            val (channel, signedProfile, _) = channelState as ChannelState.Success
                            val isSubscribed = channel.id in subs
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Channel avatar
                                AsyncImage(
                                    model = signedProfile ?: Constants.DEFAULT_CHANNEL_IMG,
                                    contentDescription = channel.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable{
                                            navController.navigate("publicChannel/${channel.id}")
                                        },
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f).clickable {
                                    navController.navigate("publicChannel/${channel.id}")
                                }) {
                                    Text(channel.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${channel.totalSubscribers}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Button(onClick = {
                                    if (isSubscribed){
                                        subscriptionViewModel.unsubscribe(channel.id)
                                    } else {
                                        subscriptionViewModel.subscribe(channel.id)
                                    }
                                }) {
                                    Text(if (isSubscribed) "Unsubscribe" else "Subscribe")
                                }
                            }
                        }

                        VideoMetaRow(video)

                        // — Video Description —
                        ChannelDescription(description = video.description)

                        // — Action Row (Like / Share / etc) —
                        PublicVideoActionRow(
                            video = video,
                            savedState = savedState,
                            interactionState = interactionState,
                            viewModel = videoViewModel
                        ) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }

                        // — Comments Input & List —
                        CommentsSection(video,videoId, videoViewModel)

                        // — Up Next and More Videos as before —
                        SectionTitle("Up Next")
                        UpNextCarousel(
                            videoViewModel,
                            videoViewModel.upNext.collectAsState().value,
                            navController
                        )
                        SectionTitle("More Videos")
                        RelatedVideosList(
                            videoViewModel,
                            videoViewModel.upNext.collectAsState().value,
                            navController
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsSection(
    video: Video,
    videoId: String,
    viewModel: VideoViewModel
) {
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.state.collectAsState()
    val comments = remember(uiState) {
        (uiState as? VideoUiState.Comments)?.comments.orEmpty()
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // load on open
    LaunchedEffect(showSheet) {
        if (showSheet) viewModel.loadComments(videoId)
    }

    // trigger add
    fun post() {
        if (commentText.isBlank()) return
        viewModel.addComment(videoId, commentText.trim())
        commentText = ""
        // reload afterwards
        viewModel.loadComments(videoId)
        Toast.makeText(context, "Comment posted", Toast.LENGTH_SHORT).show()
    }

    // trigger delete
    fun delete(id: String) {
        viewModel.deleteComment(videoId, id)
        viewModel.loadComments(videoId)
        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { showSheet = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(R.drawable.comment_icon), contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Show comments (${video.comments.size})", style = MaterialTheme.typography.bodyMedium)
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column {
                // ✔️ Input bar pinned at top
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a public comment…") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { post() }) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post comment")
                            }
                        }
                    )
                }

                HorizontalDivider()

                // ✔️ Comments list
                if (comments.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No comments yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxHeight(0.7f)
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(comments) { c ->
                            CommentCard(video, viewModel, c, onDelete = { delete(c.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentCard(video: Video, viewModel: VideoViewModel, c: Comment, onDelete: () -> Unit) {
    val imageUrl by produceState<String?>(null, video.channelProfilePictureUrl) {
        value = if (!video.channelProfilePictureUrl.isNullOrEmpty()) {
            viewModel.signedUrlForSingle(video.channelProfilePictureUrl)
        } else {
            Constants.DEFAULT_CHANNEL_IMG
        }
    }
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${c.user.firstName} ${c.user.lastName}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        c.createdAt.toRelativeTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(c.content, style = MaterialTheme.typography.bodyMedium)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete comment")
            }
        }
    }
}

@Composable
fun PublicVideoActionRow(
    video: Video,
    viewModel: VideoViewModel,
    interactionState: VideoUiState,
    savedState: VideoUiState,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var liked by remember { mutableStateOf(false) }
    var disliked by remember { mutableStateOf(false) }
    val likes = (interactionState as? VideoUiState.Interaction)?.likes ?: video.likes.size
    val dislikes = (interactionState as? VideoUiState.Interaction)?.dislikes ?: video.dislikes.size

    val signedVideoUrl = produceState<String?>(initialValue = null, key1 = video.videoUrls.original) {
        if (video.videoUrls.original.isNotBlank()){
            value = viewModel.signedUrlForSingle(video.videoUrls.original)
        }
    }.value

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton({
                liked = true; disliked = false
                viewModel.likeVideo(video.id)
            }) {
                Icon(
                    painter = painterResource(R.drawable.thumb_up), contentDescription = "Like",
                    tint = if (liked) Color.Blue else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(likes.toString())
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton({
                disliked = true; liked = false
                viewModel.dislikeVideo(video.id)
            }) {
                Icon(
                    painterResource(R.drawable.thumb_down), contentDescription = "Dislike",
                    tint = if (disliked) Color.Red else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(dislikes.toString())
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton({
                shareVideo(context, video)
            }) { Icon(Icons.Filled.Share, "Share") }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Share")
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton({
                signedVideoUrl?.let { url->
                    viewModel.addDownload(video.id)

                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE)
                                                  as DownloadManager
                    val req = DownloadManager.Request(Uri.parse(url)).apply {
                        setTitle(video.title)
                        setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, "${video.title}.mp4"
                        )
                    }
                    dm.enqueue(req)
                } ?: run {
                    Toast.makeText(context,
                        "Failed to get download URL",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) { Icon(painterResource(R.drawable.download_icon), "Download") }
            Spacer(Modifier.height(6.dp))
            Text("Download")
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton({
                viewModel.toggleSaveForLater(video.id)
                Toast.makeText(context, "Added to watch later", Toast.LENGTH_SHORT).show()
            }) {
                Icon(painterResource(R.drawable.history_icon), "Watch Later")
            }
            Spacer(Modifier.height(6.dp))
            Text("Watch Later")
        }
    }
    // Handle messages
    LaunchedEffect(interactionState, savedState) {
        (savedState as? VideoUiState.Saved)?.let { onMessage(if (it.saved) "Saved for later" else "Removed from saved") }
    }
}

@Composable
fun VideoMetaRow(video: Video) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "${video.views} views",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Divider(
            color = Color.Gray,
            modifier = Modifier
                .height(14.dp)
                .width(1.dp)
        )
        Text(
            text = "Published ${video.createdAt?.toRelativeTime()}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDescription(
    description: String
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp)
        ) {
            ListItem(
                headlineContent = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text("Description", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TextButton(
            onClick = { showSheet = true }
        ) {
            Text("See full description...")
        }
    }
}
