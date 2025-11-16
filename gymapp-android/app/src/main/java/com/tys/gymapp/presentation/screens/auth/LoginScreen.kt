package com.tys.gymapp.presentation.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.R
import com.tys.gymapp.presentation.components.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    // Handle success navigation
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onNavigateToMain()
            viewModel.resetState()
        }
    }

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Error) {
            snackbarHostState.showSnackbar((loginState as LoginState.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo & Title
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "GYM APP",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Quản lý tập luyện hiệu quả",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email/Phone TextField
            GymTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = "Email hoặc Số điện thoại",
                placeholder = "user@example.com hoặc 0912345678",
                leadingIcon = Icons.Default.Person,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            GymPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                placeholder = "Nhập mật khẩu",
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            GymButton(
                text = "Đăng nhập",
                onClick = { viewModel.login(emailOrPhone, password) },
                loading = loginState is LoginState.Loading,
                icon = Icons.Default.Login
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chưa có tài khoản?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Đăng ký ngay",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}