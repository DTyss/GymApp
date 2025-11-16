package com.tys.gymapp.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.data.remote.dto.Plan
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing
import com.tys.gymapp.presentation.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePlansScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToEditPlan: (Plan) -> Unit,
    viewModel: ManagePlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Plan?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is ActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Gói tập") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPlans() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePlan,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo gói mới")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ManagePlansUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is ManagePlansUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadPlans() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ManagePlansUiState.Success -> {
                if (state.plans.isEmpty()) {
                    AdminEmptyState(
                        message = "Chưa có gói tập nào",
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(
                            items = state.plans,
                            key = { it.id }
                        ) { plan ->
                            SimpleManagePlanCard(
                                plan = plan,
                                onEdit = { onNavigateToEditPlan(plan) },
                                onDelete = { showDeleteDialog = plan }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { plan ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa gói \"${plan.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlan(plan.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun SimpleManagePlanCard(
    plan: Plan,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdminListItemCard(
        title = plan.name,
        subtitle = "${formatPrice(plan.price)} • ${plan.durationDays} ngày • ${plan.sessions} buổi",
        onClick = onEdit,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminBadge(
                    text = if (plan.isActive) "Đang bán" else "Ngừng bán",
                    color = if (plan.isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}


