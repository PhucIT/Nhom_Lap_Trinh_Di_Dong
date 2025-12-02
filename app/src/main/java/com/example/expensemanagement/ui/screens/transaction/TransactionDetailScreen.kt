package com.example.expensemanagement.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.ui.screens.home.formatCurrency
import com.example.expensemanagement.viewmodel.TransactionDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết giao dịch", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa giao dịch này không? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTransaction(onSuccess = onNavigateBack)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Xóa") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
                }
            )
        }

        val transaction = uiState.transaction

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (transaction != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                DetailRow("Ghi chú:", transaction.note ?: "Không có")
                DetailRow("Số tiền:", transaction.amount.formatCurrency())
                DetailRow("Loại:", transaction.type)
                DetailRow("Hạng mục:", transaction.categoryName)
                DetailRow("Ngày:", transaction.date.formatDateDetail())

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToEdit(transaction.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sửa")
                    }
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xóa")
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "Không tìm thấy giao dịch")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
        Text(value)
    }
}

private fun Date?.formatDateDetail(): String {
    if (this == null) return ""
    return try {
        SimpleDateFormat("EEEE, dd MMMM, yyyy HH:mm", Locale("vi", "VN")).format(this)
    } catch (e: Exception) {
        ""
    }
}