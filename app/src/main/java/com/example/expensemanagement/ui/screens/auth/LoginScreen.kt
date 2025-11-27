package com.example.expensemanagement.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.R
import com.example.expensemanagement.ui.theme.ExpenseManagementTheme
import com.example.expensemanagement.ui.theme.PrimaryGreen
import com.example.expensemanagement.viewmodel.AuthState
import com.example.expensemanagement.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateBack: () -> Unit, // Để quay lại Onboarding
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Lắng nghe trạng thái từ ViewModel
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // KHỐI 1: Lấy Web Client ID (RẤT QUAN TRỌNG)
    // Bạn phải lấy ID này từ file google-services.json
    // (Tìm dòng "client_type": 3, rồi copy cái "client_id" ở trên nó)
    // HOẶC lấy từ Firebase Console (Authentication -> Sign-in -> Google)
    val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com" // THAY THẾ CHỖ NÀY

    // KHỐI 2: Cấu hình Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId) // Yêu cầu IdToken
            .requestEmail()
            .build()
    }

    // KHỐI 3: Lấy GoogleSignInClient
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // KHỐI 4: Tạo Launcher để mở cửa sổ Google
    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Kết quả trả về sau khi người dùng chọn tài khoản Google
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            // Lấy được IdToken thành công!
            val idToken = account.idToken!!

            // GỌI VIEWMODEL: Gửi IdToken này cho ViewModel để đăng nhập Firebase
            viewModel.signInWithGoogleToken(idToken, onLoginSuccess)

        } catch (e: ApiException) {
            // Lỗi
            Toast.makeText(context, "Lỗi Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Xử lý logic khi trạng thái thay đổi
    LaunchedEffect(authState) {
        when(val state = authState) {
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthStateToIdle() // Reset lại để không hiện toast liên tục
            }
            is AuthState.Authenticated -> {
                // Đã đăng nhập thành công, gọi hàm điều hướng
                onLoginSuccess()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đăng nhập") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Tiêu đề
                Text(
                    text = "Số Chi Tiêu",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 2. Icon Heo đất
                Image(
                    // BẠN CẦN THÊM FILE "ic_piggy_bank.xml" VÀO res/drawable
                    painter = painterResource(id = R.drawable.piggy_bank), // DÙNG TẠM
                    contentDescription = "Icon",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 32.dp),
//                    colorFilter = ColorFilter.tint(PrimaryGreen)
                )

                // 3. Email/Phone
                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("Email/Phone") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Mật khẩu
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Mật khẩu") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // 5. Quên mật khẩu & Đăng ký
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quên mật khẩu?",
                        color = PrimaryGreen,
                        modifier = Modifier.clickable { onNavigateToForgotPassword() }
                    )
                    Text(
                        text = "Đăng ký",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }

                // 6. Nút Đăng nhập
                Button(
                    onClick = {
                        viewModel.login(onSuccess = onLoginSuccess)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (authState != AuthState.Loading)
                ) {
                    if (authState == AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Đăng nhập", fontSize = 18.sp, color = Color.White)
                    }
                }

                // 7. "Hoặc"
                Text(
                    text = "Hoặc",
                    modifier = Modifier.padding(vertical = 24.dp),
                    color = Color.Gray
                )

                // 8. Đăng nhập Google
                OutlinedButton(
                    onClick = {
                    /* TODO: Xử lý Google Sign In */
                        val signInIntent = googleSignInClient.signInIntent
                        authLauncher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    // BẠN CẦN THÊM "ic_google_logo.xml" VÀO res/drawable
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo), // DÙNG TẠM
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Đăng nhập với Google",
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ExpenseManagementTheme {
        LoginScreen({}, {}, {}, {})
    }
}
