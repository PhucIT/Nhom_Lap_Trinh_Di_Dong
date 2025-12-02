package com.example.expensemanagement.data.model

data class WalletGroupInfo(
    val groupName: String,
    val wallets: List<Wallet>,
    val totalBalance: Double,
    val totalSpentThisMonth: Double // Thêm trường này để chứa tổng chi tiêu đã tính
)