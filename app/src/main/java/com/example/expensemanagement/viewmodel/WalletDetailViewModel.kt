//
//package com.example.expensemanagement.viewmodel
//
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.expensemanagement.data.model.Transaction
//import com.example.expensemanagement.data.model.Wallet
//import com.example.expensemanagement.data.repository.transaction.TransactionRepository
//import com.example.expensemanagement.data.repository.WalletRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//// Trạng thái UI cho Màn 2 (Chi Tiết Ví)
//// (Nó chứa TOÀN BỘ dữ liệu mà Màn 2 cần)
//data class WalletDetailUiState(
//    val isLoading: Boolean = true,
//
//    // Dữ liệu Khối 1 (Số Dư)
//    val totalBalance: Double = 0.0,
//
//    // Dữ liệu Khối 2 (Ngân Sách)
//    val spentAmount: Double = 0.0, // "Đã chi" (THẬT)
//    val remainingAmount: Double = 0.0, // "Còn lại" (THẬT)
//    val budgetProgress: Float = 0f, // Thanh tiến trình
//
//    // Dữ liệu Khối 3 (Giao Dịch)
//    val transactions: List<Transaction> = emptyList(),
//
//    val walletType: String = "", // Tên nhóm (để hiển thị tiêu đề)
//    val errorMessage: String? = null
//)
//
//@HiltViewModel
//class WalletDetailViewModel @Inject constructor(
//    private val walletRepository: WalletRepository, // Kho Ví (của bạn)
//    private val transactionRepository: TransactionRepository, // Kho Giao dịch (của bạn)
//    // TODO: Thêm BudgetRepository
//    savedStateHandle: SavedStateHandle // Dùng để lấy ID từ navigation
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(WalletDetailUiState())
//    val uiState: StateFlow<WalletDetailUiState> = _uiState.asStateFlow()
//
//    // Lấy "groupId" (ví dụ: "Normal", "Saving") mà Màn 1 (Home) đã gửi
//    private val groupId: String = savedStateHandle.get<String>("groupId")!!
//
//    init {
//        fetchWalletDetails()
//    }
//
//    private fun fetchWalletDetails() {
//        viewModelScope.launch {
//            // Lấy 2 nguồn: (1) TẤT CẢ Ví và (2) TẤT CẢ Giao dịch
//            // (Chúng ta dùng Repository "xịn" của bạn)
//            val walletsFlow = walletRepository.getWallets()
//            val transactionsFlow = transactionRepository.getTransactions()
//
//            // TODO: Chúng ta sẽ combine 3 nguồn (thêm BudgetFlow) ở đây
//
//
//            // Kết hợp (combine) 2 flow này lại
//            walletsFlow.combine(flow = transactionsFlow) { walletList, transactionList ->
//
//                // --- BẮT ĐẦU LỌC (Filter) ---
//
//                // 1. Lọc ra các ví CHỈ thuộc nhóm này (Ví dụ: "Normal")
//                val groupWallets = walletList.filter {
//                    val walletType = when(it.type) {
//                        "Normal" -> "Ví thường"
//                        "Saving" -> "Ví tiết kiệm"
//                        "Credit" -> "Ví tín dụng"
//                        else -> it.type
//                    }
//                    walletType == groupId
//                }
//
//                // 2. Lấy ID của các ví đó
//                val groupWalletIds = groupWallets.map { it.id }
//
//                // 3. Lọc ra các giao dịch CHỈ thuộc các ví đó
//                val groupTransactions = transactionList.filter {
//                    it.walletId in groupWalletIds
//                }
//
//                // --- BẮT ĐẦU TÍNH TOÁN (Y HỆT FIGMA) ---
//
//                // 1. Tính "Số Dư" (Khối 1)
//                val totalBalance = groupWallets.sumOf { it.balance }
//
//                // 2. Tính "Đã chi" (THẬT) (Khối 2)
//                // (Đây là "Đã chi" của TOÀN BỘ giao dịch chi tiêu)
//                val totalSpent = groupTransactions
//                    .filter { it.type == "Expense" }
//                    .sumOf { it.amount }
//
//                // 3. Tính "Ngân Sách" (Khối 2)
//                // (CHƯA CÓ BudgetRepository, TÔI SẼ GIẢ LẬP (MOCK) ĐỂ HIỂN THỊ)
//                val mockTotalBudget = when(groupId) {
//                    "Ví thường" -> 20000000.0
//                    "Ví tiết kiệm" -> 50000000.0
//                    "Ví tín dụng" -> 40000000.0
//                    else -> 1000000.0
//                }
//
//                val remainingAmount = mockTotalBudget - totalSpent // "Còn lại" (THẬT)
//
//                // Tính thanh Progress
//                val progress = if (mockTotalBudget > 0) {
//                    (totalSpent / mockTotalBudget).toFloat()
//                } else {
//                    0f
//                }
//
//                // Cập nhật UI
//                WalletDetailUiState(
//                    isLoading = false,
//                    totalBalance = totalBalance, // Khối 1
//                    spentAmount = totalSpent, // Khối 2
//                    remainingAmount = remainingAmount, // Khối 2
//                    budgetProgress = progress, // Khối 2
//                    transactions = groupTransactions, // Khối 3
//                    walletType = groupId // Tên tiêu đề
//                )
//
//            }
//                .catch { e ->
//                    _uiState.value = WalletDetailUiState(isLoading = false, errorMessage = e.message)
//                }
//                .collect { newState ->
//                    _uiState.value = newState
//                }
//        }
//    }
//}

package com.example.expensemanagement.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.model.Budget
import com.example.expensemanagement.data.model.Transaction
import com.example.expensemanagement.data.model.User
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.data.repository.Auth.AuthRepository
import com.example.expensemanagement.data.repository.Budget.BudgetRepository
import com.example.expensemanagement.data.repository.WalletRepository
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- NÂNG CẤP UI STATE ĐỂ CHỨA THÊM USER ---
data class WalletDetailUiState(
    val isLoading: Boolean = true,
    val totalBalance: Double = 0.0,
    val spentAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val budgetProgress: Float = 0f,
    val transactions: List<Transaction> = emptyList(),
    val walletType: String = "",
    val errorMessage: String? = null,
    val currentUser: User? = null // <-- THÊM THÔNG TIN USER
)

@HiltViewModel
class WalletDetailViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val authRepository: AuthRepository, // <-- THÊM AUTH REPOSITORY
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletDetailUiState())
    val uiState: StateFlow<WalletDetailUiState> = _uiState.asStateFlow()

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    init {
        if (groupId.isNotEmpty()) {
            fetchGroupDetails()
        } else {
            _uiState.value = WalletDetailUiState(isLoading = false, errorMessage = "Lỗi: Không tìm thấy ID nhóm ví")
        }
    }

    private fun fetchGroupDetails() {
        viewModelScope.launch {
            // Lấy 4 nguồn dữ liệu
            val walletsFlow = walletRepository.getWallets().catch { emit(emptyList()) }
            val transactionsFlow = transactionRepository.getTransactions().catch { emit(emptyList()) }
            val budgetsFlow = budgetRepository.getBudgets().catch { emit(emptyList()) }
            val userFlow = authRepository.currentUserFlow // <-- LUỒNG DỮ LIỆU USER

            // Kết hợp 4 luồng dữ liệu
            combine(walletsFlow, transactionsFlow, budgetsFlow, userFlow) { walletList, transactionList, budgetList, firebaseUser ->

                // --- LOGIC TÍNH TOÁN ---
                val groupWallets = walletList.filter { wallet ->
                    mapTypeToGroupId(wallet.type) == groupId
                }
                val groupWalletIds = groupWallets.map { it.id }
                val groupTransactions = transactionList.filter { it.walletId in groupWalletIds }

                val totalBalance = groupWallets.sumOf { it.balance }
                val totalSpent = groupTransactions
                    .filter { it.type == "Expense" }
                    .sumOf { it.amount }

                val walletTypeForBudget = mapGroupIdToType(groupId)
                val budget = budgetList.firstOrNull { it.walletType == walletTypeForBudget }
                val budgetLimit = budget?.limitAmount ?: 0.0

                val remaining = budgetLimit - totalSpent
                val progress = if (budgetLimit > 0) (totalSpent / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f

                // Tạo đối tượng User từ FirebaseUser
                val currentUser = firebaseUser?.let {
                    User(
                        uid = it.uid,
                        displayName = it.displayName,
                        email = it.email,
                        photoUrl = it.photoUrl?.toString()
                    )
                }

                // Trả về trạng thái UI hoàn chỉnh
                WalletDetailUiState(
                    isLoading = false,
                    totalBalance = totalBalance,
                    spentAmount = totalSpent,
                    remainingAmount = remaining,
                    budgetProgress = progress,
                    transactions = groupTransactions,
                    walletType = groupId,
                    currentUser = currentUser // <-- TRUYỀN USER VÀO STATE
                )
            }.catch { e ->
                _uiState.value = WalletDetailUiState(isLoading = false, errorMessage = e.message)
                e.printStackTrace()
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    private fun mapGroupIdToType(groupId: String): String {
        return when(groupId) {
            "Ví thường" -> "Normal"
            "Ví tiết kiệm" -> "Saving"
            "Ví tín dụng" -> "Credit"
            else -> groupId
        }
    }

    private fun mapTypeToGroupId(type: String): String {
        return when(type) {
            "Normal" -> "Ví thường"
            "Saving" -> "Ví tiết kiệm"
            "Credit" -> "Ví tín dụng"
            else -> type
        }
    }
}