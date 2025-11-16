package com.tys.gymapp.presentation.screens.plans

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
import com.tys.gymapp.data.remote.dto.Plan
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing
import com.tys.gymapp.presentation.theme.Elevation
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Các gói tập") },
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
            is PlansUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(Spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(3) {
                        ShimmerCard()
                    }
                }
            }
            is PlansUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.loadPlans() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is PlansUiState.Success -> {
                if (state.plans.isEmpty()) {
                    EnhancedEmptyState(
                        message = "Chưa có gói tập nào",
                        icon = Icons.Default.CardMembership,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(Spacing.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        // Header text
                        item {
                            AnimatedVisibilityWithFade(visible = true) {
                                Column {
                                    Text(
                                        text = "Chọn gói tập phù hợp",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    Text(
                                        text = "Liên hệ quầy lễ tân hoặc admin để đăng ký gói",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // Plans list
                        items(
                            items = state.plans,
                            key = { it.id }
                        ) { plan ->
                            AnimatedVisibilityWithFade(visible = true) {
                                EnhancedPlanCard(plan = plan)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedPlanCard(plan: Plan) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = if (plan.isActive) Elevation.level2 else Elevation.level1
    ) {
        // Plan name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Active badge
            if (plan.isActive) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Đang bán",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Price
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = formatPrice(plan.price),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = " VNĐ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Features
        PlanFeatureRow(
            icon = Icons.Default.Event,
            label = "Thời hạn",
            value = "${plan.durationDays} ngày"
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        PlanFeatureRow(
            icon = Icons.Default.FitnessCenter,
            label = "Số buổi tập",
            value = "${plan.sessions} buổi"
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        PlanFeatureRow(
            icon = Icons.Default.TrendingDown,
            label = "Giá mỗi buổi",
            value = "${formatPrice(plan.price / plan.sessions)} VNĐ"
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Contact info card
        EnhancedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = Elevation.none
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(Spacing.iconSizeSmall)
                )
                Text(
                    text = "Liên hệ quầy lễ tân để đăng ký gói này",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PlanFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(price)
}