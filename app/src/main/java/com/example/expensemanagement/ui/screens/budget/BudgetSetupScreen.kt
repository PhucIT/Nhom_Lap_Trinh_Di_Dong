package com.example.expensemanagement.ui.screens.budget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.data.model.Category
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.BudgetSetupUiState
import com.example.expensemanagement.viewmodel.BudgetSetupViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val cycle by viewModel.cycle.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categoryOptions by viewModel.categoryOptions.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is BudgetSetupUiState.Success -> {
                Toast.makeText(context, "Lưu ngân sách thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onNavigateBack()
            }
            is BudgetSetupUiState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color.White) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD1F5F1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Thiết Lập Ngân Sách",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Hạn Mức Ngân Sách",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = viewModel::onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                placeholder = { Text("0 VND", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.LightGray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0F7FA),
                    unfocusedContainerColor = Color(0xFFE0F7FA),
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            CycleSelector(
                label = "Chu kỳ\nÁp dụng",
                selectedCycle = cycle,
                options = viewModel.cycleOptions,
                onCycleSelected = viewModel::onCycleChange
            )
            Spacer(modifier = Modifier.height(24.dp))

            DateSelector(
                label = "Ngày Bắt\nĐầu",
                date = startDate,
                onClick = { showStartDatePicker = true }
            )
            if (cycle == "Tùy Chỉnh") {
                Spacer(modifier = Modifier.height(24.dp))
                DateSelector(
                    label = "Ngày Kết\nThúc",
                    date = endDate,
                    onClick = { showEndDatePicker = true }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hạng Mục Chi Tiêu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(8.dp))

            CategorySelector(
                selectedCategory = selectedCategory,
                options = categoryOptions,
                onCategorySelected = viewModel::onCategoryChange
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(50.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) { Text("Hủy", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                Button(
                    onClick = { viewModel.saveBudget() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80DEEA)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(50.dp),
                    enabled = (uiState != BudgetSetupUiState.Loading)
                ) {
                    if (uiState == BudgetSetupUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else { Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onStartDateChange(Date(it)) }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onEndDateChange(Date(it)) }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: Category?,
    options: List<Category>,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
//            value = selectedCategory?.name ?: "Đang tải...",
            value = selectedCategory?.name ?: "Chọn hạng mục",
            onValueChange = {},
            readOnly = true,
            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color.Gray) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE0F7FA),
                unfocusedContainerColor = Color(0xFFE0F7FA),
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color.Gray
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onCategorySelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleSelector(
    label: String,
    selectedCycle: String,
    options: List<String>,
    onCycleSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, modifier = Modifier.weight(0.3f))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(0.7f)
        ) {
            OutlinedTextField(
                value = selectedCycle,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onCycleSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DateSelector(label: String, date: Date?, onClick: () -> Unit) {
    val dateText = if (date != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date) else "Chọn ngày"
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, modifier = Modifier.weight(0.3f))
        Card(
            modifier = Modifier
                .weight(0.7f)
                .height(50.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFF48FB1))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = dateText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
