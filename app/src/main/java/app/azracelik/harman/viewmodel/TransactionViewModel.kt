package app.azracelik.harman.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.azracelik.harman.data.Transaction
import app.azracelik.harman.data.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionViewModel(private val dao: TransactionDao) : ViewModel() {

    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions() // Verileri Dinleme

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }
}

// ViewModel Üreticisi (Factory)
class TransactionViewModelFactory(private val dao: TransactionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(dao) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
    }
}