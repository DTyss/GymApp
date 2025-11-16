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
import com.tys.gymapp.data.remote.dto.MembershipDetail
import com.tys.gymapp.presentation.components.*
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMembershipsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ManageMembershipsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedMembership by remember { mutableStateOf<MembershipDetail?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var dialogAction by remember { mutableStateOf<MembershipAction?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is MembershipActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is MembershipActionState.Error -> {
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
                title = { Text("Quản lý Memberships") },
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
                            text = { Text("Tất cả") },
                            onClick = {
                                viewModel.setStatusFilter(null)
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (statusFilter == null) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đang hoạt động") },
                            onClick = {
                                viewModel.setStatusFilter("active")
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (statusFilter == "active") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hết hạn") },
                            onClick = {
                                viewModel.setStatusFilter("expired")
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (statusFilter == "expired") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tạm dừng") },
                            onClick = {
                                viewModel.setStatusFilter("paused")
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (statusFilter == "paused") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }

                    // Refresh
                    IconButton(onClick = { viewModel.loadMemberships() }) {
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
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo membership")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ManageMembershipsUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is ManageMembershipsUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadMemberships() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ManageMembershipsUiState.Success -> {
                if (state.memberships.isEmpty()) {
                    EmptyState(
                        message = "Chưa có membership nào",
                        icon = Icons.Default.CardMembership,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.memberships) { membership ->
                            MembershipManagementCard(
                                membership = membership,
                                onDetail = { onNavigateToDetail(membership.id) },
                                onExtend = {
                                    selectedMembership = membership
                                    dialogAction = MembershipAction.EXTEND
                                    showActionDialog = true
                                },
                                onPause = {
                                    selectedMembership = membership
                                    dialogAction = MembershipAction.PAUSE
                                    showActionDialog = true
                                },
                                onResume = {
                                    selectedMembership = membership
                                    dialogAction = MembershipAction.RESUME
                                    showActionDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Action confirmation dialog
    if (showActionDialog && selectedMembership != null && dialogAction != null) {
        MembershipActionDialog(
            membership = selectedMembership!!,
            action = dialogAction!!,
            onConfirm = {
                when (dialogAction) {
                    MembershipAction.PAUSE -> viewModel.pauseMembership(selectedMembership!!.id)
                    MembershipAction.RESUME -> viewModel.resumeMembership(selectedMembership!!.id)
                    MembershipAction.EXTEND -> {
                        // Navigate to extend screen
                        onNavigateToDetail(selectedMembership!!.id)
                    }
                    else -> {}
                }
                showActionDialog = false
                selectedMembership = null
                dialogAction = null
            },
            onDismiss = {
                showActionDialog = false
                selectedMembership = null
                dialogAction = null
            }
        )
    }
}

@Composable
fun MembershipManagementCard(
    membership: MembershipDetail,
    onDetail: () -> Unit,
    onExtend: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onDetail
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = membership.user.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = membership.plan.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Status badge
                Surface(
                    color = when (membership.status) {
                        "active" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        "expired" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        "paused" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (membership.status) {
                            "active" -> "Hoạt động"
                            "expired" -> "Hết hạn"
                            "paused" -> "Tạm dừng"
                            else -> membership.status
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (membership.status) {
                            "active" -> MaterialTheme.colorScheme.primary
                            "expired" -> MaterialTheme.colorScheme.error
                            "paused" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info rows
            MembershipInfoRow(
                icon = Icons.Default.Event,
                label = "Hạn sử dụng",
                value = formatDate(membership.endDate)
            )
            MembershipInfoRow(
                icon = Icons.Default.Loop,
                label = "Còn lại",
                value = "${membership.remainingSessions} buổi"
            )
            MembershipInfoRow(
                icon = Icons.Default.AttachMoney,
                label = "Giá gói",
                value = formatPrice(membership.plan.price)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Extend button (always show)
                OutlinedButton(
                    onClick = onExtend,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Update,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gia hạn")
                }

                // Pause/Resume button (based on status)
                when (membership.status) {
                    "active" -> {
                        OutlinedButton(
                            onClick = onPause,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tạm dừng")
                        }
                    }
                    "paused" -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kích hoạt")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MembershipInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MembershipActionDialog(
    membership: MembershipDetail,
    action: MembershipAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (action) {
                    MembershipAction.PAUSE -> "Tạm dừng membership"
                    MembershipAction.RESUME -> "Kích hoạt lại"
                    MembershipAction.EXTEND -> "Gia hạn"
                    else -> "Xác nhận"
                }
            )
        },
        text = {
            Text(
                when (action) {
                    MembershipAction.PAUSE ->
                        "Tạm dừng membership của ${membership.user.fullName}?"
                    MembershipAction.RESUME ->
                        "Kích hoạt lại membership của ${membership.user.fullName}?"
                    MembershipAction.EXTEND ->
                        "Mở trang gia hạn membership?"
                    else -> "Xác nhận thao tác?"
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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

enum class MembershipAction {
    EXTEND, PAUSE, RESUME
}

fun formatDate(dateString: String): String {
    return try {
        val date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        dateString
    }
}

fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(price)} VNĐ"
}