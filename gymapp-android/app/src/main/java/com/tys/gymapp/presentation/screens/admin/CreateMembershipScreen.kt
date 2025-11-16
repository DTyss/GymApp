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
import com.tys.gymapp.presentation.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMembershipScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateMembershipViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val plans by viewModel.plans.collectAsState()

    var userId by remember { mutableStateOf("") }
    var selectedPlanId by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf("") }
    var showPlanDropdown by remember { mutableStateOf(false) }

    // Handle success
    LaunchedEffect(uiState) {
        if (uiState is CreateMembershipUiState.Success) {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is CreateMembershipUiState.Error) {
            snackbarHostState.showSnackbar((uiState as CreateMembershipUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tạo Membership") },
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
            // Info text
            Text(
                text = "Tạo membership mới cho hội viên",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User ID input
            GymTextField(
                value = userId,
                onValueChange = { userId = it },
                label = "User ID",
                placeholder = "Nhập ID của user (ví dụ: 123)",
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )

            // Plan dropdown
            Text(
                text = "Chọn gói tập",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            ExposedDropdownMenuBox(
                expanded = showPlanDropdown,
                onExpandedChange = { showPlanDropdown = it }
            ) {
                OutlinedTextField(
                    value = plans.find { it.id == selectedPlanId }?.name ?: "Chọn gói",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gói tập") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPlanDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showPlanDropdown,
                    onDismissRequest = { showPlanDropdown = false }
                ) {
                    plans.forEach { plan ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${formatPrice(plan.price)} - ${plan.sessions} buổi - ${plan.durationDays} ngày",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            onClick = {
                                selectedPlanId = plan.id
                                showPlanDropdown = false
                            }
                        )
                    }
                }
            }

            // Show plan details if selected
            selectedPlanId?.let { planId ->
                plans.find { it.id == planId }?.let { plan ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Chi tiết gói",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PlanDetailRow("Giá", formatPrice(plan.price))
                            PlanDetailRow("Số buổi", "${plan.sessions} buổi")
                            PlanDetailRow("Thời hạn", "${plan.durationDays} ngày")
                            PlanDetailRow(
                                "Giá/buổi",
                                formatPrice(plan.price / plan.sessions)
                            )
                        }
                    }
                }
            }

            // Start date (optional)
            GymTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = "Ngày bắt đầu (tùy chọn)",
                placeholder = "yyyy-MM-dd (để trống = hôm nay)",
                leadingIcon = Icons.Default.CalendarToday,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Create button
            GymButton(
                text = "Tạo Membership",
                onClick = {
                    viewModel.createMembership(
                        userId = userId,
                        planId = selectedPlanId ?: "",
                        startDate = startDate.ifBlank { null }
                    )
                },
                loading = uiState is CreateMembershipUiState.Loading,
                icon = Icons.Default.Save,
                enabled = userId.isNotBlank() && selectedPlanId != null
            )

            // Cancel button
            GymOutlinedButton(
                text = "Hủy",
                onClick = onNavigateBack,
                enabled = uiState !is CreateMembershipUiState.Loading
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
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Membership sẽ được tạo với trạng thái 'active'. Thời hạn tự động tính từ ngày bắt đầu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
