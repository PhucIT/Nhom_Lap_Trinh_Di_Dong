package com.example.expensemanagement.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.Result
import com.example.expensemanagement.data.model.Transaction
import com.example.expensemanagement.data.repository.WalletRepository
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val transactionId: String = savedStateHandle.get<String>("transactionId") ?: ""

    init {
        if (transactionId.isNotBlank()) {
            viewModelScope.launch {
                transactionRepository.getTransactionById(transactionId).collect { tx ->
                    _uiState.value = TransactionDetailUiState(isLoading = false, transaction = tx)
                }
            }
        } else {
            _uiState.value = TransactionDetailUiState(isLoading = false, errorMessage = "Không tìm thấy giao dịch")
        }
    }

    fun deleteTransaction(onSuccess: () -> Unit) {
        val transaction = _uiState.value.transaction ?: return

        viewModelScope.launch {
            val deleteResult = transactionRepository.deleteTransaction(transaction.id)

            if (deleteResult is Result.Success) {
                // Nếu là chi tiêu, xóa đi -> cộng tiền lại
                // Nếu là thu nhập, xóa đi -> trừ tiền đi
                val amountChange = if (transaction.type == "Expense") transaction.amount else -transaction.amount

                walletRepository.updateWalletBalance(transaction.walletId, amountChange)
                onSuccess()
            } else if (deleteResult is Result.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = deleteResult.exception.message)
            }
        }
    }
}