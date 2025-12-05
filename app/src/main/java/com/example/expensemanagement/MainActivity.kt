package com.example.expensemanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.ui.navigation.AppDestinations
import com.example.expensemanagement.ui.navigation.AppNavigation
import com.example.expensemanagement.ui.theme.ExpenseManagementTheme
import com.example.expensemanagement.viewmodel.AuthState
import com.example.expensemanagement.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.produceState
import com.example.expensemanagement.viewmodel.MainViewModel
import com.example.expensemanagement.viewmodel.SecurityViewModel
import com.example.expensemanagement.viewmodel.StartDestinationState

//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            ExpenseManagementTheme {
//                // Surface chỉ dùng để đặt màu nền mặc định
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    // Toàn bộ ứng dụng (AppNavigation) nằm bên trong Theme
//                    AppNavigation(startDestination = "splash") // Hoặc route khởi đầu của bạn
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AppContent(
//    viewModel: AuthViewModel = hiltViewModel()
//) {
//    val authState by viewModel.authState.collectAsStateWithLifecycle()
//
//    Surface(modifier = Modifier.fillMaxSize()) {
//
//        // Dùng 'when' để quyết định hiển thị cái gì
//        when (authState) {
//
//            // 1. Nếu đang tải (mới mở app), hiển thị vòng quay loading
//            // (Chúng ta gộp Idle vào đây luôn, vì nó cũng là trạng thái chờ)
//            is AuthState.Loading, is AuthState.Idle -> {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            // 2. Nếu đã đăng nhập, chạy AppNavigation bắt đầu từ Home
//            is AuthState.Authenticated -> {
//                AppNavigation(startDestination = AppDestinations.Home.route)
//            }
//
//            // 3. SỬA LỖI Ở ĐÂY:
//            // Nếu chưa đăng nhập, hoặc có lỗi, hoặc báo thành công (như gửi link)
//            // thì vẫn ở luồng xác thực (bắt đầu từ Onboarding).
//            is AuthState.Unauthenticated, is AuthState.Error, is AuthState.Success -> {
//                AppNavigation(startDestination = AppDestinations.Onboarding.route)
//            }
//
//            // Cách 2: Bạn cũng có thể dùng 'else' để bắt tất cả trường hợp còn lại
//             else -> {
//                 AppNavigation(startDestination = AppDestinations.Onboarding.route)
//             }
//        }
//    }
//}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cài đặt Splash Screen (nếu bạn có)
        installSplashScreen()

        enableEdgeToEdge()
        setContent {
//            ExpenseManagementTheme {
//                // Surface chỉ dùng để đặt màu nền mặc định
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    // ---- SỬA LẠI HOÀN TOÀN LOGIC Ở ĐÂY ----
//                    val viewModel: AuthViewModel = hiltViewModel()
//                    val authState by viewModel.authState.collectAsStateWithLifecycle()
//
//                    // Dùng `remember` để chỉ xác định startDestination một lần
//                    var startDestination by remember { mutableStateOf<String?>(null) }
//
//                    // Dùng LaunchedEffect để xác định màn hình bắt đầu một cách an toàn
//                    LaunchedEffect(authState) {
//                        startDestination = when (authState) {
//                            is AuthState.Authenticated -> AppDestinations.Dashboard.route
//                            is AuthState.Unauthenticated -> AppDestinations.Onboarding.route
//                            else -> null // Các trạng thái khác (Loading, Idle) thì chưa quyết định
//                        }
//                    }
//
//                    // Chỉ hiển thị AppNavigation KHI VÀ CHỈ KHI đã xác định được startDestination
//                    if (startDestination != null) {
//                        AppNavigation(startDestination = startDestination!!)
//                    } else {
//                        // Trong khi chờ đợi, hiển thị một vòng xoay loading
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator()
//                        }
//                    }
//                }
//            }

            ExpenseManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ---- SỬA LẠI HOÀN TOÀN LOGIC Ở ĐÂY ----
                    val mainViewModel: MainViewModel = hiltViewModel()
                    val startState by mainViewModel.startDestinationState.collectAsStateWithLifecycle()

                    when (val state = startState) {
                        is StartDestinationState.Loading -> {
                            // Trong khi chờ xác định, hiển thị loading
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is StartDestinationState.Destination -> {
                            // Khi đã xác định được màn hình bắt đầu, gọi AppNavigation
                            AppNavigation(startDestination = state.route)
                        }
                    }
                }
            }
        }
    }
}