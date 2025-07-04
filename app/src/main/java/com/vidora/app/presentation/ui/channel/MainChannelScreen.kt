package com.vidora.app.presentation.ui.channel

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.vidora.app.R
import com.vidora.app.data.remote.models.channel.Channel
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.presentation.channel.ChannelState
import com.vidora.app.presentation.channel.ChannelViewModel
import com.vidora.app.presentation.navigation.main.Screen
import com.vidora.app.presentation.ui.channel.components.BottomSheet
import com.vidora.app.presentation.video.VideoUiState
import com.vidora.app.presentation.video.VideoViewModel
import com.vidora.app.utils.Constants

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainChannelScreen(
    channelId: String,
    navController: NavHostController,
    viewModel: ChannelViewModel = hiltViewModel(),
    videoViewModel: VideoViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val videoState by videoViewModel.state.collectAsState()

    // Collapsing toolbar state
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollOffset = scrollBehavior.state.collapsedFraction

    // Pull-to-refresh state
    val pullState = rememberPullRefreshState(
        refreshing = uiState is ChannelState.Loading,
        onRefresh = { viewModel.refreshChannel(channelId) }
    )

    var showBottomSheet by remember { mutableStateOf(false) }

    // Tab state
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf("Home", "Videos", "Playlist", "About")

    // Initial load
    LaunchedEffect(channelId) {
        viewModel.loadChannel(channelId)
        videoViewModel.listVideos()
    }

    val videos by remember(videoState) {
        derivedStateOf {
            when (videoState) {
                is VideoUiState.Lists -> (videoState as VideoUiState.Lists).videos
                else -> emptyList()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // FIXED: Use LargeTopAppBar for better collapsing behavior
            LargeTopAppBar(
                title = {
                    Text(
                        (uiState as? ChannelState.Success)?.channel?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            alpha = scrollOffset // FIXED: Changed to scrollOffset to make it visible when collapsed
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SearchScreen.route) }) {
                        Icon(
                            painter = painterResource(R.drawable.search_icon),
                            contentDescription = "Search Icon"
                        )
                    }
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                scrollBehavior = scrollBehavior
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
                    Text(
                        text = (uiState as ChannelState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ChannelState.Success -> {
                    val (channel, signedProfile, signedBanner) = uiState as ChannelState.Success
                    val lazyListState = rememberLazyListState()

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Channel header
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                // Banner with avatar overlay - FIXED: Corrected avatar positioning
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                ) {
                                    AsyncImage(
                                        model = signedBanner ?: Constants.DEFAULT_BANNER_IMG,
                                        contentDescription = "Banner",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                AsyncImage(
                                    model = signedProfile ?: Constants.DEFAULT_CHANNEL_IMG,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.CenterHorizontally)
                                )

                                // Spacer to account for the avatar overlay
                                Spacer(Modifier.height(8.dp))

                                // Channel name and handle
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        channel.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "@${channel.handle}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Stats row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            channel.totalSubscribers.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Subscribers",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            channel.videos?.size?.toString() ?: "0",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Videos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Action buttons
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate(Screen.UserVideoScreen.route)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Manage Videos")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { navController.navigate(Screen.AnalyticsScreen.route) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.analytics_icon),
                                            contentDescription = "Analytics",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            navController.navigate("edit-channel/${channelId}")
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.edit_icon),
                                            contentDescription = "Edit Channel",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Sticky tabs header
                        stickyHeader {
                            TabRow(
                                selectedTabIndex = selectedTab,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                tabTitles.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = {
                                            Text(
                                                title,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Tab content
                        when (selectedTab) {
                            0 -> homeTabContent(
                                videoViewModel = videoViewModel,
                                recentVideos = videos.take(5),
                                publicVideos = videos.filter { it.visibility == "public" },
                                navController = navController
                            )
                            1 -> videosTabContent(
                                videoViewModel = videoViewModel,
                                videos = videos,
                                navController = navController
                            )
                            2 -> playlistTabContent()
                            3 -> aboutTabContent(channel = channel)
                        }
                    }
                }
                ChannelState.Idle -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            // Pull-to-refresh spinner
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

// Tab content functions
private fun LazyListScope.homeTabContent(
    videoViewModel: VideoViewModel,
    recentVideos: List<Video>,
    publicVideos: List<Video>,
    navController: NavHostController
) {
    // Recent Videos Section
    item {
        Text(
            "Recent Videos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
        )
    }

    item {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recentVideos) { video ->
                RecentVideoCard(
                    video = video,
                    rawUrl = video.thumbnailUrl!!,
                    viewModel = videoViewModel,
                    navController = navController
                )
            }
        }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }

    // Public Videos Section
    item {
        Text(
            "Public Videos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 8.dp)
        )
    }

    items(publicVideos) { video ->
        VideoListItem(
            rawUrl = video.thumbnailUrl!!,
            viewModel = videoViewModel,
            video = video,
            navController = navController
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun LazyListScope.videosTabContent(
    videoViewModel: VideoViewModel,
    videos: List<Video>,
    navController: NavHostController
) {
    if (videos.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No videos uploaded yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    items(videos) { video ->
        VideoGridItem(
            video = video,
            rawUrl = video.thumbnailUrl!!,
            viewModel = videoViewModel,
            navController = navController
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}


private fun LazyListScope.playlistTabContent() {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.playlist_add),
                    contentDescription = "No playlists",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No playlists created yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Create your first playlist to organize your videos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Create playlist action */ }) {
                    Text("Create Playlist")
                }
            }
        }
    }
}

fun LazyListScope.aboutTabContent(channel: Channel) {
    item {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                channel.description ?: "No description provided",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    item {
        Text(
            "Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Location
    channel.location?.takeIf { it.isNotEmpty() }?.let {
        item {
            ListItem(
                headlineContent = { Text("Location") },
                supportingContent = { Text(it) },
                leadingContent = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
            Divider()
        }
    }

    // Joined date
    channel.createdAt?.takeIf { it.isNotEmpty() }?.let {
        item {
            ListItem(
                headlineContent = { Text("Joined") },
                supportingContent = { Text(it.substringBefore("T")) },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.calender),
                        contentDescription = "Joined",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
            Divider()
        }
    }

    // Subscribers
    item {
        ListItem(
            headlineContent = { Text("Subscribers") },
            supportingContent = { Text("${channel.totalSubscribers}") },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.subscriptions_icon),
                    contentDescription = "Subscribers",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.padding(8.dp)
        )
        Divider()
    }

    // Videos
    item {
        ListItem(
            headlineContent = { Text("Videos") },
            supportingContent = { Text("${channel.videos?.size ?: 0}") },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.photo_library),
                    contentDescription = "Videos",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.padding(8.dp)
        )
        Divider()
    }

    // Social Links
    if (!channel.socialLinks.isNullOrEmpty()) {
        item {
            Text(
                "Social Links",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        channel.socialLinks.forEach { (platform, link) ->
            if (link.isNotEmpty()) {
                item {
                    ListItem(
                        headlineContent = { Text(platform) },
                        supportingContent = { Text(link) },
                        leadingContent = {
                            Icon(
                                painter = getSocialIcon(platform),
                                contentDescription = platform,
                                tint = Color.Unspecified
                            )
                        },
                        modifier = Modifier.padding(8.dp).clickable { /* Open link */ }
                    )
                    Divider()
                }
            }
        }
    }

    // Contact Email
    channel.contactEmail?.takeIf { it.isNotEmpty() }?.let {
        item {
            Text(
                "Contact",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Email") },
                supportingContent = { Text(it) },
                leadingContent = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.padding(8.dp).clickable { /* Open email client */ }
            )
        }
    }
}

@Composable
fun RecentVideoCard(video: Video, rawUrl: String, viewModel: VideoViewModel, navController: NavHostController) {
    val signedThumb = produceState<String?>(initialValue = null, key1 = rawUrl) {
        if (rawUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(rawUrl)
        }
    }.value

    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(8.dp)
            .clickable { navController.navigate("videoDetail/${video.id}") },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = signedThumb ?: Constants.DEFAULT_BANNER_IMG,
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${video.views} views",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VideoListItem(rawUrl: String, viewModel: VideoViewModel, video: Video, navController: NavHostController) {
    val signedThumb = produceState<String?>(initialValue = null, key1 = rawUrl) {
        if (rawUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(rawUrl)
        }
    }.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("videoDetail/${video.id}") },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = signedThumb ?: Constants.DEFAULT_BANNER_IMG,
                contentDescription = "Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${video.views} views • ${video.createdAt?.substringBefore("T")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VideoGridItem(video: Video, rawUrl: String, viewModel: VideoViewModel, navController: NavHostController) {
    val signedThumb = produceState<String?>(initialValue = null, key1 = rawUrl) {
        if (rawUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(rawUrl)
        }
    }.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("videoDetail/${video.id}") },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = signedThumb ?: Constants.DEFAULT_BANNER_IMG,
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )

                // Duration badge
                video.duration.let {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            formatDuration(it),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${video.views} views • ${video.createdAt?.substringBefore("T")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to format duration in seconds to MM:SS format
@SuppressLint("DefaultLocale")
fun formatDuration(duration: Double): String {
    val minutes = (duration / 60).toInt()
    val seconds = (duration % 60).toInt()
    return String.format("%02d:%02d", minutes, seconds)
}

// Helper function to get social media icons
@Composable
private fun getSocialIcon(platform: String): Painter {
    return when (platform.lowercase()) {
        "facebook" -> painterResource(R.drawable.facebook)
        "twitter" -> painterResource(R.drawable.twitter)
        "instagram" -> painterResource(R.drawable.instagram)
        else -> painterResource(R.drawable.link)
    }
}