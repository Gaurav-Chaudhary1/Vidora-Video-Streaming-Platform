package com.vidora.app.presentation.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.presentation.navigation.bottom.BottomScreen
import com.vidora.app.presentation.navigation.main.Screen
import com.vidora.app.presentation.video.VideoUiState
import com.vidora.app.presentation.video.VideoViewModel
import com.vidora.app.utils.Constants
import com.vidora.app.utils.toRelativeTime

@Composable
fun HomeScreen(
    rootNavController: NavHostController,
    bottomNavHostController: NavHostController,
    videoViewModel: VideoViewModel = hiltViewModel()
) {
    // 1) Load all videos once
    val uiState by videoViewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        videoViewModel.listVideos(page = 1, limit = 50)
    }

    // 2) Keep list in snapshot
    val allVideos = remember { mutableStateListOf<Video>() }
    LaunchedEffect(uiState) {
        if (uiState is VideoUiState.Lists) {
            allVideos.clear()
            allVideos.addAll((uiState as VideoUiState.Lists).videos)
        }
    }

    // 3) Derive horizontal lists
    val recommendedVideos by remember {
        derivedStateOf { allVideos.take(8) }
    }

    val trendingVideos by remember {
        derivedStateOf { allVideos.sortedByDescending { it.views }.take(8) }
    }

    val signedProfileUrl by produceState<String?>(initialValue = null, key1 = uiState) {
        if (uiState is VideoUiState.Success) {
            // get the raw profile URL from the loaded Video
            val raw = (uiState as VideoUiState.Success).video.channelProfilePictureUrl
            if (!raw.isNullOrBlank()) {
                // suspend call to fetch your signed URL
                val signed = videoViewModel.signedUrlForSingle(raw)
                value = signed
            }
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = { rootNavController.navigate(Screen.SearchScreen.route) },
                onNotificationClick = { /*…*/ },
                onProfileClick = { bottomNavHostController.navigate(BottomScreen.Profile.route) },
                profileImage = signedProfileUrl
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Recommended
            item { SectionHeader("Recommended") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedVideos) { video ->
                        VideoCard(
                            thumbUrl = video.thumbnailUrl ?: Constants.DEFAULT_BANNER_IMG,
                            imageUrl = video.channelProfilePictureUrl ?: Constants.DEFAULT_CHANNEL_IMG,
                            viewModel = videoViewModel,
                            video = video
                        ) {
                            rootNavController.navigate("publicVideo/${video.id}")
                        }
                    }
                }
            }

            // Trending
            item { SectionHeader("Trending") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingVideos) { video ->
                        VideoCard(
                            thumbUrl = video.thumbnailUrl ?: Constants.DEFAULT_BANNER_IMG,
                            imageUrl = video.channelProfilePictureUrl ?: Constants.DEFAULT_CHANNEL_IMG,
                            viewModel = videoViewModel,
                            video = video
                        ) {
                            rootNavController.navigate("publicVideo/${video.id}")
                        }
                    }
                }
            }

            // For You
            item { SectionHeader("For You") }
            itemsIndexed(allVideos) { index, video ->
                VerticalVideoCard(
                    thumbUrl = video.thumbnailUrl ?: Constants.DEFAULT_BANNER_IMG,
                    imageUrl = video.channelProfilePictureUrl ?: Constants.DEFAULT_CHANNEL_IMG,
                    video = video,
                    viewModel = videoViewModel
                ) {
                    rootNavController.navigate("publicVideo/${video.id}")
                }
            }

            // Loading
            if (uiState is VideoUiState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalVideoCard(
    thumbUrl: String,
    imageUrl: String,
    video: Video,
    viewModel: VideoViewModel,
    onClick: () -> Unit
) {
    val signedThumb = produceState<String?>(initialValue = null, key1 = thumbUrl) {
        if (thumbUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(thumbUrl)
        }
    }.value

    val signedImage = produceState<String?>(initialValue = null, key1 = imageUrl) {
        if (imageUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(imageUrl)
        }
    }.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Thumbnail
        AsyncImage(
            model = signedThumb ?: Constants.DEFAULT_BANNER_IMG,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(MaterialTheme.shapes.medium)
        )

        // Video info
        Row(
            modifier = Modifier.padding(top = 12.dp)
        ) {
            // Channel avatar
            AsyncImage(
                model = signedImage ?: Constants.DEFAULT_CHANNEL_IMG,
                contentDescription = video.channelName,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            // Video details
            Column(Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = video.channelName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "${video.views} views • ${video.createdAt?.toRelativeTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun VideoCard(
    thumbUrl: String,
    imageUrl: String,
    viewModel: VideoViewModel,
    video: Video,
    onClick: () -> Unit
) {
    val signedThumb = produceState<String?>(initialValue = null, key1 = thumbUrl) {
        if (thumbUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(thumbUrl)
        }
    }.value

    val signedImage = produceState<String?>(initialValue = null, key1 = imageUrl) {
        if (imageUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(imageUrl)
        }
    }.value

    Column(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = signedThumb ?: Constants.DEFAULT_BANNER_IMG,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        ) {
            AsyncImage(
                model = signedImage ?: Constants.DEFAULT_CHANNEL_IMG,
                contentDescription = video.channelName,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Text(
                    text = "${video.channelName} • ${video.views} views",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}