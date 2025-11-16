package com.tys.gymapp.presentation.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tys.gymapp.data.local.TokenManager
import com.tys.gymapp.presentation.navigation.BottomNavScreen
import com.tys.gymapp.presentation.screens.checkin.CheckinHistoryScreen
import com.tys.gymapp.presentation.screens.checkin.CheckinScreen
import com.tys.gymapp.presentation.screens.classes.ClassesScreen
import com.tys.gymapp.presentation.screens.home.HomeScreen
import com.tys.gymapp.presentation.screens.notifications.NotificationsScreen
import com.tys.gymapp.presentation.screens.plans.PlansScreen
import com.tys.gymapp.presentation.screens.profile.EditProfileScreen
import com.tys.gymapp.presentation.screens.profile.ProfileScreen
import com.tys.gymapp.presentation.screens.admin.*
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination


    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val userRole by tokenManager.getUserRole().collectAsState(initial = null)

    val bottomNavItems = remember(userRole) {
        buildList<BottomNavItem> {
            add(BottomNavItem(
                route = BottomNavScreen.Home.route,
                title = BottomNavScreen.Home.title,
                icon = Icons.Default.Home
            ))
            add(BottomNavItem(
                route = BottomNavScreen.Classes.route,
                title = BottomNavScreen.Classes.title,
                icon = Icons.Default.CalendarMonth
            ))
            add(BottomNavItem(
                route = BottomNavScreen.Checkin.route,
                title = BottomNavScreen.Checkin.title,
                icon = Icons.Default.QrCode2
            ))
            add(BottomNavItem(
                route = BottomNavScreen.Notifications.route,
                title = BottomNavScreen.Notifications.title,
                icon = Icons.Default.Notifications
            ))

            // Chỉ show Admin tab nếu role = admin
            if (userRole == "admin") {
                add(BottomNavItem(
                    route = BottomNavScreen.Admin.route,
                    title = BottomNavScreen.Admin.title,
                    icon = Icons.Default.AdminPanelSettings
                ))
            }

            add(BottomNavItem(
                route = BottomNavScreen.Profile.route,
                title = BottomNavScreen.Profile.title,
                icon = Icons.Default.Person
            ))
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavScreen.Home.route) {
                HomeScreen(
                    onNavigateToPlans = {
                        navController.navigate("plans")
                    }
                )
            }

            composable(BottomNavScreen.Classes.route) {
                ClassesScreen()
            }

            composable(BottomNavScreen.Checkin.route) {
                CheckinScreen(
                    onNavigateToHistory = {
                        navController.navigate("checkin_history")
                    }
                )
            }

            composable("checkin_history") {
                CheckinHistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(BottomNavScreen.Notifications.route) {
                NotificationsScreen()
            }

            composable(BottomNavScreen.Profile.route) {
                ProfileScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToEditProfile = {
                        navController.navigate("edit_profile")
                    }
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("plans") {
                PlansScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ===== ADMIN ROUTES =====
            // Admin Menu (Landing screen)
            composable(BottomNavScreen.Admin.route) {
                AdminMenuScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = {
                        navController.navigate("admin_dashboard")
                    },
                    onNavigateToManageClasses = {
                        navController.navigate("admin_manage_classes")
                    },
                    onNavigateToManageMemberships = {
                        navController.navigate("admin_manage_memberships")
                    },
                    onNavigateToManageUsers = {
                        navController.navigate("admin_manage_users")
                    },
                    onNavigateToManagePlans = {
                        navController.navigate("admin_manage_plans")
                    },
                    onNavigateToManageBranches = {
                        navController.navigate("admin_manage_branches")
                    }
                )
            }

            // Dashboard
            composable("admin_dashboard") {
                DashboardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Manage Classes
            composable("admin_manage_classes") {
                ManageClassesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreateClass = {
                        navController.navigate("admin_create_class")
                    },
                    onNavigateToEditClass = { classItem ->
                        val classJson = com.tys.gymapp.presentation.utils.NavigationUtils.classToJson(classItem)
                        navController.navigate("admin_create_class?classJson=${android.net.Uri.encode(classJson)}")
                    }
                )
            }

            // Create/Edit Class
            composable(
                route = "admin_create_class?classJson={classJson}",
                arguments = listOf(
                    navArgument("classJson") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val classJson = backStackEntry.arguments?.getString("classJson") ?: ""
                CreateEditClassScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Manage Memberships
            composable("admin_manage_memberships") {
                ManageMembershipsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreate = {
                        navController.navigate("admin_create_membership")
                    },
                    onNavigateToDetail = { membershipId ->
                        navController.navigate("admin_extend_membership/$membershipId")
                    }
                )
            }

            // Create Membership
            composable("admin_create_membership") {
                CreateMembershipScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Extend Membership
            composable(
                route = "admin_extend_membership/{membershipId}",
                arguments = listOf(
                    navArgument("membershipId") { type = NavType.StringType }
                )
            ) {
                ExtendMembershipScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Manage Users
            composable("admin_manage_users") {
                ManageUsersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToUserDetail = { userId ->
                        navController.navigate("admin_user_detail/$userId")
                    }
                )
            }

            // User Detail
            composable(
                route = "admin_user_detail/{userId}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) {
                UserDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Manage Plans
            composable("admin_manage_plans") {
                ManagePlansScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreatePlan = {
                        navController.navigate("admin_create_plan")
                    },
                    onNavigateToEditPlan = { plan ->
                        val planJson = com.tys.gymapp.presentation.utils.NavigationUtils.planToJson(plan)
                        navController.navigate("admin_create_plan?planJson=${android.net.Uri.encode(planJson)}")
                    }
                )
            }

            // Create/Edit Plan
            composable(
                route = "admin_create_plan?planJson={planJson}",
                arguments = listOf(
                    navArgument("planJson") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val planJson = backStackEntry.arguments?.getString("planJson") ?: ""
                CreateEditPlanScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Manage Branches
            composable("admin_manage_branches") {
                ManageBranchesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreateBranch = {
                        navController.navigate("admin_create_branch")
                    },
                    onNavigateToEditBranch = { branch ->
                        val branchJson = com.tys.gymapp.presentation.utils.NavigationUtils.branchToJson(branch)
                        navController.navigate("admin_create_branch?branchJson=${android.net.Uri.encode(branchJson)}")
                    }
                )
            }

            // Create/Edit Branch
            composable(
                route = "admin_create_branch?branchJson={branchJson}",
                arguments = listOf(
                    navArgument("branchJson") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val branchJson = backStackEntry.arguments?.getString("branchJson") ?: ""
                CreateEditBranchScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// Bottom Navigation Items
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = BottomNavScreen.Home.route,
        title = "Trang chủ",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = BottomNavScreen.Classes.route,
        title = "Lịch lớp",
        icon = Icons.Default.CalendarMonth
    ),
    BottomNavItem(
        route = BottomNavScreen.Checkin.route,
        title = "Check-in",
        icon = Icons.Default.QrCode2
    ),
    BottomNavItem(
        route = BottomNavScreen.Notifications.route,
        title = "Thông báo",
        icon = Icons.Default.Notifications
    ),
    BottomNavItem(
        route = BottomNavScreen.Profile.route,
        title = "Hồ sơ",
        icon = Icons.Default.Person
    )
)