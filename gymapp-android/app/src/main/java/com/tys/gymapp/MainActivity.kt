package com.tys.gymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tys.gymapp.presentation.navigation.GymNavGraph
import com.tys.gymapp.presentation.theme.GymAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigateTo = intent.getStringExtra("navigate_to")

        setContent {
            GymAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Navigation Graph
                    GymNavGraph()

                    // TODO: Nếu cần navigate đến NotificationsScreen sau khi click notification
                    // Có thể pass navigateTo vào GymNavGraph hoặc dùng shared ViewModel
                }
            }
        }
    }
}