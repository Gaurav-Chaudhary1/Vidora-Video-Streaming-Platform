package com.vidora.app.presentation.ui.upload

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vidora.app.R
import com.vidora.app.presentation.navigation.main.Screen

@Composable
fun UploadScreen(navController: NavHostController) {

    var buttonClicked by remember { mutableStateOf(false) }

    if (!buttonClicked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            // Full-screen background illustration
            AsyncImage(
                model = R.drawable.img0,
                contentDescription = "Creator Upload Illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // A fading gradient at the bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to Color(0xCC000000)
                        )
                    )
            )

            // Footer content: terms text + button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                Text(
                    text = "By continuing, you agree to our Terms & Conditions.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        buttonClicked = true
                        navController.navigate(Screen.UploadVideoScreen.route)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(text = "Continue to Upload", color = Color.White)
                }
            }
        }
    } else {
        navController.navigate(Screen.UploadVideoScreen.route)
    }
}