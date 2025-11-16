package com.tys.gymapp.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendMembershipScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExtendMembershipViewModel = hiltViewModel()
) {
    val membership by viewModel.membership.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var additionalDays by remember { mutableStateOf("") }
    var additionalSessions by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is ExtendMembershipUiState.Success) {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is ExtendMembershipUiState.Error) {
            snackbarHostState.showSnackbar((uiState as ExtendMembershipUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gia hạn Membership") },
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
        if (membership == null) {
            LoadingIndicator(Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current membership info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Thông tin hiện tại",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        MembershipCurrentInfoRow(
                            icon = Icons.Default.Person,
                            label = "Hội viên",
                            value = membership!!.user.fullName
                        )
                        MembershipCurrentInfoRow(
                            icon = Icons.Default.CardMembership,
                            label = "Gói",
                            value = membership!!.plan.name
                        )
                        MembershipCurrentInfoRow(
                            icon = Icons.Default.Event,
                            label = "Hạn sử dụng",
                            value = formatDate(membership!!.endDate)
                        )
                        MembershipCurrentInfoRow(
                            icon = Icons.Default.Loop,
                            label = "Còn lại",
                            value = "${membership!!.remainingSessions} buổi"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Extend form
                Text(
                    text = "Thông tin gia hạn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Nhập số ngày và/hoặc số buổi muốn thêm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Additional days
                GymTextField(
                    value = additionalDays,
                    onValueChange = { additionalDays = it },
                    label = "Thêm số ngày",
                    placeholder = "30",
                    leadingIcon = Icons.Default.Event,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )

                // Additional sessions
                GymTextField(
                    value = additionalSessions,
                    onValueChange = { additionalSessions = it },
                    label = "Thêm số buổi",
                    placeholder = "10",
                    leadingIcon = Icons.Default.Loop,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )

                // Preview
                if (additionalDays.isNotBlank() || additionalSessions.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Sau khi gia hạn",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (additionalDays.isNotBlank()) {
                                val days = additionalDays.toIntOrNull() ?: 0
                                Text(
                                    text = "• Thời hạn: +$days ngày",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            if (additionalSessions.isNotBlank()) {
                                val sessions = additionalSessions.toIntOrNull() ?: 0
                                val newTotal = membership!!.remainingSessions + sessions
                                Text(
                                    text = "• Số buổi: ${membership!!.remainingSessions} → $newTotal buổi",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Extend button
                GymButton(
                    text = "Gia hạn",
                    onClick = {
                        viewModel.extendMembership(
                            additionalDays = additionalDays.toIntOrNull(),
                            additionalSessions = additionalSessions.toIntOrNull()
                        )
                    },
                    loading = uiState is ExtendMembershipUiState.Loading,
                    icon = Icons.Default.Update,
                    enabled = additionalDays.isNotBlank() || additionalSessions.isNotBlank()
                )

                // Cancel button
                GymOutlinedButton(
                    text = "Hủy",
                    onClick = onNavigateBack,
                    enabled = uiState !is ExtendMembershipUiState.Loading
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
                            text = "Nếu membership đang 'expired', gia hạn ngày sẽ tự động kích hoạt lại membership.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MembershipCurrentInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

fun formatDate(dateString: String): String {
    return try {
        val date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        dateString
    }
}