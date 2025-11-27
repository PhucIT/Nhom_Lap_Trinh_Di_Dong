package com.example.expensemanagement.ui.components.TransactionHistory


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanagement.R
import com.example.expensemanagement.data.model.history.TransactionUiModel
import com.example.expensemanagement.ui.screens.home.formatCurrency
import com.example.expensemanagement.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.*
import com.example.expensemanagement.ui.mapIconNameToResource

// 1. Nút Tab chuyển đổi (Income / Expense)
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PrimaryGreen else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = null
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

// 2. Chip lọc tĩnh (dùng cho các bộ lọc như “Hôm nay”, “Tuần này”, …)
@Composable
fun FilterChipStatic(text: String, isSelected: Boolean = false) {
    Surface(
        color = if (isSelected) PrimaryGreen else Color(0xFFE0F2F1),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 3. Header hiển thị ngày (ví dụ: Thứ Hai, ngày 15 tháng 08, 2025)
@Composable
fun DateHeader(date: Date) {
    val formatter = SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM, yyyy", Locale("vi", "VN"))
    val dateString = formatter.format(date)

    Text(
        text = dateString.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        },
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

// 4. Một dòng giao dịch trong lịch sử
@Composable
fun TransactionRowItem(item: TransactionUiModel) {
    val isExpense = item.type == "Expense"
    val bgColor = if (isExpense) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = mapIconNameToResource(item.categoryIcon)),
                contentDescription = item.categoryName,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.categoryName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = item.note,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = (if (isExpense) "-" else "+") + item.amount.formatCurrency(),
                fontWeight = FontWeight.Bold,
                color = if (isExpense) Color.Red else Color(0xFF00897B)
            )
        }
    }
}