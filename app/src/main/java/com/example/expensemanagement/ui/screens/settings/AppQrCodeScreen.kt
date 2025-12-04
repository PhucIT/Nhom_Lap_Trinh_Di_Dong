package com.example.expensemanagement.ui.screens.settings

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppQrCodeScreen(
    onNavigateBack: () -> Unit
) {
    // ĐÂY LÀ LIÊN KẾT BẠN MUỐN CHIA SẺ
    val appDownloadLink = "https://drive.google.com/file/d/1lavgsp8bGTFsKmbpz30nelX_T5wsnL5z/view?usp=drive_link"

    // Trạng thái chứa ảnh QR
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Tạo mã QR trong background thread để không làm đơ UI
    LaunchedEffect(appDownloadLink) {
        withContext(Dispatchers.IO) {
            val bitmap = generateAppQR(appDownloadLink)
            withContext(Dispatchers.Main) {
                qrBitmap = bitmap
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chia sẻ ứng dụng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
//        containerColor = Color(0xFFE0F7FA) // Nền xanh nhạt
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
//                colors = CardDefaults.cardColors(containerColor = Color.White),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Icon Share trang trí
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
//                        tint = Color(0xFF00695C)
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Tải Ứng Dụng Ngay",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Quét mã QR bên dưới để tải và cài đặt ứng dụng Quản Lý Chi Tiêu",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
//                        color = Color.Gray
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Hiển thị mã QR
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Mã QR tải ứng dụng",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(260.dp) // Kích thước mã QR
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .padding(16.dp)
                        )
                    } else {
                        // Hiển thị loading khi đang tạo mã
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .background(
//                                    Color.LightGray,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // Hiển thị link text bên dưới (tùy chọn)
                    Text(
                        text = "hoặc truy cập: $appDownloadLink",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Hàm tạo Bitmap QR Code sử dụng thư viện ZXing
fun generateAppQR(content: String): Bitmap? {
    return try {
        val size = 512 // Độ phân giải của ảnh QR
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                // Màu đen (0xFF000000) cho điểm đen, Màu trắng (0xFFFFFFFF) cho nền
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}