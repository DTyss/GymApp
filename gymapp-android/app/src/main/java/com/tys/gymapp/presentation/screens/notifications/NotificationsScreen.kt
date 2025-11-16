package com.tys.gymapp.presentation.screens.notifications

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
import com.tys.gymapp.data.remote.dto.Notification
import com.tys.gymapp.presentation.components.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo") },
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Đánh dấu tất cả đã đọc"
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
            is NotificationsUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is NotificationsUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadNotifications() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is NotificationsUiState.Success -> {
                if (state.notifications.isEmpty()) {
                    EmptyState(
                        message = "Chưa có thông báo nào",
                        icon = Icons.Default.Notifications,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onMarkAsRead = { viewModel.markAsRead(notification.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onMarkAsRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            }
        ),
        onClick = if (!notification.isRead) onMarkAsRead else ({})
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Icon(
                imageVector = if (notification.isRead) {
                    Icons.Default.NotificationsNone
                } else {
                    Icons.Default.Notifications
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (notification.isRead) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) {
                        FontWeight.Normal
                    } else {
                        FontWeight.Bold
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Body
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatNotificationTime(notification.sentAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Unread indicator
            if (!notification.isRead) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
            }
        }
    }
}

fun formatNotificationTime(dateString: String): String {
    return try {
        val date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        dateString
    }
}