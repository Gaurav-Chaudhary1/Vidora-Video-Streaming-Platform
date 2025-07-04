package com.vidora.app.presentation.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.vidora.app.presentation.navigation.main.Screen
import com.vidora.app.R
import com.vidora.app.presentation.ui.profile.components.ProfileHeader
import com.vidora.app.presentation.ui.profile.components.LibrarySection
import com.vidora.app.presentation.ui.profile.components.RecentVideosSection
import com.vidora.app.presentation.ui.profile.components.PlaylistsSection
import com.vidora.app.presentation.ui.profile.components.HelpSupportSection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.vidora.app.presentation.history.HistoryUIState
import com.vidora.app.presentation.history.HistoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    rootNavController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    var showSignOutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val historyState by historyViewModel.historyState.collectAsState()

    // 1️⃣ Create the pull‑to‑refresh state
    val isRefreshing = uiState.loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshProfile() }
    )

    LaunchedEffect(Unit) {
        historyViewModel.loadWatchHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Welcome, ${uiState.profile?.firstName ?: "User"}",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { rootNavController.navigate(Screen.SearchScreen.route) }) {
                        Image(painter = painterResource(R.drawable.search_icon), contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                // 2️⃣ Attach the pullRefresh modifier
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            // 3️⃣ Your content—scrollable or placeholder
            if (uiState.profile == null && isRefreshing) {
                // initial loading
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    uiState.error?.let {
                        Text(
                            "Error: $it",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    uiState.profile?.let { user ->
                        ProfileHeader(
                            user = user,
                            signedImageUrl = uiState.signedImageUrl,
                            onViewChannel = { rootNavController.navigate("main-channel/${user.channelId}") },
                            onCreateChannel = { rootNavController.navigate(Screen.CreateChannelScreen.route) }
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    // New: Recent Videos carousel
                    when (historyState) {
                        HistoryUIState.Loading -> {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is HistoryUIState.Error -> {
                            Text(
                                text = (historyState as HistoryUIState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                        is HistoryUIState.Success -> {
                            RecentVideosSection(
                                recentVideos = (historyState as HistoryUIState.Success).videos,
                                viewModel = historyViewModel,
                                onItemClick = { videoId ->
                                    navController.navigate("publicVideo/$videoId")
                                }
                            )
                        }
                        else -> { /* Idle – nothing */ }
                    }
                    Spacer(Modifier.height(24.dp))
                    LibrarySection(rootNavController)
                    Spacer(Modifier.height(24.dp))
                    HelpSupportSection(rootNavController)
                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Sign Out")
                    }
                    if (showSignOutDialog) {
                        AlertDialog(
                            onDismissRequest = { showSignOutDialog = false },
                            title = { Text("Confirm Sign Out") },
                            text = { Text("Are you sure you want to sign out?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showSignOutDialog = false
                                        scope.launch {
                                            viewModel.signOut()
                                            rootNavController.popBackStack()
                                            rootNavController.navigate(Screen.Login.route) {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ) {
                                    Text(text = "Sign Out")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showSignOutDialog = false
                                    }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
            // 4️⃣ The material pull‑to‑refresh spinner
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}
