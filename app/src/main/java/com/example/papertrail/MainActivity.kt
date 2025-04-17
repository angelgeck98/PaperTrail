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
import android.app.Activity
import android.content.Context
import androidx.compose.ui.platform.LocalContext




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

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        super.attachBaseContext(com.example.papertrail.localization.LocaleHelper.wrap(newBase, lang))
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
            var selectedFont by rememberSaveable { mutableStateOf("Inter") }
            var selectedLanguage by rememberSaveable { mutableStateOf("English") }
            var savedReceipts by remember { mutableStateOf(listOf<String>()) }
            val navController = rememberNavController()
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
                val savedLangCode = prefs.getString("language", "en")
                selectedLanguage = when (savedLangCode) {
                    "es" -> "Spanish"
                    else -> "English"
                }
            }
            val activity = context as? Activity

            fun onLanguageSelected(language: String) {
                val langCode = when (language) {
                    "Spanish" -> "es"
                    "English" -> "en"
                    else -> "en"
                }

                context.getSharedPreferences("settings", MODE_PRIVATE)
                    .edit()
                    .putString("language", langCode)
                    .apply()

                activity?.recreate()
            }



            PaperTrailTheme(
                darkTheme = isDarkTheme ,
                fontName = selectedFont
                ) {
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
                            onFontChange = { selectedFont = it },
                            onLanguageChange = { onLanguageSelected(it) },
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
