
package com.example.expensemanagement.ui.screens.settings

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.viewmodel.AuthViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyQrCodeScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUserInfo.collectAsStateWithLifecycle()

    // Hàm tạo QR bằng ZXing – chắc chắn chạy ngon
    fun generateQRBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mã QR của tôi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFE0F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Avatar
                    Surface(shape = CircleShape, color = Color(0xFF00695C), modifier = Modifier.size(90.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentUser?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = currentUser?.displayName ?: "Người dùng",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Quét mã để kết nối hoặc chia sẻ chi tiêu cùng tôi",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    // QR CODE – ZXING 100% KHÔNG LỖI
                    val qrContent = currentUser?.uid ?: currentUser?.email ?: ""

                    if (qrContent.isNotBlank()) {
                        val qrBitmap by remember(qrContent) {
                            mutableStateOf(generateQRBitmap(qrContent, 600))
                        }

                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "My QR Code",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(260.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .padding(20.dp)
                            )
                        } else {
                            Text("Lỗi tạo QR", color = Color.Red)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .background(Color.LightGray, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không có dữ liệu", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}