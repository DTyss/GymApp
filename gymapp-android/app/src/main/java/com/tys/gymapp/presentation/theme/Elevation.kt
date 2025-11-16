package com.tys.gymapp.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Elevation system for Material 3
 * Provides consistent depth hierarchy
 */
object Elevation {
    val none = 0.dp
    val level1 = 1.dp   // Cards at rest
    val level2 = 3.dp   // Cards hovered/pressed
    val level3 = 6.dp   // Floating action buttons
    val level4 = 8.dp   // Dialogs, bottom sheets
    val level5 = 12.dp  // Modals, dropdowns
}

