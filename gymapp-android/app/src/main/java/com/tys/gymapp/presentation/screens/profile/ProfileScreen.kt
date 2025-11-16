package com.tys.gymapp.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle logout navigation
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.LoggedOut) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ") },
                actions = {
                    if (uiState is ProfileUiState.Success) {
                        IconButton(onClick = onNavigateToEditProfile) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa"
                            )
                        }
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
            is ProfileUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is ProfileUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    Text(
                        text = state.user.fullName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Role
                    Text(
                        text = getRoleName(state.user.role),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Profile Info Cards
                    ProfileInfoCard(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = state.user.email ?: "Chưa có"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoCard(
                        icon = Icons.Default.Phone,
                        label = "Số điện thoại",
                        value = state.user.phone ?: "Chưa có"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoCard(
                        icon = Icons.Default.Info,
                        label = "Trạng thái",
                        value = getStatusName(state.user.status)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoCard(
                        icon = Icons.Default.CardMembership,
                        label = "Số gói tập",
                        value = "${state.user.memberships.size} gói"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Logout Button
                    GymButton(
                        text = "Đăng xuất",
                        onClick = { viewModel.logout() },
                        icon = Icons.Default.Logout,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Version info
                    Text(
                        text = "Phiên bản 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            is ProfileUiState.LoggedOut -> {
                // Show nothing, navigation will happen
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
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