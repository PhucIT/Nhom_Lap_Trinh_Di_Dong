
package com.example.expensemanagement.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.ui.components.AppBottomNavBar
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.HomeUiState
import com.example.expensemanagement.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.*
import com.example.expensemanagement.data.model.WalletGroupInfo

//fun Double.formatCurrency(): String {
//    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
//    return try {
//        format.currency = Currency.getInstance("VND")
//        format.format(this).replace("₫", " VND")
//    } catch (e: Exception) {
//        this.toString()
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(
//    onAddWalletClicked: () -> Unit,
//    onWalletGroupClicked: (String) -> Unit,
//    onAddTransactionClicked: () -> Unit,
//    onLogoutSuccess: () -> Unit, // <--  ĐỂ XỬ LÝ SAU KHI ĐĂNG XUẤT
//    navController: NavController,
//    viewModel: HomeViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//
//    // State để quản lý việc đóng/mở DropdownMenu
//    var showMenu by remember { mutableStateOf(false) }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Ví", fontWeight = FontWeight.Bold) },
//                actions = {
//                    IconButton(onClick = onAddWalletClicked) {
//                        Icon(Icons.Default.Add, contentDescription = "Thêm ví mới")
//                    }
//
//                    // --- SỬA LẠI NÚT MENU ---
//                    Box {
//                        IconButton(onClick = { showMenu = true }) {
//                            Icon(Icons.Default.Menu, contentDescription = "Menu")
//                        }
//                        // DropdownMenu sẽ hiển thị khi showMenu là true
//                        UserMenu(
//                            expanded = showMenu,
//                            onDismiss = { showMenu = false },
//                            user = (uiState as? HomeUiState.Success)?.currentUser,
//                            onLogout = {
//                                viewModel.logout()
//                                showMenu = false
//                                onLogoutSuccess() // Gọi callback để điều hướng về màn hình Login
//                            }
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        bottomBar = {
//            AppBottomNavBar(navController = navController)
//        },
//        floatingActionButton = {
//            val currentState = uiState
//            if (currentState is HomeUiState.Success && currentState.walletGroups.isNotEmpty()) {
//                FloatingActionButton(
//                    onClick = onAddTransactionClicked,
//                    containerColor = PrimaryGreen,
//                    contentColor = Color.White,
//                    shape = RoundedCornerShape(16.dp)
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch")
//                }
//            }
//        }
//    ) { paddingValues ->
//        when (val state = uiState) {
//            is HomeUiState.Loading -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
//            }
//            is HomeUiState.Error -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text("Lỗi: ${state.message}", color = Color.Red) }
//            }
//            is HomeUiState.Success -> {
//                if (state.walletGroups.isEmpty()) {
//                    WalletEmptyState(
//                        onAddWalletClicked = onAddWalletClicked,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                } else {
//                    WalletListContent(
//                        groupedWallets = state.walletGroups,
//                        onWalletGroupClicked = onWalletGroupClicked,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//            }
//        }
//    }
//}
//
//// --- THÊM COMPOSABLE MỚI CHO MENU NGƯỜI DÙNG ---
//@Composable
//fun UserMenu(
//    expanded: Boolean,
//    onDismiss: () -> Unit,
//    user: User?,
//    onLogout: () -> Unit
//) {
//    DropdownMenu(
//        expanded = expanded,
//        onDismissRequest = onDismiss
//    ) {
//        // Hàng thông tin người dùng
//        if (user != null) {
//            Row(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp, vertical = 8.dp)
//                    .width(IntrinsicSize.Max),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                // Lấy 2 chữ cái đầu của tên
//                val initials = user.displayName?.split(" ")
//                    ?.take(2)
//                    ?.mapNotNull { it.firstOrNull()?.uppercase() }
//                    ?.joinToString("") ?: "U"
//
//                // Avatar chữ
//                Box(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primary),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
//                }
//
//                Column {
//                    Text(user.displayName ?: "Người dùng", fontWeight = FontWeight.Bold)
//                    Text(user.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//                }
//            }
//            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
//        }
//
//        // Nút Đăng xuất
//        DropdownMenuItem(
//            text = { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) },
//            onClick = onLogout,
//            leadingIcon = {
//                Icon(
//                    Icons.AutoMirrored.Filled.ExitToApp,
//                    contentDescription = "Đăng xuất",
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
//        )
//    }
//}
//
//@Composable
//fun WalletListContent(
//    groupedWallets: Map<String, List<Wallet>>,
//    onWalletGroupClicked: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var isBalanceVisible by remember { mutableStateOf(true) }
//
//    LazyColumn(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp),
//        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        item {
//            Text("Danh sách ví", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//        }
//
//        val typeOrder = listOf("Normal", "Saving", "Credit")
//        typeOrder.forEach { type ->
//            if (groupedWallets.containsKey(type)) {
//                val walletsInGroup = groupedWallets[type]!!
//                val groupDisplayName = when (type) {
//                    "Normal" -> "Ví thường"
//                    "Saving" -> "Ví tiết kiệm"
//                    "Credit" -> "Ví tín dụng"
//                    else -> type
//                }
//                item {
//                    WalletGroupCard(
//                        groupName = groupDisplayName,
//                        wallets = walletsInGroup,
//                        isBalanceVisible = isBalanceVisible,
//                        onToggleVisibility = { isBalanceVisible = !isBalanceVisible },
//                        onDetailClicked = { onWalletGroupClicked(groupDisplayName) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun WalletGroupCard(
//    groupName: String,
//    wallets: List<Wallet>,
//    isBalanceVisible: Boolean,
//    onToggleVisibility: () -> Unit,
//    onDetailClicked: () -> Unit
//) {
//    val totalBalance = wallets.sumOf { it.balance }
//    val totalSpent = 0.0 // Giả sử
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(groupName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//            Spacer(modifier = Modifier.height(16.dp))
//            InfoRow(label = "Số dư", amount = totalBalance, isVisible = isBalanceVisible, onToggleVisibility = onToggleVisibility)
//            Spacer(modifier = Modifier.height(8.dp))
//            InfoRow(label = "Đã chi", amount = totalSpent, isVisible = isBalanceVisible)
//            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
//            TextButton(
//                onClick = onDetailClicked,
//                modifier = Modifier.align(Alignment.End),
//                contentPadding = PaddingValues(0.dp)
//            ) {
//                Text("Chi tiết ví", color = PrimaryGreen, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//@Composable
//fun InfoRow(
//    label: String,
//    amount: Double,
//    isVisible: Boolean,
//    onToggleVisibility: (() -> Unit)? = null
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(text = "$label:", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
//        Spacer(modifier = Modifier.weight(1f))
//        Text(
//            text = if (isVisible) amount.formatCurrency() else "∗∗∗∗∗∗∗",
//            style = MaterialTheme.typography.bodyLarge,
//            fontWeight = FontWeight.Bold,
//            color = Color.Red
//        )
//        if (onToggleVisibility != null) {
//            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
//                Icon(
//                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                    contentDescription = "Hiện/Ẩn số dư",
//                    tint = Color.Gray
//                )
//            }
//        } else {
//            Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(start = 4.dp))
//        }
//    }
//}
//
//@Composable
//fun WalletEmptyState(onAddWalletClicked: () -> Unit, modifier: Modifier = Modifier) {
//    Column(
//        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.ic_piggy_bank),
//            contentDescription = "Bạn chưa có ví",
//            modifier = Modifier.size(150.dp)
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//        Text("Bạn chưa có ví nào!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = "Hãy tạo ví ngay để bắt đầu theo dõi thu nhập và chi tiêu",
//            style = MaterialTheme.typography.bodyMedium,
//            textAlign = TextAlign.Center,
//            color = Color.Gray
//        )
//        Spacer(modifier = Modifier.height(32.dp))
//        Button(
//            onClick = onAddWalletClicked,
//            modifier = Modifier.fillMaxWidth().height(56.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Thêm ví", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
//        }
//    }
//}

fun Double.formatCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return try {
        format.currency = Currency.getInstance("VND")
        format.format(this).replace("₫", " VND")
    } catch (e: Exception) {
        this.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddWalletClicked: () -> Unit,
    onWalletGroupClicked: (String) -> Unit,
    onAddTransactionClicked: () -> Unit,
    onLogoutSuccess: () -> Unit,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ví", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onAddWalletClicked) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm ví mới")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        UserMenu(
                            expanded = showMenu,
                            onDismiss = { showMenu = false },
                            user = (uiState as? HomeUiState.Success)?.currentUser,
                            onLogout = {
                                viewModel.logout()
                                showMenu = false
                                onLogoutSuccess()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            AppBottomNavBar(navController = navController)
        },
        floatingActionButton = {
            val currentState = uiState
            if (currentState is HomeUiState.Success && currentState.walletGroups.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddTransactionClicked,
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch")
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center) { Text("Lỗi: ${state.message}", color = Color.Red) }
            }
            is HomeUiState.Success -> {
                if (state.walletGroups.isEmpty()) {
                    WalletEmptyState(
                        onAddWalletClicked = onAddWalletClicked,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    WalletListContent(
                        walletGroups = state.walletGroups,
                        onWalletGroupClicked = onWalletGroupClicked,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun UserMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    user: User?,
    onLogout: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (user != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .width(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val initials = user.displayName?.split(" ")
                    ?.take(2)
                    ?.mapNotNull { it.firstOrNull()?.uppercase() }
                    ?.joinToString("") ?: "U"
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(user.displayName ?: "Người dùng", fontWeight = FontWeight.Bold)
                    Text(user.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
        DropdownMenuItem(
            text = { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) },
            onClick = onLogout,
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Đăng xuất",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

@Composable
fun WalletListContent(
    walletGroups: List<WalletGroupInfo>, // <-- Nhận vào List<WalletGroupInfo>
    onWalletGroupClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Danh sách ví", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(walletGroups, key = { it.groupName }) { groupInfo ->
            WalletGroupCard(
                groupInfo = groupInfo, // <-- Truyền cả đối tượng groupInfo
                isBalanceVisible = isBalanceVisible,
                onToggleVisibility = { isBalanceVisible = !isBalanceVisible },
                onDetailClicked = { onWalletGroupClicked(groupInfo.groupName) }
            )
        }
    }
}

@Composable
fun WalletGroupCard(
    groupInfo: WalletGroupInfo, // <-- Nhận vào WalletGroupInfo
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onDetailClicked: () -> Unit
) {
    // Không cần tính toán ở đây nữa, chỉ lấy dữ liệu đã được tính sẵn
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(groupInfo.groupName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Số dư", amount = groupInfo.totalBalance, isVisible = isBalanceVisible, onToggleVisibility = onToggleVisibility, showVisibilityToggle = true, amountColor = PrimaryGreen)
            Spacer(modifier = Modifier.height(8.dp))
            //  Lấy tổng chi tiêu từ groupInfo
            InfoRow(label = "Đã chi", amount = groupInfo.totalSpentThisMonth, isVisible = isBalanceVisible, showVisibilityToggle = false, amountColor = Color.Red)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
            TextButton(
                onClick = onDetailClicked,
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Chi tiết ví", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    amount: Double,
    isVisible: Boolean,
    onToggleVisibility: (() -> Unit)? = null,
    showVisibilityToggle: Boolean = false,
    amountColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.weight(1f))
//        Text(
//            text = if (isVisible) amount.formatCurrency() else "∗∗∗∗∗∗∗",
//            style = MaterialTheme.typography.bodyLarge,
//            fontWeight = FontWeight.Bold,
//            color = if (label == "Đã chi") Color.Red else MaterialTheme.colorScheme.onSurface
//        )
//        if (onToggleVisibility != null) {
//            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
//                Icon(
//                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                    contentDescription = "Hiện/Ẩn số dư",
//                    tint = Color.Gray
//                )
//            }
//        } else {
//            Spacer(modifier = Modifier.width(24.dp)) // Để căn chỉnh cho thẳng hàng
//        }
        // === PHẦN SỐ TIỀN + VND ĐƯỢC BỌC TRONG BOX ĐỂ CĂN CHUNG ===
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Max) // Đảm bảo 2 dòng có cùng độ rộng
                .padding(end = if (showVisibilityToggle) 0.dp else 40.dp) // Bù khoảng nếu không có icon
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (isVisible) amount.formatCurrency() else "∗∗∗∗∗∗∗",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    textAlign = TextAlign.End
                )

                if (showVisibilityToggle) {
                    Spacer(modifier = Modifier.width(8.dp)) // Cách nhỏ giữa số tiền và icon
                    IconButton(
                        onClick = onToggleVisibility!!,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Hiện/Ẩn",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun WalletEmptyState(onAddWalletClicked: () -> Unit, modifier: Modifier = Modifier) {
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(horizontal = 32.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.ic_piggy_bank),
//            contentDescription = "Bạn chưa có ví",
//            modifier = Modifier.size(150.dp)
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//        Text("Bạn chưa có ví nào!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = "Hãy tạo ví ngay để bắt đầu theo dõi thu nhập và chi tiêu",
//            style = MaterialTheme.typography.bodyMedium,
//            textAlign = TextAlign.Center,
//            color = Color.Gray
//        )
//        Spacer(modifier = Modifier.height(32.dp))
//        Button(
//            onClick = onAddWalletClicked,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Thêm ví", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
//        }
//    }
//}
