package com.tys.gymapp.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            onNavigateToMain()
            viewModel.resetState()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Error) {
            snackbarHostState.showSnackbar((registerState as RegisterState.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Đăng ký tài khoản") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tạo tài khoản mới",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Full Name
            GymTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Họ và tên",
                placeholder = "Nguyễn Văn A",
                leadingIcon = Icons.Default.Badge,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email/Phone
            GymTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = "Email hoặc Số điện thoại",
                placeholder = "user@example.com hoặc 0912345678",
                leadingIcon = Icons.Default.ContactPhone,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            GymPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                placeholder = "Ít nhất 6 ký tự",
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            GymPasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Xác nhận mật khẩu",
                placeholder = "Nhập lại mật khẩu",
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            GymButton(
                text = "Đăng ký",
                onClick = {
                    viewModel.register(emailOrPhone, fullName, password, confirmPassword)
                },
                loading = registerState is RegisterState.Loading,
                icon = Icons.Default.AppRegistration
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã có tài khoản?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateBack) {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}