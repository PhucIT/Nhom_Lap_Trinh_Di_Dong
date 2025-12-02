package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.model.User
//import com.google.firebase.firestore.auth.User
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.data.model.Transaction
import com.example.expensemanagement.data.repository.Auth.AuthRepository
import com.example.expensemanagement.data.repository.Category.CategoryRepository
import com.example.expensemanagement.data.repository.WalletRepository
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// Model để chứa thông tin của một nhóm ví trên Dashboard
data class DashboardWalletInfo(
    val groupName: String,
    val totalBalance: Double,
    val totalSpentThisMonth: Double
)

// State cho toàn bộ màn hình Dashboard
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalBalance: Double = 0.0,
    val hasWallets: Boolean = false,
    val dashboardWallets: List<DashboardWalletInfo> = emptyList(),
    val currentUser: User? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Khởi tạo hạng mục mặc định khi cần
        viewModelScope.launch {
            authRepository.currentUserFlow.first()?.uid?.let {
                categoryRepository.initDefaultCategories()
            }
        }
        observeDashboardData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDashboardData() {
        // Kết hợp 3 luồng dữ liệu chính
        combine(
            authRepository.currentUserFlow,
            walletRepository.getWallets(),
            transactionRepository.getTransactions()
        ) { firebaseUser, walletList, transactionList ->

            if (firebaseUser == null) {
                return@combine DashboardUiState(isLoading = false, error = "Người dùng chưa đăng nhập.")
            }

            val user = User(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName,
                email = firebaseUser.email,
                photoUrl = firebaseUser.photoUrl?.toString()
            )

            // Kiểm tra xem người dùng có ví nào không
            val hasWallets = walletList.isNotEmpty()

            if (!hasWallets) {
                return@combine DashboardUiState(isLoading = false, hasWallets = false, currentUser = user)
            }

            // 1. Tính tổng số dư của TẤT CẢ các ví
            val totalBalance = walletList.sumOf { it.balance }

            // 2. Tính toán cho từng nhóm ví
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val groupedWallets = walletList.groupBy { it.type }

            val dashboardWalletInfos = groupedWallets.map { (type, walletsInGroup) ->
                val groupWalletIds = walletsInGroup.map { it.id }

                // Tính tổng chi tiêu trong tháng của NHÓM này
                val totalSpentThisMonth = transactionList
                    .filter { tx ->
                        tx.walletId in groupWalletIds &&
                                tx.type == "Expense" &&
                                tx.date?.let {
                                    val txCal = Calendar.getInstance().apply { time = it }
                                    txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
                                } ?: false
                    }
                    .sumOf { it.amount }

                DashboardWalletInfo(
                    groupName = when (type) {
                        "Normal" -> "Ví thường"
                        "Saving" -> "Ví tiết kiệm"
                        "Credit" -> "Ví tín dụng"
                        else -> type
                    },
                    totalBalance = walletsInGroup.sumOf { it.balance },
                    totalSpentThisMonth = totalSpentThisMonth
                )
            }

            // Trả về State hoàn chỉnh
            DashboardUiState(
                isLoading = false,
                hasWallets = true, // <-- SET CỜ LÀ TRUE
                totalBalance = totalBalance,
                dashboardWallets = dashboardWalletInfos.sortedBy { val order = listOf("Ví thường", "Ví tiết kiệm", "Ví tín dụng"); order.indexOf(it.groupName) },
                currentUser = user
            )
        }
            .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            .onEach { newState -> _uiState.update { newState } }
            .launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
    }
}