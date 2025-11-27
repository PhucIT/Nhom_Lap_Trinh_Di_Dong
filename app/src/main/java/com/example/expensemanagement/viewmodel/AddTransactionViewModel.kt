package com.example.expensemanagement.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.Result
import com.example.expensemanagement.data.model.Category
import com.example.expensemanagement.data.model.Transaction
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.data.repository.Category.CategoryRepository
import com.example.expensemanagement.data.repository.WalletRepository
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// Data Class trạng thái UI đã được thiết kế tốt, giữ nguyên
data class AddTransactionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val type: String = "Expense",
    val amount: String = "",
    val note: String = "",
    val date: Date = Date(),
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedWallet: Wallet? = null,
    val selectedCategory: Category? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    // Lấy ID ví nếu được truyền từ màn hình trước
    private val initialWalletId: String? = savedStateHandle.get<String>("walletId")

    init {
        // --- SỬA LẠI HOÀN TOÀN LOGIC INIT ĐỂ KHÔNG BỊ GHI ĐÈ STATE ---

        // 1. Lắng nghe sự thay đổi của loại giao dịch (type) để lấy category tương ứng
        viewModelScope.launch {
            _uiState.map { it.type }
                .distinctUntilChanged() // Chỉ chạy khi type thực sự thay đổi (Expense -> Income)
                .flatMapLatest { type -> // Khi type thay đổi, hủy coroutine cũ và chạy cái mới
                    categoryRepository.getCategories(type)
                }
                .catch { e ->
                    // Xử lý lỗi nếu không lấy được category
                    _uiState.update { it.copy(error = "Lỗi tải hạng mục: ${e.message}") }
                }
                .collect { categories ->
                    // Cập nhật danh sách category và tự động chọn mục đầu tiên
                    _uiState.update {
                        it.copy(categories = categories, selectedCategory = categories.firstOrNull())
                    }
                }
        }

        // 2. Lấy danh sách ví (chỉ một lần)
        viewModelScope.launch {
            walletRepository.getWallets()
                .catch { e ->
                    _uiState.update { it.copy(error = "Lỗi tải danh sách ví: ${e.message}") }
                }
                .collect { wallets ->
                    // Tự động chọn ví nếu có ID truyền vào, hoặc chọn ví đầu tiên
                    val preSelected = wallets.find { it.id == initialWalletId } ?: wallets.firstOrNull()
                    _uiState.update {
                        it.copy(wallets = wallets, selectedWallet = preSelected)
                    }
                }
        }

        // 3. Gọi hàm khởi tạo Category mặc định (chỉ chạy khi cần)
        viewModelScope.launch {
            categoryRepository.initDefaultCategories()
        }
    }

    // --- Các hàm cập nhật UI ---
    fun onTypeChanged(newType: String) {
        // Chỉ cần cập nhật type, khối combine ở init sẽ tự động xử lý việc lấy lại category
        _uiState.update { it.copy(type = newType) }
    }

    fun onAmountChanged(newAmount: String) {
        if (newAmount.all { it.isDigit() }) {
            _uiState.update { it.copy(amount = newAmount) }
        }
    }

    fun onNoteChanged(newNote: String) { _uiState.update { it.copy(note = newNote) } }
    fun onDateChanged(newDate: Date) { _uiState.update { it.copy(date = newDate) } }
    fun onWalletSelected(wallet: Wallet) { _uiState.update { it.copy(selectedWallet = wallet) } }
    fun onCategorySelected(category: Category) { _uiState.update { it.copy(selectedCategory = category) } }

    // --- Hàm Lưu Giao Dịch ---
    fun saveTransaction() {
        val state = _uiState.value
        val amountVal = state.amount.toDoubleOrNull()

        // Phần kiểm tra lỗi giữ nguyên, đã đúng
        if (amountVal == null || amountVal <= 0) {
            _uiState.update { it.copy(error = "Vui lòng nhập số tiền hợp lệ") }
            return
        }
        if (state.selectedCategory == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn hạng mục") }
            return
        }
        if (state.selectedWallet == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn ví") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val newTransaction = Transaction(
                amount = amountVal,
                type = state.type,
                date = state.date,
                note = state.note.ifBlank { state.selectedCategory!!.name },
                walletId = state.selectedWallet!!.id,
                categoryId = state.selectedCategory!!.id,
                // Giả sử data class Transaction của bạn có 2 trường này để hiển thị nhanh
                walletName = state.selectedWallet!!.name,
                categoryName = state.selectedCategory!!.name
            )

            // 1. Lưu Giao Dịch
            val result = transactionRepository.addTransaction(newTransaction)

            if (result is Result.Success) {
                // 2. CẬP NHẬT SỐ DƯ VÍ
                val currentBalance = state.selectedWallet!!.balance
                val amountDelta = if (state.type == "Expense") -amountVal else amountVal
                val updatedWallet = state.selectedWallet!!.copy(balance = currentBalance + amountDelta)
                val walletUpdateResult = walletRepository.updateWallet(updatedWallet)

                if (walletUpdateResult is Result.Success) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Lưu giao dịch thành công nhưng lỗi cập nhật số dư.") }
                }
            } else {
                val errorMsg = (result as? Result.Error)?.exception?.message ?: "Lỗi không xác định"
                _uiState.update { it.copy(isLoading = false, error = "Lỗi lưu: $errorMsg") }
            }
        }
    }

    fun resetState() {
        // Chỉ reset các cờ trạng thái, không reset dữ liệu đã chọn
        _uiState.update { it.copy(isSuccess = false, error = null) }
    }
}
