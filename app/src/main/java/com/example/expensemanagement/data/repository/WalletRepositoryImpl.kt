package com.example.expensemanagement.data.repository

import com.example.expensemanagement.data.model.Wallet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.expensemanagement.data.Result

/**
 * Triển khai (Implementation) của WalletRepository.
 * Lấy dữ liệu thật từ Firebase Firestore.
 */

@Singleton // Chỉ tạo 1 thể hiện duy nhất
class WalletRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth, // Hilt tiêm vào
    private val firestore: FirebaseFirestore  // Hilt tiêm vào
) : WalletRepository {
    // Lấy ID người dùng (an toàn)
    private val userWalletsCollection
        get() = auth.currentUser?.uid?.let { userId ->
            // Cấu trúc đơn giản và phổ biến: users -> {userId} -> wallets
            firestore.collection("users").document(userId).collection("wallets")
        }

    override fun getWallets(): Flow<List<Wallet>> {
        // Lấy collection từ thuộc tính đã tạo
        val collection = userWalletsCollection
        if (collection == null) {
            // Nếu người dùng chưa đăng nhập, trả về một Flow chứa danh sách rỗng
            return flowOf(emptyList())
        }

        // Dùng .snapshots() để lắng nghe thay đổi theo thời gian thực
        // và .map để chuyển đổi nó thành List<Wallet>
        return collection.snapshots().map { snapshot ->
            // Sửa: Dùng toObjects<Wallet>() cho cú pháp Kotlin KTX
            snapshot.toObjects(Wallet::class.java)
        }
    }

    override fun getWalletById(walletId: String): Flow<Wallet?> {
        // Lấy collection từ thuộc tính đã tạo
        val collection = userWalletsCollection
        if (collection == null) {
            // Nếu người dùng chưa đăng nhập, trả về Flow chứa giá trị null
            return flowOf(null)
        }

        // Lắng nghe sự thay đổi trên document ví cụ thể
        return collection.document(walletId).snapshots().map { snapshot ->
            snapshot.toObject<Wallet>() // Chuyển đổi document thành một đối tượng Wallet
        }
    }

    /**
     * Thêm một ví mới vào Firestore.
     */
    override suspend fun addWallet(wallet: Wallet): Result<Unit> {
        // Lấy collection, nếu là null (chưa đăng nhập) thì báo lỗi ngay
        val collection = userWalletsCollection ?: return Result.Error(IllegalStateException("User not logged in."))
        return try {
            // Tự tạo một document mới để lấy ID
            val documentRef = collection.document()
            // Gán ID vừa tạo vào đối tượng wallet trước khi lưu
            val walletWithId = wallet.copy(id = documentRef.id)
            // Lưu ví vào Firestore
            documentRef.set(walletWithId).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Cập nhật thông tin một ví đã có.
     */
    override suspend fun updateWallet(wallet: Wallet): Result<Unit> {
        val collection = userWalletsCollection ?: return Result.Error(IllegalStateException("User not logged in."))
        return try {
            // ID của ví không được để trống khi cập nhật
            if (wallet.id.isBlank()) {
                throw IllegalArgumentException("Wallet ID cannot be empty for an update.")
            }
            // Tìm đến document có ID tương ứng và ghi đè dữ liệu
            collection.document(wallet.id).set(wallet).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Xóa một ví.
     */
    override suspend fun deleteWallet(walletId: String): Result<Unit> {
        val collection = userWalletsCollection ?: return Result.Error(IllegalStateException("User not logged in."))
        return try {
            // Tìm đến document có ID tương ứng và xóa nó
            collection.document(walletId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // hàm cập nhật lại số dư khi xóa sửa ví
    override suspend fun updateWalletBalance(walletId: String, amountChange: Double): Result<Unit> {
        val collection = userWalletsCollection ?: return Result.Error(Exception("User not logged in"))
        return try {
            firestore.runTransaction { transaction ->
                val docRef = collection.document(walletId)
                val snapshot = transaction.get(docRef)
                val currentBalance = snapshot.getDouble("balance") ?: 0.0
                val newBalance = currentBalance + amountChange
                transaction.update(docRef, "balance", newBalance)
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}