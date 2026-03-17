package org.medtroniclabs.uhis.app.analytics.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.medtroniclabs.uhis.app.analytics.model.Analytics
import org.medtroniclabs.uhis.app.analytics.model.UserJourneyAnalytics

@Database(entities = [Analytics::class, UserJourneyAnalytics::class], version = 2)
abstract class AnalyticsDatabase : RoomDatabase() {
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        private const val DB_NAME = "mdt_analytics_db"

        @Volatile
        private var INSTANCE: AnalyticsDatabase? = null

        fun getInstance(context: Context): AnalyticsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AnalyticsDatabase {
            val db = Room.databaseBuilder(
                context,
                AnalyticsDatabase::class.java,
                DB_NAME,
            )

            db.addMigrations(MIGRATION_1_2)

            return db.build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE userJourneyAnalytics ADD COLUMN userRole TEXT")
            }
        }
    }
}
