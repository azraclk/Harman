package app.azracelik.harman.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.YearMonth

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

/** Birden fazla tasarruf hedefi tutulabilir. */
@Entity(tableName = "savings_goal")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetMinor: Long,
    val savedMinor: Long = 0
)

/**
 * Ay bazlı harcama limiti. [monthIndex] ilgili ayı (yıl * 12 + ay - 1) gösterir; kayıt, sonraki
 * bir kayıt gelene kadar o ay ve sonrası için geçerlidir. Böylece limiti değiştirmek geçmiş ayların
 * raporunu etkilemez.
 */
@Entity(tableName = "month_limits")
data class MonthLimit(
    @PrimaryKey val monthIndex: Int,
    val limitMinor: Long
)

fun YearMonth.toIndex(): Int = year * 12 + monthValue - 1
