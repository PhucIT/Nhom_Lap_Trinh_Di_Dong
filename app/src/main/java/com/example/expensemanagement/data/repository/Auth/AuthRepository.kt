package com.example.expensemanagement.data.repository.Auth

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow
import com.example.expensemanagement.data.Result


interface AuthRepository {

    // Luồng để lắng nghe thông tin người dùng hiện tại
    val currentUserFlow: StateFlow<FirebaseUser?>

    suspend fun login(email: String, pass: String): Result<Unit>
    suspend fun register(name: String, email: String, pass: String): Result<FirebaseUser?>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?>
    suspend fun isFirstTimeSignInWithProvider(providerId: String): Boolean

    suspend fun updateUserProfile(newName: String, newEmail: String?, photoUri: Uri?): Result<Unit>
    suspend fun changePassword(currentPass: String, newPass: String): Result<Unit>
    // Hàm đăng xuất
    fun logout()

// ... ( có thể sẽ cần thêm các hàm khác như login, regist... say này)
}