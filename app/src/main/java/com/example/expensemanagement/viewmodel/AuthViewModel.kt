package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.app.Activity
import android.net.Uri
import android.util.Log
import com.example.expensemanagement.data.repository.Category.CategoryRepository
import com.example.expensemanagement.data.repository.Storage.StorageRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit


// Sealed class đại diện cho các trạng thái UI
sealed class AuthState {
    object Idle : AuthState() // trạng thái nghỉ
    object Loading : AuthState() // Đang tải
    object Authenticated : AuthState() // Đã đăng nhập
    object Unauthenticated : AuthState() // Chưa đăng nhập

    // THÊM DÒNG NÀY: Để báo thành công (ví dụ: "Đã gửi link")
    data class Success(val message: String) : AuthState()

    data class Error(val message: String) : AuthState() // Trạng thái lỗi

    // THÊM TRẠNG THÁI NÀY: Báo cho UI biết là đã gửi OTP
    data class OtpSent(val verificationId: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth, // Hilt "tiêm" FirebaseAuth vào đây
    private val categoryRepository: CategoryRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    // --- State chung cho cả Đăng nhập & Đăng ký ---
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // --- State chỉ dùng cho Đăng ký ---
    private val _name = MutableStateFlow("") // Tên người dùng
    val name: StateFlow<String> = _name.asStateFlow()

    private val _confirmPassword = MutableStateFlow("") // Nhập lại mật khẩu
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // --- State chỉ dùng cho Quên Mật khẩu ---
    private val _resetInput = MutableStateFlow("") // (Email hoặc SĐT)
    val resetInput: StateFlow<String> = _resetInput.asStateFlow()

    // --- State cho OTP ---
    private val _otpCode = MutableStateFlow("") // (Dùng để nhập 6 số)
    val otpCode: StateFlow<String> = _otpCode.asStateFlow()
    private val _verificationId = MutableStateFlow<String?>(null) // (Lưu ID xác thực)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    // --- State quản lý trạng thái (Loading, Success, Error...) ---
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading) // Bắt đầu bằng Loading để check
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Biến lưu thông tin User hiện tại (để hiển thị lên UI)
    private val _currentUserInfo = MutableStateFlow<FirebaseUser?>(null)
    val currentUserInfo: StateFlow<FirebaseUser?> = _currentUserInfo.asStateFlow()


    // --- State cho màn hình Profile ---
    private val _profileName = MutableStateFlow("")
    val profileName: StateFlow<String> = _profileName

    private val _profileEmail = MutableStateFlow("")
    val profileEmail: StateFlow<String> = _profileEmail

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    // --- State cho màn hình Đổi mật khẩu ---
    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword

    private val _confirmNewPassword = MutableStateFlow("")
    val confirmNewPassword: StateFlow<String> = _confirmNewPassword



    init {
        // Ngay khi ViewModel được tạo, hãy kiểm tra trạng thái đăng nhập
        checkCurrentUser()
    }

//    private fun checkCurrentUser() {
//        viewModelScope.launch {
//            auth.addAuthStateListener { firebaseAuth ->
//                _authState.value = if (firebaseAuth.currentUser != null) {
//                    AuthState.Authenticated
//                } else {
//                    AuthState.Unauthenticated
//                }
//            }
//        }
//    }


    private fun checkCurrentUser() {
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _authState.value = if (user != null) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
                // CẬP NHẬT THÔNG TIN USER VÀO ĐÂY
                _currentUserInfo.value = user
            }
        }
    }


    // --- Cập nhật các trường ---
    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updatePassword(newPassword: String) { _password.value = newPassword }
    fun updateName(newName: String) { _name.value = newName }
    fun updateConfirmPassword(newConfirm: String) { _confirmPassword.value = newConfirm }
    fun updateResetInput(input: String) { _resetInput.value = input }
    fun updateOtpCode(code: String) { _otpCode.value = code }


    // --- Các hàm Logic chính ---

    /**
     * Hàm xử lý logic Đăng nhập (kết nối Firebase)
     */
    fun login(onSuccess: () -> Unit) {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _authState.value = AuthState.Error("Email và Mật khẩu không được để trống.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(_email.value.trim(), _password.value.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = AuthState.Authenticated // Đổi trạng thái
                            onSuccess() // Gọi hàm để điều hướng sang Home

                            viewModelScope.launch {
                                categoryRepository.initDefaultCategories()
                            }
                        } else {
                            val errorMsg = when (task.exception?.message) {
                                "There is no user record corresponding to this identifier." ->
                                    "Tài khoản không tồn tại."
                                "The password is invalid or the user does not have a password." ->
                                    "Mật khẩu không đúng."
                                "The email address is badly formatted." ->
                                    "Email không hợp lệ."
                                "The email address is already in use by another account." ->
                                    "Email đã được sử dụng."
                                else -> task.exception?.message ?: "Đăng nhập thất bại."
                            }
//                            _authState.value = AuthState.Error(task.exception?.message ?: "Đăng nhập thất bại.")
                            _authState.value = AuthState.Error(errorMsg)
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    // Hàm Đăng Xuất (để dùng cho nút Đăng xuất ở màn Cài đặt)
    fun logout() {
        auth.signOut()
        // (AuthState sẽ tự động chuyển về Unauthenticated nhờ listener ở trên)
    }

    // Hàm load thông tin user hiện tại lên form
    fun loadCurrentUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            _profileName.value = user.displayName ?: ""
            _profileEmail.value = user.email ?: ""
//            _profileImageUri.value = user.photoUrl
            _profileImageUri.value = null // Reset ảnh preview
        }
    }

    // Hàm cập nhật tên
    fun onProfileNameChange(newName: String) {
        _profileName.value = newName
    }

    // hàm nhập email
    fun onProfileEmailChange(newEmail: String) {
        _profileEmail.value = newEmail.trim()
    }

    // Hàm cập nhật ảnh (tạm thời chỉ update UI state, chưa upload ảnh thật)
    fun onProfileImageChange(uri: Uri?) {
        _profileImageUri.value = uri
    }

    // Hàm Lưu Hồ Sơ (Update Profile)
//    fun updateProfile(onSuccess: () -> Unit) {
//        val user = auth.currentUser ?: return
//
//        _authState.value = AuthState.Loading
//
//        val profileUpdates = UserProfileChangeRequest.Builder()
//            .setDisplayName(_profileName.value)
//            // .setPhotoUri(_profileImageUri.value) // Nếu muốn cập nhật ảnh (cần upload lên Storage trước để lấy URL mạng)
//            .build()
//
//        user.updateProfile(profileUpdates)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    _authState.value = AuthState.Success("Cập nhật hồ sơ thành công!")
//                    onSuccess()
//                } else {
//                    _authState.value = AuthState.Error(task.exception?.message ?: "Lỗi cập nhật hồ sơ")
//                }
//            }
//    }
//    fun updateProfile(
//        newName: String? = null,
//        newEmail: String? = null,
//        onSuccess: () -> Unit
//    ) {
//        val user = auth.currentUser ?: run {
//            _authState.value = AuthState.Error("Không tìm thấy người dùng")
//            return
//        }
//
//        _authState.value = AuthState.Loading
//
//        // Tạo profile update cho tên
//        val profileBuilder = UserProfileChangeRequest.Builder()
//        newName?.let { profileBuilder.setDisplayName(it) }
//
//        val profileUpdateTask = user.updateProfile(profileBuilder.build())
//
//        // Nếu có thay đổi email → gọi updateEmail riêng
//        val emailUpdateTask = if (!newEmail.isNullOrBlank() && newEmail != user.email) {
//            user.updateEmail(newEmail)
//        } else {
//            com.google.android.gms.tasks.Tasks.forResult(null) // Task rỗng nếu không đổi email
//        }
//
//        // Chạy cả 2 task (nếu có)
//        com.google.android.gms.tasks.Tasks.whenAllComplete(profileUpdateTask, emailUpdateTask)
//            .addOnSuccessListener {
//                _authState.value = AuthState.Success("Cập nhật hồ sơ thành công!")
//                loadCurrentUserProfile() // refresh lại dữ liệu
//                onSuccess()
//            }
//            .addOnFailureListener { exception ->
//                val msg = when (exception) {
//                    is FirebaseAuthInvalidCredentialsException -> "Email không hợp lệ"
//                    is FirebaseAuthUserCollisionException -> "Email đã được sử dụng bởi tài khoản khác"
//                    else -> exception.message ?: "Cập nhật thất bại"
//                }
//                _authState.value = AuthState.Error(msg)
//            }
//    }

    fun updateProfile(onSuccess: () -> Unit) {
        val user = auth.currentUser ?: run {
            _authState.value = AuthState.Error("Không tìm thấy người dùng")
            return
        }

        val newName = _profileName.value.trim()
        val newEmail = _profileEmail.value.trim()
        val newImageUri = _profileImageUri.value // Uri của ảnh mới chọn từ thư viện

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                var finalPhotoUrl: Uri? = null

                // 1. Nếu người dùng có chọn ảnh mới, tải nó lên Firebase Storage trước
                if (newImageUri != null) {
                    when (val uploadResult = storageRepository.uploadProfileImage(user.uid, newImageUri)) {
                        is com.example.expensemanagement.data.Result.Success -> {
                            finalPhotoUrl = uploadResult.data
                        }
                        is com.example.expensemanagement.data.Result.Error -> {
                            _authState.value = AuthState.Error("Lỗi tải ảnh lên: ${uploadResult.exception.message}")
                            return@launch
                        }
                    }
                }

                // 2. Cập nhật profile của người dùng trên Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .apply {
                        if (finalPhotoUrl != null) {
                            photoUri = finalPhotoUrl
                        }
                    }
                    .build()

                user.updateProfile(profileUpdates).await()

                // 3. Cập nhật email (nếu có thay đổi)
                if (newEmail.isNotBlank() && newEmail != user.email) {
                    user.updateEmail(newEmail).await()
                }

                // 4. Báo thành công
                _authState.value = AuthState.Success("Cập nhật hồ sơ thành công!")
                loadCurrentUserProfile() // Tải lại thông tin mới nhất
                onSuccess()

            } catch (e: Exception) {
                val msg = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Email không hợp lệ."
                    is FirebaseAuthUserCollisionException -> "Email đã được sử dụng bởi tài khoản khác."
                    else -> e.message ?: "Cập nhật thất bại"
                }
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    // --- Hàm cập nhật state từ UI ---
    fun onCurrentPasswordChange(pass: String) { _currentPassword.value = pass }
    fun onNewPasswordChange(pass: String) { _newPassword.value = pass }
    fun onConfirmNewPasswordChange(pass: String) { _confirmNewPassword.value = pass }

    // --- Hàm Logic Đổi Mật Khẩu ---
    fun changePassword(onSuccess: () -> Unit) {
        val user = auth.currentUser
        val currentPass = _currentPassword.value
        val newPass = _newPassword.value
        val confirmPass = _confirmNewPassword.value

        if (user == null || user.email == null) {
            _authState.value = AuthState.Error("Lỗi: Người dùng không tồn tại")
            return
        }
        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập đầy đủ thông tin")
            return
        }
        if (newPass != confirmPass) {
            _authState.value = AuthState.Error("Mật khẩu mới không khớp")
            return
        }
        if (newPass.length < 6) {
            _authState.value = AuthState.Error("Mật khẩu mới phải có ít nhất 6 ký tự")
            return
        }

        _authState.value = AuthState.Loading

        // 1. Xác thực lại người dùng (Re-authenticate) - BẮT BUỘC trước khi đổi pass
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // 2. Đổi mật khẩu
                    user.updatePassword(newPass)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                _authState.value = AuthState.Success("Đổi mật khẩu thành công!")
                                // Reset các ô nhập
                                _currentPassword.value = ""
                                _newPassword.value = ""
                                _confirmNewPassword.value = ""
                                onSuccess()
                            } else {
                                _authState.value = AuthState.Error(updateTask.exception?.message ?: "Lỗi đổi mật khẩu")
                            }
                        }
                } else {
                    _authState.value = AuthState.Error("Mật khẩu cũ không đúng")
                }
            }
    }

    /**
     * Hàm xử lý logic Đăng ký (kết nối Firebase)
     */
    fun register(onSuccess: () -> Unit) {
        // (Code đăng ký của bạn đã đúng, giữ nguyên)
        if (_name.value.isBlank() || _email.value.isBlank() || _password.value.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập đầy đủ Tên, Email và Mật khẩu.")
            return
        }
        if (_password.value != _confirmPassword.value) {
            _authState.value = AuthState.Error("Mật khẩu nhập lại không khớp.")
            return
        }
        if (_password.value.length < 6) {
            _authState.value = AuthState.Error("Mật khẩu phải có ít nhất 6 ký tự.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(_email.value.trim(), _password.value.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(_name.value.trim())
                                .build()

                            user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                // Dù cập nhật tên thành công hay không, vẫn cho đăng nhập
                                _authState.value = AuthState.Authenticated
                                onSuccess()
                                // TODO: Lưu user vào Firestore (làm sau)

                                viewModelScope.launch {
                                    categoryRepository.initDefaultCategories()
                                }
                            }
                        } else {
                            _authState.value = AuthState.Error(task.exception?.message ?: "Đăng ký thất bại.")
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * Hàm xử lý logic Đăng nhập Google
     */
    fun signInWithGoogleToken(idToken: String, onSuccess: () -> Unit) {
        // (Code Google Sign-In của bạn đã đúng, giữ nguyên)
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = AuthState.Authenticated
                            onSuccess()

                            viewModelScope.launch {
                                categoryRepository.initDefaultCategories()
                            }
                        } else {
                            _authState.value = AuthState.Error(task.exception?.message ?: "Đăng nhập Google thất bại.")
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * HÀM MỚI: Xử lý logic Quên Mật khẩu
     */
    fun sendPasswordResetEmail() {
        if (_resetInput.value.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập Email của bạn.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try { // ĐÃ THÊM TRY-CATCH (Để bắt lỗi email sai định dạng)
                auth.sendPasswordResetEmail(_resetInput.value.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = AuthState.Success("Đã gửi link lấy lại mật khẩu qua Email!")
                        } else {
                            _authState.value = AuthState.Error(task.exception?.message ?: "Gửi link thất bại.")
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * HÀM ĐÃ SỬA: Gửi mã OTP (Cho SĐT)
     */
    fun sendOtp(phoneNumber: String, activity: Activity) {

        if (phoneNumber.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập Số điện thoại của bạn.")
            return // Dừng lại, không gửi
        }

        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+84${phoneNumber.drop(1)}"
        _authState.value = AuthState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }
            // Gửi OTP thất bại
            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error("Lỗi SĐT: ${e.message}")
            }
            // Gửi OTP thành công
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _verificationId.value = verificationId
                _authState.value = AuthState.OtpSent(verificationId)
            }
        }

        try { // BẮT LỖI ĐỒNG BỘ (ví dụ: SĐT quá ngắn/dài)
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Lỗi gửi OTP")
        }
    }

    /**
     * HÀM ĐÃ SỬA: Xác thực mã OTP
     */
    fun verifyOtp(onSuccess: () -> Unit) {
        val verificationId = _verificationId.value
        val otpCode = _otpCode.value

        if (verificationId.isNullOrBlank() || otpCode.length < 6) {
            _authState.value = AuthState.Error("Mã OTP không hợp lệ.")
            return
        }
        _authState.value = AuthState.Loading

        try { // BẮT LỖI ĐỒNG BỘ (ví dụ: otpCode có chữ)
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            signInWithPhoneCredential(credential, onSuccess)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Lỗi xác thực OTP")
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            try { // BẮT LỖI ĐỒNG BỘ
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = AuthState.Success("Xác thực SĐT thành công!")
                            // TODO: Chuyển sang màn hình "Đặt Mật khẩu Mới"
                            onSuccess?.invoke()
                        } else {
                            _authState.value = AuthState.Error(task.exception?.message ?: "Xác thực OTP thất bại.")
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Lỗi đăng nhập SĐT")
            }
        }
    }


    /**
     * HÀM ĐÃ SỬA: Hàm để reset trạng thái lỗi/thành công sau khi hiển thị
     */
    fun resetAuthStateToIdle() {
        if (_authState.value is AuthState.Error || _authState.value is AuthState.Success || _authState.value is AuthState.OtpSent) {
            _authState.value = AuthState.Idle
        }
    }
}