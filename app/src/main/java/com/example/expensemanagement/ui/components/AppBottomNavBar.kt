/*
package com.example.expensemanagement.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.expensemanagement.ui.navigation.AppDestinations
import com.example.expensemanagement.ui.theme.PrimaryGreen

/**
 * Bottom Navigation Bar
 * @param navController NavController
 */
@Composable
fun AppBottomNavBar(
    navController: NavController
) {
    // TODO: Chúng ta cần 1 State chung (trong ViewModel)
    // để quản lý tab đang được chọn.
    // Tạm thời, chúng ta hard-code là tab "Ví" (index=1)
    var selectedItem by remember { mutableStateOf(1) }

    val items = listOf("Giao dịch", "Ví", "Lịch", "Báo cáo", "Cài đặt")
    val icons = listOf(
        Icons.Default.Add, // Tab 0
        Icons.Default.Wallet, // Tab 1
        Icons.Default.CalendarMonth, // Tab 2
        Icons.Default.Assessment, // Tab 3
        Icons.Default.Settings // Tab 4
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface, // Nền trắng
        contentColor = PrimaryGreen
    ) {
        items.forEachIndexed { index, title ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = title) },
                label = { Text(title) },
                selected = (selectedItem == index),
                onClick = {
                    selectedItem = index
                    // Xử lý điều hướng
                    when (index) {
                        0 -> navController.navigate(AppDestinations.AddTransaction.route) {
                            // TODO: Thêm logic popUpTo cho đúng
                        }
                        1 -> navController.navigate(AppDestinations.Home.route) {
                            // TODO: Thêm logic popUpTo cho đúng
                        }
                        // TODO: Thêm case 2, 3, 4
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
*/

// File: ui/components/AppBottomNavBar.kt
package com.example.expensemanagement.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History // Dùng Icon Lịch sử
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.expensemanagement.ui.navigation.AppDestinations
import com.example.expensemanagement.ui.theme.PrimaryGreen

/**
 * Bottom Navigation Bar (Đã được nâng cấp)
 */
@Composable
fun AppBottomNavBar(
    navController: NavController
) {
    //  Lấy màn hình hiện tại từ NavController để đồng bộ hóa tab được chọn
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Định nghĩa các item trong bottom bar một cách rõ ràng
    val navigationItems = listOf(
        BottomNavItem.History,
        BottomNavItem.Wallets,
        BottomNavItem.Reports,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PrimaryGreen
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                // Trạng thái 'selected' bây giờ sẽ tự động cập nhật theo màn hình hiện tại
                selected = currentRoute == item.route,
                onClick = {
                    // SỬA 3: Logic điều hướng hoàn chỉnh
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when re-selecting the same item
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

// Tạo một sealed class để quản lý các item trong BottomNavBar dễ dàng hơn
sealed class BottomNavItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object History : BottomNavItem(AppDestinations.TransactionHistory.route, "Giao dịch", Icons.Default.History)
    object Wallets : BottomNavItem(AppDestinations.Home.route, "Ví", Icons.Default.Wallet)
    object Reports : BottomNavItem(AppDestinations.Reports.route, "Báo cáo", Icons.Default.Assessment)
    object Settings : BottomNavItem(AppDestinations.Settings.route, "Cài đặt", Icons.Default.Settings)
}

