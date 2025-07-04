package com.vidora.app.presentation.auth.login

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
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
import com.vidora.app.presentation.navigation.bottom.BottomScreen
import com.vidora.app.presentation.navigation.main.Screen
import com.vidora.app.ui.theme.LightAccent
import com.vidora.app.ui.theme.LightAccentPrimary
import com.vidora.app.ui.theme.LightAccentSecondary
import com.vidora.app.ui.theme.LightBackgroundBottom
import com.vidora.app.ui.theme.LightBackgroundTop
import com.vidora.app.ui.theme.LightCard
import com.vidora.app.ui.theme.LightFieldBG
import com.vidora.app.ui.theme.LightGrayText
import com.vidora.app.ui.theme.LightTextPrimary
import com.vidora.app.ui.theme.LightTextSecondary

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val isLoading = viewModel.isLoading

    // Scaffold to host a Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // 1) Make status bar transparent & set light‐icon mode
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        window?.statusBarColor = Color.Transparent.toArgb()
        WindowInsetsControllerCompat(window!!, view).isAppearanceLightStatusBars = true
    }

    // Local state for inputs and checkbox:
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Outer Box: We add status bar padding so nothing is drawn under the notch
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(LightBackgroundTop, LightBackgroundBottom)
                    )
                )
                .padding(paddingValues)
        ) {
            // Main Card container
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
                    horizontalAlignment = Alignment.Start
                ) {
                    // “Sign In” title
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = LightTextPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Email field ───────────────────────────────────────
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = LightTextSecondary) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon",
                                tint = LightAccent
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightTextPrimary,
                            unfocusedTextColor = LightTextSecondary,
                            focusedContainerColor = LightFieldBG,
                            focusedBorderColor = LightAccent,
                            unfocusedContainerColor = LightFieldBG,
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

                    // ─── Password field ────────────────────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = LightTextSecondary) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Icon",
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
                    Spacer(modifier = Modifier.height(12.dp))

                    // ─── “Remember me” + “Forgot password?” row ───────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom “Remember me” checkbox
                        Row(
                            modifier = Modifier
                                .toggleable(
                                    value = rememberMe,
                                    onValueChange = { rememberMe = it }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (rememberMe) LightAccent else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (rememberMe) LightAccent else Color.LightGray,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Checked",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Remember me",
                                color = LightTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(onClick = { navController.navigate(Screen.ForgetPassword.route) }) {
                            Text(
                                text = "Forgot password?",
                                style = MaterialTheme.typography.bodySmall.copy(color = LightAccent)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // ─── Gradient “Sign In” button ────────────────────────
                    Button(
                        onClick = { viewModel.login(email, password) },
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
                                        colors = listOf(LightAccentPrimary, LightAccentSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Divider ──────────────────────────────────────────
                    Divider(color = LightTextSecondary.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ─── “Don’t have an account? Sign Up” ─────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don’t have an account?",
                            style = MaterialTheme.typography.bodySmall.copy(color = LightTextSecondary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = {
                            navController.popBackStack()
                            navController.navigate(Screen.SignUp.route)
                        }) {
                            Text(
                                text = "Sign Up",
                                style = MaterialTheme.typography.bodySmall.copy(color = LightAccent)
                            )
                        }
                    }
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .background(Color.Black.copy(alpha = 0.4f))
                        // block all clicks beneath
                        .clickable(enabled = true, onClick = { /* consume */ }),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LightAccentPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("Please wait…", color = Color.White)
                    }
                }
            }

            // 4) Snackbar runner
            LaunchedEffect(state.error) {
                state.error?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearError()
                }
            }

            // 5) Navigation on success
            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    navController.popBackStack()
                    navController.navigate(Screen.Main.route)
                }
            }
        }
    }
}
