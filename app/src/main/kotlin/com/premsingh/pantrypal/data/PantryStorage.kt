package com.premsingh.pantrypal.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pantry_entries")
data class PantryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: String,
    val expiryEpochDay: Long
)

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_entries ORDER BY expiryEpochDay ASC, name COLLATE NOCASE ASC")
    fun observeEntries(): Flow<List<PantryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PantryEntry)

    @Query("DELETE FROM pantry_entries WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [PantryEntry::class], version = 1, exportSchema = false)
abstract class PantryDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao

    companion object {
        @Volatile private var INSTANCE: PantryDatabase? = null
        fun get(context: Context): PantryDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, PantryDatabase::class.java, "pantry-pal.db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
