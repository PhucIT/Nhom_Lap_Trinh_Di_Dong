package com.example.expensemanagement.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data Class đại diện cho 1 tài liệu (document) trong collection 'wallets'
 */

data class Wallet(
    @DocumentId // Tự động lấy ID của document
    val id: String = "",
    val name: String = "",
    val balance: Double = 0.0,
    val type: String = "Normal", // "Normal", "Saving", "Credit"
    val currency: String = "VND",

    @ServerTimestamp // Tự động lấy ngày giờ tạo từ server
    val createdAt: Date? = null,

//    val isArchived: Boolean = false, // true nếu ví đã được lưu trữ/ẩn

    // --- THÊM CÁC TRƯỜNG CHO VÍ TIẾT KIỆM (Màn 3) ---
    val sourceWalletId: String? = null, // ID của ví gốc (Ví nguồn)
    val targetBalance: Double? = null,   // Tiền lãi/gốc dự kiến
    val startDate: Date? = null,         // Ngày bắt đầu gửi
    val endDate: Date? = null,           // Ngày đáo hạn

    // --- THÊM CÁC TRƯỜNG CHO VÍ TÍN DỤNG (Màn 4) ---
    val creditLimit: Double? = null,     // Hạn mức tín dụng
    val statementDate: Int? = null,      // Ngày sao kê (ví dụ: 25)
    val paymentDueDate: Int? = null      // Ngày thanh toán (ví dụ: 15)
)
