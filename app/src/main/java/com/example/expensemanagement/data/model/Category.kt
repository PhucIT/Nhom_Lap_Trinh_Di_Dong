package com.example.expensemanagement.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**     * Data Class đại diện cho 1 tài liệu trong collection 'categories'
 */
data class Category(
    @DocumentId
    val id: String = "",
    val name: String = "",      // Tên hạng mục (VD: "Ăn uống", "Giải trí")
//    @PropertyName("kind")
    @field:PropertyName("kind") @get:PropertyName("kind")
//    val type: String = "Expense",
    val type: String? = "",
    val icon: String = "",       // Tên của icon để hiển thị

    // Dùng Annotation @PropertyName để ánh xạ trường "kind" trong Firestore
    // vào thuộc tính "type" trong code Kotlin của bạn.
//    @get:PropertyName("kind")
//    val type: String = "Expense"
)