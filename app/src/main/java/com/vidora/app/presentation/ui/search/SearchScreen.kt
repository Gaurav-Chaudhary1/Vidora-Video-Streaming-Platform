package com.vidora.app.presentation.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.vidora.app.R
import com.vidora.app.presentation.search.SearchState
import com.vidora.app.presentation.search.SearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    val history by viewModel.history.collectAsState()
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        placeholder = { Text(text = "Search...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (query.isNotBlank()) {
                                    viewModel.search(query)
                                    navController.popBackStack()
                                    navController.navigate("searchResults/${query}")
                                }
                            }
                        )
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 🕓 Recent Searches
            if (history.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = {
                        coroutineScope.launch {
                            viewModel.clearHistory()
                        }
                    }) {
                        Text("Clear All")
                    }
                }

                LazyColumn {
                    items(history) { item ->
                        SearchHistoryItem(
                            query = item,
                            onClick = {
                                query = item
                                viewModel.search(item)
                                navController.popBackStack()
                                navController.navigate("searchResults/${item}")
                            }
                        )
                    }
                }
            }

            // 🔄 Search Result State
            when (state) {
                is SearchState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                is SearchState.Success -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Found ${(state as SearchState.Success).data} results",
                        style = MaterialTheme.typography.titleMedium
                    )
                    // 🔥 Replace with real result UI
                }

                is SearchState.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error: ${(state as SearchState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun SearchHistoryItem(
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.history_icon),
            contentDescription = "History"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = query,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.grow),
            contentDescription = "Analytics"
        )
    }
}
