package com.example.expensemanagement.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.R
import com.example.expensemanagement.data.model.Transaction
import com.example.expensemanagement.ui.screens.home.UserMenu // <-- TÁI SỬ DỤNG MENU TỪ HOME
import com.example.expensemanagement.ui.screens.home.formatCurrency
import com.example.expensemanagement.viewmodel.WalletDetailViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBudgetSetup: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onLogoutSuccess: () -> Unit, // <-- THÊM HÀM MỚI
    viewModel: WalletDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isBalanceVisible by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) } // <-- State cho menu

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.walletType.ifEmpty { "Chi Tiết Ví" }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // --- SỬA LẠI NÚT MENU ---
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        UserMenu(
                            expanded = showMenu,
                            onDismiss = { showMenu = false },
                            user = uiState.currentUser,
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = Color(0xFF80DEEA),
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm Giao Dịch", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (uiState.errorMessage != null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Lỗi: ${uiState.errorMessage}", color = Color.Red)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // KHỐI 1: SỐ DƯ
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Số Dư:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (isBalanceVisible) uiState.totalBalance.formatCurrency() else "∗∗∗∗∗∗∗", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Red)
                        }
                        IconButton(onClick = { isBalanceVisible = !isBalanceVisible }) {
                            Icon(imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Ẩn/Hiện", tint = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // KHỐI 2: NGÂN SÁCH CÁ NHÂN
            item {
                Text(
                    text = "Ngân Sách Cá Nhân",
                    style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.height(8.dp))

                BudgetCard(
                    spentAmount = uiState.spentAmount,
                    remainingAmount = uiState.remainingAmount,
                    limitAmount = uiState.budgetLimit, // <-- Truyền Hạn mức
                    progress = uiState.budgetProgress
//                    isBalanceVisible = true
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // KHỐI 3: NÚT "+ THIẾT LẬP NGÂN SÁCH"
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(
                        onClick = onNavigateToBudgetSetup,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .background(Color(0xFF80CBC4), RoundedCornerShape(8.dp))
                    ) {
//                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text("Thiết lập ngân sách", fontSize = 12.sp)
                        // Hiển thị icon và chữ khác nhau
                        if (uiState.hasBudget) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sửa ngân sách", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thiết lập ngân sách", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }


            // KHỐI 4: GIAO DỊCH GẦN ĐÂY
            item {
                Text(
                    text = "Giao Dịch gần Đây",
                    style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(uiState.transactions) { transaction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun BudgetCard(
    spentAmount: Double,
    remainingAmount: Double,
    progress: Float,
    limitAmount: Double // <-- Nhận Hạn mức
//    isBalanceVisible: Boolean
) {

    val isOverBudget = spentAmount > limitAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Dòng 1: Hạn mức
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hạn mức:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    text = limitAmount.formatCurrency(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // đã chi
            Row {
                Text("đã chi: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = spentAmount.formatCurrency(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverBudget) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverBudget) Color.Red else Color(0xFF26A69A), // Đổi màu nếu vượt mức
                trackColor = Color.LightGray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Dòng 3: Còn lại hoặc Cảnh báo
            if (isOverBudget) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Warning, contentDescription = "Cảnh báo", tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Bạn đã vượt hạn mức!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Còn lại:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = remainingAmount.formatCurrency(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isExpense = transaction.type == "Expense"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Thay bằng icon thật
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note ?: "Giao dịch",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.date.formatDateSimple(), // dd/MM/yyyy
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Text(
            text = (if(isExpense) "" else "+") + transaction.amount.formatCurrency(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
    }
}

private fun Date?.formatDateSimple(): String {
    if (this == null) return ""
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
    } catch (e: Exception) {
        ""
    }
}