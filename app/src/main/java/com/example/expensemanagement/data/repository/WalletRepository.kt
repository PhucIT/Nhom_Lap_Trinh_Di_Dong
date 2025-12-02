package com.example.expensemanagement.data.repository

import com.example.expensemanagement.data.model.Wallet
import kotlinx.coroutines.flow.Flow
import com.example.expensemanagement.data.Result

/**
 * Interface (Hợp đồng) cho Kho chứa Ví.
 */

interface WalletRepository {
    /**
     * Lấy danh sách TẤT CẢ các ví của người dùng hiện tại (theo thời gian thực).
     */
    fun getWallets(): Flow<List<Wallet>>

    // (Chúng ta sẽ thêm hàm addWallet, updateWallet... vào đây sau)
    /**
     * Lấy thông tin chi tiết của MỘT ví cụ thể.
     * @param walletId ID của ví cần lấy.
     * @return một Flow chứa thông tin ví, tự động cập nhật.
     */
    fun getWalletById(walletId: String): Flow<Wallet?>

    /**
     * Thêm một ví mới vào Firestore.
     * @param wallet Đối tượng Wallet chứa thông tin ví mới.
     * @return Result để biết thành công hay thất bại.
     */
    suspend fun addWallet(wallet: Wallet): Result<Unit>

    /**
     * Cập nhật thông tin của một ví đã có.
     * @param wallet Đối tượng Wallet chứa thông tin mới. ID của ví phải tồn tại.
     * @return Result để biết thành công hay thất bại.
     */
    suspend fun updateWallet(wallet: Wallet): Result<Unit>

    /**
     * Xóa một ví khỏi Firestore.
     * @param walletId ID của ví cần xóa.
     * @return Result để biết thành công hay thất bại.
     */
    suspend fun deleteWallet(walletId: String): Result<Unit>

    // Hàm để cập nhật số dư ví
    suspend fun updateWalletBalance(walletId: String, amountChange: Double): Result<Unit>
}