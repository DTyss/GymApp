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
import com.tys.gymapp.data.remote.dto.User
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserDetail: (String) -> Unit,
    viewModel: ManageUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val roleFilter by viewModel.roleFilter.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf<User?>(null) }

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

    // Debounce search
    LaunchedEffect(searchQuery) {
        delay(500)
        viewModel.loadUsers()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Người dùng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Lọc")
                    }

                    // Filter menu
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tất cả Role") },
                            onClick = {
                                viewModel.setRoleFilter(null)
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (roleFilter == null) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Member") },
                            onClick = {
                                viewModel.setRoleFilter("member")
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (roleFilter == "member") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Trainer") },
                            onClick = {
                                viewModel.setRoleFilter("trainer")
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (roleFilter == "trainer") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Admin") },
                            onClick = {
                                viewModel.setRoleFilter("admin")
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (roleFilter == "admin") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }

                    // Refresh
                    IconButton(onClick = { viewModel.loadUsers() }) {
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                placeholder = { Text("Tìm kiếm...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = statusFilter == "active",
                    onClick = { viewModel.setStatusFilter("active") },
                    label = { Text("Active") }
                )
                FilterChip(
                    selected = statusFilter == "inactive",
                    onClick = { viewModel.setStatusFilter("inactive") },
                    label = { Text("Inactive") }
                )
                FilterChip(
                    selected = statusFilter == "banned",
                    onClick = { viewModel.setStatusFilter("banned") },
                    label = { Text("Banned") }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Users list
            when (val state = uiState) {
                is ManageUsersUiState.Loading -> {
                    LoadingIndicator()
                }
                is ManageUsersUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = { viewModel.loadUsers() }
                    )
                }
                is ManageUsersUiState.Success -> {
                    if (state.users.isEmpty()) {
                        AdminEmptyState(
                            message = "Không tìm thấy người dùng nào",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            items(
                                items = state.users,
                                key = { it.id }
                            ) { user ->
                                SimpleManageUserCard(
                                    user = user,
                                    onDetail = { onNavigateToUserDetail(user.id) },
                                    onStatusChange = { showStatusDialog = user }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Status change dialog
    showStatusDialog?.let { user ->
        StatusChangeDialog(
            user = user,
            onStatusChange = { status ->
                viewModel.updateUserStatus(user.id, status)
                showStatusDialog = null
            },
            onDismiss = { showStatusDialog = null }
        )
    }
}

@Composable
fun SimpleManageUserCard(
    user: User,
    onDetail: () -> Unit,
    onStatusChange: () -> Unit
) {
    AdminListItemCard(
        title = user.fullName,
        subtitle = buildString {
            append(getRoleName(user.role))
            user.email?.let { append(" • $it") }
        },
        onClick = onDetail,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminBadge(
                    text = getStatusName(user.status),
                    color = when (user.status) {
                        "active" -> MaterialTheme.colorScheme.primary
                        "inactive" -> MaterialTheme.colorScheme.secondary
                        "banned" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                IconButton(
                    onClick = onStatusChange,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Đổi trạng thái",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}


@Composable
fun StatusChangeDialog(
    user: User,
    onStatusChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(user.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi trạng thái") },
        text = {
            Column {
                Text("Người dùng: ${user.fullName}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chọn trạng thái mới:")
                Spacer(modifier = Modifier.height(8.dp))
                RadioButton(
                    selected = selectedStatus == "active",
                    onClick = { selectedStatus = "active" }
                )
                Text("Active")
                RadioButton(
                    selected = selectedStatus == "inactive",
                    onClick = { selectedStatus = "inactive" }
                )
                Text("Inactive")
                RadioButton(
                    selected = selectedStatus == "banned",
                    onClick = { selectedStatus = "banned" }
                )
                Text("Banned")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onStatusChange(selectedStatus) }
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

fun getRoleName(role: String): String {
    return when (role) {
        "member" -> "Hội viên"
        "trainer" -> "Huấn luyện viên"
        "admin" -> "Quản trị viên"
        "receptionist" -> "Lễ tân"
        else -> role
    }
}

fun getStatusName(status: String): String {
    return when (status) {
        "active" -> "Đang hoạt động"
        "inactive" -> "Tạm ngừng"
        "banned" -> "Bị khóa"
        else -> status
    }
}

