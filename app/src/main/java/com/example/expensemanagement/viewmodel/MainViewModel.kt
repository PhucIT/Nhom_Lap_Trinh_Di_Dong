package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.repository.Auth.AuthRepository
import com.example.expensemanagement.data.repository.Security.SecurityRepository
import com.example.expensemanagement.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Sealed class để định nghĩa các màn hình khởi đầu có thể có
sealed class StartDestinationState {
    data object Loading : StartDestinationState() // Trạng thái đang chờ, chưa quyết định
    data class Destination(val route: String) : StartDestinationState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    securityRepository: SecurityRepository
) : ViewModel() {

    // State này sẽ quyết định màn hình bắt đầu của AppNavigation
    val startDestinationState: StateFlow<StartDestinationState> = combine(
        authRepository.currentUserFlow,
        securityRepository.isAppLockEnabled
    ) { user, isAppLockEnabled ->
        // Logic quyết định màn hình bắt đầu
        if (user != null) {
            // Nếu đã đăng nhập
            if (isAppLockEnabled) {
                // và có bật khóa app -> màn hình bắt đầu là màn hình PIN
                StartDestinationState.Destination("pin_lock") // Route đặc biệt cho màn hình khóa
            } else {
                // và không bật khóa app -> màn hình bắt đầu là Dashboard
                StartDestinationState.Destination(AppDestinations.Dashboard.route)
            }
        } else {
            // Nếu chưa đăng nhập -> màn hình bắt đầu là Onboarding
            StartDestinationState.Destination(AppDestinations.Onboarding.route)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StartDestinationState.Loading // Ban đầu ở trạng thái Loading
    )
}