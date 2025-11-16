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
import com.tys.gymapp.data.remote.dto.ClassItem
import com.tys.gymapp.presentation.components.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClassesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateClass: () -> Unit,
    onNavigateToEditClass: (ClassItem) -> Unit,
    viewModel: ManageClassesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<ClassItem?>(null) }

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
                title = { Text("Quản lý Lớp học") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadClasses() }) {
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
                onClick = onNavigateToCreateClass,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo lớp mới")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ManageClassesUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is ManageClassesUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadClasses() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ManageClassesUiState.Success -> {
                if (state.classes.isEmpty()) {
                    EmptyState(
                        message = "Chưa có lớp học nào",
                        icon = Icons.Default.Class,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.classes) { classItem ->
                            ManageClassCard(
                                classItem = classItem,
                                onEdit = { onNavigateToEditClass(classItem) },
                                onDelete = { showDeleteDialog = classItem }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { classItem ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa lớp \"${classItem.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClass(classItem.id)
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
fun ManageClassCard(
    classItem: ClassItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = classItem.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Sửa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info rows
            ClassInfoRow(
                icon = Icons.Default.Person,
                text = "HLV: ${classItem.trainer.fullName}"
            )
            ClassInfoRow(
                icon = Icons.Default.LocationOn,
                text = classItem.branch.name
            )
            ClassInfoRow(
                icon = Icons.Default.Schedule,
                text = formatDateTime(classItem.startTime)
            )
            ClassInfoRow(
                icon = Icons.Default.Groups,
                text = "${classItem.available}/${classItem.capacity} chỗ"
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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