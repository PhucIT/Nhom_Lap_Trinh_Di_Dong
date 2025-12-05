
package com.example.expensemanagement.data.repository.Security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.first

// Khởi tạo DataStore dùng chung
//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecurityRepository {

    private object Keys {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val USER_PIN = stringPreferencesKey("user_pin_encrypted") // Tên key cho mã PIN
    }

    // Hàm tiện ích để xử lý lỗi đọc DataStore
    private val dataStoreFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    override val isAppLockEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.APP_LOCK_ENABLED] ?: false }

    override suspend fun setAppLockEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[Keys.APP_LOCK_ENABLED] = isEnabled }
    }

    override val isBiometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.BIOMETRIC_ENABLED] ?: false }

    override suspend fun setBiometricEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[Keys.BIOMETRIC_ENABLED] = isEnabled }
    }

    override val savedPin: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[Keys.USER_PIN] }

    override suspend fun savePin(pin: String) {
        // TODO: Ở đây bạn NÊN mã hóa mã PIN trước khi lưu.
        // Tạm thời, chúng ta lưu trực tiếp để demo.
        val encryptedPin = pin // Thay bằng logic mã hóa thật
        context.dataStore.edit { preferences -> preferences[Keys.USER_PIN] = encryptedPin }
    }

    override fun hasPinSetup(): Flow<Boolean> = context.dataStore.data
        .map { preferences -> !preferences[Keys.USER_PIN].isNullOrBlank() }

    override suspend fun verifyPin(pin: String): Boolean {
        // Đọc mã PIN đã lưu từ DataStore
        val savedPin = savedPin.first()
        // So sánh với mã PIN người dùng nhập
        return pin == savedPin
    }
}