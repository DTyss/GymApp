package com.tys.gymapp.presentation.screens.classes

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
import com.tys.gymapp.data.remote.dto.ClassItem
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing
import com.tys.gymapp.presentation.theme.Elevation
import androidx.compose.foundation.lazy.rememberLazyListState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    viewModel: ClassesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bookingState by viewModel.bookingState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookingState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetBookingState()
            }
            is BookingState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetBookingState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Lịch lớp") },
                actions = {
                    IconButton(onClick = { viewModel.loadClasses() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ClassesUiState.Loading -> {
                    if (state.classes.isEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(3) {
                                ShimmerCard()
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(
                                items = state.classes,
                                key = { it.id }
                            ) { classItem ->
                                AnimatedVisibilityWithFade(visible = true) {
                                    EnhancedClassCard(
                                        classItem = classItem,
                                        onBookClick = { viewModel.bookClass(classItem.id) },
                                        isBooking = bookingState is BookingState.Loading
                                    )
                                }
                            }
                        }
                    }
                }
                is ClassesUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = { viewModel.loadClasses() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is ClassesUiState.Success -> {
                    if (state.classes.isEmpty()) {
                        EnhancedEmptyState(
                            message = "Chưa có lớp học nào",
                            icon = Icons.Default.Event,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(
                                items = state.classes,
                                key = { it.id }
                            ) { classItem ->
                                AnimatedVisibilityWithFade(visible = true) {
                                    EnhancedClassCard(
                                        classItem = classItem,
                                        onBookClick = { viewModel.bookClass(classItem.id) },
                                        isBooking = bookingState is BookingState.Loading
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedClassCard(
    classItem: ClassItem,
    onBookClick: () -> Unit,
    isBooking: Boolean
) {
    val availabilityRatio = if (classItem.capacity > 0) {
        classItem.available.toFloat() / classItem.capacity.toFloat()
    } else 0f

    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = Elevation.level1
    ) {
        // Title
        Text(
            text = classItem.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Description
        if (classItem.description != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = classItem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Info rows
        ClassInfoRow(
            icon = Icons.Default.Person,
            text = "HLV: ${classItem.trainer.fullName}"
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        ClassInfoRow(
            icon = Icons.Default.LocationOn,
            text = "Chi nhánh: ${classItem.branch.name}"
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        ClassInfoRow(
            icon = Icons.Default.Schedule,
            text = formatDateTime(classItem.startTime)
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Availability with progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClassInfoRow(
                icon = Icons.Default.Groups,
                text = "Còn ${classItem.available}/${classItem.capacity} chỗ"
            )
            Surface(
                color = when {
                    availabilityRatio > 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    availabilityRatio > 0.2f -> MaterialTheme.colorScheme.warning.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when {
                        classItem.available == 0 -> "Hết chỗ"
                        availabilityRatio < 0.2f -> "Sắp hết"
                        else -> "Còn chỗ"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        availabilityRatio > 0.5f -> MaterialTheme.colorScheme.primary
                        availabilityRatio > 0.2f -> MaterialTheme.colorScheme.warning
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Book button
        EnhancedGymButton(
            text = if (classItem.available > 0) "Đặt lớp" else "Hết chỗ",
            onClick = onBookClick,
            enabled = classItem.available > 0 && !isBooking,
            loading = isBooking,
            variant = if (classItem.available > 0) ButtonVariant.Filled else ButtonVariant.Outlined,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ClassInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Spacing.iconSizeSmall),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatDateTime(dateString: String): String {
    return try {
        val date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        dateString
    }
}