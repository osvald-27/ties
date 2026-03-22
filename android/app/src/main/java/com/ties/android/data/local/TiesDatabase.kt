package com.ties.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * TiesDatabase — Room Database
 *
 * Local SQLite database for offline block queuing.
 * Single instance shared across the app via companion object.
 */
@Database(
    entities = [QueuedBlock::class],
    version = 1,
    exportSchema = false
)
abstract class TiesDatabase : RoomDatabase() {

    abstract fun queuedBlockDao(): QueuedBlockDao

    companion object {
        @Volatile
        private var INSTANCE: TiesDatabase? = null

        fun getDatabase(context: Context): TiesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TiesDatabase::class.java,
                    "ties_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}