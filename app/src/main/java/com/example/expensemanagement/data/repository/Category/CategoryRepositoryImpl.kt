package com.example.expensemanagement.data.repository.Category

import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import com.example.expensemanagement.data.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flowOf
import com.google.firebase.firestore.WriteBatch

//@Singleton
//class CategoryRepositoryImpl @Inject constructor(
//    private val auth: FirebaseAuth,
//    private val firestore: FirebaseFirestore
//) : CategoryRepository {
//
//    private val userId: String?
//        get() = auth.currentUser?.uid
//
//    // Giả sử bạn lưu categories chung cho tất cả user hoặc theo user
//    // Cách 1: Chung cho mọi user
//    private val categoriesCollection
//        get() = firestore.collection("categories")
//
//    override fun getExpenseCategories(): Flow<List<Category>> {
//        // Chỉ lấy các hạng mục có type là "Expense"
//        return categoriesCollection
//            .whereEqualTo("type", "Expense")
//            .snapshots()
//            .map { snapshot ->
//                snapshot.toObjects<Category>()
//            }
//    }
//}
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val userCategoriesCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("categories")
        }

//    override fun getExpenseCategories(): Flow<List<Category>> {
//        val collection = userCategoriesCollection
//            ?: return emptyFlow<List<Category>>().also {
//                Log.e("CategoryRepo", "User chưa đăng nhập!")
//            }
//
//        return collection
//            .whereEqualTo("kind", "Expense")
//            .snapshots()
//            .map { snapshot ->
//                val list = snapshot.toObjects<Category>()
//                Log.d("CategoryRepo", "Lấy được ${list.size} hạng mục chi tiêu: $list")
//                list
//            }
//            .catch { e ->
//                Log.e("CategoryRepo", "Lỗi lấy hạng mục chi tiêu", e)
//                emit(emptyList())
//            }
//    }

    override fun getExpenseCategories(userId: String): Flow<List<Category>> {
        val collection = firestore.collection("users").document(userId).collection("categories")
        return collection
            .whereEqualTo("kind", "Expense")
            .snapshots()
            .map { it.toObjects<Category>() }
            .catch { e -> emit(emptyList()) }
    }

//    override fun getCategories(type: String): Flow<List<Category>> {
//        val collection = userCategoriesCollection ?: return emptyFlow()
//        return collection
//            .whereEqualTo("kind", type)
//            .snapshots()
//            .map { snapshot -> snapshot.toObjects<Category>() }
//            .catch { e ->
//                Log.e("CategoryRepo", "Lỗi lấy category loại $type", e)
//                emit(emptyList())
//            }
//    }

    override fun getCategories(userId: String, type: String): Flow<List<Category>> {
        val collection = firestore.collection("users").document(userId).collection("categories")
        return collection
            .whereEqualTo("kind", type)
            .snapshots()
            .map { it.toObjects<Category>() }
            .catch { e -> emit(emptyList()) }
    }

    override suspend fun initDefaultCategories() {
        val collection = userCategoriesCollection
            ?: throw IllegalStateException("User chưa đăng nhập khi init categories")

        try {
            val snapshot = collection.limit(1).get().await()
            if (snapshot.isEmpty) {
                Log.d("CategoryRepo", "Chưa có hạng mục → tạo mặc định cho user hiện tại")

                val defaults = listOf(
                    Category(id = "", name = "Ăn uống",       type = "Expense", icon = "ic_food"),
                    Category(id = "", name = "Đi lại",        type = "Expense", icon = "ic_transport"),
                    Category(id = "", name = "Mua sắm",       type = "Expense", icon = "ic_shopping"),
                    Category(id = "", name = "Giải trí",      type = "Expense", icon = "ic_entertainment"),
                    Category(id = "", name = "Hóa đơn",       type = "Expense", icon = "ic_bill"),
                    Category(id = "", name = "Sức khỏe",     type = "Expense", icon = "ic_health"),
                    Category(id = "", name = "Lương",         type = "Income",  icon = "ic_salary"),
                    Category(id = "", name = "Thưởng",        type = "Income",  icon = "ic_bonus")
                )

                // ĐÚNG CÚ PHÁP VIẾT BATCH TRONG KOTLIN COROUTINES
                val batch: WriteBatch = firestore.batch()
                defaults.forEach { category ->
                    val docRef = collection.document()
                    batch.set(docRef, category.copy(id = docRef.id))
                }
                batch.commit().await()
                Log.d("CategoryRepo", "Tạo mặc định thành công!")
            }
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Lỗi init categories", e)
        }
    }
}