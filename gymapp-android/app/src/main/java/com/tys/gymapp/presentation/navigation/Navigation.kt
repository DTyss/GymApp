package com.tys.gymapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tys.gymapp.presentation.screens.auth.LoginScreen
import com.tys.gymapp.presentation.screens.auth.RegisterScreen
import com.tys.gymapp.presentation.screens.main.MainScreen

/**
 * Navigation Routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object ClassDetail : Screen("class_detail/{classId}") {
        fun createRoute(classId: String) = "class_detail/$classId"
    }
    object CheckinHistory : Screen("checkin_history")
    object EditProfile : Screen("edit_profile")
    object Plans : Screen("plans")

    // ADMIN ROUTES
    object AdminDashboard : Screen("admin_dashboard")
    object ManageClasses : Screen("admin_manage_classes")
    object CreateClass : Screen("admin_create_class")
    object ManageMemberships : Screen("admin_manage_memberships")
    object CreateMembership : Screen("admin_create_membership")
    object ExtendMembership : Screen("admin_extend_membership/{membershipId}") {
        fun createRoute(membershipId: String) = "admin_extend_membership/$membershipId"
    }
}

/**
 * Main Navigation Graph
 */
@Composable
fun GymNavGraph(
    startDestination: String = Screen.Login.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Screen (Contains Bottom Navigation)
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}


/**
 * Bottom Navigation Routes (Inside Main Screen)
 */
sealed class BottomNavScreen(
    val route: String,
    val title: String
) {
    object Home : BottomNavScreen("home", "Trang chủ")
    object Classes : BottomNavScreen("classes", "Lịch lớp")
    object Checkin : BottomNavScreen("checkin", "Check-in")
    object Notifications : BottomNavScreen("notifications", "Thông báo")
    object Admin : BottomNavScreen("admin", "Admin")
    object Profile : BottomNavScreen("profile", "Hồ sơ")
}