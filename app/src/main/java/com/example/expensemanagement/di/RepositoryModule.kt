package com.example.expensemanagement.di

import com.example.expensemanagement.data.repository.Auth.AuthRepository
import com.example.expensemanagement.data.repository.Auth.AuthRepositoryImpl
import com.example.expensemanagement.data.repository.Budget.BudgetRepository
import com.example.expensemanagement.data.repository.Budget.BudgetRepositoryImpl
import com.example.expensemanagement.data.repository.Category.CategoryRepository
import com.example.expensemanagement.data.repository.Category.CategoryRepositoryImpl
import com.example.expensemanagement.data.repository.Security.SecurityRepository
import com.example.expensemanagement.data.repository.Security.SecurityRepositoryImpl
import com.example.expensemanagement.data.repository.Theme.ThemeRepository
import com.example.expensemanagement.data.repository.Theme.ThemeRepositoryImpl
import com.example.expensemanagement.data.repository.WalletRepository
import com.example.expensemanagement.data.repository.WalletRepositoryImpl
import com.example.expensemanagement.data.repository.transaction.TransactionRepository
import com.example.expensemanagement.data.repository.transaction.TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository


    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(
        securityRepositoryImpl: SecurityRepositoryImpl
    ): SecurityRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    // (Chúng ta sẽ thêm TransactionRepository... vào đây sau)
}