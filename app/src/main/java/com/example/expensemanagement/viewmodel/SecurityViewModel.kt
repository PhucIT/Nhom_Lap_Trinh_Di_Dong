package com.example.expensemanagement.viewmodel

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK // Thêm cái này cho chắc
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.repository.Security.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityUiState(
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val hasPinSetup: Boolean = false,
    val canUseBiometrics: Boolean = false
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // StateFlow kết hợp từ các nguồn dữ liệu
    val uiState: StateFlow<SecurityUiState> = combine(
        securityRepository.isAppLockEnabled, // // Flow: khóa bật/tắt
        securityRepository.isBiometricEnabled, // Flow: vân tay bật/tắt
        securityRepository.hasPinSetup() // Flow: có PIN chưa
    ) { appLock, biometric, hasPin ->
        SecurityUiState(
            isAppLockEnabled = appLock,
            isBiometricEnabled = biometric,
            hasPinSetup = hasPin,
            // Kiểm tra biometric mỗi khi state thay đổi (hoặc lấy giá trị tĩnh)
            canUseBiometrics = checkBiometricSupport() // ← Kiểm tra máy có vân tay không
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SecurityUiState()
    )

    // Bật/tắt Khóa ứng dụng
    fun onAppLockToggle(isEnabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setAppLockEnabled(isEnabled)
            if (!isEnabled) {
                securityRepository.setBiometricEnabled(false) // ← TẮT LUÔN VÂN TAY
            }
        }
    }

    // Bật/tắt Sinh trắc học
    fun onBiometricToggle(isEnabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setBiometricEnabled(isEnabled)
        }
    }

    // kiểm tra máy có vân tay kh
    private fun checkBiometricSupport(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            // Cho phép cả STRONG và WEAK (ví dụ Face Unlock trên một số máy Android)
            val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK

            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> true
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}