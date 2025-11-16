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
    viewModel: CreateEditClassViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val isEditMode = viewModel.editMode

    var title by remember { mutableStateOf(viewModel.title.value) }
    var description by remember { mutableStateOf(viewModel.description.value) }
    var selectedBranchId by remember { mutableStateOf<String?>(viewModel.selectedBranchId.value) }
    var capacity by remember { mutableStateOf(viewModel.capacity.value) }
    
    // Parse datetime from ViewModel
    val startDateTime = viewModel.startDateTime.value
    val endDateTime = viewModel.endDateTime.value
    
    var startDate by remember { 
        mutableStateOf(
            if (startDateTime.isNotBlank()) {
                startDateTime.split("T")[0].split("-").reversed().joinToString("/")
            } else ""
        )
    }
    var startTime by remember { 
        mutableStateOf(
            if (startDateTime.isNotBlank() && "T" in startDateTime) {
                startDateTime.split("T")[1].substringBefore(":")
            } else ""
        )
    }
    var endTime by remember { 
        mutableStateOf(
            if (endDateTime.isNotBlank() && "T" in endDateTime) {
                endDateTime.split("T")[1].substringBefore(":")
            } else ""
        )
    }

    var showBranchDropdown by remember { mutableStateOf(false) }

    // Sync with ViewModel when it changes
    LaunchedEffect(viewModel.title.value) { title = viewModel.title.value }
    LaunchedEffect(viewModel.description.value) { description = viewModel.description.value }
    LaunchedEffect(viewModel.selectedBranchId.value) { selectedBranchId = viewModel.selectedBranchId.value }
    LaunchedEffect(viewModel.capacity.value) { capacity = viewModel.capacity.value }

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
                    viewModel.title.value = title
                    viewModel.description.value = description
                    viewModel.selectedBranchId.value = selectedBranchId
                    viewModel.capacity.value = capacity
                    
                    // Format datetime: combine date and time
                    val startDateTimeFormatted = if (startDate.isNotBlank() && startTime.isNotBlank()) {
                        // Convert dd/MM/yyyy to yyyy-MM-dd
                        val dateParts = startDate.split("/")
                        if (dateParts.size == 3) {
                            "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${startTime.padStart(5, '0')}:00"
                        } else ""
                    } else ""
                    
                    val endDateTimeFormatted = if (startDate.isNotBlank() && endTime.isNotBlank()) {
                        val dateParts = startDate.split("/")
                        if (dateParts.size == 3) {
                            "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${endTime.padStart(5, '0')}:00"
                        } else ""
                    } else ""
                    
                    viewModel.startDateTime.value = startDateTimeFormatted
                    viewModel.endDateTime.value = endDateTimeFormatted
                    
                    if (isEditMode) {
                        viewModel.updateClass()
                    } else {
                        viewModel.createClass()
                    }
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