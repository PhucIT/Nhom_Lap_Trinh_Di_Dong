package com.example.expensemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.repository.Security.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//enum class PinScreenMode {
//    SET,
//    CONFIRM,
//    AUTH,
//    CHANGE_OLD,
//    CHANGE_NEW,
//    CHANGE_CONFIRM
//}
//
//data class PinUiState(
//    val mode: PinScreenMode = PinScreenMode.SET,
//    val enteredPin: String = "",
//    val error: String? = null,
//    val title: String = "Tạo mã PIN",
//    val pinLength: Int = 4,
//    val isPinCorrect: Boolean? = null
//)
//
//@HiltViewModel
//class PinViewModel @Inject constructor(
//    private val securityRepository: SecurityRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(PinUiState())
//    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()
//
//    private var firstPin = ""
//    private var oldPin = ""
//
//    fun setMode(mode: PinScreenMode) {
//        firstPin = ""
//        val newTitle = when (mode) {
//            PinScreenMode.SET -> "Tạo mã PIN mới"
//            PinScreenMode.CONFIRM -> "Xác nhận mã PIN"
//            PinScreenMode.AUTH -> "Nhập mã PIN để mở khóa"
//            PinScreenMode.CHANGE_OLD -> "Nhập mã PIN cũ"
//            PinScreenMode.CHANGE_NEW -> "Nhập mã PIN mới"
//            PinScreenMode.CHANGE_CONFIRM -> "Xác nhận mã PIN mới"
//        }
//        _uiState.value = PinUiState(mode = mode, title = newTitle)
//    }
//
//    fun onPinDigitInput(digit: String) {
//        if (_uiState.value.enteredPin.length < _uiState.value.pinLength) {
//            _uiState.update { it.copy(enteredPin = it.enteredPin + digit, error = null) }
//            if (_uiState.value.enteredPin.length == _uiState.value.pinLength) {
//                processPin()
//            }
//        }
//    }
//
//    fun onPinBackspace() {
//        if (_uiState.value.enteredPin.isNotEmpty()) {
//            _uiState.update { it.copy(enteredPin = it.enteredPin.dropLast(1), error = null) }
//        }
//    }
//
//    private fun processPin() {
//        val currentPin = _uiState.value.enteredPin
//        viewModelScope.launch { // Đảm bảo tất cả logic I/O chạy trên background thread
//            when (_uiState.value.mode) {
//                PinScreenMode.SET -> {
//                    firstPin = currentPin
//                    _uiState.update {
//                        it.copy(
//                            mode = PinScreenMode.CONFIRM,
//                            enteredPin = "",
//                            title = "Xác nhận mã PIN"
//                        )
//                    }
//                }
//                PinScreenMode.CONFIRM -> {
//                    if (currentPin == firstPin) {
//                        securityRepository.savePin(currentPin)
//                        securityRepository.setAppLockEnabled(true)
//                        _uiState.update { it.copy(isPinCorrect = true) }
//                    } else {
//                        _uiState.update { it.copy(error = "Mã PIN không khớp!", enteredPin = "") }
//                    }
//                }
//                PinScreenMode.AUTH -> {
//                    val savedPin = securityRepository.savedPin.first()
//                    if (currentPin == savedPin) {
//                        _uiState.update { it.copy(isPinCorrect = true) }
//                    } else {
//                        _uiState.update { it.copy(error = "Mã PIN sai!", enteredPin = "") }
//                    }
//                }
//                PinScreenMode.CHANGE_OLD -> {
//                    val savedPin = securityRepository.savedPin.first()
//                    if (currentPin == savedPin) {
//                        oldPin = currentPin
//                        _uiState.update {
//                            it.copy(
//                                mode = PinScreenMode.CHANGE_NEW,
//                                enteredPin = "",
//                                title = "Nhập mã PIN mới"
//                            )
//                        }
//                    } else {
//                        _uiState.update { it.copy(error = "Mã PIN cũ không đúng!", enteredPin = "") }
//                    }
//                }
//                PinScreenMode.CHANGE_NEW -> {
//                    firstPin = currentPin
//                    _uiState.update {
//                        it.copy(
//                            mode = PinScreenMode.CHANGE_CONFIRM,
//                            enteredPin = "",
//                            title = "Xác nhận mã PIN mới"
//                        )
//                    }
//                }
//                PinScreenMode.CHANGE_CONFIRM -> {
//                    if (currentPin == firstPin) {
//                        securityRepository.savePin(currentPin)
//                        _uiState.update { it.copy(isPinCorrect = true) }
//                    } else {
//                        _uiState.update { it.copy(error = "Mã PIN mới không khớp!", enteredPin = "") }
//                    }
//                }
//            }
//        }
//    }
//}

enum class PinScreenMode {
    SET,
    CONFIRM,
    VERIFY,      // Chế độ xác thực PIN khi mở app
    AUTH,        // (Tạm thời có thể chưa dùng đến)
    CHANGE_OLD,
    CHANGE_NEW,
    CHANGE_CONFIRM
}

data class PinUiState(
    val mode: PinScreenMode = PinScreenMode.SET,
    val enteredPin: String = "",
    val error: String? = null,
    val title: String = "Tạo mã PIN",
    val subtitle: String = "Mã PIN sẽ được dùng để bảo vệ ứng dụng của bạn.", // <-- THÊM DÒNG NÀY
    val pinLength: Int = 4,
    val isPinCorrect: Boolean? = null
)

@HiltViewModel
class PinViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    private var firstPin = ""
    private var oldPin = "" // Không cần thiết lắm nhưng cứ để lại

    // SỬA LẠI: Hàm setMode để cập nhật cả title và subtitle
    fun setMode(mode: PinScreenMode) {
        firstPin = "" // Reset mã pin tạm
        val (newTitle, newSubtitle) = when (mode) {
            PinScreenMode.SET -> "Tạo mã PIN mới" to "Mã PIN sẽ được dùng để bảo vệ ứng dụng của bạn."
            PinScreenMode.CONFIRM -> "Xác nhận mã PIN" to "Vui lòng nhập lại mã PIN của bạn."
            PinScreenMode.VERIFY -> "Nhập mã PIN" to "Vui lòng nhập mã PIN để tiếp tục." // <-- Sửa lại cho đúng
            PinScreenMode.AUTH -> "Nhập mã PIN" to "Nhập mã PIN để mở khóa."
            PinScreenMode.CHANGE_OLD -> "Nhập mã PIN cũ" to "Vui lòng nhập mã PIN hiện tại của bạn."
            PinScreenMode.CHANGE_NEW -> "Nhập mã PIN mới" to "Tạo mã PIN mới cho ứng dụng của bạn."
            PinScreenMode.CHANGE_CONFIRM -> "Xác nhận mã PIN mới" to "Vui lòng nhập lại mã PIN mới."
        }
        _uiState.value = PinUiState(mode = mode, title = newTitle, subtitle = newSubtitle)
    }

    fun onPinDigitInput(digit: String) {
        if (_uiState.value.enteredPin.length < _uiState.value.pinLength) {
            _uiState.update { it.copy(enteredPin = it.enteredPin + digit, error = null) }
            if (_uiState.value.enteredPin.length == _uiState.value.pinLength) {
                processPin()
            }
        }
    }

    fun onPinBackspace() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = it.enteredPin.dropLast(1), error = null) }
        }
    }

    private fun processPin() {
        val currentPin = _uiState.value.enteredPin
        viewModelScope.launch {
            when (_uiState.value.mode) {
                PinScreenMode.SET -> {
                    firstPin = currentPin
                    // Chuyển sang chế độ xác nhận
                    setMode(PinScreenMode.CONFIRM)
                }
                PinScreenMode.CONFIRM -> {
                    if (currentPin == firstPin) {
                        securityRepository.savePin(currentPin)
                        securityRepository.setAppLockEnabled(true)
                        _uiState.update { it.copy(isPinCorrect = true) }
                    } else {
                        _uiState.update { it.copy(error = "Mã PIN không khớp!", enteredPin = "") }
                    }
                }
                // SỬA LẠI: Thêm logic cho VERIFY và AUTH
                PinScreenMode.VERIFY, PinScreenMode.AUTH -> {
                    val isCorrect = securityRepository.verifyPin(currentPin)
                    if (isCorrect) {
                        _uiState.update { it.copy(isPinCorrect = true) }
                    } else {
                        _uiState.update { it.copy(error = "Mã PIN sai!", enteredPin = "") }
                    }
                }
                PinScreenMode.CHANGE_OLD -> {
                    val isCorrect = securityRepository.verifyPin(currentPin)
                    if (isCorrect) {
                        // Chuyển sang chế độ nhập PIN mới
                        setMode(PinScreenMode.CHANGE_NEW)
                    } else {
                        _uiState.update { it.copy(error = "Mã PIN cũ không đúng!", enteredPin = "") }
                    }
                }
                PinScreenMode.CHANGE_NEW -> {
                    firstPin = currentPin
                    // Chuyển sang chế độ xác nhận PIN mới
                    setMode(PinScreenMode.CHANGE_CONFIRM)
                }
                PinScreenMode.CHANGE_CONFIRM -> {
                    if (currentPin == firstPin) {
                        securityRepository.savePin(currentPin)
                        _uiState.update { it.copy(isPinCorrect = true) }
                    } else {
                        _uiState.update { it.copy(error = "Mã PIN mới không khớp!", enteredPin = "") }
                    }
                }
            }
        }
    }
}