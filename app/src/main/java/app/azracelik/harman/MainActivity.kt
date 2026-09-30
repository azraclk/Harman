package app.azracelik.harman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.azracelik.harman.data.HarmanDatabase
import app.azracelik.harman.ui.navigation.BottomNavigationBar
import app.azracelik.harman.ui.screens.AddTransactionScreen
import app.azracelik.harman.ui.screens.DashboardScreen
import app.azracelik.harman.ui.screens.ReportScreen
import app.azracelik.harman.ui.theme.HarmanTheme
import app.azracelik.harman.viewmodel.TransactionViewModel
import app.azracelik.harman.viewmodel.TransactionViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Veritabanı ve DAO'yu başlat
        val database = HarmanDatabase.getDatabase(applicationContext)
        val transactionDao = database.transactionDao()

        // 2. Factory kullanarak ViewModel'ı oluştur
        val factory = TransactionViewModelFactory(transactionDao)
        val viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        setContent {
            HarmanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        bottomBar = {
                            BottomNavigationBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    if (currentRoute != route) {
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // 1. Rota: Ana Ekran (Dashboard)
                            composable("dashboard") {
                                DashboardScreen(viewModel = viewModel)
                            }

                            // 2. Rota: İşlem Ekleme Ekranı
                            composable("add_transaction") {
                                AddTransactionScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // 3. Rota: Rapor Ekranı
                            composable("report") {
                                ReportScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}