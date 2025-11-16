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
import com.tys.gymapp.data.remote.dto.Branch
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBranchesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateBranch: () -> Unit,
    onNavigateToEditBranch: (Branch) -> Unit,
    viewModel: ManageBranchesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Branch?>(null) }

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
                title = { Text("Quản lý Chi nhánh") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadBranches() }) {
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
                onClick = onNavigateToCreateBranch,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo chi nhánh mới")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ManageBranchesUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is ManageBranchesUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadBranches() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ManageBranchesUiState.Success -> {
                if (state.branches.isEmpty()) {
                    AdminEmptyState(
                        message = "Chưa có chi nhánh nào",
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(
                            items = state.branches,
                            key = { it.id }
                        ) { branch ->
                            SimpleManageBranchCard(
                                branch = branch,
                                onEdit = { onNavigateToEditBranch(branch) },
                                onDelete = { showDeleteDialog = branch }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { branch ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa chi nhánh \"${branch.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBranch(branch.id)
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
fun SimpleManageBranchCard(
    branch: Branch,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdminListItemCard(
        title = branch.name,
        subtitle = branch.address ?: "Chưa có địa chỉ",
        onClick = onEdit,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminBadge(
                    text = if (branch.isActive) "Hoạt động" else "Ngừng",
                    color = if (branch.isActive) {
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

