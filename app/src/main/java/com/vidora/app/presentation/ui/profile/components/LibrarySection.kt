package com.vidora.app.presentation.ui.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vidora.app.R
import com.vidora.app.presentation.navigation.main.Screen

@Composable
fun LibrarySection(navController: NavHostController) {
    Column {
        Text(
            text = "Library",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        LibraryItem("Watch Later", R.drawable.clock_icon) {
            navController.navigate(Screen.WatchLater.route)
        }
        LibraryItem("Downloads", R.drawable.download_icon) {
            navController.navigate(Screen.DownloadScreen.route)
        }
        LibraryItem("Watch History", R.drawable.history_icon) {
            navController.navigate(Screen.WatchHistory.route)
        }
    }
}

@Composable
private fun LibraryItem(
    title: String,
    image: Int,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Image(painter = painterResource(image), contentDescription = title) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}
