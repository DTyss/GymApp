package com.tys.gymapp.presentation.screens.home

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.rememberPermissionState
import com.tys.gymapp.data.remote.dto.Membership
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.GradientEnd
import com.tys.gymapp.presentation.theme.GradientStart
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.Manifest
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlans: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )

        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted &&
                !notificationPermissionState.status.shouldShowRationale) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang chủ") },
                actions = {
                    IconButton(onClick = onNavigateToPlans) {
                        Icon(
                            imageVector = Icons.Default.CardMembership,
                            contentDescription = "Xem gói tập"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is HomeUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadData() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Welcome Card
                    item {
                        WelcomeCard(userName = state.user.fullName)
                    }

                    // XEM GÓI TẬP
                    item {
                        QuickActionCard(
                            onViewPlans = onNavigateToPlans
                        )
                    }

                    // Active Memberships
                    item {
                        Text(
                            text = "Gói tập của bạn",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val activeMemberships = state.memberships.filter { it.status == "active" }
                    if (activeMemberships.isEmpty()) {
                        item {
                            EmptyState(
                                message = "Bạn chưa có gói tập nào",
                                icon = Icons.Default.CardMembership
                            )
                        }
                    } else {
                        items(activeMemberships) { membership ->
                            MembershipCard(membership = membership)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(userName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = "Xin chào,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.CenterEnd),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun QuickActionCard(onViewPlans: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        onClick = onViewPlans
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Khám phá các gói tập",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Xem các gói tập phù hợp với bạn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun MembershipCard(membership: Membership) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Plan Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = membership.plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Status Badge
                Surface(
                    color = when (membership.status) {
                        "active" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (membership.status) {
                            "active" -> "Đang hoạt động"
                            "expired" -> "Hết hạn"
                            "paused" -> "Tạm dừng"
                            else -> membership.status
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (membership.status) {
                            "active" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info Rows
            MembershipInfoRow(
                icon = Icons.Default.Event,
                label = "Hạn sử dụng",
                value = formatDate(membership.endDate)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MembershipInfoRow(
                icon = Icons.Default.Loop,
                label = "Số buổi còn lại",
                value = "${membership.remainingSessions} buổi"
            )
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
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