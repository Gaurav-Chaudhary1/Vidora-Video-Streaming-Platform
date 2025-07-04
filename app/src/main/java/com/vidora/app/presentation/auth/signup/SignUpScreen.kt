package com.vidora.app.presentation.auth.signup

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalContext
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
import coil.compose.rememberAsyncImagePainter
import com.vidora.app.R
import com.vidora.app.presentation.navigation.bottom.BottomScreen
import com.vidora.app.ui.theme.*
import com.vidora.app.presentation.navigation.main.Screen
import java.io.File

@Composable
fun SignUpScreen(
    navController: NavHostController,
    viewModel: SignUpViewModel = hiltViewModel()
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
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Outer Box: We add status bar padding so nothing is drawn under the notch
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
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
                    // “Create Account” title, centered
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = LightTextPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileImagePicker(viewModel)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── First Name field ──────────────────────────────────
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name", color = LightTextSecondary) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "First Name Icon",
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
                            cursorColor = LightAccentPrimary,
                            focusedPlaceholderColor = LightGrayText,
                            focusedLabelColor = LightGrayText,
                            unfocusedLabelColor = LightGrayText,
                            focusedLeadingIconColor = LightAccent,
                            unfocusedLeadingIconColor = LightAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ─── Last Name field ───────────────────────────────────
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name", color = LightTextSecondary) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Last Name Icon",
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
                            cursorColor = LightAccentPrimary,
                            focusedPlaceholderColor = LightGrayText,
                            focusedLabelColor = LightGrayText,
                            unfocusedLabelColor = LightGrayText,
                            focusedLeadingIconColor = LightAccent,
                            unfocusedLeadingIconColor = LightAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

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
                            cursorColor = LightAccentPrimary,
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
                                tint = LightAccentPrimary
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
                            cursorColor = LightAccentPrimary,
                            focusedPlaceholderColor = LightGrayText,
                            focusedLabelColor = LightGrayText,
                            unfocusedLabelColor = LightGrayText,
                            focusedLeadingIconColor = LightAccent,
                            unfocusedLeadingIconColor = LightAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // ─── “Agree to Terms” checkbox ────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .toggleable(
                                    value = agreeTerms,
                                    onValueChange = { agreeTerms = it }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (agreeTerms) LightAccent else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (agreeTerms) LightAccent else Color.LightGray,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (agreeTerms) {
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
                                text = "I agree to the Terms",
                                color = LightTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // ─── Gradient “Create Account” button ────────────────
                    Button(
                        onClick = {
                            if (agreeTerms) {
                                viewModel.signUp(firstName, lastName, email, password)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please agree our terms & conditions",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
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
                                text = "Create Account",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Divider ──────────────────────────────────────────
                    Divider(color = LightTextSecondary.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ─── “Already have an account? Sign In” ──────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account?",
                            style = MaterialTheme.typography.bodySmall.copy(color = LightTextSecondary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = {
                            navController.popBackStack()
                            navController.navigate(Screen.Login.route)
                        }) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.bodySmall.copy(color = LightAccentPrimary)
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

@Composable
fun ProfileImagePicker(viewModel: SignUpViewModel) {
    val context = LocalContext.current
    val imageFile = viewModel.selectedImageFile
    var imageUri by remember { mutableStateOf(imageFile?.let { Uri.fromFile(it) }) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val file = uriToFile(context, it)
            viewModel.selectedImageFile = file
        }
    }

    Box(modifier = Modifier.size(120.dp)) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Placeholder",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                tint = Color.Gray
            )
        }

        IconButton(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Pick Image",
                tint = Color.Black
            )
        }
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val tempFile = File.createTempFile("temp_img", ".png", context.cacheDir)
    tempFile.outputStream().use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    return tempFile
}

