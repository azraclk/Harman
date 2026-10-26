package app.azracelik.harman.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Tek hedef (id = 1) yerine otomatik artan id ile birden fazla tasarruf hedefi. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `savings_goal_new` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`targetMinor` INTEGER NOT NULL, `savedMinor` INTEGER NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO `savings_goal_new` (`id`, `name`, `targetMinor`, `savedMinor`) " +
                "SELECT `id`, `name`, `targetMinor`, `savedMinor` FROM `savings_goal`"
        )
        db.execSQL("DROP TABLE `savings_goal`")
        db.execSQL("ALTER TABLE `savings_goal_new` RENAME TO `savings_goal`")
    }
}

/** Ay bazlı limit tablosu. Eski genel limit DataStore'da kalır ve ilk kayıttan önceki aylar için taban olur. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `month_limits` (" +
                "`monthIndex` INTEGER NOT NULL, `limitMinor` INTEGER NOT NULL, PRIMARY KEY(`monthIndex`))"
        )
    }
}
