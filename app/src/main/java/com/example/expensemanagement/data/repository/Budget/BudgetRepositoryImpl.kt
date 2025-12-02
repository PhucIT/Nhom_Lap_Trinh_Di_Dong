package com.example.expensemanagement.data.repository.Budget

import android.util.Log
import com.example.expensemanagement.data.Result
import com.example.expensemanagement.data.model.Budget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BudgetRepository {

    private val userBudgetsCollection
        get() = auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId).collection("budgets")
        }

    override fun getBudgets(): Flow<List<Budget>> {
        val collection = userBudgetsCollection ?: return flowOf(emptyList())

        return collection.snapshots().map { snapshot ->
            snapshot.toObjects<Budget>()
        }
    }

    override suspend fun setBudget(budget: Budget): Result<Unit> {
        val collection = userBudgetsCollection
            ?: return Result.Error(IllegalStateException("User not logged in"))
        return try {
            // NẾU LÀ SỬA (đã có ID từ trước)
            if (budget.id.isNotBlank()) {
                Log.d("BudgetRepo", "Đang cập nhật budget ID: ${budget.id}")
                collection.document(budget.id).set(budget).await()
            }
            // NẾU LÀ TẠO MỚI (chưa có ID)
            else {
                // Kiểm tra xem đã có ngân sách cho loại ví này chưa
                val existingDoc = collection
                    .whereEqualTo("walletType", budget.walletType)
                    .limit(1)
                    .get()
                    .await()

                if (existingDoc.isEmpty) {
                    // Nếu chưa có, tạo mới với ID tự động
                    Log.d("BudgetRepo", "Tạo ngân sách mới cho loại: ${budget.walletType}")
                    val newDocRef = collection.document()
                    collection.document(newDocRef.id).set(budget.copy(id = newDocRef.id)).await()
                } else {
                    // Nếu đã có, cập nhật (ghi đè) vào document đó
                    val docId = existingDoc.documents.first().id
                    Log.d("BudgetRepo", "Đã có ngân sách, cập nhật vào ID: $docId")
                    collection.document(docId).set(budget.copy(id = docId)).await()
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Lỗi khi setBudget", e)
            Result.Error(e)
        }
    }
}