package com.example.expensemanagement.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanagement.viewmodel.PinScreenMode
import com.example.expensemanagement.viewmodel.PinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinInputScreen(
    mode: PinScreenMode,
    onNavigateBack: () -> Unit,
    onPinSuccess: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mode) {
        viewModel.setMode(mode)
    }

    LaunchedEffect(uiState.isPinCorrect) {
        if (uiState.isPinCorrect == true) {
            onPinSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            PinDots(
                pinLength = uiState.pinLength,
                enteredCount = uiState.enteredPin.length
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.weight(1f))

            PinNumpad(
                onDigitClick = viewModel::onPinDigitInput,
                onBackspaceClick = viewModel::onPinBackspace
            )
        }
    }
}

@Composable
private fun PinDots(pinLength: Int, enteredCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(pinLength) { index ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (index < enteredCount) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PinNumpad(onDigitClick: (String) -> Unit, onBackspaceClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        (1..9).chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { number ->
                    NumberButton(number.toString()) { onDigitClick(number.toString()) }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumberButton("0") { onDigitClick("0") }
            IconButton(onClick = onBackspaceClick, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Backspace, contentDescription = "Xóa", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun RowScope.NumberButton(number: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f),
        shape = CircleShape
    ) {
        Text(number, fontSize = 28.sp, fontWeight = FontWeight.Normal)
    }
}