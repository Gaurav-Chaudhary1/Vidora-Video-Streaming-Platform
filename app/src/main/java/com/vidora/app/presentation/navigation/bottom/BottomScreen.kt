package com.vidora.app.presentation.navigation.bottom

import androidx.annotation.DrawableRes
import com.vidora.app.R

sealed class BottomScreen(val route: String, @DrawableRes val icon: Int, val label: String) {
    object Home : BottomScreen("home_screen", R.drawable.home_vector, "Home")
    object Categories : BottomScreen("categories_screen", R.drawable.category_icon, "Categories")
    object Upload : BottomScreen("upload_screen", R.drawable.add_icon, "Upload")
    object Subscription : BottomScreen("subscriptions_screen", R.drawable.subscriptions_icon, "Subscription")
    object Profile : BottomScreen("profile_screen", R.drawable.profile_icon, "Profile")
}