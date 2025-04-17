package com.example.papertrail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.example.papertrail.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onFontChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var expandedFont by remember { mutableStateOf(false) }
    var selectedFont by rememberSaveable { mutableStateOf("Inter") }

    var expandedLanguage by remember { mutableStateOf(false) }
    var selectedLanguage by rememberSaveable { mutableStateOf("English") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Theme toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.dark_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeChange
                )
            }

            // Font Style Dropdown
            Text(stringResource(R.string.font_style), style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = expandedFont,
                onExpandedChange = { expandedFont = !expandedFont }
            ) {
                TextField(
                    value = selectedFont,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(R.string.choose_font)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFont)
                    },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedFont,
                    onDismissRequest = { expandedFont = false }
                ) {
                    listOf("Inter", "Roboto", "Lato").forEach { font ->
                        DropdownMenuItem(
                            text = { Text(font) },
                            onClick = {
                                selectedFont = font
                                onFontChange(font)
                                expandedFont = false
                            }
                        )
                    }
                }
            }

            // Language Dropdown
            Text(stringResource(R.string.language), style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = expandedLanguage,
                onExpandedChange = { expandedLanguage = !expandedLanguage }
            ) {
                TextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.choose_language)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguage)
                    },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedLanguage,
                    onDismissRequest = { expandedLanguage = false }
                ) {
                    listOf("English", "Spanish").forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language) },
                            onClick = {
                                selectedLanguage = language
                                onLanguageChange(language)
                                expandedLanguage = false
                            }
                        )
                    }
                }
            }
        }
    }
}
