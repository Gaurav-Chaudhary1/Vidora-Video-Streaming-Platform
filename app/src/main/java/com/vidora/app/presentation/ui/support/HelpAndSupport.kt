package com.vidora.app.presentation.ui.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

/**
 * Common Scaffold with a top‑app bar and back button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    navController: NavHostController,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

/** 1️⃣ Help Center Screen */
@Composable
fun HelpCenterScreen(navController: NavHostController) {
    AppScaffold(title = "Help Center", navController = navController) {
        Text(
            "Welcome to our Help Center!  \n" +
                    "Here you can find answers to frequently asked questions, step-by-step tutorials, and troubleshooting tips."
        )
        HorizontalDivider()
        Text("❓ **Getting Started**")
        Text("• How do I create an account?\n• How do I upload my first video?\n• How do I manage my profile?")
        HorizontalDivider()
        Text("⚙️ **Account & Settings**")
        Text("• Resetting your password\n• Enabling two‑factor authentication\n• Managing notifications")
        HorizontalDivider()
        Text("📞 **Still need help?**")
        Text("Contact our support team at support@vidora.com or tap the button below.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = {  }) {
            Text("Contact Support")
        }
    }
}

/** 2️⃣ Privacy Policy Screen */
@Composable
fun PrivacyPolicyScreen(navController: NavHostController) {
    AppScaffold(title = "Privacy Policy", navController = navController) {
        Text(
            "Your privacy is important to us.  \n\n" +
                    "We collect, use, and share your information in accordance with this policy."
        )
        HorizontalDivider()
        Text("1. **Information We Collect**")
        Text("• Personal data (e.g. name, email)\n• Usage data (e.g. pages visited, features used)")
        HorizontalDivider()
        Text("2. **How We Use Your Information**")
        Text("• To provide and maintain our service\n• To personalize your experience\n• To communicate updates and support")
        HorizontalDivider()
        Text("3. **Your Choices**")
        Text("• You can update your profile anytime.\n• You can opt‑out of marketing emails in your settings.")
        HorizontalDivider()
        Text("4. **Data Security**")
        Text("We employ industry‑standard security measures to protect your data.")
    }
}

/** 3️⃣ Terms & Conditions Screen */
@Composable
fun TermsConditionsScreen(navController: NavHostController) {
    AppScaffold(title = "Terms & Conditions", navController = navController) {
        Text(
            "Welcome to Vidora! By using our service, you agree to be bound by these Terms & Conditions."
        )
        HorizontalDivider()
        Text("1. **Use of Service**")
        Text("• You must be at least 13 years old.\n• You may not upload infringing or illegal content.")
        HorizontalDivider()
        Text("2. **User Content**")
        Text("• You retain all rights to content you upload.\n• You grant Vidora a worldwide license to use and display your content.")
        HorizontalDivider()
        Text("3. **Prohibited Conduct**")
        Text("• Harassment, hate speech, or spam is strictly forbidden.\n• Violations may result in account termination.")
        HorizontalDivider()
        Text("4. **Limitation of Liability**")
        Text("Vidora is not liable for any indirect or consequential damages arising from use of the service.")
    }
}

/** 4️⃣ About Us Screen */
@Composable
fun AboutUsScreen(navController: NavHostController) {
    AppScaffold(title = "About Us", navController = navController) {
        Text(
            "Vidora is the next‑gen video platform built by creators, for creators."
        )
        HorizontalDivider()
        Text("**Our Mission**")
        Text("To empower anyone to share their stories with the world.")
        HorizontalDivider()
        Text("**Our Team**")
        Text("• Jane Doe, CEO\n• John Smith, CTO\n• Priya Patel, Head of Design")
        HorizontalDivider()
        Text("**Contact**")
        Text("Email: hello@vidora.com\nTwitter: @vidora_app\nWebsite: https://vidora.com")
    }
}