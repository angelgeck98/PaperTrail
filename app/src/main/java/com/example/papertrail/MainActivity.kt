package com.example.papertrail


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.papertrail.ui.screens.*
import com.example.papertrail.ui.theme.PaperTrailTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.saveable.rememberSaveable




@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera permission is required to take photos",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}




class MainActivity : ComponentActivity() {
    private var hasCameraPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Log.e("MainActivity", "Camera permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var savedReceipts by remember { mutableStateOf(listOf<String>()) }
            val navController = rememberNavController()

            PaperTrailTheme(darkTheme = isDarkTheme) {
                NavHost(navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onCaptureReceipt = { navController.navigate("camera") },
                            onViewReceipts = { navController.navigate("receipts") },
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("receipts") {
                        ReceiptListScreen(
                            receipts = savedReceipts,
                            onReceiptSelected = { text ->
                                navController.navigate("detail/${text}")
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        "breakdown/{text}",
                        arguments = listOf(navArgument("text") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val text = backStackEntry.arguments?.getString("text") ?: ""
                        ExpenseBreakdownScreen(
                            ocrText = text,
                            onBack = { navController.popBackStack() },
                            onSave = {
                                // TODO: Save functionality here
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        "detail/{text}",
                        arguments = listOf(navArgument("text") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val text = backStackEntry.arguments?.getString("text") ?: ""
                        ReceiptDetailScreen(
                            text = text,
                            onBack = { navController.popBackStack() },
                            onNewReceipt = { navController.navigate("camera") }
                        )
                    }
                }
            }
        }
    }
}
