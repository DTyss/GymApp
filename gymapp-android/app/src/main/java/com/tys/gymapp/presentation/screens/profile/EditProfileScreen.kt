package com.tys.gymapp.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Load initial values
    LaunchedEffect(uiState) {
        if (uiState is EditProfileUiState.Success) {
            val user = (uiState as EditProfileUiState.Success).user
            fullName = user.fullName
            email = user.email ?: ""
            phone = user.phone ?: ""
        }
    }

    // Handle update success
    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Success) {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Error) {
            snackbarHostState.showSnackbar((updateState as UpdateState.Error).message)
            viewModel.resetUpdateState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is EditProfileUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is EditProfileUiState.Error -> {
                ErrorMessage(
                    message = (uiState as EditProfileUiState.Error).message,
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is EditProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Info text
                    AnimatedVisibilityWithFade(visible = true) {
                        Text(
                            text = "Cập nhật thông tin cá nhân của bạn",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    // Full Name
                    EnhancedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Họ và tên",
                        placeholder = "Nguyễn Văn A",
                        leadingIcon = Icons.Default.Person,
                        imeAction = ImeAction.Next
                    )

                    // Email
                    EnhancedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        placeholder = "email@example.com",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )

                    // Phone
                    EnhancedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Số điện thoại",
                        placeholder = "0912345678",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    // Save Button
                    EnhancedGymButton(
                        text = "Lưu thay đổi",
                        onClick = {
                            viewModel.updateProfile(
                                fullName = fullName,
                                email = email.ifBlank { null },
                                phone = phone.ifBlank { null }
                            )
                        },
                        loading = updateState is UpdateState.Loading,
                        icon = Icons.Default.Save,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Cancel Button
                    EnhancedGymButton(
                        text = "Hủy",
                        onClick = onNavigateBack,
                        enabled = updateState !is UpdateState.Loading,
                        variant = ButtonVariant.Outlined,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}