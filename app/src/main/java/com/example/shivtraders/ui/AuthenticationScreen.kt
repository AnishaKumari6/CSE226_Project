
package com.example.shivtraders.ui

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun AuthenticationScreen(
    activity: FragmentActivity,
    onAuthenticated: () -> Unit
) {
    var errorMessage by remember { mutableStateOf("") }

    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)

    val authenticators = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }
    }

    val executor = remember(activity) {
        ContextCompat.getMainExecutor(activity)
    }

    val prompt = remember(activity) {
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    currentOnAuthenticated()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    errorMessage = errString.toString()
                }

                override fun onAuthenticationFailed() {
                    errorMessage =
                        "Fingerprint not recognized. Please try again."
                }
            }
        )
    }

    val promptInfo = remember(authenticators) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Shiv Traders")
            .setSubtitle("Verify your identity to access inventory")
            .setAllowedAuthenticators(authenticators)
            .apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    setNegativeButtonText("Cancel")
                }
            }
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Burgundy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Rounded.Inventory2,
                contentDescription = "Shiv Traders",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "SHIV TRADERS",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "INVENTORY MANAGEMENT",
                color = Color(0xFFE9CDD6),
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceWhite
                )
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Ink,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Authenticate to access your inventory",
                        color = Muted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = Burgundy,
                        modifier = Modifier.size(76.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            errorMessage = ""

                            try {
                                prompt.authenticate(promptInfo)
                            } catch (e: Exception) {
                                errorMessage =
                                    "Authentication is unavailable on this device."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Burgundy
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Unlock Shiv Traders",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = errorMessage,
                            color = Danger,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SECURE ACCESS • PRIVATE INVENTORY",
                color = Color(0xFFE9CDD6),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
