package com.example.expensemanagement.data.repository.Security

import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    // Trạng thái bật/tắt khóa ứng dụng
    val isAppLockEnabled: Flow<Boolean>
    suspend fun setAppLockEnabled(isEnabled: Boolean)

    // Trạng thái bật/tắt sinh trắc học
    val isBiometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(isEnabled: Boolean)

    // Lấy mã PIN đã lưu (đã được mã hóa)
    val savedPin: Flow<String?>
    suspend fun savePin(pin: String)

    // Kiểm tra xem đã có mã PIN được thiết lập hay chưa
    fun hasPinSetup(): Flow<Boolean>

    /**
    * Xác thực mã PIN người dùng nhập vào.
    * @param pin Mã PIN người dùng vừa nhập.
    * @return `true` nếu mã PIN đúng, `false` nếu sai.
    */
    suspend fun verifyPin(pin: String): Boolean
}