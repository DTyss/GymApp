package com.tys.gymapp.presentation.screens.admin

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditClassScreen(
    onNavigateBack: () -> Unit,
    isEditMode: Boolean = false,
    viewModel: CreateEditClassViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val branches by viewModel.branches.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var capacity by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    var showBranchDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is CreateEditClassUiState.Success) {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is CreateEditClassUiState.Error) {
            snackbarHostState.showSnackbar((uiState as CreateEditClassUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Sửa lớp học" else "Tạo lớp mới") },
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
            // Title
            GymTextField(
                value = title,
                onValueChange = { title = it },
                label = "Tên lớp",
                placeholder = "Yoga buổi sáng",
                leadingIcon = Icons.Default.Class,
                imeAction = ImeAction.Next
            )

            // Description
            GymTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả",
                placeholder = "Mô tả chi tiết về lớp học...",
                leadingIcon = Icons.Default.Description,
                imeAction = ImeAction.Next,
                singleLine = false
            )

            // Branch Dropdown
            ExposedDropdownMenuBox(
                expanded = showBranchDropdown,
                onExpandedChange = { showBranchDropdown = it }
            ) {
                OutlinedTextField(
                    value = branches.find { it.id == selectedBranchId }?.name ?: "Chọn chi nhánh",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chi nhánh") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBranchDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showBranchDropdown,
                    onDismissRequest = { showBranchDropdown = false }
                ) {
                    branches.forEach { branch ->
                        DropdownMenuItem(
                            text = { Text(branch.name) },
                            onClick = {
                                selectedBranchId = branch.id
                                showBranchDropdown = false
                            }
                        )
                    }
                }
            }

            // Capacity
            GymTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = "Sức chứa",
                placeholder = "20",
                leadingIcon = Icons.Default.Groups,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )

            // Date & Time inputs
            Text(
                text = "Thời gian",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            GymTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = "Ngày (dd/MM/yyyy)",
                placeholder = "15/11/2024",
                leadingIcon = Icons.Default.CalendarToday,
                imeAction = ImeAction.Next
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GymTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = "Giờ bắt đầu (HH:mm)",
                    placeholder = "08:00",
                    leadingIcon = Icons.Default.Schedule,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f)
                )

                GymTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = "Giờ kết thúc (HH:mm)",
                    placeholder = "09:30",
                    leadingIcon = Icons.Default.Schedule,
                    imeAction = ImeAction.Done,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            GymButton(
                text = if (isEditMode) "Lưu thay đổi" else "Tạo lớp",
                onClick = {
                    // Format datetime before sending
                    viewModel.title.value = title
                    viewModel.description.value = description
                    viewModel.selectedBranchId.value = selectedBranchId
                    viewModel.capacity.value = capacity
                    // TODO: Format startDate + startTime → ISO datetime
                    // TODO: Format startDate + endTime → ISO datetime
                    viewModel.createClass()
                },
                loading = uiState is CreateEditClassUiState.Loading,
                icon = Icons.Default.Save
            )

            // Cancel button
            GymOutlinedButton(
                text = "Hủy",
                onClick = onNavigateBack,
                enabled = uiState !is CreateEditClassUiState.Loading
            )

            // Note
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Hệ thống sẽ tự động kiểm tra xung đột lịch của huấn luyện viên",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}