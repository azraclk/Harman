package app.azracelik.harman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import app.azracelik.harman.ui.theme.semantic
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showChrome) {
                FloatingNavBar(
                    isSelected = { tab -> destination?.hierarchy?.any { it.route == tab.route.path } == true },
                    onSelect = { tab ->
                        navController.navigate(tab.route.path) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
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

/** Yüzen, koyu "hap" çubuk: seçili sekme sarı bir hapla etiketini açar. */
@Composable
private fun FloatingNavBar(isSelected: (TabItem) -> Boolean, onSelect: (TabItem) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = isSelected(tab)
                val label = stringResource(tab.labelRes)
                Row(
                    Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(if (selected) MaterialTheme.semantic.butter.container else Color.Transparent)
                        .clickable(role = Role.Tab) { onSelect(tab) }
                        .padding(horizontal = if (selected) 18.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = if (selected) null else label,
                        tint = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                    )
                    if (selected) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
