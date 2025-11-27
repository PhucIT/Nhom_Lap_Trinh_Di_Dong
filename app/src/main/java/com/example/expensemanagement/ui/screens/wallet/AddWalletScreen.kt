package com.example.expensemanagement.ui.screens.wallet

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.AddWalletUiState
import com.example.expensemanagement.viewmodel.AddWalletViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddWalletViewModel = hiltViewModel()
) {
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val tabs = listOf("Ví thường", "Ví tiết kiệm", "Ví tín dụng")

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddWalletUiState.Success -> {
                Toast.makeText(context, "Thêm ví thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onNavigateBack()
            }
            is AddWalletUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // Cân nhắc reset lại uiState về Idle sau khi hiển thị lỗi
                // viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm Ví") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.saveWallet() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = (uiState != AddWalletUiState.Loading)
            ) {
                if (uiState == AddWalletUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Thêm", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = PrimaryGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTabIndex == index),
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 24.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> NormalWalletForm(viewModel = viewModel)
                    1 -> SavingWalletForm(viewModel = viewModel)
                    2 -> CreditWalletForm(viewModel = viewModel)
                }
            }
        }
    }
}

// --- SỬA LẠI CÁC FORM ĐỂ DÙNG ĐÚNG BIẾN STATE RIÊNG BIỆT ---

@Composable
fun NormalWalletForm(viewModel: AddWalletViewModel) {
    val name by viewModel.normalWalletName.collectAsStateWithLifecycle()
    val balance by viewModel.normalWalletBalance.collectAsStateWithLifecycle()
    val currency by viewModel.walletCurrency.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = viewModel::onNormalNameChange,
            label = { Text("Tên ví") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        CurrencyDropdown(
            selectedCurrency = currency,
            onCurrencyChange = viewModel::onCurrencyChange
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = balance,
            onValueChange = viewModel::onNormalBalanceChange,
            label = { Text("Số dư") },
            leadingIcon = { Text("$", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingWalletForm(viewModel: AddWalletViewModel) {
    val name by viewModel.savingWalletName.collectAsStateWithLifecycle()
    val balance by viewModel.savingBalance.collectAsStateWithLifecycle()
    val targetBalance by viewModel.savingTargetBalance.collectAsStateWithLifecycle()
    val sourceName by viewModel.savingSourceName.collectAsStateWithLifecycle()
    val startDate by viewModel.savingStartDate.collectAsStateWithLifecycle()
    val endDate by viewModel.savingEndDate.collectAsStateWithLifecycle()
    val currency by viewModel.walletCurrency.collectAsStateWithLifecycle()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = viewModel::onSavingNameChange,
            label = { Text("Tên sổ tiết kiệm") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        CurrencyDropdown(
            selectedCurrency = currency,
            onCurrencyChange = viewModel::onCurrencyChange
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = sourceName,
            onValueChange = viewModel::onSavingSourceNameChange,
            label = { Text("Ví nguồn") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = balance,
                onValueChange = viewModel::onSavingBalanceChange,
                label = { Text("Số tiền gốc", maxLines = 1) },
                leadingIcon = { Text("$", fontSize = 18.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = targetBalance,
                onValueChange = viewModel::onSavingTargetChange,
                label = { Text("Tiền lãi dự kiến", maxLines = 1) },
                leadingIcon = { Text("$", fontSize = 18.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = startDate.formatDate(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Ngày bắt đầu") },
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = null) }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = endDate.formatDate(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Ngày đáo hạn", maxLines = 1) },
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = null) }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onSavingStartDateChange(Date(it)) }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { Button(onClick = { showStartDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onSavingEndDateChange(Date(it)) }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { Button(onClick = { showEndDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = datePickerState) }
    }
}

// KHÔI PHỤC LẠI HÀM NÀY VỀ NGUYÊN BẢN CỦA BẠN
@Composable
fun CreditWalletForm(viewModel: AddWalletViewModel) {
    val name by viewModel.creditWalletName.collectAsStateWithLifecycle()
    val balance by viewModel.creditBalance.collectAsStateWithLifecycle() // Chỉ dùng Dư nợ
    val currency by viewModel.walletCurrency.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = viewModel::onCreditNameChange,
            label = { Text("Tên thẻ tín dụng") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        CurrencyDropdown(
            selectedCurrency = currency,
            onCurrencyChange = viewModel::onCurrencyChange
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = balance,
            onValueChange = viewModel::onCreditBalanceChange,
            label = { Text("Dư nợ hiện tại") },
            leadingIcon = { Text("$", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        // Cảnh báo
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Cảnh báo",
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Cài đặt ngày thanh toán thẻ!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Không có chương trình thiết lập hoàn tiền.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit
) {
    val currencyList = listOf("VND", "USD", "EUR", "JPY", "KRW")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCurrency,
            onValueChange = {},
            readOnly = true,
            label = { Text("Đơn vị tiền tệ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencyList.forEach { currencyCode ->
                DropdownMenuItem(
                    text = { Text(currencyCode) },
                    onClick = {
                        onCurrencyChange(currencyCode)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun Date?.formatDate(): String {
    if (this == null) return ""
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
    } catch (e: Exception) {
        ""
    }
}

