package com.example.expensemanagement.ui.screens

/*
package com.example.expensemanagement.data.repository.Category

import com.example.expensemanagement.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getExpenseCategories(): Flow<List<Category>> // Chỉ lấy các hạng mục CHI TIÊU

    /**
     * HÀM MỚI - Dành cho AddTransactionViewModel.
     * Lấy danh sách các hạng mục dựa trên loại (type) được truyền vào ("Expense" hoặc "Income").
     */
    fun getCategories(type: String): Flow<List<Category>>

    /**
     * HÀM MỚI - Dành cho việc khởi tạo ứng dụng lần đầu.
     * Dùng một lần để tạo các hạng mục mặc định cho người dùng mới.
     */
    suspend fun initDefaultCategories()
}
 */

/*
package com.example.expensemanagement.data.repository.Category

import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import com.example.expensemanagement.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    // Đường dẫn đến collection categories.
    // Giả sử collection "categories" nằm ở cấp cao nhất.
    private val categoriesCollection = firestore.collection("categories")

    override fun getExpenseCategories(): Flow<List<Category>> {
        Log.d("CategoryRepo", "Bắt đầu lấy danh mục chi tiêu...")
        return categoriesCollection
            .whereEqualTo("kind", "Expense") // Chỉ lấy hạng mục "Chi"
            .snapshots()
            .map { snapshot ->
                val categories = snapshot.toObjects<Category>()
                Log.d("CategoryRepo", "Lấy thành công ${categories.size} hạng mục.")
                categories // Trả về danh sách
            }
            .catch { exception ->
                // **QUAN TRỌNG NHẤT:** Nếu có lỗi, in nó ra Logcat
                Log.e("CategoryRepo", "Lỗi khi lấy danh mục: ", exception)
                emit(emptyList()) // Vẫn trả về danh sách rỗng để app không crash
            }
    }

    /**
     * HÀM MỚI - Dành cho AddTransactionViewModel.
     * Lấy danh sách các hạng mục dựa trên loại (type) được truyền vào.
     */
    override fun getCategories(type: String): Flow<List<Category>> {
        Log.d("CategoryRepo", "Bắt đầu lấy danh mục loại: $type")
        return categoriesCollection
            .whereEqualTo("type", type) // Lọc theo type được truyền vào ("Expense" hoặc "Income")
            .snapshots()
            .map { snapshot ->
                val categories = snapshot.toObjects<Category>()
                Log.d("CategoryRepo", "Lấy thành công ${categories.size} hạng mục loại $type.")
                categories
            }
            .catch { exception ->
                Log.e("CategoryRepo", "Lỗi khi lấy danh mục loại $type: ", exception)
                emit(emptyList()) // Trả về danh sách rỗng nếu có lỗi
            }
    }

    /**
     * HÀM MỚI - Dành cho việc khởi tạo ứng dụng lần đầu.
     * Dùng một lần để tạo các hạng mục mặc định cho người dùng mới.
     */
    override suspend fun initDefaultCategories() {
        try {
            // 1. Kiểm tra xem collection 'categories' đã có dữ liệu chưa
            val snapshot = categoriesCollection.limit(1).get().await()

            // 2. Nếu chưa có, thì mới thêm dữ liệu mặc định
            if (snapshot.isEmpty) {
                Log.d("CategoryRepo", "Không có hạng mục nào, bắt đầu tạo dữ liệu mặc định...")
                val defaultCategories = listOf(
                    // Hạng mục chi tiêu
                    Category(name = "Ăn uống", type = "Expense", icon = "ic_food"),
                    Category(name = "Đi lại", type = "Expense", icon = "ic_transport"),
                    Category(name = "Hóa đơn", type = "Expense", icon = "ic_bill"),
                    Category(name = "Giải trí", type = "Expense", icon = "ic_entertainment"),
                    Category(name = "Mua sắm", type = "Expense", icon = "ic_shopping"),
                    // Hạng mục thu nhập
                    Category(name = "Lương", type = "Income", icon = "ic_salary"),
                    Category(name = "Thưởng", type = "Income", icon = "ic_bonus"),
                    Category(name = "Thu nhập phụ", type = "Income", icon = "ic_side_hustle")
                )

                // 3. Dùng WriteBatch để thêm tất cả một lúc cho hiệu quả
                val batch = firestore.batch()
                defaultCategories.forEach { category ->
                    val docRef = categoriesCollection.document()
                    // Nhớ copy lại ID vào object trước khi set
                    batch.set(docRef, category.copy(id = docRef.id))
                }
                batch.commit().await()
                Log.d("CategoryRepo", "Tạo các hạng mục mặc định thành công.")
            } else {
                Log.d("CategoryRepo", "Đã có dữ liệu hạng mục, không cần tạo.")
            }
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Lỗi nghiêm trọng khi khởi tạo hạng mục: ", e)
        }
    }
}
 */