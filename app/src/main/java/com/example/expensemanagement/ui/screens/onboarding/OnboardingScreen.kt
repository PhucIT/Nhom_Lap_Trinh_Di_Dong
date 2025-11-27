package com.example.expensemanagement.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanagement.R
import com.example.expensemanagement.ui.theme.ExpenseManagementTheme
import com.example.expensemanagement.ui.theme.PrimaryGreen

@Composable
fun OnboardingScreen(
    // Hàm này sẽ được gọi để điều hướng sang màn hình Đăng nhập
    onContinueClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Nền Trắng
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Đẩy nội dung ra xa nhau
            verticalArrangement = Arrangement.SpaceAround
        ) {

            // Phần nội dung (Icon và Text)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Icon Heo đất
                Image(
                    // THÊM 1 ICON ic_piggy_bank VÀO res/drawable
                    painter = painterResource(id = R.drawable.piggy_bank),
                    contentDescription = "Icon Heo đất",
                    modifier = Modifier.size(180.dp),
//                    colorFilter = ColorFilter.tint(PrimaryGreen) // Tô màu Xanh cho icon
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Dòng chữ 1 (Tiêu đề)
                Text(
                    text = "Số Chi Tiêu",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Màu đen/tối
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dòng chữ 2 (Slogan)
                Text(
                    text = "Tiết kiệm vì tương lai",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Màu xám
                )
            }

            // Nút "Tiếp tục"
            Button(
                onClick = onContinueClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen // Dùng màu Xanh chủ đạo
                ),
                shape = RoundedCornerShape(16.dp) // Bo góc
            ) {
                Text(
                    text = "Tiếp tục",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Chữ màu trắng
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    ExpenseManagementTheme {
        OnboardingScreen(onContinueClicked = {})
    }
}