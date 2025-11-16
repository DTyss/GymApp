package com.tys.gymapp.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.tys.gymapp.presentation.theme.Spacing

/**
 * ===================================
 * ANIMATION UTILITIES
 * ===================================
 */

@Composable
fun AnimatedVisibilityWithFade(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
    ) {
        content()
    }
}

@Composable
fun AnimatedScale(
    scale: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    Box(
        modifier = modifier.scale(animatedScale)
    ) {
        content()
    }
}

/**
 * Success checkmark animation
 */
@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    var showAnimation by remember { mutableStateOf(true) }
    
    val scale by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "success_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0f,
        animationSpec = tween(300),
        label = "success_alpha"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        showAnimation = false
        kotlinx.coroutines.delay(300)
        onAnimationComplete()
    }

    if (showAnimation || alpha > 0f) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                modifier = Modifier
                    .scale(scale)
                    .then(Modifier.size(80.dp)),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
            )
        }
    }
}

/**
 * Pulse animation for important elements
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

/**
 * Loading shimmer for text
 */
@Composable
fun ShimmerText(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    if (isLoading) {
        ShimmerEffect(
            modifier = modifier
                .fillMaxSize()
                .then(Modifier.height(20.dp))
        )
    }
}

