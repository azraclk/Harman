package app.azracelik.harman.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import app.azracelik.harman.data.local.AppDatabase
import app.azracelik.harman.data.local.MIGRATION_1_2
import app.azracelik.harman.data.local.MIGRATION_2_3
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private val dbName = "migration-test"

    @Test
    fun migrate1To3_keepsTransactionsAndGoal() {
        helper.createDatabase(dbName, 1).apply {
            execSQL("INSERT INTO transactions (id, amountMinor, type, category, epochDay, note) VALUES (1, 12550, 'EXPENSE', 'MARKET', 20000, 'süt')")
            execSQL("INSERT INTO savings_goal (id, name, targetMinor, savedMinor) VALUES (1, 'Tatil', 500000, 120000)")
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_1_2, MIGRATION_2_3)

        db.query("SELECT amountMinor, note FROM transactions WHERE id = 1").use {
            it.moveToFirst()
            assertEquals(12550L, it.getLong(0))
            assertEquals("süt", it.getString(1))
        }
        db.query("SELECT name, targetMinor, savedMinor FROM savings_goal WHERE id = 1").use {
            it.moveToFirst()
            assertEquals("Tatil", it.getString(0))
            assertEquals(500000L, it.getLong(1))
            assertEquals(120000L, it.getLong(2))
        }
        // Yeni hedef artık otomatik id alır.
        db.execSQL("INSERT INTO savings_goal (name, targetMinor, savedMinor) VALUES ('Laptop', 1, 0)")
        db.query("SELECT COUNT(*) FROM savings_goal").use {
            it.moveToFirst()
            assertEquals(2, it.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM month_limits").use {
            it.moveToFirst()
            assertEquals(0, it.getInt(0))
        }
    }
}
