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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditBranchScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateEditBranchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = viewModel.editMode

    var name by remember { mutableStateOf(viewModel.name.value) }
    var address by remember { mutableStateOf(viewModel.address.value) }
    var isActive by remember { mutableStateOf(viewModel.isActive.value) }

    // Sync with ViewModel when it changes
    LaunchedEffect(viewModel.name.value) { name = viewModel.name.value }
    LaunchedEffect(viewModel.address.value) { address = viewModel.address.value }
    LaunchedEffect(viewModel.isActive.value) { isActive = viewModel.isActive.value }

    LaunchedEffect(uiState) {
        if (uiState is CreateEditBranchUiState.Success) {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is CreateEditBranchUiState.Error) {
            snackbarHostState.showSnackbar((uiState as CreateEditBranchUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Sửa chi nhánh" else "Tạo chi nhánh mới") },
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
                label = "Tên chi nhánh",
                placeholder = "Chi nhánh Quận 1",
                leadingIcon = Icons.Default.Store,
                imeAction = ImeAction.Next
            )

            // Address
            GymTextField(
                value = address,
                onValueChange = { address = it },
                label = "Địa chỉ",
                placeholder = "123 Đường ABC, Quận 1, TP.HCM",
                leadingIcon = Icons.Default.LocationOn,
                imeAction = ImeAction.Done,
                singleLine = false
            )

            // Is Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đang hoạt động",
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
                text = if (isEditMode) "Lưu thay đổi" else "Tạo chi nhánh",
                onClick = {
                    viewModel.name.value = name
                    viewModel.address.value = address
                    viewModel.isActive.value = isActive
                    if (isEditMode && viewModel.branchId != null) {
                        viewModel.updateBranch(viewModel.branchId!!)
                    } else {
                        viewModel.createBranch()
                    }
                },
                loading = uiState is CreateEditBranchUiState.Loading,
                icon = Icons.Default.Save
            )

            // Cancel button
            GymOutlinedButton(
                text = "Hủy",
                onClick = onNavigateBack,
                enabled = uiState !is CreateEditBranchUiState.Loading
            )
        }
    }
}

