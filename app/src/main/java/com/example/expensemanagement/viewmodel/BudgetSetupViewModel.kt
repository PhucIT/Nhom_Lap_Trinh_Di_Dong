package com.example.expensemanagement.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanagement.data.Result
import com.example.expensemanagement.data.model.Budget
import com.example.expensemanagement.data.repository.Budget.BudgetRepository
import com.example.expensemanagement.data.model.Category
import com.example.expensemanagement.data.repository.Category.CategoryRepository
import com.example.expensemanagement.data.repository.Auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import android.util.Log


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
    private val authRepository: AuthRepository,
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

//    private val _startDate = MutableStateFlow(Date()) // Ngày bắt đầu (Mặc định là hôm nay)
//    val startDate: StateFlow<Date> = _startDate.asStateFlow()

    //startDate phải là Date?, nhưng ta sẽ cung cấp giá trị mặc định là Date()
    private val _startDate = MutableStateFlow(Date())
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


    // Biến để lưu lại ngân sách đã có, nếu là chế độ "Sửa"
    private var existingBudget: Budget? = null


    init {
        loadInitialData() // Tải dữ liệu ngân sách cũ (nếu có)
        loadCategories()
    }

//    private fun loadCategories() {
//        viewModelScope.launch {
//            categoryRepository.getExpenseCategories().collect { categoriesFromDb ->
//                // 2. TẠO LỰA CHỌN "TẤT CẢ": Tạo một đối tượng Category đặc biệt.
//                val allCategoriesOption = Category(id = "all", name = "Tất cả các Hạng mục")
//
//                // 3. GỘP DANH SÁCH: Thêm lựa chọn "Tất cả" vào ĐẦU danh sách hạng mục thậ
//                // Thêm lựa chọn "Tất cả" vào đầu danh sách
//                val fullOptions = listOf(allCategoriesOption) + categoriesFromDb
//                // 4. CẬP NHẬT UI: Gửi danh sách hoàn chỉnh này đến màn hình để hiển thị.
//                _categoryOptions.value = fullOptions
//                // Mặc định chọn "Tất cả"
//                if (_selectedCategory.value == null) {
//                    _selectedCategory.value = allCategoriesOption
//                }
//            }
//        }
//    }

    private fun loadCategories() {
        viewModelScope.launch {
            // 1. LẤY USER ID TRƯỚC
            val userId = authRepository.currentUserFlow.first()?.uid
            if (userId == null) {
                _uiState.value = BudgetSetupUiState.Error("Không thể xác thực người dùng.")
                return@launch
            }

            // 2. SỬA LẠI: GỌI HÀM VỚI USER ID
            categoryRepository.getExpenseCategories(userId).collect { categoriesFromDb ->
                val allCategoriesOption = Category(id = "all", name = "Tất cả các Hạng mục")
                val fullOptions = listOf(allCategoriesOption) + categoriesFromDb
                _categoryOptions.value = fullOptions

                // Chỉ chọn mặc định nếu chưa có ngân sách cũ nào được load
                if (existingBudget == null && _selectedCategory.value == null) {
                    _selectedCategory.value = allCategoriesOption
                }
            }
        }
    }

    // --- HÀM MỚI: Dùng để load dữ liệu ngân sách đã có ---
    private fun loadInitialData() {
        viewModelScope.launch {
            if (groupId.isNotEmpty()) {
                val walletType = mapGroupIdToType(groupId)
                Log.d("BudgetVM", "Bắt đầu load dữ liệu ngân sách cho walletType: $walletType")

                // 1. Tìm xem có ngân sách nào đã tồn tại cho nhóm ví này không
                val budget = budgetRepository.getBudgets().first().firstOrNull { it.walletType == walletType }

                if (budget != null) {
                    existingBudget = budget // Lưu lại để dùng lúc update
                    Log.d("BudgetVM", "Đã tìm thấy ngân sách cũ: Hạn mức ${budget.limitAmount}")

                    // 2. Nếu có, cập nhật UI bằng dữ liệu của nó
                    _amount.value = budget.limitAmount.toLong().toString()
                    _cycle.value = budget.cycle
                    // Xử lý trường hợp startDate có thể là null
                    _startDate.value = budget.startDate ?: Date() // Nếu null thì lấy ngày hiện tại
                    _endDate.value = budget.endDate
                    // Logic chọn lại category đã lưu
                    if (budget.categoryIds.isNotEmpty()) {
                        val savedCategoryId = budget.categoryIds.first()
                        // Chờ cho `_categoryOptions` có dữ liệu rồi mới tìm
                        _categoryOptions.first { it.isNotEmpty() }.find { it.id == savedCategoryId }
                            ?.let {
                                _selectedCategory.value = it
                            }
                    } else {
                        Log.d("BudgetVM", "Không tìm thấy ngân sách cũ, ở chế độ tạo mới.")
                        _selectedCategory.value = Category(id = "all", name = "Tất cả các Hạng mục")
                    }
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
                // NẾU LÀ SỬA, GIỮ NGUYÊN ID CŨ. NẾU LÀ TẠO MỚI, ID SẼ LÀ RỖNG
                id = existingBudget?.id ?: "",
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
                is Result.Success -> {
                    _uiState.value = BudgetSetupUiState.Success
                }
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