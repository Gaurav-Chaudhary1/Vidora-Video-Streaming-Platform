package com.vidora.app.presentation.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.vidora.app.presentation.history.HistoryUIState
import com.vidora.app.presentation.history.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    navController: NavHostController,
    vm: HistoryViewModel = hiltViewModel()
) {
    val state by vm.downloadState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDownloadedVideos() }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Downloads") },
            navigationIcon = {
            IconButton(onClick = { navController.popBackStack()})  {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }})
    }) { padding ->
        when (state) {
            HistoryUIState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HistoryUIState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text((state as HistoryUIState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
            is HistoryUIState.Success -> {
                val videos = (state as HistoryUIState.Success).videos
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(padding)
                ) {
                    items(videos) { vid ->
                        FullWidthVideoItem(vm, vid) { id ->
                            navController.navigate("publicVideo/$id")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}