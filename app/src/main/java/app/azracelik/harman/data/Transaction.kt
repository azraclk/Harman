package app.azracelik.harman.data
import androidx.room.Entity
import androidx.room.PrimaryKey


//(Entity - Veri Modeli / Tablo) - Transaction "Neyi" kaydedeceğimiz

@Entity(tableName = "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String,
    val categoryName: String,
    val date: Long,
    val note: String
)