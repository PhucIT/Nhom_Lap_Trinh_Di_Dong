package com.example.expensemanagement.data.repository.transaction

import android.util.Log
import com.example.expensemanagement.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.toObject
//import com.google.firebase.firestore.ktx.toObjects
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.toObjects
import com.example.expensemanagement.data.Result
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import java.util.Date

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    // Lấy ID của người dùng đang đăng nhập
    // Nếu chưa đăng nhập, sẽ ném ra lỗi để ngăn các thao tác không hợp lệ
    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in.")

    // Thuộc tính để lấy đường dẫn đến collection "transactions" của người dùng hiện tại một cách gọn gàng
    private val userTransactionsCollection
        get() = firestore.collection("users").document(userId).collection("transactions")

    override fun getTransactions(): Flow<List<Transaction>> {
        // Lắng nghe sự thay đổi trên collection và sắp xếp các giao dịch theo ngày (mới nhất lên đầu)
        return userTransactionsCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Transaction>()
            }
    }

    override fun getTransactionById(transactionId: String): Flow<Transaction?> {
        // Lắng nghe sự thay đổi trên một document giao dịch cụ thể
        return userTransactionsCollection.document(transactionId).snapshots().map { snapshot ->
            snapshot.toObject<Transaction>()
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            // Firestore sẽ tự tạo ID cho document mới
            // Chúng ta không cần gán ID thủ công như với Wallet nữa vì Transaction có annotation @DocumentId
            userTransactionsCollection.add(transaction).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace() // In lỗi ra logcat để debug
            Result.Error(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            // Để cập nhật, ID của giao dịch phải tồn tại
            if (transaction.id.isBlank()) {
                throw IllegalArgumentException("Transaction ID cannot be empty for an update.")
            }
            // Tìm đến document có ID tương ứng và ghi đè dữ liệu
            userTransactionsCollection.document(transaction.id).set(transaction).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

//    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
//        return try {
//            // Tìm đến document có ID tương ứng và xóa nó
//            userTransactionsCollection.document(transactionId).delete().await()
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Result.Error(e)
//        }
//    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        val collection = userTransactionsCollection ?: return Result.Error(Exception("User not logged in"))
        return try {
            collection.document(transactionId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    //(CHO MÀN 2: CHI TIẾT VÍ)
    override fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>> {
        // Lấy giao dịch VÀ LỌC (whereEqualTo) theo walletId
        return userTransactionsCollection
            .whereEqualTo("walletId", walletId) // LỌC THEO VÍ
            .orderBy("date", Query.Direction.DESCENDING) // Sắp xếp
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Transaction>()
            }
    }

    // --- hàm mới Report ---
    override fun getTransactionsBetween(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        val collection = userTransactionsCollection ?: return flowOf(emptyList())

        return collection
            // Lọc các giao dịch có ngày nằm trong khoảng [startDate, endDate]
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects<Transaction>() }
            .catch { exception ->
                Log.e("TransactionRepo", "Lỗi khi lấy giao dịch theo khoảng thời gian: ", exception)
                emit(emptyList())
            }
    }
}