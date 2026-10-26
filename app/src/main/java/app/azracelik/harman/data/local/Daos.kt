package app.azracelik.harman.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import app.azracelik.harman.data.model.MonthLimit
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        "SELECT * FROM transactions WHERE epochDay BETWEEN :fromDay AND :toDay " +
            "ORDER BY epochDay DESC, id DESC"
    )
    fun observeBetween(fromDay: Long, toDay: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Upsert
    suspend fun upsert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clear()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goal ORDER BY id DESC")
    fun observeAll(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goal WHERE id = :id")
    suspend fun getById(id: Long): SavingsGoal?

    @Upsert
    suspend fun upsert(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)

    @Query("DELETE FROM savings_goal")
    suspend fun clear()
}

@Dao
interface MonthLimitDao {
    /** [monthIndex] veya öncesindeki en yakın limit kaydı; yoksa null. */
    @Query("SELECT * FROM month_limits WHERE monthIndex <= :monthIndex ORDER BY monthIndex DESC LIMIT 1")
    fun observeLatestUpTo(monthIndex: Int): Flow<MonthLimit?>

    @Upsert
    suspend fun upsert(limit: MonthLimit)

    @Query("DELETE FROM month_limits")
    suspend fun clear()
}
