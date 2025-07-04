package com.vidora.app.presentation.ui.subscription

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.vidora.app.data.remote.models.subscription.VideoResponse
import com.vidora.app.presentation.navigation.main.Screen
import com.vidora.app.presentation.subscription.SubscriptionUIState
import com.vidora.app.presentation.subscription.SubscriptionViewModel
import com.vidora.app.utils.Constants

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.subscriptions.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadSubscriptions()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Your Subscriptions") })
        }
    ) { padding ->
        when (uiState) {
            SubscriptionUIState.Loading -> {
                CircularProgressIndicator(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .wrapContentSize(Alignment.Center)
                )
            }

            is SubscriptionUIState.Error -> {
                Text(
                    text = (uiState as SubscriptionUIState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .wrapContentSize(Alignment.Center)
                )
            }

            is SubscriptionUIState.ListLoaded -> {
                val channels = (uiState as SubscriptionUIState.ListLoaded).channels

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // ── Horizontal carousel ─────────────────────────────
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // show up to 10
                        items(channels.take(10)) { ch ->
                            val channelImageUrl = ch.profilePictureUrl
                            val signedImage = produceState<String?>(initialValue = null, key1 = channelImageUrl) {
                                if (channelImageUrl!!.isNotBlank()) {
                                    value = viewModel.signedUrlForSubscribe(channelImageUrl)
                                }
                            }.value
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable { navController.navigate("publicChannel/${ch.id}") }
                            ) {
                                AsyncImage(
                                    model = signedImage ?: Constants.DEFAULT_CHANNEL_IMG,
                                    contentDescription = ch.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ch.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                        // “Show more” button if >10
                        if (channels.size > 10) {
                            item {
                                OutlinedButton(
                                    onClick = { navController.navigate(Screen.MySubscriptionScreen.route) },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Show More")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Latest from your channels",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    // flatten video + channelName pairs
                    val videoItems = channels.flatMap { ch ->
                        ch.videos.map { vid -> vid to ch.name }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(videoItems) { (video, channelName) ->
                            SubscriptionVideoCard(
                                viewModel = viewModel,
                                video = video,
                                channelName = channelName,
                                onClick = {
                                    navController.navigate("publicVideo/${video.id}")
                                }
                            )
                        }
                    }
                }
            }
            else -> {
                // Idle
            }
        }
    }
}

@Composable
private fun SubscriptionVideoCard(
    viewModel: SubscriptionViewModel,
    video: VideoResponse,
    channelName: String,
    onClick: () -> Unit
) {
    val videoThumbUrl = video.thumbnailUrl!!
    val signedThumb = produceState<String?>(initialValue = null, key1 = videoThumbUrl) {
        if (videoThumbUrl.isNotBlank()){
            value = viewModel.signedUrlForSubscribe(videoThumbUrl)
        }
    }.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = signedThumb ?: Constants.DEFAULT_BANNER_IMG,
                contentDescription = video.title,
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}