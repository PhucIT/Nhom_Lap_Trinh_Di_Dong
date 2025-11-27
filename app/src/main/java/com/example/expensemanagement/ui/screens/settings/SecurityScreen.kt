package com.example.expensemanagement.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.viewmodel.SecurityUiState
import com.example.expensemanagement.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSetPin: () -> Unit, // Đi đến màn hình Tạo PIN
    onNavigateToChangePin: () -> Unit, // Đi đến màn hình Đổi PIN
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bảo mật") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nhóm 1: Khóa ứng dụng
            Text("Khóa ứng dụng", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Card {
                SecuritySwitchItem(
                    icon = Icons.Default.Lock,
                    title = "Khóa ứng dụng",
                    subtitle = "Yêu cầu xác thực mỗi khi mở ứng dụng",
                    checked = uiState.isAppLockEnabled,
                    onCheckedChange = { isEnabled ->
                        // Nếu người dùng bật Khóa lần đầu và chưa có PIN, điều hướng đến màn hình Tạo PIN
                        if (isEnabled && !uiState.hasPinSetup) {
                            onNavigateToSetPin()
                        } else {
                            viewModel.onAppLockToggle(isEnabled)
                        }
                    }
                )
            }

            // Nhóm 2: Phương thức khóa (Chỉ hiện khi Khóa ứng dụng được bật)
            if (uiState.isAppLockEnabled) {
                Text("Phương thức khóa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Card {
                    Column {
                        // Bật/tắt sinh trắc học
                        SecuritySwitchItem(
                            icon = Icons.Default.Fingerprint,
                            title = "Sử dụng vân tay/khuôn mặt",
                            subtitle = "Dùng sinh trắc học để mở khóa nhanh hơn",
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = viewModel::onBiometricToggle,
                            enabled = uiState.canUseBiometrics // Chỉ bật được khi máy có hỗ trợ
                        )

                        Divider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Điều hướng đến màn hình đổi mã PIN
                        SecurityActionItem(
                            icon = Icons.Default.Password,
                            title = "Đổi mã PIN",
                            onClick = onNavigateToChangePin
                        )
                    }
                }
            }
        }
    }
}


// Composable cho một dòng cài đặt có Switch
@Composable
private fun SecuritySwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

// Composable cho một dòng cài đặt có hành động (mũi tên)
@Composable
private fun SecurityActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}