package com.vidora.app.presentation.navigation.bottom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.vidora.app.presentation.ui.category.CategoriesScreen
import com.vidora.app.presentation.ui.home.HomeScreen
import com.vidora.app.presentation.ui.profile.ProfileScreen
import com.vidora.app.presentation.ui.subscription.SubscriptionScreen
import com.vidora.app.presentation.ui.upload.UploadScreen

@Composable
fun MainScreen(
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()
    val tabs = listOf(
        BottomScreen.Home,
        BottomScreen.Categories,
        BottomScreen.Upload,
        BottomScreen.Subscription,
        BottomScreen.Profile
    )

    Scaffold(
        bottomBar = {
            BottomBar(
                navController = bottomNavController,
                items = tabs
            ) { screen ->
                bottomNavController.navigate(screen.route) {
                    popUpTo(bottomNavController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = BottomScreen.Home.route
            ) {
                composable(BottomScreen.Home.route) { HomeScreen(rootNavController, bottomNavController) }
                composable(BottomScreen.Categories.route) { CategoriesScreen(rootNavController) }
                composable(BottomScreen.Upload.route) { UploadScreen(rootNavController) }
                composable(BottomScreen.Subscription.route) { SubscriptionScreen(rootNavController) }
                composable(BottomScreen.Profile.route) {
                    ProfileScreen(bottomNavController, rootNavController) }
            }
        }
    }
}
