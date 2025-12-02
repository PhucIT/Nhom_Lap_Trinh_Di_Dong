package com.example.expensemanagement.viewmodel

import androidx.compose.ui.input.key.type
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
import com.example.expensemanagement.data.repository.Auth.AuthRepository
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
    val selectedCategory: Category? = null,
    val isEditMode: Boolean = false          // true → đang sửa, false → thêm mới
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository, // Thêm để lấy userId
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    // Lấy ID ví nếu được truyền từ màn hình trước
//    private val initialWalletId: String? = savedStateHandle.get<String>("walletId")

    // Lấy ID từ navigation
    private val transactionId: String? = savedStateHandle.get<String>("transactionId")
    private val initialWalletId: String? = savedStateHandle.get<String>("walletId")

    // Biến để lưu lại giao dịch gốc khi sửa
    private var originalTransaction: Transaction? = null

//    init {
//        // --- SỬA LẠI HOÀN TOÀN LOGIC INIT ĐỂ KHÔNG BỊ GHI ĐÈ STATE ---
//
//        // 1. Lắng nghe sự thay đổi của loại giao dịch (type) để lấy category tương ứng
//        viewModelScope.launch {
//            _uiState.map { it.type }
//                .distinctUntilChanged() // Chỉ chạy khi type thực sự thay đổi (Expense -> Income)
//                .flatMapLatest { type -> // Khi type thay đổi, hủy coroutine cũ và chạy cái mới
//                    categoryRepository.getCategories(type)
//                }
//                .catch { e ->
//                    // Xử lý lỗi nếu không lấy được category
//                    _uiState.update { it.copy(error = "Lỗi tải hạng mục: ${e.message}") }
//                }
//                .collect { categories ->
//                    // Cập nhật danh sách category và tự động chọn mục đầu tiên
//                    _uiState.update {
//                        it.copy(categories = categories, selectedCategory = categories.firstOrNull())
//                    }
//                }
//        }
//
//        // 2. Lấy danh sách ví (chỉ một lần)
//        viewModelScope.launch {
//            walletRepository.getWallets()
//                .catch { e ->
//                    _uiState.update { it.copy(error = "Lỗi tải danh sách ví: ${e.message}") }
//                }
//                .collect { wallets ->
//                    // Tự động chọn ví nếu có ID truyền vào, hoặc chọn ví đầu tiên
//                    val preSelected = wallets.find { it.id == initialWalletId } ?: wallets.firstOrNull()
//                    _uiState.update {
//                        it.copy(wallets = wallets, selectedWallet = preSelected)
//                    }
//                }
//        }
//
//        // 3. Gọi hàm khởi tạo Category mặc định (chỉ chạy khi cần)
//        viewModelScope.launch {
//            categoryRepository.initDefaultCategories()
//        }
//    }

    init {
        // Cập nhật trạng thái isEditMode
        _uiState.update { it.copy(isEditMode = transactionId != null) }
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val userId = authRepository.currentUserFlow.first()?.uid ?: return@launch

            // Nếu là chế độ sửa, tải dữ liệu giao dịch cũ trước tiên
            if (_uiState.value.isEditMode) {
                transactionRepository.getTransactionById(transactionId!!).firstOrNull()?.let { tx ->
                    originalTransaction = tx
                    // Cập nhật các trường nhập liệu với dữ liệu cũ
                    _uiState.update {
                        it.copy(
                            amount = tx.amount.toLong().toString(),
                            note = tx.note ?: "",
                            date = tx.date ?: Date(),
                            type = tx.type
                        )
                    }
                }
            }
            // Kết hợp luồng lấy ví và hạng mục
            combine(
                walletRepository.getWallets(),
                // Lắng nghe type để lấy đúng category
                _uiState.map { it.type }.distinctUntilChanged().flatMapLatest { type ->
                    categoryRepository.getCategories(userId, type)
                }
            ) { wallets, categories ->
                // Tự động chọn giá trị
                val state = _uiState.value
                val preSelectedWallet = if (state.isEditMode) {
                    wallets.find { it.id == originalTransaction?.walletId }
                } else {
                    wallets.find { it.id == initialWalletId } ?: wallets.firstOrNull()
                }

                val preSelectedCategory = if (state.isEditMode) {
                    categories.find { it.id == originalTransaction?.categoryId }
                } else {
                    categories.firstOrNull()
                }

                // Cập nhật state với dữ liệu mới
                state.copy(
                    wallets = wallets,
                    categories = categories,
                    selectedWallet = preSelectedWallet ?: state.selectedWallet,
                    selectedCategory = preSelectedCategory ?: state.selectedCategory
                )
            }.catch { e ->
                _uiState.update { it.copy(error = "Lỗi tải dữ liệu: ${e.message}") }
            }.collect { newState ->
                _uiState.value = newState
            }
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
    fun saveTransaction(onSuccess: () -> Unit) {
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
                id = originalTransaction?.id ?: "", // Giữ ID cũ nếu là sửa
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
//                // 2. CẬP NHẬT SỐ DƯ VÍ
//                val currentBalance = state.selectedWallet!!.balance
//                val amountDelta = if (state.type == "Expense") -amountVal else amountVal
//                val updatedWallet = state.selectedWallet!!.copy(balance = currentBalance + amountDelta)
//                val walletUpdateResult = walletRepository.updateWallet(updatedWallet)
//
//                if (walletUpdateResult is Result.Success) {
//                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
//                } else {
//                    _uiState.update { it.copy(isLoading = false, error = "Lưu giao dịch thành công nhưng lỗi cập nhật số dư.") }
//                }
                // 2. CẬP NHẬT LẠI SỐ DƯ VÍ
                if (result is Result.Success) {
                    if (state.isEditMode && originalTransaction != null) {
                        val oldAmountChange = if (originalTransaction!!.type == "Expense") originalTransaction!!.amount else -originalTransaction!!.amount
                        walletRepository.updateWalletBalance(originalTransaction!!.walletId, oldAmountChange)

                        val newAmountChange = if (newTransaction.type == "Expense") -newTransaction.amount else newTransaction.amount
                        walletRepository.updateWalletBalance(newTransaction.walletId, newAmountChange)
                    } else {
                        val amountChange = if (newTransaction.type == "Expense") -newTransaction.amount else newTransaction.amount
                        walletRepository.updateWalletBalance(newTransaction.walletId, amountChange)
                    }

                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                } else {
                    val errorMsg =
                        (result as? Result.Error)?.exception?.message ?: "Lỗi không xác định"
                    _uiState.update { it.copy(isLoading = false, error = "Lỗi lưu: $errorMsg") }
                }
            }
        }
    }

    fun resetState() {
        // Chỉ reset các cờ trạng thái, không reset dữ liệu đã chọn
        _uiState.update { it.copy(isSuccess = false, error = null) }
    }
}
