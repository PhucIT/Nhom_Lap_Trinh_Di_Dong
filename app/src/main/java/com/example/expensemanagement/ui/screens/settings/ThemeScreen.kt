package com.example.expensemanagement.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.data.repository.Theme.ThemeSetting
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val currentThemeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
    val currentColorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chủ đề") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Nhóm Chế Độ Giao Diện
            ThemeOptionGroup(
                title = "Chế độ",
                options = listOf(
                    ThemeOption("Sáng", Icons.Default.LightMode, ThemeSetting.LIGHT),
                    ThemeOption("Tối", Icons.Default.DarkMode, ThemeSetting.DARK),
                    ThemeOption("Theo hệ thống", Icons.Default.PhoneAndroid, ThemeSetting.SYSTEM)
                ),
                selectedOption = currentThemeSetting,
                onOptionSelected = { viewModel.setThemeSetting(it) }
            )

            // Nhóm Màu Sắc
            ColorOptionGroup(
                title = "Màu sắc",
                options = listOf(
                    ColorOption("PrimaryGreen", PrimaryGreen),
                    ColorOption("Purple", Color(0xFF673AB7)),
                    ColorOption("Orange", Color(0xFFFF9800)),
                    ColorOption("Red", Color(0xFFF44336))
                ),
                selectedOption = currentColorTheme,
                onOptionSelected = { viewModel.setColorTheme(it) }
            )
        }
    }
}

// Data class để định nghĩa một lựa chọn chủ đề
private data class ThemeOption(val title: String, val icon: ImageVector, val setting: ThemeSetting)
private data class ColorOption(val name: String, val color: Color)

@Composable
private fun ThemeOptionGroup(
    title: String,
    options: List<ThemeOption>,
    selectedOption: ThemeSetting,
    onOptionSelected: (ThemeSetting) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option.setting) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(option.icon, contentDescription = option.title, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(option.title, modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = (option.setting == selectedOption),
                            onClick = { onOptionSelected(option.setting) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOptionGroup(
    title: String,
    options: List<ColorOption>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { option ->
                    ColorCircle(
                        color = option.color,
                        isSelected = (option.name == selectedOption),
                        onClick = { onOptionSelected(option.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Đã chọn", tint = Color.White)
        }
    }
}