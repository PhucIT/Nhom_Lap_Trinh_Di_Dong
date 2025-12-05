    package com.example.expensemanagement.ui.screens.auth


import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.expensemanagement.viewmodel.AuthState
import com.example.expensemanagement.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit, // Hàm mới: để điều hướng khi thành công
    viewModel: AuthViewModel = hiltViewModel()
) {
    // --- State (Trạng thái) ---

    val resetInput by viewModel.resetInput.collectAsStateWithLifecycle()
    val otpCode by viewModel.otpCode.collectAsStateWithLifecycle() // State mới
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = LocalContext.current as Activity // Lấy Activity (BẮT BUỘC)

    // State cục bộ: 0 = Email, 1 = Số điện thoại
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Email", "Số điện thoại")

    // State cục bộ: Đã gửi OTP hay chưa?
    var isOtpSent by remember { mutableStateOf(false) }

    // Xử lý logic khi trạng thái thay đổi
    LaunchedEffect(authState) {
        when(val state = authState) {
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthStateToIdle()
            }
            is AuthState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthStateToIdle()
                if (selectedTabIndex == 0) {
                    onNavigateBack() // Gửi mail thành công -> quay lại
                } else {
                    // Xác thực SĐT thành công -> đi đến Home (hoặc màn Đổi MK)
                    onAuthSuccess()
                }
            }
            is AuthState.OtpSent -> {
                // ĐÃ GỬI OTP THÀNH CÔNG!
                Toast.makeText(context, "Đã gửi OTP!", Toast.LENGTH_SHORT).show()
                isOtpSent = true // -> Chuyển sang màn hình Nhập OTP
                viewModel.resetAuthStateToIdle()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quên mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // 1. Tab "Email" / "Số điện thoại"
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = PrimaryGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTabIndex == index),
                        onClick = {
                            selectedTabIndex = index
                            viewModel.updateResetInput("") // Xóa text cũ
                            viewModel.updateOtpCode("") // Xóa OTP cũ
                            isOtpSent = false // Reset trạng thái
                        },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Ô nhập liệu (Dùng when để hiển thị)
            when (selectedTabIndex) {
                // --- TAB EMAIL ---
                0 -> EmailResetContent(
                    email = resetInput,
                    onEmailChange = viewModel::updateResetInput,
                    onSendClick = { viewModel.sendPasswordResetEmail() },
                    isLoading = (authState is AuthState.Loading)
                )

                // --- TAB SỐ ĐIỆN THOẠI ---
                1 -> PhoneResetContent(
                    phone = resetInput,
                    onPhoneChange = viewModel::updateResetInput,
                    otp = otpCode,
                    onOtpChange = viewModel::updateOtpCode,
                    isOtpSent = isOtpSent,
                    onSendOtpClick = {
                        // Gọi hàm với SĐT và Activity
                        viewModel.sendOtp(resetInput, activity)
                    },
                    onVerifyOtpClick = {
                        viewModel.verifyOtp(onSuccess = onAuthSuccess)
                    },
                    isLoading = (authState is AuthState.Loading)
                )
            }
        }
    }
}

// --- Composable Phụ cho Tab Email ---
@Composable
private fun EmailResetContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Text("Nhập Email đăng ký của bạn. Chúng tôi sẽ gửi một link để lấy lại mật khẩu.")
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onSendClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
        shape = RoundedCornerShape(12.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        } else {
            Text("Gửi link Email", fontSize = 18.sp, color = Color.White)
        }
    }
}

// --- Composable Phụ cho Tab SĐT (2 bước) ---
@Composable
private fun PhoneResetContent(
    phone: String,
    onPhoneChange: (String) -> Unit,
    otp: String,
    onOtpChange: (String) -> Unit,
    isOtpSent: Boolean,
    onSendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    isLoading: Boolean
) {
    // Dùng AnimatedContent để chuyển 2 màn hình (Nhập SĐT <-> Nhập OTP)
    AnimatedContent(targetState = isOtpSent) { hasOtpBeenSent ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!hasOtpBeenSent) {
                // --- Bước 1: Nhập SĐT ---
                Text("Nhập SĐT đăng ký của bạn. Chúng tôi sẽ gửi một mã OTP.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Số điện thoại") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onSendOtpClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Gửi mã OTP", fontSize = 18.sp, color = Color.White)
                    }
                }
            } else {
                // --- Bước 2: Nhập OTP ---
                Text("Mã OTP đã được gửi đến SĐT $phone. Vui lòng nhập 6 số.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = onOtpChange,
                    label = { Text("Mã OTP (6 số)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onVerifyOtpClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (otp.length == 6 && !isLoading)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Xác nhận OTP", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}