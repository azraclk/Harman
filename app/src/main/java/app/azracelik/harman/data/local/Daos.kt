package app.azracelik.harman.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
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
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goal WHERE id = 1")
    fun observe(): Flow<SavingsGoal?>

    @Query("SELECT * FROM savings_goal WHERE id = 1")
    suspend fun get(): SavingsGoal?

    @Upsert
    suspend fun upsert(goal: SavingsGoal)

    @Query("DELETE FROM savings_goal")
    suspend fun clear()
}
