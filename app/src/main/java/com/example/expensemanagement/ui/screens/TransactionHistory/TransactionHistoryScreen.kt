// File: ui/screens/history/TransactionHistoryScreen.kt
package com.example.expensemanagement.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.data.model.history.TransactionHistoryItem
import com.example.expensemanagement.ui.components.TransactionHistory.DateHeader
import com.example.expensemanagement.ui.components.TransactionHistory.TabButton
import com.example.expensemanagement.ui.components.TransactionHistory.TransactionRowItem
import com.example.expensemanagement.viewmodel.TransactionHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit, // điều hướng
    viewModel: TransactionHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch sử giao dịch",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    Card(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(8.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Lọc ngày */ }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Lọc ngày",
//                            tint = Color(0xFFFFC107)
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE0F7FA))
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
//        containerColor = Color(0xFFE0F7FA) // Nền xanh nhạt toàn màn hình
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. Tab Chuyển đổi (Chi phí / Thu nhập)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFB2EBF2))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabButton(
                    text = "Chi phí",
                    isSelected = uiState.typeFilter == "Expense",
                    onClick = { viewModel.onTypeChanged("Expense") },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Thu nhập",
                    isSelected = uiState.typeFilter == "Income",
                    onClick = { viewModel.onTypeChanged("Income") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Khối Tổng quan & Tìm kiếm
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White)
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hôm nay, ${SimpleDateFormat("dd 'tháng' MM, yyyy", Locale("vi", "VN")).format(Date())}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Ô TÌM KIẾM THẬT
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged, // <-- KẾT NỐI VỚI VIEWMODEL
                        placeholder = { Text("Tìm kiếm giao dịch", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Danh sách Giao dịch
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.listItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (uiState.searchQuery.isNotBlank()) "Không tìm thấy kết quả" else "Chưa có giao dịch nào", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.listItems,
                        key = { item ->
                            when (item) {
                                is TransactionHistoryItem.Header -> item.date.time
                                is TransactionHistoryItem.TransactionRow -> item.transactionUiModel.id
                            }
                        }
                    ) { item ->
                        when (item) {
                            is TransactionHistoryItem.Header -> DateHeader(date = item.date)
                            is TransactionHistoryItem.TransactionRow -> {
                                Box(
                                    modifier = Modifier.clickable {
                                        onNavigateToDetail(item.transactionUiModel.id)
                                    }
                                ){
                                    TransactionRowItem(item = item.transactionUiModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
