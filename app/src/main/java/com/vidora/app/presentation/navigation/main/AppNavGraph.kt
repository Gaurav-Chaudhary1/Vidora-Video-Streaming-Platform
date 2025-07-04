package com.vidora.app.presentation.navigation.main

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vidora.app.presentation.auth.forgetpassword.ForgetPasswordScreen
import com.vidora.app.presentation.auth.login.LoginScreen
import com.vidora.app.presentation.auth.signup.SignUpScreen
import com.vidora.app.presentation.navigation.bottom.MainScreen
import com.vidora.app.presentation.ui.analytics.AnalyticsScreen
import com.vidora.app.presentation.ui.channel.CreateChannelScreen
import com.vidora.app.presentation.ui.channel.EditChannelScreen
import com.vidora.app.presentation.ui.channel.MainChannelScreen
import com.vidora.app.presentation.ui.channel.PublicChannelScreen
import com.vidora.app.presentation.ui.history.DownloadScreen
import com.vidora.app.presentation.ui.history.WatchHistoryScreen
import com.vidora.app.presentation.ui.history.WatchLaterScreen
import com.vidora.app.presentation.ui.search.SearchResultsScreen
import com.vidora.app.presentation.ui.search.SearchScreen
import com.vidora.app.presentation.ui.splash.SplashScreen
import com.vidora.app.presentation.ui.splash.SplashViewModel
import com.vidora.app.presentation.ui.subscription.FullSubscriptionList
import com.vidora.app.presentation.ui.support.AboutUsScreen
import com.vidora.app.presentation.ui.support.HelpCenterScreen
import com.vidora.app.presentation.ui.support.PrivacyPolicyScreen
import com.vidora.app.presentation.ui.support.TermsConditionsScreen
import com.vidora.app.presentation.ui.video.EditVideoScreen
import com.vidora.app.presentation.ui.video.MyVideosScreen
import com.vidora.app.presentation.ui.video.PublicVideoDetailScreen
import com.vidora.app.presentation.ui.video.UploadVideoScreen
import com.vidora.app.presentation.ui.video.VideoDetailScreen
import com.vidora.app.presentation.ui.video.VideoPreviewScreen

@Composable
fun AppNavGraph(
    viewModel: SplashViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen() }

        // Auth screens
        composable(Screen.Login.route)        { LoginScreen(navController) }
        composable(Screen.SignUp.route)       { SignUpScreen(navController) }
        composable(Screen.ForgetPassword.route){ ForgetPasswordScreen(navController) }

        // **MAIN container** (must be here)
        composable(Screen.Main.route) {
            MainScreen(navController)
        }

        composable(Screen.CreateChannelScreen.route){
            CreateChannelScreen(navController)
        }

        composable(
            route = Screen.MainChannelScreen.route,
            arguments = listOf(
                navArgument("channelId") { type = NavType.StringType }
            )
        ) { backStack ->
            val channelId = backStack.arguments?.getString("channelId")!!
            MainChannelScreen(
                channelId = channelId,
                navController = navController
            )
        }

        composable(
            route = Screen.EditChannel.route,
            arguments = listOf( navArgument("channelId"){
                type = NavType.StringType
            })
        ){ backStack ->
            val channelId = backStack.arguments?.getString("channelId")!!
            EditChannelScreen(channelId, navController)
        }

        composable(
            Screen.UserVideoScreen.route
        ) {
            MyVideosScreen(navController)
        }

        composable(Screen.UploadVideoScreen.route){
            UploadVideoScreen(navController)
        }

        composable(
            route = Screen.VideoDetailScreen.route,
            arguments = listOf(
                navArgument("uri"){
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val uri = Uri.parse(backStackEntry.arguments?.getString("uri"))
            VideoPreviewScreen(videoUri = uri)
        }

        composable(
            "videoDetail/{videoId}",
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStack ->
            val vid = backStack.arguments!!.getString("videoId")!!
            VideoDetailScreen(navController, vid)
        }

        composable(
            route = Screen.EditVideoScreen.route,
            arguments = listOf(
                navArgument("id"){
                    NavType.StringType
                }
            )
        ){ backStack ->
            val vId = backStack.arguments!!.getString("id")!!
            EditVideoScreen(navController, vId)
        }

        composable(
            Screen.PublicVideoScreen.route,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStack ->
            val vid = backStack.arguments!!.getString("videoId")!!
            PublicVideoDetailScreen(navController, vid)
        }

        composable(
            route = Screen.PublicChannelScreen.route,
            arguments = listOf(
                navArgument("channelId"){
                    type = NavType.StringType
                }
            )
        ) { backStack ->
            val id = backStack.arguments!!.getString("channelId")!!
            PublicChannelScreen(channelId = id, navController)
        }

        composable(
            route = Screen.SearchScreen.route
        ) {
            SearchScreen(navController)
        }

        composable(
            route = Screen.SearchResultScreen.route,
            arguments = listOf(
                navArgument("query"){
                    type = NavType.StringType
                }
            )
        ) {backStack ->
            val query = backStack.arguments!!.getString("query")!!
            SearchResultsScreen(navController, query)
        }

        composable(Screen.MySubscriptionScreen.route) {
            FullSubscriptionList(navController)
        }

        composable(Screen.WatchHistory.route){
            WatchHistoryScreen(navController)
        }

        composable(Screen.WatchLater.route){
            WatchLaterScreen(navController)
        }

        composable(Screen.DownloadScreen.route){
            DownloadScreen(navController)
        }

        composable(Screen.HelpScreen.route){
            HelpCenterScreen(navController)
        }
        composable(Screen.PrivacyScreen.route){
            PrivacyPolicyScreen(navController)
        }
        composable(Screen.TermsScreen.route){
            TermsConditionsScreen(navController)
        }
        composable(Screen.AboutUsScreen.route){
            AboutUsScreen(navController)
        }

        composable(Screen.AnalyticsScreen.route){
            AnalyticsScreen(navController)
        }
    }

    // When login succeeds, navigate to "main"
    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true -> {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }

            false -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }

            null -> println("Don't navigate")
        }
    }
}
