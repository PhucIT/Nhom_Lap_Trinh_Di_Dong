package com.example.expensemanagement.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.repository.Theme.ThemeRepository
import com.example.expensemanagement.data.repository.Theme.ThemeSetting
import com.example.expensemanagement.ui.theme.PrimaryGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    // Đọc trạng thái từ Repository và cung cấp cho toàn bộ ứng dụng
    val themeSetting: StateFlow<ThemeSetting> = themeRepository.themeSetting
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeSetting.SYSTEM
        )

    val colorTheme: StateFlow<String> = themeRepository.colorTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "PrimaryGreen"
        )

    // Hàm được gọi từ UI khi người dùng chọn chế độ Sáng/Tối
    fun setThemeSetting(setting: ThemeSetting) {
        viewModelScope.launch {
            themeRepository.setThemeSetting(setting)
        }
    }

    // Hàm được gọi từ UI khi người dùng chọn màu mới
    fun setColorTheme(colorName: String) {
        viewModelScope.launch {
            themeRepository.setColorTheme(colorName)
        }
    }
}