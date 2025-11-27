package com.example.expensemanagement.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.Result
import com.example.expensemanagement.data.model.Budget
import com.example.expensemanagement.data.repository.Budget.BudgetRepository
import com.example.expensemanagement.data.model.Category
import com.example.expensemanagement.data.repository.Category.CategoryRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


// Trạng thái UI
sealed class BudgetSetupUiState {
    object Idle : BudgetSetupUiState()
    object Loading : BudgetSetupUiState()
    object Success : BudgetSetupUiState()
    data class Error(val message: String) : BudgetSetupUiState()
}

@HiltViewModel
class BudgetSetupViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Lấy ID ví (hoặc ID nhóm) được truyền từ màn hình trước
//    private val walletId: String = savedStateHandle.get<String>("walletId") ?: ""
    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""
    // --- Các biến nhập liệu ---
    private val _amount = MutableStateFlow("") // Hạn mức
    val amount: StateFlow<String> = _amount.asStateFlow()

//    private val _cycle = MutableStateFlow("Hàng Tháng") // Chu kỳ
//    val cycle: StateFlow<String> = _cycle.asStateFlow()

    private val _startDate = MutableStateFlow(Date()) // Ngày bắt đầu (Mặc định là hôm nay)
    val startDate: StateFlow<Date> = _startDate.asStateFlow()
    private val _endDate = MutableStateFlow<Date?>(null)
    val endDate: StateFlow<Date?> = _endDate.asStateFlow()

//    private val _selectedCategory = MutableStateFlow("Tất cả chi tiêu") // Hạng mục
//    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    //  THÊM DANH SÁCH LỰA CHỌN CHU KỲ
    val cycleOptions = listOf("Hàng Tuần", "Hàng Tháng", "Tùy Chỉnh")
    private val _cycle = MutableStateFlow(cycleOptions[1])
    val cycle: StateFlow<String> = _cycle.asStateFlow()

    // Trạng thái lưu
    private val _uiState = MutableStateFlow<BudgetSetupUiState>(BudgetSetupUiState.Idle)
    val uiState: StateFlow<BudgetSetupUiState> = _uiState.asStateFlow()

    // ---LOGIC CATEGORY: DÙNG DATA THẬT ---
    private val _categoryOptions = MutableStateFlow<List<Category>>(emptyList())
    val categoryOptions: StateFlow<List<Category>> = _categoryOptions.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    // Lựa chọn "Tất cả các Hạng mục" đặc biệt
    private val allCategoriesOption = Category(id = "all", name = "Tất cả các Hạng mục")

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getExpenseCategories().collect { categoriesFromDb ->
                // 2. TẠO LỰA CHỌN "TẤT CẢ": Tạo một đối tượng Category đặc biệt.
                val allCategoriesOption = Category(id = "all", name = "Tất cả các Hạng mục")

                // 3. GỘP DANH SÁCH: Thêm lựa chọn "Tất cả" vào ĐẦU danh sách hạng mục thậ
                // Thêm lựa chọn "Tất cả" vào đầu danh sách
                val fullOptions = listOf(allCategoriesOption) + categoriesFromDb
                // 4. CẬP NHẬT UI: Gửi danh sách hoàn chỉnh này đến màn hình để hiển thị.
                _categoryOptions.value = fullOptions
                // Mặc định chọn "Tất cả"
                if (_selectedCategory.value == null) {
                    _selectedCategory.value = fullOptions.first()
                }
            }
        }
    }


    // --- Hàm cập nhật dữ liệu từ UI ---
    fun onAmountChange(newAmount: String) {
        // Chỉ cho nhập số
        if (newAmount.all { it.isDigit() }) {
            _amount.value = newAmount
        }
    }
    fun onCycleChange(newCycle: String) { _cycle.value = newCycle }
    fun onStartDateChange(newDate: Date?) { newDate?.let { _startDate.value = it } }
//    fun onCategoryChange(newCategory: String) { _selectedCategory.value = newCategory }
fun onCategoryChange(newCategory: Category) { _selectedCategory.value = newCategory }

    fun onEndDateChange(newDate: Date?) {
        newDate?.let { _endDate.value = it }
    }

    // --- Hàm Lưu Ngân Sách ---
    fun saveBudget() {
        val limit = _amount.value.toDoubleOrNull()
        val selectedCat = _selectedCategory.value
        if (limit == null || limit <= 0) {
            _uiState.value = BudgetSetupUiState.Error("Vui lòng nhập hạn mức hợp lệ")
            return
        }

        if (selectedCat == null) {
            _uiState.value = BudgetSetupUiState.Error("Vui lòng chọn hạng mục")
            return
        }

        _uiState.value = BudgetSetupUiState.Loading

        viewModelScope.launch {
            // Tạo đối tượng Budget
            val newBudget = Budget(
                name = "Ngân sách ${_selectedCategory.value}", // Tên tự động
                limitAmount = limit,
                cycle = _cycle.value,
                startDate = _startDate.value,
                endDate = if (_cycle.value == "Tùy Chỉnh") _endDate.value else null,
                // logic tính endDate tùy thuộc vào cycle, tạm thời để null hoặc tính sau

                // QUAN TRỌNG: Liên kết với Ví hiện tại
//                walletId = this@BudgetSetupViewModel.walletId
                 walletType = mapGroupIdToType(this@BudgetSetupViewModel.groupId),

                // QUAN TRỌNG: LƯU ID CỦA HẠNG MỤC (Quan trọng)
                // Giả sử bạn có logic để lấy ID từ tên, nếu không, tạm thời để trống
                // Tương lai, _selectedCategory nên là một đối tượng Category thay vì String
//                categoryIds = if (_selectedCategory.value != "Tất cả chi tiêu") {
//                    listOf(_selectedCategory.value) // Tạm thời lưu tên, sau này đổi thành ID
//                } else {
//                    emptyList() // Nếu là "Tất cả chi tiêu", danh sách ID sẽ rỗng
//                }

                // Nếu người dùng chọn "Tất cả", danh sách ID sẽ rỗng
            // Nếu chọn một hạng mục cụ thể, danh sách sẽ chứa ID của nó
            categoryIds = if (selectedCat.id != "all") listOf(selectedCat.id) else emptyList()
            )

            val result = budgetRepository.setBudget(newBudget)

            when (result) {
                is Result.Success -> _uiState.value = BudgetSetupUiState.Success
                is Result.Error -> _uiState.value = BudgetSetupUiState.Error(result.exception.message ?: "Lỗi lưu ngân sách")
            }
        }
    }

    fun resetState() {
        _uiState.value = BudgetSetupUiState.Idle
    }

    /**
     * Hàm phụ trợ để chuyển đổi tên nhóm ví (hiển thị cho người dùng)
     * sang loại ví (lưu trong database) cho nhất quán.
     */
    private fun mapGroupIdToType(groupId: String): String {
        return when(groupId) {
            "Ví thường" -> "Normal"
            "Ví tiết kiệm" -> "Saving"
            "Ví tín dụng" -> "Credit"
            else -> groupId // Nếu không khớp, trả về chính nó để dự phòng
        }
    }

}