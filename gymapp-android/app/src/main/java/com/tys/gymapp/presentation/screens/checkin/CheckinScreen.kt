package com.tys.gymapp.presentation.screens.checkin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tys.gymapp.presentation.components.*
import com.tys.gymapp.presentation.theme.Spacing
import com.tys.gymapp.presentation.theme.Elevation
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScreen(
    onNavigateToHistory: () -> Unit,
    viewModel: CheckinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    
    // Countdown timer state
    var remainingSeconds by remember { mutableStateOf(0L) }
    
    LaunchedEffect(uiState) {
        if (uiState is CheckinUiState.Success) {
            val state = uiState as CheckinUiState.Success
            while (true) {
                remainingSeconds = getRemainingSeconds(state.qrPayload.exp)
                if (remainingSeconds <= 0) break
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Lịch sử check-in"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CheckinUiState.Loading -> {
                LoadingIndicator(Modifier.padding(paddingValues))
            }
            is CheckinUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.generateQr() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is CheckinUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // QR Card with animated border
                    EnhancedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = Elevation.level3
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                modifier = Modifier.size(Spacing.xxxl),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

                            Text(
                                text = "Mã QR Check-in",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = "Đưa mã này cho nhân viên quầy để check-in",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(Spacing.lg))

                            // QR Code Image with animated border
                            qrBitmap?.let { bitmap ->
                                AnimatedQrCode(
                                    bitmap = bitmap,
                                    remainingSeconds = remainingSeconds
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.lg))

                            // Timer with pulse animation
                            PulseAnimation {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        modifier = Modifier.size(Spacing.iconSizeSmall),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text(
                                        text = "Mã tự động làm mới sau ${remainingSeconds}s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Refresh Button
                    EnhancedGymButton(
                        text = "Làm mới mã",
                        onClick = { viewModel.generateQr() },
                        icon = Icons.Default.Refresh,
                        variant = ButtonVariant.Outlined,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Info card
                    EnhancedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = Elevation.none
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(Spacing.iconSize)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "Mỗi lần check-in sẽ trừ 1 buổi tập trong gói của bạn",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedQrCode(
    bitmap: android.graphics.Bitmap,
    remainingSeconds: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "qr_border")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Animated border
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
                            MaterialTheme.colorScheme.secondary.copy(alpha = borderAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xs)
            )
        }
    }
}

fun getRemainingSeconds(exp: Long): Long {
    return maxOf(0, exp - System.currentTimeMillis() / 1000)
}