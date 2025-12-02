package com.example.expensemanagement.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit, // Đi đến Màn 18 (Hồ sơ)
    onNavigateToChangePassword: () -> Unit, // Đi đến Màn 22 (Đổi mật khẩu)
    onNavigateToMyQrCode: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onLogout: () -> Unit, // Xử lý đăng xuất
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Lấy thông tin user từ ViewModel
    val currentUser by viewModel.currentUserInfo.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // <-- SỬ DỤNG onNavigateBack
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = Color(0xFFE0F7FA) // Màu xanh nhạt giống ảnh
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
//        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // 1. HEADER PROFILE (Thẻ Thông Tin Cá Nhân)
            // (Phần nền xanh nhạt phía trên)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(Color(0xFFE0F7FA))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .clickable { onNavigateToProfile() }, // Nhấn vào để sửa hồ sơ
                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar (Hình tròn)
                        Surface(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable { onNavigateToProfile() },
                            shape = CircleShape,
//                            color = Color.LightGray
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            // Hiển thị icon người nếu chưa có ảnh
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
//                                    tint = Color.White
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            // TODO: Dùng Coil để load ảnh avatar thật từ URL (currentUser?.photoUrl)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Tên và Email
                        Column(modifier = Modifier
                            .weight(1f)
                            .clickable{onNavigateToProfile()}
                        ) {
                            Text(
                                text = currentUser?.displayName ?: "Người dùng mới",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser?.email ?: "Chưa có email",
                                style = MaterialTheme.typography.bodySmall,
//                                color = Color.Gray
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Icon QR Code (Giống ảnh)
//                        Icon(
//                            imageVector = Icons.Default.QrCode,
//                            contentDescription = "QR Code",
//                            modifier = Modifier.size(24.dp),
//                            tint = Color.Black
//                        )
                        IconButton(onClick = onNavigateToMyQrCode) { // <-- GỌI HÀM ĐIỀU HƯỚNG MỚI
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Mã QR của tôi",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. DANH SÁCH TÙY CHỌN (Settings List)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                SettingsItem(
                    icon = Icons.Default.Contacts,
                    title = "Danh bạ",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider()

                SettingsItem(
                    icon = Icons.Default.CreditCard,
                    title = "Mở thẻ",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider()

                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Bảo mật",
//                    onClick = { onNavigateToSecurity() }
                    onClick = onNavigateToSecurity
                )
                HorizontalDivider()

                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Chia sẻ",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider()

                SettingsItem(
                    icon = Icons.Default.Palette, // Hoặc Icons.Default.Image
                    title = "Chủ đề",
                    onClick = { onNavigateToTheme() }
                )
                HorizontalDivider()

                SettingsItem(
                    icon = Icons.Default.Key, // Hoặc Lock
                    title = "Đổi mật khẩu",
                    onClick = onNavigateToChangePassword // Điều hướng đến Màn 22
                )
                HorizontalDivider()

                SettingsItem(
                    icon = Icons.Default.Link,
                    title = "Tài khoản liên kết",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider()

                Spacer(modifier = Modifier.height(24.dp))

                // 3. KHỐI DƯỚI CÙNG (Nhóm & Đăng xuất)
                SettingsItem(
                    icon = Icons.Default.Group,
                    title = "Nhóm",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider()

                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Đăng xuất",
                    onClick = {
                        viewModel.logout() // Gọi hàm đăng xuất
                        onLogout() // Gọi callback để chuyển về màn Login
                    },
//                    textColor = Color.Red, // Chữ đỏ cho nút đăng xuất (nếu muốn)
//                    iconColor = Color.Red
                    textColor = MaterialTheme.colorScheme.error,
                    iconColor = MaterialTheme.colorScheme.error
                )
            }

            // Thêm khoảng trắng ở dưới để không bị che bởi BottomBar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Composable cho một dòng Cài đặt (Reusable)
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
//    iconColor: Color = Color.Black,
//    textColor: Color = Color.Black
    // SỬA LẠI: Giá trị mặc định bây giờ là Color.Unspecified để nó tự lấy màu theme
    iconColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon bên trái
        Icon(
            imageVector = icon,
            contentDescription = title,
//            tint = iconColor,
//            modifier = Modifier.size(24.dp)
            // SỬA LẠI: Nếu không truyền màu, nó sẽ tự lấy màu onSurface (LocalContentColor.current)
            tint = if (iconColor != Color.Unspecified) iconColor else LocalContentColor.current,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Text tiêu đề
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium, // Đậm vừa phải
//            color = textColor,
            color = if (textColor != Color.Unspecified) textColor else Color.Unspecified,
            modifier = Modifier.weight(1f)
        )

        // Mũi tên nhỏ bên phải
         Icon(
             Icons.Default.ChevronRight,
             contentDescription = null,
//             tint = Color.Gray
             tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
         )
    }
}

@Composable
fun HorizontalDivider() {
//    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
}