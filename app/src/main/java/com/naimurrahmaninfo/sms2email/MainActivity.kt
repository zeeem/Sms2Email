package com.naimurrahmaninfo.sms2email

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.naimurrahmaninfo.sms2email.ui.theme.Sms2EmailTheme
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sms2EmailTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val permissions = listOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    ) + if (Build.VERSION.SDK_INT >= 33) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList()
    var allGranted by remember { mutableStateOf(checkPermissions(context, permissions)) }
    var serviceActive by remember { mutableStateOf(false) }
    var emailCount by remember { mutableStateOf(SmsForwardService.getEmailSentCount(context)) }
    var emailFailCount by remember { mutableStateOf(SmsForwardService.getEmailFailedCount(context)) }
    var label by remember { mutableStateOf(getLabelPref(context)) }
    var labelInput by remember { mutableStateOf(label.removePrefix("SMS_FWD_")) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        allGranted = checkPermissions(context, permissions)
        emailCount = SmsForwardService.getEmailSentCount(context)
        emailFailCount = SmsForwardService.getEmailFailedCount(context)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Permission status: ${if (allGranted) "Granted" else "Missing"}")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = labelInput,
                onValueChange = {
                    if (!serviceActive) labelInput = it
                },
                label = { Text("Label Suffix (e.g. 26106)") },
                enabled = !serviceActive,
                singleLine = true
            )
            Button(onClick = {
                if (!serviceActive) {
                    val newLabel = "SMS_FWD_${labelInput.trim()}"
                    setLabelPref(context, newLabel)
                    label = newLabel
                }
            }, enabled = !serviceActive) {
                Text("Set Label")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                requestPermissions(context as ComponentActivity, permissions) {
                    allGranted = it
                }
            }, enabled = !allGranted) {
                Text("Request Permissions")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    // Stop any running service before starting a new one
                    context.stopService(Intent(context, SmsForwardService::class.java))
                    if (!serviceActive) {
                        ContextCompat.startForegroundService(context, Intent(context, SmsForwardService::class.java))
                    } else {
                        context.stopService(Intent(context, SmsForwardService::class.java))
                    }
                    serviceActive = !serviceActive
                    // Refresh email count after service action
                    scope.launch(Dispatchers.IO) {
                        emailCount = SmsForwardService.getEmailSentCount(context)
                        emailFailCount = SmsForwardService.getEmailFailedCount(context)
                        label = getLabelPref(context)
                    }
                },
                enabled = allGranted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!serviceActive) Color(0xFF4CAF50) else Color(0xFFF44336), // Green if not active, Red if active
                    contentColor = Color.White
                )
            ) {
                Text(if (!serviceActive) "Start SMS Forwarding" else "Stop SMS Forwarding")
            }
            Spacer(Modifier.height(16.dp))
            Text("Service status: ${if (serviceActive) "Active" else "Inactive"}")
            Spacer(Modifier.height(16.dp))
            Text("Label: $label")
            Text("Emails sent: $emailCount")
            Text("Failed attempts: $emailFailCount")
            Button(onClick = {
                // Manual refresh
                scope.launch(Dispatchers.IO) {
                    emailCount = SmsForwardService.getEmailSentCount(context)
                    emailFailCount = SmsForwardService.getEmailFailedCount(context)
                    label = getLabelPref(context)
                }
            }) {
                Text("Refresh Email Count")
            }
        }
    }
}

fun checkPermissions(context: Context, permissions: List<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun requestPermissions(activity: ComponentActivity, permissions: List<String>, onResult: (Boolean) -> Unit) {
    activity.requestPermissions(permissions.toTypedArray(), 0)
    // This is a minimal implementation; for production, use ActivityResultContracts
    onResult(checkPermissions(activity, permissions))
}

fun getLabelPref(context: Context): String {
    val prefs = context.getSharedPreferences("sms2email_prefs", Context.MODE_PRIVATE)
    return prefs.getString("sms_label", "SMS_FWD_26106") ?: "SMS_FWD_26106"
}

fun setLabelPref(context: Context, label: String) {
    val prefs = context.getSharedPreferences("sms2email_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("sms_label", label).apply()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Sms2EmailTheme {
        MainScreen()
    }
}