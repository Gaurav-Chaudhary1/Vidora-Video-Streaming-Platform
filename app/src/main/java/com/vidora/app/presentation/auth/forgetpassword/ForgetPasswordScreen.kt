package com.vidora.app.presentation.auth.forgetpassword

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.vidora.app.R
import com.vidora.app.ui.theme.*
import com.vidora.app.presentation.navigation.main.Screen

@Composable
fun ForgetPasswordScreen(
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val isLoading = viewModel.isLoading
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    // 1) Make status bar transparent & light icons
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        window?.statusBarColor = Color.Transparent.toArgb()
        WindowInsetsControllerCompat(window!!, view).isAppearanceLightStatusBars = true
    }

    // Local state for email, code, newPassword, showPassword
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Launch dialog when code is sent
    LaunchedEffect(state.requestSent) {
        if (state.requestSent) {
            showDialog = true
            // auto-dismiss after 10s
            kotlinx.coroutines.delay(10_000)
            showDialog = false
        }
    }

    // Show snackbar on error or success messages
    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Navigate back to Login on success
    LaunchedEffect(state.isResetSuccess) {
        if (state.isResetSuccess) {
            navController.popBackStack()
            navController.navigate(Screen.Login.route)
        }
    }

    // Outer Box with gradient background
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LightBackgroundTop,
                            LightBackgroundBottom
                        )
                    )
                )
                .padding(padding)
        ) {
            // Centered Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = if (!state.requestSent) "Forgot Password" else "Reset Password",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = LightTextPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // If code not sent yet: show email field + send button
                    if (!state.requestSent) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", color = LightTextSecondary) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = LightAccentPrimary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = VisualTransformation.None,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightTextPrimary,
                                unfocusedTextColor = LightTextSecondary,
                                focusedContainerColor = LightFieldBG,
                                unfocusedContainerColor = LightFieldBG,
                                focusedBorderColor = LightAccent,
                                unfocusedBorderColor = LightTextSecondary,
                                cursorColor = LightAccent,
                                focusedPlaceholderColor = LightGrayText,
                                focusedLabelColor = LightGrayText,
                                unfocusedLabelColor = LightGrayText,
                                focusedLeadingIconColor = LightAccent,
                                unfocusedLeadingIconColor = LightAccent
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.requestResetCode(email) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                LightAccentPrimary,
                                                LightAccentSecondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Send Code",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = {
                            navController.popBackStack()
                            navController.navigate(Screen.Login.route)
                        }) {
                            Text(
                                text = "Go Back to Login",
                                style = MaterialTheme.typography.bodySmall.copy(color = LightAccent)
                            )
                        }
                    } else {
                        // Code has been sent: show code + new password fields
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Reset Code", color = LightTextSecondary) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.key),
                                    contentDescription = "Code Icon",
                                    tint = LightAccent
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = VisualTransformation.None,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightTextPrimary,
                                unfocusedTextColor = LightTextSecondary,
                                focusedContainerColor = LightFieldBG,
                                unfocusedContainerColor = LightFieldBG,
                                focusedBorderColor = LightAccent,
                                unfocusedBorderColor = LightTextSecondary,
                                cursorColor = LightAccent,
                                focusedPlaceholderColor = LightGrayText,
                                focusedLabelColor = LightGrayText,
                                unfocusedLabelColor = LightGrayText,
                                focusedLeadingIconColor = LightAccent,
                                unfocusedLeadingIconColor = LightAccent
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password", color = LightTextSecondary) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password Icon",
                                    tint = LightAccent
                                )
                            },
                            trailingIcon = {
                                val iconRes =
                                    if (showPassword) R.drawable.visibility else R.drawable.visibility_off
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = iconRes),
                                        contentDescription = if (showPassword) "Hide password" else "Show password",
                                        tint = LightAccent
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightTextPrimary,
                                unfocusedTextColor = LightTextSecondary,
                                focusedContainerColor = LightFieldBG,
                                unfocusedContainerColor = LightFieldBG,
                                focusedBorderColor = LightAccent,
                                unfocusedBorderColor = LightTextSecondary,
                                cursorColor = LightAccent,
                                focusedPlaceholderColor = LightGrayText,
                                focusedLabelColor = LightGrayText,
                                unfocusedLabelColor = LightGrayText,
                                focusedLeadingIconColor = LightAccent,
                                unfocusedLeadingIconColor = LightAccent
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.resetPassword(email, code, newPassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                LightAccentPrimary,
                                                LightAccentSecondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Reset Password",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = {
                            navController.popBackStack()
                            navController.navigate(Screen.Login.route)
                        }) {
                            Text(
                                text = "Go Back to Login",
                                style = MaterialTheme.typography.bodySmall.copy(color = LightAccent)
                            )
                        }
                    }
                }
            }
            // Loading overlay
            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = true, onClick = {}),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LightAccentPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("Please wait…", color = Color.White)
                    }
                }
            }

            // Transient “Code Sent” Dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { /* No-op: will auto-dismiss */ },
                    title = { Text("Reset Code Sent") },
                    text = {
                        Text(
                            "We’ve sent a 6-digit code to your email. " +
                                    "Please check your inbox (or spam) and enter it here."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false; }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}
