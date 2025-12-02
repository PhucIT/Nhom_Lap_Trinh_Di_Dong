package com.example.expensemanagement.data.repository.Auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.userProfileChangeRequest
import com.example.expensemanagement.data.Result
import com.google.firebase.auth.EmailAuthProvider


@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val _currentUserFlow = MutableStateFlow(
        auth.currentUser)
    override val currentUserFlow: StateFlow<FirebaseUser?> = _currentUserFlow.asStateFlow()

    init {
        // Lắng nghe sự thay đổi trạng thái đăng nhập của Firebase
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserFlow.value = firebaseAuth.currentUser
        }
    }

    override suspend fun login(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun register(name: String, email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) {
                val profileUpdates = userProfileChangeRequest { displayName = name }
                user.updateProfile(profileUpdates).await()
            }
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            Result.Success(authResult.user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun isFirstTimeSignInWithProvider(providerId: String): Boolean {
        return try {
            val signInMethods = auth.currentUser?.email?.let {
                auth.fetchSignInMethodsForEmail(it).await().signInMethods
            }
            signInMethods?.size == 1 && signInMethods.contains(providerId)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateUserProfile(newName: String, newEmail: String?): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error(Exception("User not logged in"))
        return try {
            val profileUpdates = userProfileChangeRequest { displayName = newName }
            user.updateProfile(profileUpdates).await()
            if (newEmail != null&& newEmail != user.email) {
                user.updateEmail(newEmail).await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun changePassword(currentPass: String, newPass: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error(Exception("User not logged in"))
        return try {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
            user.reauthenticate(credential).await()
            user.updatePassword(newPass).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }

    // ... (Triển khai các hàm login, register khác ở đây nếu cần)
}
