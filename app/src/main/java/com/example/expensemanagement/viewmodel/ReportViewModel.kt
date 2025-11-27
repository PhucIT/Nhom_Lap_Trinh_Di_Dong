// File: viewmodel/ReportViewModel.kt
package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.model.Transaction
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

enum class TimeFilter {
    WEEK, MONTH, QUARTER, YEAR
}

// --- SỬA LẠI UI STATE CHO KHỚP VỚI GIAO DIỆN MỚI ---
data class ReportUiState(
    val isLoading: Boolean = true,
    val selectedTimeFilter: TimeFilter = TimeFilter.MONTH,
    val totalIncome: Double = 0.0,
    val incomeChangePercent: Int = 0,
    val totalExpense: Double = 0.0,
    val expenseChangePercent: Int = 0,
    // SỬA 1: Đổi tên chartData thành expenseTrend và dùng Map<String, Double>
    val expenseTrend: Map<String, Double> = emptyMap(),
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        // Lắng nghe sự thay đổi của bộ lọc thời gian
        viewModelScope.launch {
            _uiState.map { it.selectedTimeFilter }
                .distinctUntilChanged()
                .flatMapLatest { filter ->
                    val (currentStart, currentEnd) = getDateRange(filter, 0)
                    val (previousStart, previousEnd) = getDateRange(filter, 1)

                    combine(
                        transactionRepository.getTransactionsBetween(currentStart, currentEnd),
                        transactionRepository.getTransactionsBetween(previousStart, previousEnd)
                    ) { currentTransactions, previousTransactions ->
                        // --- Thực hiện tính toán ---
                        val totalIncome = currentTransactions.filter { it.type == "Income" }.sumOf { it.amount }
                        val totalExpense = currentTransactions.filter { it.type == "Expense" }.sumOf { it.amount }

                        val prevTotalIncome = previousTransactions.filter { it.type == "Income" }.sumOf { it.amount }
                        val prevTotalExpense = previousTransactions.filter { it.type == "Expense" }.sumOf { it.amount }

                        val incomeChange = calculatePercentageChange(prevTotalIncome, totalIncome)
                        val expenseChange = calculatePercentageChange(prevTotalExpense, totalExpense)

                        // SỬA 2: Chuẩn bị dữ liệu cho SimpleBarChart
                        val trendData = currentTransactions
                            .filter { it.type == "Expense" }
                            .groupBy { it.categoryName }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                            .entries
                            .sortedByDescending { it.value }
                            .take(5)
                            .associate { it.key to it.value } // Chuyển thành Map<String, Double>

                        // Cập nhật State
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                incomeChangePercent = incomeChange,
                                expenseChangePercent = expenseChange,
                                expenseTrend = trendData // <-- Dùng expenseTrend
                            )
                        }
                    }
                }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect()
        }
    }

    // Hàm được gọi từ UI khi người dùng chọn bộ lọc mới
    fun onTimeFilterChanged(newFilter: TimeFilter) {
        _uiState.update { it.copy(selectedTimeFilter = newFilter, isLoading = true) }
    }

    // Hàm tính toán khoảng thời gian
    private fun getDateRange(filter: TimeFilter, periodOffset: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        when (filter) {
            TimeFilter.WEEK -> { /* ... */ }
            TimeFilter.MONTH -> {
                calendar.add(Calendar.MONTH, -periodOffset)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.time
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = calendar.time
                return Pair(start, end)
            }
            TimeFilter.QUARTER -> { /* ... */ }
            TimeFilter.YEAR -> { /* ... */ }
        }
        // Thêm return mặc định để tránh lỗi
        return Pair(Date(), Date())
    }

    // Hàm tính phần trăm thay đổi
    private fun calculatePercentageChange(oldValue: Double, newValue: Double): Int {
        if (oldValue == 0.0) {
            return if (newValue > 0.0) 100 else 0
        }
        return (((newValue - oldValue) / oldValue) * 100).toInt()
    }
}
