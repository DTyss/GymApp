package com.tys.gymapp.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing system based on 8dp grid
 * All spacing values are multiples of 8dp for consistency
 */
object Spacing {
    // Base unit
    val xs = 4.dp      // 0.5x base
    val sm = 8.dp      // 1x base
    val md = 16.dp     // 2x base
    val lg = 24.dp     // 3x base
    val xl = 32.dp     // 4x base
    val xxl = 40.dp    // 5x base
    val xxxl = 48.dp   // 6x base
    
    // Specific use cases
    val cardPadding = 16.dp
    val screenPadding = 16.dp
    val sectionSpacing = 24.dp
    val itemSpacing = 12.dp
    val buttonHeight = 56.dp
    val iconSize = 24.dp
    val iconSizeSmall = 20.dp
    val iconSizeLarge = 32.dp
    val avatarSize = 48.dp
    val avatarSizeLarge = 64.dp
}

