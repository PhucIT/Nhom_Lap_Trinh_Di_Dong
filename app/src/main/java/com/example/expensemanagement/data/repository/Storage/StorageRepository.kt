package com.example.expensemanagement.data.repository.Storage

import android.net.Uri
import com.example.expensemanagement.data.Result

interface StorageRepository {
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<Uri>
}