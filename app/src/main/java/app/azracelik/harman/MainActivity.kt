package app.azracelik.harman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import app.azracelik.harman.data.HarmanDatabase
import app.azracelik.harman.ui.screens.DashboardScreen
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
            // Uygulamanın temasını başlatır (HarmanTheme adı ui.theme içindeki Theme.kt'den gelir)
            HarmanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Ana ekranımızı çağır ve ViewModel'ı içine gönder
                    DashboardScreen(
                        viewModel = viewModel,
                        onAddClick = {
                            // Şimdilik burası boş kalacak.
                            // İleride "Yeni Ekle" ekranına geçiş (Navigation) kodlarını buraya yazacağız.
                        }
                    )
                }
            }
        }
    }
}