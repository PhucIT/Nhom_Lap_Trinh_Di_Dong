
package com.example.expensemanagement.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.ui.screens.home.formatCurrency
import com.example.expensemanagement.viewmodel.ReportUiState
import com.example.expensemanagement.viewmodel.ReportViewModel
import com.example.expensemanagement.viewmodel.TimeFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo Cáo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE3F2FD))
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
//        containerColor = Color(0xFFE3F2FD)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ReportHeader(
                selectedFilter = uiState.selectedTimeFilter,
                onFilterSelected = viewModel::onTimeFilterChanged
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
//                    .background(Color.White)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ... (Phần Loading và các khối khác giữ nguyên)
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.error != null) {
                    Text("Lỗi: ${uiState.error}", color = Color.Red)
                } else {
                    IncomeExpenseSummary(
                        totalIncome = uiState.totalIncome,
                        incomeChange = uiState.incomeChangePercent,
                        totalExpense = uiState.totalExpense,
                        expenseChange = uiState.expenseChangePercent
                    )
                    SpendingTrendChart(data = uiState.expenseTrend)
                }
            }
        }
    }
}


@Composable
fun ReportHeader(selectedFilter: TimeFilter, onFilterSelected: (TimeFilter) -> Unit) {
    val monthYear = SimpleDateFormat("MMMM, yyyy", Locale("vi", "VN")).format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tổng quan chi tiêu ${monthYear.replaceFirstChar { it.titlecase(Locale.getDefault()) }}",
            color = MaterialTheme.colorScheme.onBackground
        )



        TimeRangeSelector(
            selectedRange = selectedFilter,
            onRangeSelected = onFilterSelected
        )
    }
}

@Composable
fun TimeRangeSelector(selectedRange: TimeFilter, onRangeSelected: (TimeFilter) -> Unit) {
    // Khung nền màu xám nhạt bo góc cho cả bộ lọc
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
//            .background(Color.White.copy(alpha = 0.7f))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeFilter.values().forEach { range ->
            val isSelected = range == selectedRange
            val text = when (range) {
                TimeFilter.WEEK -> "Tuần"
                TimeFilter.MONTH -> "Tháng"
                TimeFilter.QUARTER -> "Quý"
                TimeFilter.YEAR -> "Năm"
            }

            // Mỗi nút là một Box có thể nhấn
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    // Nền trắng và có viền khi được chọn
//                    .background(if (isSelected) Color.White else Color.Transparent)
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
//                        color = if (isSelected) Color(0xFF00897B) else Color.Transparent,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
//                    color = if (isSelected) Color(0xFF00897B) else Color.Gray,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun IncomeExpenseSummary(totalIncome: Double, incomeChange: Int, totalExpense: Double, expenseChange: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryColumn(label = "TỔNG THU", amount = totalIncome, percentage = incomeChange, color = Color(0xFF00897B))
        Divider(modifier = Modifier.height(60.dp).width(1.dp), color = Color.LightGray)
        SummaryColumn(label = "TỔNG CHI", amount = totalExpense, percentage = expenseChange, color = Color.Red)
    }
}

@Composable
fun SummaryColumn(label: String, amount: Double, percentage: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
//        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = amount.formatCurrency(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "${if (percentage >= 0) "+$percentage%" else "$percentage%"} so với tháng trước",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
//            color = Color.Gray
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SpendingTrendChart(data: Map<String, Double>) {
    val maxValue = data.values.maxOrNull() ?: 1.0
    Column {
        Text("Xu Hướng Chi Tiêu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (data.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (label, value) ->
                    val heightFraction = (value / maxValue).toFloat().coerceIn(0f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(heightFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
//                                .background(Color(0xFFFF5252))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
//                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Chưa có dữ liệu chi tiêu",
//                    color = Color.Gray
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

