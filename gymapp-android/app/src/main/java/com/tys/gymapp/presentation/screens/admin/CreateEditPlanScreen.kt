package com.tys.gymapp.presentation.screens.admin

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPlanScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateEditPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = viewModel.editMode

    var name by remember { mutableStateOf(viewModel.name.value) }
    var price by remember { mutableStateOf(viewModel.price.value) }
    var sessions by remember { mutableStateOf(viewModel.sessions.value) }
    var durationDays by remember { mutableStateOf(viewModel.durationDays.value) }
    var isActive by remember { mutableStateOf(viewModel.isActive.value) }

    // Sync with ViewModel when it changes
    LaunchedEffect(viewModel.name.value) { name = viewModel.name.value }
    LaunchedEffect(viewModel.price.value) { price = viewModel.price.value }
    LaunchedEffect(viewModel.sessions.value) { sessions = viewModel.sessions.value }
    LaunchedEffect(viewModel.durationDays.value) { durationDays = viewModel.durationDays.value }
    LaunchedEffect(viewModel.isActive.value) { isActive = viewModel.isActive.value }

    LaunchedEffect(uiState) {
        if (uiState is CreateEditPlanUiState.Success) {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is CreateEditPlanUiState.Error) {
            snackbarHostState.showSnackbar((uiState as CreateEditPlanUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Sửa gói tập" else "Tạo gói mới") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            GymTextField(
                value = name,
                onValueChange = { name = it },
                label = "Tên gói tập",
                placeholder = "Gói 1 tháng",
                leadingIcon = Icons.Default.CardMembership,
                imeAction = ImeAction.Next
            )

            // Price
            GymTextField(
                value = price,
                onValueChange = { price = it },
                label = "Giá (VNĐ)",
                placeholder = "1000000",
                leadingIcon = Icons.Default.AttachMoney,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )

            // Sessions
            GymTextField(
                value = sessions,
                onValueChange = { sessions = it },
                label = "Số buổi tập",
                placeholder = "30",
                leadingIcon = Icons.Default.FitnessCenter,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )

            // Duration Days
            GymTextField(
                value = durationDays,
                onValueChange = { durationDays = it },
                label = "Thời hạn (ngày)",
                placeholder = "30",
                leadingIcon = Icons.Default.Event,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )

            // Is Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đang bán",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            GymButton(
                text = if (isEditMode) "Lưu thay đổi" else "Tạo gói",
                onClick = {
                    viewModel.name.value = name
                    viewModel.price.value = price
                    viewModel.sessions.value = sessions
                    viewModel.durationDays.value = durationDays
                    viewModel.isActive.value = isActive
                    if (isEditMode && viewModel.planId != null) {
                        viewModel.updatePlan(viewModel.planId!!)
                    } else {
                        viewModel.createPlan()
                    }
                },
                loading = uiState is CreateEditPlanUiState.Loading,
                icon = Icons.Default.Save
            )

            // Cancel button
            GymOutlinedButton(
                text = "Hủy",
                onClick = onNavigateBack,
                enabled = uiState !is CreateEditPlanUiState.Loading
            )
        }
    }
}

