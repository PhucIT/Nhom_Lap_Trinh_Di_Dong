// File: viewmodel/TransactionHistoryViewModel.kt
package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.model.Transaction
import com.example.expensemanagement.data.model.history.TransactionHistoryItem
import com.example.expensemanagement.data.model.history.toUiModel
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

// --- ĐƠN GIẢN HÓA UI STATE, BỎ filterMode ---
data class TransactionHistoryUiState(
    val isLoading: Boolean = true,
    val typeFilter: String = "Expense",
    val listItems: List<TransactionHistoryItem> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "" // Chỉ giữ lại tìm kiếm
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            // --- SỬA LẠI COMBINE: Chỉ lắng nghe Giao dịch, Loại Chi/Thu, và Tìm kiếm ---
            combine(
                transactionRepository.getTransactions(),
                _uiState.map { it.typeFilter }.distinctUntilChanged(),
                _uiState.map { it.searchQuery }.distinctUntilChanged()
            ) { transactions, filterType, query ->
                // Gọi hàm xử lý đã được đơn giản hóa
                processAndGroupTransactions(transactions, filterType, query)
            }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { groupedList ->
                    _uiState.update { it.copy(isLoading = false, listItems = groupedList) }
                }
        }
    }

    /**
     * Hàm này nhận vào danh sách Transaction thô, thực hiện các bước:
     * 1. Lọc theo loại Chi/Thu.
     * 2. Lọc theo từ khóa tìm kiếm.
     * 3. Sắp xếp theo ngày mới nhất lên đầu.
     * 4. Luôn luôn gom nhóm theo ngày.
     */
    private fun processAndGroupTransactions(
        transactions: List<Transaction>,
        filterType: String,
        query: String
    ): List<TransactionHistoryItem> {

        val groupedItems = mutableListOf<TransactionHistoryItem>()

        transactions
            .filter { it.type == filterType } // 1. Lọc theo Chi/Thu
            .filter {
                // 2. Lọc theo từ khóa tìm kiếm
                if (query.isNotBlank()) {
                    it.categoryName.contains(query, ignoreCase = true) ||
                            it.note?.contains(query, ignoreCase = true) == true
                } else {
                    true // Nếu query rỗng thì không lọc
                }
            }
            .sortedByDescending { it.date } // 3. Sắp xếp
            .groupBy { transaction ->
                // 4. Luôn luôn gom nhóm theo ngày
                val cal = Calendar.getInstance()
                cal.time = transaction.date ?: Date()
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.time
            }
            .forEach { (date, transactionList) ->
                groupedItems.add(TransactionHistoryItem.Header(date))
                transactionList.forEach { transaction ->
                    groupedItems.add(TransactionHistoryItem.TransactionRow(transaction.toUiModel()))
                }
            }

        return groupedItems
    }

    // --- Các hàm cập nhật được gọi từ UI ---
    fun onTypeChanged(newType: String) {
        _uiState.update { it.copy(typeFilter = newType) }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }
}
