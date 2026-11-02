package id.dietku.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.dietku.app.feature.settings.SettingsViewModel
import id.dietku.app.ui.screens.EducationScreen
import id.dietku.app.ui.screens.ExerciseScreen
import id.dietku.app.ui.screens.FoodLoggingScreen
import id.dietku.app.ui.screens.FastingScreen
import id.dietku.app.ui.screens.HomeScreen
import id.dietku.app.ui.screens.ProfileScreen
import id.dietku.app.ui.screens.ProgressScreen
import id.dietku.app.ui.screens.MoreScreen
import id.dietku.app.ui.screens.MealPlannerScreen
import id.dietku.app.ui.screens.ReminderSettingsScreen
import id.dietku.app.ui.theme.DietKuTheme

private data class AppTab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    AppTab("home", "Beranda", Icons.Outlined.Home),
    AppTab("food", "Makan", Icons.Outlined.Restaurant),
    AppTab("fasting", "Puasa", Icons.Outlined.DarkMode),
    AppTab("progress", "Progres", Icons.Outlined.ShowChart),
    AppTab("more", "Lainnya", Icons.Outlined.MoreHoriz),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietKuApp(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val darkMode by settingsViewModel.darkMode.collectAsStateWithLifecycle()
    val isDarkTheme = darkMode ?: isSystemInDarkTheme()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    DietKuTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("DietKu", style = MaterialTheme.typography.titleLarge)
                    },
                    actions = {
                        IconButton(
                            onClick = { settingsViewModel.setDarkMode(!isDarkTheme) },
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = if (isDarkTheme) "Aktifkan tema terang" else "Aktifkan tema gelap",
                            )
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any {
                            it.route == tab.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding),
            ) {
                composable("home") { HomeScreen(onNavigate = { route -> navController.navigate(route) }) }
                composable("food") { FoodLoggingScreen() }
                composable("fasting") { FastingScreen() }
                composable("profile") { ProfileScreen() }
                composable("education") { EducationScreen() }
                composable("exercise") { ExerciseScreen() }
                composable("meal-plan") { MealPlannerScreen() }
                composable("reminders") { ReminderSettingsScreen() }
                composable("progress") { ProgressScreen() }
                composable("more") { MoreScreen(onNavigate = { route -> navController.navigate(route) }) }
            }
        }
    }
}