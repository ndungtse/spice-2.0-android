package com.mdtlabs.ncd.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mdtlabs.ncd.db.dao.LanguageDAO
import com.mdtlabs.ncd.db.tables.LanguageEntity

@Database(
    entities = [
        LanguageEntity::class
    ], version = 1
)
abstract class NCDMergerDatabase : RoomDatabase() {

    abstract fun languageDao(): LanguageDAO

    companion object {

        private const val DATABASE_NAME = "NCDMergerDatabase"

        @Volatile
        private var INSTANCE: NCDMergerDatabase? = null

        fun getInstance(context: Context): NCDMergerDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): NCDMergerDatabase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                NCDMergerDatabase::class.java,
                DATABASE_NAME
            )
            //.addMigrations(MIGRATION_1_2)
            return db.build()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL("ALTER TABLE TestEntity ADD COLUMN age TEXT")
            }
        }

    }

}