package com.example.expensemanagement.data.repository.Category

import com.example.expensemanagement.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
//    fun getExpenseCategories(): Flow<List<Category>> // Chỉ lấy các hạng mục CHI TIÊU

    /**
     * HÀM MỚI - Dành cho AddTransactionViewModel.
     * Lấy danh sách các hạng mục dựa trên loại (type) được truyền vào ("Expense" hoặc "Income").
     */
//    fun getCategories(type: String): Flow<List<Category>>

    fun getCategories(userId: String, type: String): Flow<List<Category>>
    fun getExpenseCategories(userId: String): Flow<List<Category>>
    /**
     * HÀM MỚI - Dành cho việc khởi tạo ứng dụng lần đầu.
     * Dùng một lần để tạo các hạng mục mặc định cho người dùng mới.
     */
    suspend fun initDefaultCategories()
}