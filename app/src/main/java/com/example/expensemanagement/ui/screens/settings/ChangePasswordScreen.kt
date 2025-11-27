package com.example.expensemanagement.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.AuthState
import com.example.expensemanagement.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val currentPassword by viewModel.currentPassword.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmNewPassword by viewModel.confirmNewPassword.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val context = LocalContext.current

    // Xử lý thông báo
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetAuthStateToIdle()
                onNavigateBack()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthStateToIdle()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Icon Back trong khung vuông (giống ảnh)
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                            modifier = Modifier.padding(start = 8.dp).size(40.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFE0F7FA)
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. Mật khẩu cũ
            Text("Mật khẩu cũ", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Mật khẩu mới
            Text("Mật khẩu mới", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Xác nhận mật khẩu
            Text("Xác nhận mật khẩu", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = confirmNewPassword,
                onValueChange = viewModel::onConfirmNewPasswordChange,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 4. Nút Cập nhật
            Button(
                onClick = { viewModel.changePassword(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = (authState !is AuthState.Loading)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Cập nhật", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- Composable Ô nhập Mật khẩu (Có icon mắt) ---
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction
) {
    // State để quản lý ẩn/hiện mật khẩu
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE0F7FA),
            unfocusedContainerColor = Color(0xFFE0F7FA),
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        trailingIcon = {
            // Icon Mắt
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                    tint = Color.Gray
                )
            }
        }
    )
}