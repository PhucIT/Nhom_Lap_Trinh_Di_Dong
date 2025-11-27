// File: viewmodel/HomeViewModel.kt
package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.model.User
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.data.repository.Auth.AuthRepository
import com.example.expensemanagement.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// --- NÂNG CẤP UI STATE ĐỂ CHỨA THÊM THÔNG TIN USER ---
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val walletGroups: Map<String, List<Wallet>>,
        val currentUser: User? = null // Thêm thông tin người dùng ở đây
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository // <-- THÊM AUTH REPOSITORY
) : ViewModel() {

    // Lắng nghe cả Wallet và User, sau đó kết hợp chúng lại
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> =
        walletRepository.getWallets()
            .flatMapLatest { walletList ->
                // Sau khi có danh sách ví, kết hợp nó với thông tin người dùng
                authRepository.currentUserFlow.map { firebaseUser ->
                    if (firebaseUser != null) {
                        val user = User(
                            uid = firebaseUser.uid,
                            displayName = firebaseUser.displayName,
                            email = firebaseUser.email,
                            photoUrl = firebaseUser.photoUrl?.toString()
                        )
                        val groupedWallets = if (walletList.isEmpty()) emptyMap() else walletList.groupBy { it.type }
                        HomeUiState.Success(groupedWallets, user)
                    } else {
                        // Trường hợp người dùng không tồn tại (lỗi hoặc đã đăng xuất)
                        HomeUiState.Error("Không tìm thấy người dùng.")
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState.Loading
            )

    // Hàm để gọi khi người dùng nhấn Đăng xuất
    fun logout() {
        authRepository.logout()
    }
}
