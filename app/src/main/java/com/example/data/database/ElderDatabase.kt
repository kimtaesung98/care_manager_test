package com.example.data.database

import android.content.Context
import androidx.room.*

@Database(
    entities = [
        ElderEntity::class,
        AlertEntity::class,
        VisitRecordEntity::class,
        ThresholdsEntity::class,
        ChecklistItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class ElderDatabase : RoomDatabase() {

    abstract fun elderDao(): ElderDao

    companion object {
        @Volatile
        private var INSTANCE: ElderDatabase? = null

        fun getDatabase(context: Context): ElderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ElderDatabase::class.java,
                    "elder_health_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
