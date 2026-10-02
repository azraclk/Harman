package app.azracelik.harman.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.data.model.Transaction

@Database(entities = [Transaction::class, SavingsGoal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "harman.db").build()
    }
}
