package com.example.expensemanagement.ui.screens.transaction

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.data.model.Category
import com.example.expensemanagement.data.model.Wallet
import com.example.expensemanagement.ui.screens.home.formatCurrency
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.AddTransactionUiState
import com.example.expensemanagement.viewmodel.AddTransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Lắng nghe kết quả và hành động
    LaunchedEffect(key1 = uiState.isSuccess, key2 = uiState.error) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Lưu giao dịch thành công!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onNavigateBack()
        }
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetState() // Reset lỗi sau khi đã hiển thị
        }
    }

    // --- State để điều khiển việc mở/đóng Dialog ---
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm Giao Dịch Mới", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Nút Chuyển Loại
            TransactionTypeSelector(
                selectedType = uiState.type,
                onTypeSelected = viewModel::onTypeChanged
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Form Nhập Liệu
            Column(modifier = Modifier.weight(1f)) {
                // Số Tiền
                TransactionInputField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChanged,
                    label = "Số Tiền",
                    placeholder = "0",
                    keyboardType = KeyboardType.Number,
                    trailingIcon = { Text("VND", fontWeight = FontWeight.Bold, color = Color.Gray) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Hạng Mục
                TransactionSelectorField(
                    label = "Hạng Mục",
                    text = uiState.selectedCategory?.name ?: "Chọn hạng mục",
                    icon = Icons.Default.Category,
                    onClick = { showCategoryDialog = true } // <-- Mở dialog
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Ghi Chú
                TransactionInputField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChanged,
                    label = "Ghi Chú",
                    placeholder = "Thêm ghi chú...",
                    icon = Icons.Default.Edit
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Ví
                TransactionSelectorField(
                    label = "Ví",
                    text = uiState.selectedWallet?.name ?: "Chọn ví",
                    icon = Icons.Default.AccountBalanceWallet,
                    onClick = { showWalletDialog = true } // <-- Mở dialog
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Ngày Giờ
                val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
                TransactionSelectorField(
                    label = "Thời Gian",
                    text = dateFormat.format(uiState.date),
                    icon = Icons.Default.AccessTime,
                    onClick = { showDatePicker = true } // <-- Mở dialog
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Nút Lưu Giao Dịch
            Button(
                onClick = { viewModel.saveTransaction(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Lưu Giao Dịch", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- HIỂN THỊ CÁC DIALOG KHI STATE LÀ TRUE ---

    // Dialog chọn Hạng mục
    if (showCategoryDialog) {
        SelectionDialog(
            title = "Chọn Hạng Mục",
            options = uiState.categories,
            onItemSelected = { category -> viewModel.onCategorySelected(category) },
            onDismiss = { showCategoryDialog = false },
            itemText = { it.name }
        )
    }

    // Dialog chọn Ví
    if (showWalletDialog) {
        SelectionDialog(
            title = "Chọn Ví",
            options = uiState.wallets,
            onItemSelected = { wallet -> viewModel.onWalletSelected(wallet) },
            onDismiss = { showWalletDialog = false },
            itemContent = { wallet ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(wallet.name)
                    Text(wallet.balance.formatCurrency(), color = if (wallet.type == "Credit") Color.Red else Color.Black)
                }
            }
        )
    }

    // Dialog chọn ngày
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.date.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChanged(Date(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// --- COMPOSABLE DIALOG TÁI SỬ DỤNG ---
@Composable
fun <T> SelectionDialog(
    title: String,
    options: List<T>,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    itemText: ((T) -> String)? = null,
    itemContent: (@Composable (T) -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn {
                items(options) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemSelected(item)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (itemContent != null) {
                            itemContent(item)
                        } else if (itemText != null) {
                            Text(itemText(item))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

// --- COMPOSABLE PHỤ TRỢ CHO CÁC Ô NHẬP LIỆU ---

@Composable
fun TransactionInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = if (icon != null) { { Icon(icon, contentDescription = label) } } else null,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedContainerColor = Color(0xFFF5F5F5)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSelectorField(
    label: String,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label) },
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        readOnly = true, // Quan trọng: Ngăn không cho bàn phím hiện lên
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedContainerColor = Color(0xFFF5F5F5),
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = false // Dùng `clickable` ở Modifier thay thế
    )
}

// --- Composable phụ cho nút chọn Loại Giao dịch ---
@Composable
fun TransactionTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    val expenseColor = if (selectedType == "Expense") Color(0xFFEF5350) else Color.LightGray
    val incomeColor = if (selectedType == "Income") PrimaryGreen else Color.LightGray

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { onTypeSelected("Expense") },
            colors = ButtonDefaults.buttonColors(containerColor = expenseColor),
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Chi Tiêu")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chi Tiêu")
        }
        Button(
            onClick = { onTypeSelected("Income") },
            colors = ButtonDefaults.buttonColors(containerColor = incomeColor),
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.AttachMoney, contentDescription = "Thu Nhập")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thu Nhập")
        }
    }
}
