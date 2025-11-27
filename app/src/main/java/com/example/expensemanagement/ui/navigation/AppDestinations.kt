package com.example.expensemanagement.ui.navigation


//import com.example.expensemanagement.ui.screens.detail.WalletDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

/**
 * Định nghĩa tất cả các "Điểm đến" (Màn hình) trong ứng dụng.
 * Dùng sealed class (lớp niêm phong) là cách làm chuyên nghiệp và an toàn nhất.
 *
 * @param route Tên đường dẫn (ví dụ: "login", "wallet/{walletId}")
 */
sealed class AppDestinations(val route: String) {

    // --- CÁC MÀN HÌNH XÁC THỰC (AUTH) ---

    // Màn hình Onboarding (Màn đầu tiên)
    object Onboarding : AppDestinations("onboarding")

    // Màn hình Đăng nhập
    object Login : AppDestinations("login")

    // Màn hình Đăng ký
    object Register : AppDestinations("register")

    // Màn hình Quên Mật khẩu
    object ForgotPassword : AppDestinations("forgot_password")


    // --- CÁC MÀN HÌNH CHÍNH (SAU KHI ĐĂNG NHẬP) ---

    // Màn hình Chính (hiển thị danh sách ví)
    object Home : AppDestinations("home")

    object AddWallet : AppDestinations("add_wallet") // Màn hình "Thêm Ví"


//     Màn hình Chi tiết Ví (Ví dụ về màn hình CÓ THAM SỐ)
//     Nó sẽ có đường dẫn là "wallet_detail/abcxyz" (với abcxyz là ID của ví)


    // lịch sử giao dịch
    object TransactionHistory : AppDestinations("transaction_history")

    object WalletDetail : AppDestinations("wallet_detail/{groupId}") {
        // Hàm này giúp chúng ta tạo đường dẫn an toàn
        fun createRoute(groupId: String) = "wallet_detail/$groupId"
    }

//    object WalletDetail : AppDestinations("wallet_detail/{walletId}") {
//        // Hàm này giúp chúng ta tạo đường dẫn an toàn
//        fun createRoute(walletId: String) = "wallet_detail/$walletId"
//    }

    // Màn hình Thêm Giao dịch
    object AddTransaction : AppDestinations("add_transaction")

    // Màn hình Ngân sách
    object Budget : AppDestinations("budget")

    // Màn hình Hồ sơ (Profile)
    object Profile : AppDestinations("profile")

    object Reports : AppDestinations("reports")
    object Settings : AppDestinations("settings")

    // Màn hình thay đổi mật khẩu
    object ChangePassword : AppDestinations("change_password")

    // màn QR profile
    object MyQrCode : AppDestinations("my_qr_code")

    // màn chủ đề
    object Theme: AppDestinations("theme")

    // Màn bảo mật
    object Security : AppDestinations("security")
    object SetPin : AppDestinations("set_pin")
    object ChangePin : AppDestinations("change_pin")

    //Màn 3 (Thiết lập Ngân Sách)
//    object BudgetSetup : AppDestinations("budget_setup/{budgetId}") {
//        fun createRoute(budgetId: String) = "budget_setup/$budgetId"
//    }

//    object BudgetSetup : AppDestinations("budget_setup/{walletId}") {
//        fun createRoute(walletId: String) = "budget_setup/$walletId"
//    }
object BudgetSetup : AppDestinations("budget_setup/{groupId}") {
    fun createRoute(groupId: String) = "budget_setup/$groupId"
}


    // ... Bạn có thể thêm 10 màn hình nữa vào đây ...
}