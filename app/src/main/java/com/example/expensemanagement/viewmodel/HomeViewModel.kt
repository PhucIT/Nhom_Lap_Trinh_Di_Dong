
package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.model.User
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.data.repository.Auth.AuthRepository
import com.example.expensemanagement.data.repository.Category.CategoryRepository
import com.example.expensemanagement.data.repository.WalletRepository
import com.example.expensemanagement.data.model.WalletGroupInfo
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import java.util.Calendar


sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
//        val walletGroups: Map<String, List<Wallet>>,
        val walletGroups: List<WalletGroupInfo>,
        val currentUser: User? = null // Thêm thông tin người dùng
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository, // <-- THÊM AUTH REPOSITORY
    private val categoryRepository: CategoryRepository, //
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    init {
        // GỌI HÀM INIT KHI VIEWMODEL ĐƯỢC TẠO
        // Tại thời điểm này, user chắc chắn đã đăng nhập thành công
        viewModelScope.launch {
            categoryRepository.initDefaultCategories()
        }
    }

    // Lắng nghe cả Wallet và User, sau đó kết hợp chúng lại
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val uiState: StateFlow<HomeUiState> =
//        walletRepository.getWallets()
//            .flatMapLatest { walletList ->
//                // Sau khi có danh sách ví, kết hợp nó với thông tin người dùng
//                authRepository.currentUserFlow.map { firebaseUser ->
//                    if (firebaseUser != null) {
//                        val user = User(
//                            uid = firebaseUser.uid,
//                            displayName = firebaseUser.displayName,
//                            email = firebaseUser.email,
//                            photoUrl = firebaseUser.photoUrl?.toString()
//                        )
//                        val groupedWallets = if (walletList.isEmpty()) emptyMap() else walletList.groupBy { it.type }
//                        HomeUiState.Success(groupedWallets, user)
//                    } else {
//                        // Trường hợp người dùng không tồn tại (lỗi hoặc đã đăng xuất)
//                        HomeUiState.Error("Không tìm thấy người dùng.")
//                    }
//                }
//            }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> =
        // KẾT HỢP 3 LUỒNG DỮ LIỆU
        combine(
            authRepository.currentUserFlow,
            walletRepository.getWallets(),
            transactionRepository.getTransactions()
        ) { firebaseUser, walletList, transactionList ->
            if (firebaseUser == null) {
                return@combine HomeUiState.Error("Người dùng đã đăng xuất.")
            }

            val user = User(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName,
                email = firebaseUser.email,
                photoUrl = firebaseUser.photoUrl?.toString()
            )

            // --- LOGIC TÍNH TOÁN MỚI ĐƯỢC CHUYỂN VỀ ĐÂY ---
            if (walletList.isEmpty()) {
                return@combine HomeUiState.Success(emptyList(), user)
            }

            val groupedWallets = walletList.groupBy { it.type }
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val walletGroupInfos = groupedWallets.map { (type, walletsInGroup) ->
                val groupWalletIds = walletsInGroup.map { it.id }

                val totalSpentThisMonth = transactionList
                val totalSpent = transactionList
                    .filter { tx ->
                        tx.walletId in groupWalletIds && tx.type == "Expense"
                    }
                    .sumOf { it.amount }

                WalletGroupInfo(
                    groupName = when (type) {
                        "Normal" -> "Ví thường"
                        "Saving" -> "Ví tiết kiệm"
                        "Credit" -> "Ví tín dụng"
                        else -> type
                    },
                    wallets = walletsInGroup,
                    totalBalance = walletsInGroup.sumOf { it.balance },
                    totalSpentThisMonth = totalSpent
                )
            }
            HomeUiState.Success(walletGroupInfos.sortedBy { val typeOrder = listOf("Ví thường", "Ví tiết kiệm", "Ví tín dụng"); typeOrder.indexOf(it.groupName) }, user)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState.Loading
            )

    // Hàm để gọi khi người dùng nhấn Đăng xuất
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
