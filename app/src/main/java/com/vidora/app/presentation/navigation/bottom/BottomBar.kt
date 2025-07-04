package com.vidora.app.presentation.navigation.bottom

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(
    navController: NavController,
    items: List<BottomScreen>,
    onItemSelected: (BottomScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val selected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.icon),
                        contentDescription = screen.label,
                        tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                selected = selected,
                onClick = { onItemSelected(screen) },
                alwaysShowLabel = true
            )
        }
    }
}
