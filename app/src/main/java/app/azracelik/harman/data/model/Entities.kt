package app.azracelik.harman.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tutarlar kuruş cinsinden Long (Double yuvarlama hatalarından kaçınmak için). */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val type: TransactionType,
    val category: Category,
    /** LocalDate.toEpochDay() */
    val epochDay: Long,
    val note: String = ""
)

/** Tek bir tasarruf hedefi tutulur (id her zaman 1). */
@Entity(tableName = "savings_goal")
data class SavingsGoal(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val targetMinor: Long,
    val savedMinor: Long = 0
)
