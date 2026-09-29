package app.azracelik.harman.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//(Ana Veritabanı Kurulumu) - HarmanDatabase "Nerede" çalıştıracağımız

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class HarmanDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: HarmanDatabase? = null

        fun getDatabase(context: Context): HarmanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HarmanDatabase::class.java,
                    "harman_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}