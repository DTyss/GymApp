package com.tys.gymapp.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Admin") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
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
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is DashboardUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadDashboard() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is DashboardUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "Tổng quan hệ thống",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Users Stats
                    StatCard(
                        title = "Người dùng",
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        StatRow("Tổng số", state.stats.users.total.toString())
                        StatRow("Hội viên", state.stats.users.members.toString())
                        StatRow("Đang hoạt động", state.stats.users.activeMembers.toString())
                    }

                    // Classes Stats
                    StatCard(
                        title = "Lớp học",
                        icon = Icons.Default.Class,
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        StatRow("Tổng số lớp", state.stats.classes.total.toString())
                        StatRow("Lớp hôm nay", state.stats.classes.today.toString())
                    }

                    // Check-ins Stats
                    StatCard(
                        title = "Check-in",
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.tertiary
                    ) {
                        StatRow("Tổng số", state.stats.checkins.total.toString())
                        StatRow("Hôm nay", state.stats.checkins.today.toString())
                        StatRow("Tháng này", state.stats.checkins.thisMonth.toString())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bookings
                        StatCard(
                            title = "Bookings",
                            icon = Icons.Default.BookOnline,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        ) {
                            StatRow("Tổng số", state.stats.bookings.total.toString())
                        }

                        // Memberships
                        StatCard(
                            title = "Memberships",
                            icon = Icons.Default.CardMembership,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        ) {
                            StatRow("Đang active", state.stats.memberships.active.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}