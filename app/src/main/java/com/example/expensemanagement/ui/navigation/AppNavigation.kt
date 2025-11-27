package com.example.expensemanagement.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.example.expensemanagement.ui.screens.auth.ForgotPasswordScreen
import com.example.expensemanagement.ui.screens.auth.LoginScreen
import com.example.expensemanagement.ui.screens.auth.RegisterScreen
import com.example.expensemanagement.ui.screens.detail.WalletDetailScreen
//import com.example.expensemanagement.ui.screens.home.HomeScreen
import com.example.expensemanagement.ui.screens.onboarding.OnboardingScreen
import com.example.expensemanagement.viewmodel.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import com.example.expensemanagement.ui.screens.auth.ForgotPasswordScreen
import com.example.expensemanagement.ui.screens.home.HomeScreen
import com.example.expensemanagement.ui.screens.wallet.AddWalletScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.expensemanagement.data.model.Budget
//import com.example.expensemanagement.ui.components.settings.MyQrCodeScreen
import com.example.expensemanagement.ui.screens.budget.BudgetSetupScreen
import com.example.expensemanagement.ui.screens.history.TransactionHistoryScreen
import com.example.expensemanagement.ui.screens.transaction.AddTransactionScreen
import com.example.expensemanagement.ui.screens.report.ReportScreen
import com.example.expensemanagement.ui.screens.settings.ChangePasswordScreen
import com.example.expensemanagement.ui.screens.settings.MyQrCodeScreen
import com.example.expensemanagement.ui.screens.settings.PinInputScreen
import com.example.expensemanagement.ui.screens.settings.ProfileScreen
import com.example.expensemanagement.ui.screens.settings.SettingsScreen
import com.example.expensemanagement.ui.screens.settings.ThemeScreen
import com.example.expensemanagement.ui.screens.settings.SecurityScreen
import com.example.expensemanagement.viewmodel.PinScreenMode


@Composable
fun AppNavigation(
    startDestination: String
) {
    val navController = rememberNavController()

    // Tạo AuthViewModel ở đây, nó sẽ được chia sẻ cho cả Login và Register
    // (Vì NavHost chứa cả 2 màn hình này)
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // 1. Màn hình Onboarding
        composable(AppDestinations.Onboarding.route) {
            OnboardingScreen(
                onContinueClicked = {
                    navController.navigate(AppDestinations.Login.route) {
                        popUpTo(AppDestinations.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Màn hình Đăng nhập
        composable(AppDestinations.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Đăng nhập thành công, đi đến Home
                    navController.navigate(AppDestinations.Home.route) {
                        popUpTo(AppDestinations.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // Đi đến Đăng ký
                    navController.navigate(AppDestinations.Register.route)
                },
                onNavigateToForgotPassword = {
                    // Đi đến Quên mật khẩu
                    navController.navigate(AppDestinations.ForgotPassword.route)
                },
                onNavigateBack = {
                    // Quay lại Onboarding
                    navController.navigate(AppDestinations.Onboarding.route) {
                        popUpTo(AppDestinations.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel // Dùng chung ViewModel
            )
        }

        // 3. Màn hình Đăng ký
        composable(AppDestinations.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Đăng ký thành công, đi đến Home
                    // (Bạn có thể đổi logic này để đi đến màn "Tạo Ví" sau)
                    navController.navigate(AppDestinations.Home.route) {
                        popUpTo(AppDestinations.Register.route) { inclusive = true }
                        popUpTo(AppDestinations.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // Quay lại Đăng nhập
                    navController.popBackStack()
                },
                viewModel = authViewModel // Dùng chung ViewModel
            )
        }

        // 4. Màn hình Quên Mật khẩu
        composable(AppDestinations.ForgotPassword.route) {
            // (Chúng ta sẽ code màn hình này ở bước tiếp theo)
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    // TODO: Nên chuyển sang màn "Đặt Mật khẩu Mới"
                    navController.navigate(AppDestinations.Home.route) {
                        popUpTo(AppDestinations.ForgotPassword.route) { inclusive = true }
                        popUpTo(AppDestinations.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel // Dùng chung ViewModel
            )
        }

        // 5. Màn hình Chính (Home) (Vẫn tạm thời)
        composable(AppDestinations.Home.route) {
            HomeScreen(
                onAddWalletClicked = {
                    // Nhấn nút "+ Thêm ví" -> đi đến màn Thêm Ví
                    navController.navigate(AppDestinations.AddWallet.route)
                },
                // THÊM THAM SỐ 2
                onWalletGroupClicked =  { groupId ->
                    // Điều hướng đến màn Chi tiết, truyền ID
                    navController.navigate(
                        AppDestinations.WalletDetail.createRoute(groupId)
                    )
                },

                // THÊM THAM SỐ 3
                onAddTransactionClicked = {
                    // Điều hướng đến màn Thêm Giao dịch
                    navController.navigate(AppDestinations.AddTransaction.route)
                },
                navController = navController,
                onLogoutSuccess = {
                    navController.navigate(AppDestinations.Login.route) {
                        // Xóa tất cả các màn hình trước đó khỏi back stack
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Màn hình Thêm Ví (Màn 2)
        composable(AppDestinations.AddWallet.route) {
            AddWalletScreen(
                onNavigateBack = {
                    navController.popBackStack() // Quay lại HomeScreen
                }
            )
        }

        // 7. Màn hình Thêm Giao Dịch
        composable(
            AppDestinations.AddTransaction.route
        ) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack() // Quay lại màn hình trước đó (Chi Tiết Ví)
                }
            )
        }

        // 8. Màn hình Chi Tiết Ví
        composable(
            route = AppDestinations.WalletDetail.route,
            // Định nghĩa tham số {groupId}
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
//            arguments = listOf(navArgument("walletId") { type = NavType.StringType })

        ) { backStackEntry ->

            // 1. LẤY GROUP ID TỪ ĐƯỜNG DẪN
//            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
//            val currentId = backStackEntry.arguments?.getString("walletId") ?: ""

//            WalletDetailScreen(
//                onNavigateBack = {
//                    navController.popBackStack()
//                },
//
//
//                onNavigateToBudgetSetup = {
//                    // Khi nhấn nút "Thiết lập ngân sách",
//                    // Dùng chính cái groupId này để đi tiếp
//                    if (currentId.isNotEmpty()) {
//                        navController.navigate(AppDestinations.BudgetSetup.createRoute(currentId))
//                    } else {
//                        // Xử lý trường hợp lỗi (ví dụ: log ra)
//                        println("Lỗi: groupId bị rỗng, không thể điều hướng")
//                    }
//                },
//                // -----------------------------
//
//                onNavigateToAddTransaction = {
//                    navController.navigate(AppDestinations.AddTransaction.route)
//                }
//            )
//            WalletDetailScreen(
//                onNavigateBack = {
//                    navController.popBackStack()
//                },
//                onNavigateToBudgetSetup = {
//                    // SỬA 2: Lấy đúng "walletId" và truyền đi
//                    val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
//                    if (walletId.isNotEmpty()) {
//                        // Điều hướng đến màn hình BudgetSetup với walletId đúng
//                        navController.navigate(AppDestinations.BudgetSetup.createRoute(walletId))
//                    } else {
//                        println("Lỗi: walletId bị rỗng, không thể điều hướng đến BudgetSetup")
//                    }
//                },
//                onNavigateToAddTransaction = {
//                    navController.navigate(AppDestinations.AddTransaction.route)
//                }
//            )
            // 1. LẤY GROUP ID TỪ ĐƯỜNG DẪN
            WalletDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBudgetSetup = {
                    val currentGroupId = backStackEntry.arguments?.getString("groupId") ?: ""

                    // SỬA LỖI CRASH: Dùng chính cái currentGroupId đã lấy
                    if (currentGroupId.isNotEmpty()) {
                        navController.navigate(AppDestinations.BudgetSetup.createRoute(currentGroupId))
                    } else {
                        // Nếu ID bị lỗi, tránh crash
                        println("Lỗi: groupId bị rỗng, không thể điều hướng")
                    }
                },
                onNavigateToAddTransaction = {
                    navController.navigate(AppDestinations.AddTransaction.route)
                },
                onLogoutSuccess = {
                    navController.navigate(AppDestinations.Login.route) {
                        popUpTo(navController.graph.startDestinationId) {                    inclusive = true
                        }
                    }
                }
            )
        }

        // 9. Màn hình Thiết Lập Ngân Sách
        composable(
            route = AppDestinations.BudgetSetup.route,
//            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
//            val groupId = backStackEntry.arguments?.getString("groupId") ?: "No ID"
            BudgetSetupScreen(
//                groupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
//            val groupId = backStackEntry.arguments?.getString("groupId") ?: "ID không có"
//            BudgetSetupScreen(
//                groupId = groupId,
//                onNavigateBack = { navController.popBackStack() }
//            )
        }

        // lịch sử giao dịch
        composable(AppDestinations.TransactionHistory.route) {
            TransactionHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // màn báo cáo
        composable(AppDestinations.Reports.route) {
            ReportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // settings
        composable(AppDestinations.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = {
                    navController.navigate(AppDestinations.Profile.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(AppDestinations.ChangePassword.route)
                },
                onLogout = {
                    // Logic đăng xuất và quay về màn hình Login
                    navController.navigate(AppDestinations.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                // hàm điều hướng cho QR
                onNavigateToMyQrCode = {
                    navController.navigate(AppDestinations.MyQrCode.route)
                },
                onNavigateToTheme = {
                    navController.navigate(AppDestinations.Theme.route)
                },
                onNavigateToSecurity = { navController.navigate(AppDestinations.Security.route) }
                // viewModel tự động được inject
            )
        }


        // QR code
        composable(AppDestinations.MyQrCode.route) {
            MyQrCodeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // profile
        composable(AppDestinations.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                // viewModel được inject tự động
            )
        }

        // Màn hình Đổi mật khẩu
        composable(AppDestinations.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                // viewModel được inject tự động
            )
        }

        // Màn chủ đề
        composable(AppDestinations.Theme.route) {
            ThemeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AppDestinations.Security.route) {
            SecurityScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSetPin = { navController.navigate(AppDestinations.SetPin.route) },
                onNavigateToChangePin = { navController.navigate(AppDestinations.ChangePin.route) }
            )
        }

// màn hình trống cho SetPin và ChangePin
        composable(AppDestinations.SetPin.route) {
            PinInputScreen(
                mode = PinScreenMode.SET,
                onNavigateBack = { navController.popBackStack() },
                onPinSuccess = {
                    // Tạo PIN thành công, quay lại màn hình Bảo mật
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.ChangePin.route) {
            PinInputScreen(
                mode = PinScreenMode.CHANGE_OLD,
                onNavigateBack = { navController.popBackStack() },
                onPinSuccess = {
                    // Đổi PIN thành công, quay lại màn hình Bảo mật
                    navController.popBackStack()
                }
            )
        }
    }

}


