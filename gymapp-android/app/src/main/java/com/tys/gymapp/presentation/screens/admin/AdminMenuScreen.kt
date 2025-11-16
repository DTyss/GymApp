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
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToManageClasses: () -> Unit,
    onNavigateToManageMemberships: () -> Unit,
    onNavigateToManageUsers: () -> Unit,
    onNavigateToManagePlans: () -> Unit,
    onNavigateToManageBranches: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản trị hệ thống") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Header
            Text(
                text = "Quản trị hệ thống",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Menu items
            SimpleAdminMenuItem(
                title = "Dashboard",
                description = "Thống kê tổng quan",
                icon = Icons.Default.Dashboard,
                onClick = onNavigateToDashboard
            )

            SimpleAdminMenuItem(
                title = "Quản lý Lớp học",
                description = "Tạo, sửa, xóa lớp học",
                icon = Icons.Default.Class,
                onClick = onNavigateToManageClasses
            )

            SimpleAdminMenuItem(
                title = "Quản lý Memberships",
                description = "Quản lý gói tập của hội viên",
                icon = Icons.Default.CardMembership,
                onClick = onNavigateToManageMemberships
            )

            SimpleAdminMenuItem(
                title = "Quản lý Users",
                description = "Xem, sửa thông tin người dùng",
                icon = Icons.Default.People,
                onClick = onNavigateToManageUsers
            )

            SimpleAdminMenuItem(
                title = "Quản lý Plans",
                description = "CRUD các gói tập",
                icon = Icons.Default.WorkspacePremium,
                onClick = onNavigateToManagePlans
            )

            SimpleAdminMenuItem(
                title = "Quản lý Branches",
                description = "CRUD chi nhánh",
                icon = Icons.Default.Store,
                onClick = onNavigateToManageBranches
            )
        }
    }
}

@Composable
fun SimpleAdminMenuItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    AdminListItemCard(
        title = title,
        subtitle = description,
        onClick = onClick,
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    )
}