package com.example.expensemanagement.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.expensemanagement.R
import com.example.expensemanagement.data.model.User
import com.example.expensemanagement.ui.components.AppBottomNavBar
import com.example.expensemanagement.ui.screens.home.formatCurrency
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.DashboardUiState
import com.example.expensemanagement.viewmodel.DashboardViewModel
import com.example.expensemanagement.viewmodel.DashboardWalletInfo

@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigateToAddWallet: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = { AppBottomNavBar(navController = navController) },
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Lỗi: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            }

            !uiState.hasWallets -> {
                WalletEmptyState(
                    onAddWalletClicked = onNavigateToAddWallet,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                DashboardContent(
                    uiState = uiState,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // KHỐI 1: HEADER (Tự quản lý state của nó)
        item {
            Header(
                user = uiState.currentUser,
                totalBalance = uiState.totalBalance
            )
        }

        // KHỐI 2: DANH SÁCH VÍ
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Danh sách ví của bạn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(uiState.dashboardWallets, key = { it.groupName }) { walletInfo ->
            // Mỗi WalletItem cũng sẽ tự quản lý state của riêng nó
            DashboardWalletItem(
                walletInfo = walletInfo,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun Header(
    user: User?,
    totalBalance: Double
) {
    // TẠO STATE RIÊNG CHO HEADER Ở ĐÂY
    var isBalanceVisible by remember { mutableStateOf(false) } // Mặc định là ẩn

    val headerColor = Brush.verticalGradient(
        colors = listOf(Color(0xFF80DEEA), Color(0xFF26C6DA))
    )
    val initials = user?.displayName?.split(" ")
        ?.take(2)?.mapNotNull { it.firstOrNull()?.uppercase() }?.joinToString("") ?: "U"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerColor)
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 48.dp)
    ) {
        Column {
            // Dòng Chào & Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Chào buổi sáng! 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        user?.displayName ?: "Hôm nay bạn thế nào?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Thẻ Tổng số dư
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBalanceVisible) totalBalance.formatCurrency() else "∗∗∗∗∗∗∗",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    // Nút này chỉ thay đổi state của Header
                    IconButton(onClick = { isBalanceVisible = !isBalanceVisible }) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Hiện/Ẩn số dư"
                        )
                    }
                }
            }
        }
    }
}

/**
 * SỬA LẠI HOÀN TOÀN: Composable này để căn chỉnh cho đúng
 */
@Composable
fun DashboardWalletItem(
    walletInfo: DashboardWalletInfo,
    modifier: Modifier = Modifier
) {
    // TẠO STATE RIÊNG CHO TỪNG ITEM Ở ĐÂY
    var isBalanceVisible by remember { mutableStateOf(false) } // Mặc định là ẩn

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                walletInfo.groupName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Dòng số dư
            BalanceInfoRow(
                label = "Số dư:",
                amount = walletInfo.totalBalance,
                isVisible = isBalanceVisible,
                amountColor = MaterialTheme.colorScheme.primary,
                onToggleVisibility = { isBalanceVisible = !isBalanceVisible } // Có nút mắt
            )

            // Dòng đã chi
            BalanceInfoRow(
                label = "Đã chi tháng này:",
                amount = walletInfo.totalSpentThisMonth,
                isVisible = isBalanceVisible,
                amountColor = MaterialTheme.colorScheme.error,
                onToggleVisibility = null // Không có nút mắt
            )
        }
    }
}

/**
 * TẠO COMPOSABLE MỚI: Dùng riêng cho việc hiển thị các dòng số dư/đã chi
 */
@Composable
private fun BalanceInfoRow(
    label: String,
    amount: Double,
    isVisible: Boolean,
    amountColor: Color,
    onToggleVisibility: (() -> Unit)? // Nút mắt là tùy chọn
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f) // Đẩy số tiền sang phải
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = if (isVisible) amount.formatCurrency() else "∗∗∗∗∗∗∗",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )

            // Icon mắt hoặc khoảng trống để căn chỉnh
            if (onToggleVisibility != null) {
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Hiện/Ẩn số dư",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Đặt một Spacer có cùng kích thước với IconButton để căn lề
                Spacer(modifier = Modifier.width(28.dp))
            }
        }
    }
}

@Composable
fun WalletEmptyState(onAddWalletClicked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_piggy_bank),
            contentDescription = "Bạn chưa có ví",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Bạn chưa có ví nào!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hãy tạo ví ngay để bắt đầu theo dõi thu nhập và chi tiêu",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddWalletClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm ví", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
