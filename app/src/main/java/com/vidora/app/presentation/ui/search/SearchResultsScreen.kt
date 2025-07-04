package com.vidora.app.presentation.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.vidora.app.data.remote.models.channel.Channel
import com.vidora.app.data.remote.models.search.SearchResult
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.presentation.search.SearchState
import com.vidora.app.presentation.search.SearchViewModel
import com.vidora.app.R
import com.vidora.app.data.remote.models.video.SearchChannel
import com.vidora.app.presentation.channel.ChannelViewModel
import com.vidora.app.presentation.video.VideoViewModel
import com.vidora.app.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    navController: NavHostController,
    initialQuery: String? = null,
    viewModel: SearchViewModel = hiltViewModel(),
    channelViewModel: ChannelViewModel = hiltViewModel(),
    videoViewModel: VideoViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf(initialQuery.orEmpty()) }
    val state by viewModel.state.collectAsState()

    // trigger search on initial load
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            viewModel.search(initialQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (query.isNotBlank()) {
                                    viewModel.search(query.trim())
                                }
                            }
                        ),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
        },
        modifier = Modifier.padding(top = 16.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is SearchState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is SearchState.Error -> {
                    val msg = (state as SearchState.Error).message
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bc_error),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Oops, something went wrong.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is SearchState.Success -> {
                    val data: SearchResult = (state as SearchState.Success).data
                    if (data.channel == null && data.videos.isEmpty()) {
                        Text(
                            text = "No results found for \"$query\"",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Channel Section
                            data.channel?.let { channel ->
                                item {
                                    ChannelHeader(
                                        viewModel = channelViewModel,
                                        channelImageUrl = channel.profilePictureUrl.orEmpty(),
                                        channel = channel,
                                        onClick = {
                                            navController.navigate("publicChannel/${channel.id}")
                                        }
                                    )
                                }

                                if (data.channelVideos.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "From ${channel.name}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                    item {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp)
                                        ) {
                                            items(data.channelVideos) { video ->
                                                VideoCard(
                                                    viewModel = videoViewModel,
                                                    thumbImgUrl = video.thumbnailUrl.orEmpty(),
                                                    video = video,
                                                    onClick = {
                                                        navController.navigate("publicVideo/${video.id}") }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Related Videos
                            item {
                                Text(
                                    text = if (data.channel != null) "Related Videos" else "Results",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                            items(data.videos) { video ->
                                VideoListItem(
                                    viewModel = videoViewModel,
                                    thumbImgUrl = video.thumbnailUrl.orEmpty(),
                                    video = video,
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
                    Text(
                        text = "Search for videos or channels",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelHeader(
    channelImageUrl: String,
    viewModel: ChannelViewModel,
    channel: SearchChannel,
    onClick: () -> Unit
) {
    val signedImage = produceState<String?>(initialValue = null, key1 = channelImageUrl) {
        if (channelImageUrl.isNotBlank()) {
            value = viewModel.signedUrlForChannel(channelImageUrl)
        }
    }.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = signedImage ?: Constants.DEFAULT_BANNER_IMG,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(56.dp)
                    .padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(onClick = { /* TODO: subscribe */ }) {
                Text("Subscribe")
            }
        }
    }
}

@Composable
fun VideoCard(
    thumbImgUrl: String,
    viewModel: VideoViewModel,
    video: Video,
    onClick: () -> Unit
) {
    val signedImage = produceState<String?>(initialValue = null, key1 = thumbImgUrl) {
        if (thumbImgUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(thumbImgUrl)
        }
    }.value
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            AsyncImage(
                model = signedImage ?: Constants.DEFAULT_BANNER_IMG,
                contentDescription = video.title,
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth()
            )
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun VideoListItem(
    thumbImgUrl: String,
    viewModel: VideoViewModel,
    video: Video,
    onClick: () -> Unit
) {
    val signedImage = produceState<String?>(initialValue = null, key1 = thumbImgUrl) {
        if (thumbImgUrl.isNotBlank()) {
            value = viewModel.signedUrlForSingle(thumbImgUrl)
        }
    }.value
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            model = signedImage ?: Constants.DEFAULT_BANNER_IMG,
            contentDescription = video.title,
            modifier = Modifier
                .size(120.dp)
                .padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${video.views} views • ${video.createdAt}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}