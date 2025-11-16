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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ClassesUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is ClassesUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadClasses() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ClassesUiState.Success -> {
                if (state.classes.isEmpty()) {
                    EmptyState(
                        message = "Chưa có lớp học nào",
                        icon = Icons.Default.Event,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.classes) { classItem ->
                            ClassCard(
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

@Composable
fun ClassCard(
    classItem: ClassItem,
    onBookClick: () -> Unit,
    isBooking: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = classItem.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Description
            if (classItem.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = classItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info rows
            ClassInfoRow(
                icon = Icons.Default.Person,
                text = "HLV: ${classItem.trainer.fullName}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            ClassInfoRow(
                icon = Icons.Default.LocationOn,
                text = "Chi nhánh: ${classItem.branch.name}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            ClassInfoRow(
                icon = Icons.Default.Schedule,
                text = formatDateTime(classItem.startTime)
            )

            Spacer(modifier = Modifier.height(4.dp))

            ClassInfoRow(
                icon = Icons.Default.Groups,
                text = "Còn ${classItem.available}/${classItem.capacity} chỗ"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Book button
            GymButton(
                text = if (classItem.available > 0) "Đặt lớp" else "Hết chỗ",
                onClick = onBookClick,
                enabled = classItem.available > 0 && !isBooking,
                loading = isBooking,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ClassInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
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