package app.azracelik.harman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.ui.AppViewModel
import app.azracelik.harman.ui.ViewModelFactory
import app.azracelik.harman.ui.goals.GoalsScreen
import app.azracelik.harman.ui.home.HomeScreen
import app.azracelik.harman.ui.onboarding.OnboardingScreen
import app.azracelik.harman.ui.report.ReportScreen
import app.azracelik.harman.ui.theme.HarmanTheme
import app.azracelik.harman.ui.transaction.TransactionSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HarmanTheme { HarmanRoot() }
        }
    }
}

private enum class Route(val path: String) {
    Onboarding("onboarding"), Home("home"), Report("report"), Goals("goals")
}

private class TabItem(val route: Route, val labelRes: Int, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Route.Home, R.string.nav_home, Icons.Rounded.Home),
    TabItem(Route.Report, R.string.nav_report, Icons.Rounded.BarChart),
    TabItem(Route.Goals, R.string.nav_goals, Icons.Rounded.Flag)
)

/** [tx] null ise yeni işlem. */
private class SheetRequest(val tx: Transaction?)

@Composable
private fun HarmanRoot(appViewModel: AppViewModel = viewModel(factory = ViewModelFactory)) {
    val onboardingDone by appViewModel.onboardingDone.collectAsStateWithLifecycle()
    val done = onboardingDone
    if (done == null) {
        // DataStore okunurken boş zemin (flash'ı önler)
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val startRoute = remember { if (done) Route.Home else Route.Onboarding }
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    var sheet by remember { mutableStateOf<SheetRequest?>(null) }

    val showChrome = tabs.any { tab -> destination?.hierarchy?.any { it.route == tab.route.path } == true }
    val showFab = destination?.route == Route.Home.path || destination?.route == Route.Report.path

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showChrome) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.route == tab.route.path } == true,
                            onClick = {
                                navController.navigate(tab.route.path) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { sheet = SheetRequest(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_transaction))
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute.path,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Onboarding.path) {
                OnboardingScreen(onFinish = { limit ->
                    appViewModel.finishOnboarding(limit)
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Onboarding.path) { inclusive = true }
                    }
                })
            }
            composable(Route.Home.path) {
                HomeScreen(
                    onAddClick = { sheet = SheetRequest(null) },
                    onEditClick = { sheet = SheetRequest(it) },
                    onGoalsClick = {
                        navController.navigate(Route.Goals.path) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Route.Report.path) { ReportScreen() }
            composable(Route.Goals.path) { GoalsScreen() }
        }
    }

    sheet?.let { request ->
        TransactionSheet(
            initial = request.tx,
            onDismiss = { sheet = null },
            onSave = { appViewModel.save(it) },
            onDelete = { appViewModel.delete(it) }
        )
    }
}
