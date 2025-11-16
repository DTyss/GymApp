package com.tys.gymapp.presentation.screens.checkin

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
import com.tys.gymapp.data.remote.dto.Checkin
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing
import com.tys.gymapp.presentation.theme.Elevation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheckinHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử Check-in") },
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
        when (val state = uiState) {
            is CheckinHistoryUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(Spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(5) {
                        ShimmerCard()
                    }
                }
            }
            is CheckinHistoryUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadCheckins() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is CheckinHistoryUiState.Success -> {
                if (state.checkins.isEmpty()) {
                    EnhancedEmptyState(
                        message = "Chưa có lịch sử check-in",
                        icon = Icons.Default.History,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(Spacing.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        // Summary card
                        item {
                            AnimatedVisibilityWithFade(visible = true) {
                                EnhancedCheckinSummaryCard(totalCheckins = state.checkins.size)
                            }
                        }

                        // Checkin items with timeline
                        items(
                            items = state.checkins,
                            key = { it.id }
                        ) { checkin ->
                            AnimatedVisibilityWithFade(visible = true) {
                                EnhancedCheckinHistoryCard(checkin = checkin)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedCheckinSummaryCard(totalCheckins: Int) {
    GradientCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Tổng số lần check-in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Text(
                    text = "$totalCheckins lần",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(Spacing.xxxl),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EnhancedCheckinHistoryCard(checkin: Checkin) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = Elevation.level1
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with status
            Surface(
                modifier = Modifier.size(Spacing.avatarSize),
                shape = RoundedCornerShape(8.dp),
                color = if (checkin.status == "success") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (checkin.status == "success") {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Error
                        },
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.iconSize),
                        tint = if (checkin.status == "success") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatCheckinDateTime(checkin.checkedAt),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Branch
                if (checkin.branch != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.iconSizeSmall),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = checkin.branch.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Method
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = if (checkin.method == "qr") {
                            Icons.Default.QrCode2
                        } else {
                            Icons.Default.TouchApp
                        },
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.iconSizeSmall),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (checkin.method == "qr") "QR Code" else "Thủ công",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Status badge
            Surface(
                color = if (checkin.status == "success") {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (checkin.status == "success") "Thành công" else "Thất bại",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (checkin.status == "success") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun formatCheckinDateTime(dateString: String): String {
    return try {
        val date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        dateString
    }
}