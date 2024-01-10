package com.medtroniclabs.spice.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.entity.HouseholdEntity

@Database(
    entities = [HouseholdEntity::class], version = 1
)
abstract class SpiceDataBase : RoomDatabase() {
    abstract fun householdDAO(): HouseholdDAO

    companion object {
        private const val DATABASE_NAME = "SpiceDataBase"

        @Volatile
        private var INSTANCE: SpiceDataBase? = null

        fun getInstance(context: Context): SpiceDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }


        private fun buildDatabase(context: Context): SpiceDataBase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                SpiceDataBase::class.java,
                DATABASE_NAME
            )
            return db.build()
        }


    }

}