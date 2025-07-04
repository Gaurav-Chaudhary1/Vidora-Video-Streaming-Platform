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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vidora.app.R
import com.vidora.app.presentation.navigation.main.Screen

@Composable
fun HelpSupportSection(navController: NavHostController) {
    Column {
        Text(
            text = "Help & Support",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        SupportItem("Help Center", R.drawable.help_icon) {
            navController.navigate(Screen.HelpScreen.route)
        }
        SupportItem("Privacy Policy", R.drawable.privacy_icon) {
            navController.navigate(Screen.PrivacyScreen.route)
        }
        SupportItem("Terms & Conditions", R.drawable.terms_icon) {
            navController.navigate(Screen.TermsScreen.route)
        }
        SupportItem("About Us", R.drawable.about_icon) {
            navController.navigate(Screen.AboutUsScreen.route)
        }
    }
}

@Composable
private fun SupportItem(
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
