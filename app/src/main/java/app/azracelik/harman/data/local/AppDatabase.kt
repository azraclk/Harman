package app.azracelik.harman.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.azracelik.harman.data.model.MonthLimit
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.data.model.Transaction

@Database(
    entities = [Transaction::class, SavingsGoal::class, MonthLimit::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun monthLimitDao(): MonthLimitDao

    companion object {
        /** Şema değişince Migrations.kt'ye gerçek bir migration eklenmeli; veri silinmez. */
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "harman.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
