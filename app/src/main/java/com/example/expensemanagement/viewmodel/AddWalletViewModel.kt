package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import com.example.expensemanagement.data.Result

// Trạng thái UI (Giữ nguyên)
sealed class AddWalletUiState {
    object Idle : AddWalletUiState()
    object Loading : AddWalletUiState()
    object Success : AddWalletUiState()
    data class Error(val message: String) : AddWalletUiState()
}

@HiltViewModel
class AddWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    // --- State chung ---
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()
    private val _uiState = MutableStateFlow<AddWalletUiState>(AddWalletUiState.Idle)
    val uiState: StateFlow<AddWalletUiState> = _uiState.asStateFlow()
    private val _walletCurrency = MutableStateFlow("VND") // Vẫn có thể dùng chung
    val walletCurrency: StateFlow<String> = _walletCurrency.asStateFlow()

    // --- SỬA 1: TÁCH RIÊNG STATE CHO TỪNG TAB ---

    // State cho Tab 0 (Ví thường)
    private val _normalWalletName = MutableStateFlow("")
    val normalWalletName: StateFlow<String> = _normalWalletName.asStateFlow()
    private val _normalWalletBalance = MutableStateFlow("")
    val normalWalletBalance: StateFlow<String> = _normalWalletBalance.asStateFlow()

    // State cho Tab 1 (Ví tiết kiệm)
    private val _savingWalletName = MutableStateFlow("")
    val savingWalletName: StateFlow<String> = _savingWalletName.asStateFlow()
    private val _savingSourceName = MutableStateFlow("")
    val savingSourceName: StateFlow<String> = _savingSourceName.asStateFlow()
    private val _savingBalance = MutableStateFlow("") // Tiền gốc
    val savingBalance: StateFlow<String> = _savingBalance.asStateFlow()
    private val _savingTargetBalance = MutableStateFlow("") // Tiền lãi
    val savingTargetBalance: StateFlow<String> = _savingTargetBalance.asStateFlow()
    private val _savingStartDate = MutableStateFlow<Date?>(null)
    val savingStartDate: StateFlow<Date?> = _savingStartDate.asStateFlow()
    private val _savingEndDate = MutableStateFlow<Date?>(null)
    val savingEndDate: StateFlow<Date?> = _savingEndDate.asStateFlow()

    // State cho Tab 2 (Ví tín dụng)
    private val _creditWalletName = MutableStateFlow("")
    val creditWalletName: StateFlow<String> = _creditWalletName.asStateFlow()
    private val _creditBalance = MutableStateFlow("") // Dư nợ
    val creditBalance: StateFlow<String> = _creditBalance.asStateFlow()
//    private val _creditLimit = MutableStateFlow("") // Hạn mức
//    val creditLimit: StateFlow<String> = _creditLimit.asStateFlow()
//    private val _statementDate = MutableStateFlow("") // Ngày sao kê
//    val statementDate: StateFlow<String> = _statementDate.asStateFlow()
//    private val _paymentDueDate = MutableStateFlow("") // Ngày thanh toán
//    val paymentDueDate: StateFlow<String> = _paymentDueDate.asStateFlow()


    // --- Hàm cập nhật (Cũng được tách riêng) ---
    fun onTabSelected(index: Int) { _selectedTabIndex.value = index }
    fun onCurrencyChange(currency: String) { _walletCurrency.value = currency }

    // Tab 0
    fun onNormalNameChange(name: String) { _normalWalletName.value = name }
    fun onNormalBalanceChange(balance: String) { _normalWalletBalance.value = balance }

    // Tab 1
    fun onSavingNameChange(name: String) { _savingWalletName.value = name }
    fun onSavingSourceNameChange(name: String) { _savingSourceName.value = name }
    fun onSavingBalanceChange(balance: String) { _savingBalance.value = balance }
    fun onSavingTargetChange(target: String) { _savingTargetBalance.value = target }
    fun onSavingStartDateChange(date: Date?) { _savingStartDate.value = date }
    fun onSavingEndDateChange(date: Date?) { _savingEndDate.value = date }

    // Tab 2
    fun onCreditNameChange(name: String) { _creditWalletName.value = name }
    fun onCreditBalanceChange(balance: String) { _creditBalance.value = balance }
//    fun onCreditLimitChange(limit: String) { _creditLimit.value = limit }
//    fun onStatementDateChange(date: String) { _statementDate.value = date }
//    fun onPaymentDueDateChange(date: String) { _paymentDueDate.value = date }


    // --- HÀM LƯU VÍ (Đã sửa lại để dùng các biến riêng) ---
    fun saveWallet() {
        when (_selectedTabIndex.value) {
            0 -> saveNormalWallet()
            1 -> saveSavingWallet()
            2 -> saveCreditWallet()
        }
    }

    private fun saveNormalWallet() {
        val name = _normalWalletName.value.trim()
        val balance = _normalWalletBalance.value.toDoubleOrNull()
        if (name.isBlank() || balance == null) {
            _uiState.value = AddWalletUiState.Error("Tên ví và Số dư không hợp lệ")
            return
        }
        val wallet = Wallet(name = name, balance = balance, currency = _walletCurrency.value.trim(), type = "Normal")
        executeSave(wallet)
    }

    private fun saveSavingWallet() {
        val name = _savingWalletName.value.trim()
        val balance = _savingBalance.value.toDoubleOrNull()
        val sourceName = _savingSourceName.value.trim()
        val targetBalance = _savingTargetBalance.value.toDoubleOrNull()
        val startDate = _savingStartDate.value
        val endDate = _savingEndDate.value
        if (name.isBlank() || balance == null || sourceName.isBlank() || targetBalance == null || startDate == null || endDate == null) {
            _uiState.value = AddWalletUiState.Error("Vui lòng nhập đầy đủ thông tin")
            return
        }
        val wallet = Wallet(
            name = name, balance = balance, currency = _walletCurrency.value.trim(), type = "Saving",
            sourceWalletId = sourceName, targetBalance = targetBalance, startDate = startDate, endDate = endDate
        )
        executeSave(wallet)
    }

    private fun saveCreditWallet() {
        val name = _creditWalletName.value.trim()
        val balance = _creditBalance.value.toDoubleOrNull() // Dư nợ

        // Bây giờ chỉ cần kiểm tra tên và dư nợ
        if (name.isBlank() || balance == null) {
            _uiState.value = AddWalletUiState.Error("Vui lòng nhập Tên thẻ và Dư nợ hiện tại")
            return
        }

        // Tạo ví chỉ với các thông tin cần thiết
        val wallet = Wallet(
            name = name,
            balance = balance, // Dư nợ sẽ được lưu vào trường balance
            currency = _walletCurrency.value.trim(),
            type = "Credit",
            // Các trường khác sẽ có giá trị mặc định là null hoặc 0.0
            creditLimit = 0.0,
            statementDate = null,
            paymentDueDate = null
        )
        executeSave(wallet)
    }

    private fun executeSave(wallet: Wallet) {
        _uiState.value = AddWalletUiState.Loading
        viewModelScope.launch {
            val result = walletRepository.addWallet(wallet)
            when (result) {
                is Result.Success -> _uiState.value = AddWalletUiState.Success
                is Result.Error -> _uiState.value = AddWalletUiState.Error(result.exception.message ?: "Lỗi không xác định")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddWalletUiState.Idle
        // Reset tất cả các state
        _normalWalletName.value = ""
        _normalWalletBalance.value = ""
        _savingWalletName.value = ""
        _savingBalance.value = ""
        _savingSourceName.value = ""
        _savingTargetBalance.value = ""
        _savingStartDate.value = null
        _savingEndDate.value = null
        _creditWalletName.value = ""
        _creditBalance.value = ""
//        _creditLimit.value = ""
//        _statementDate.value = ""
//        _paymentDueDate.value = ""
    }
}
