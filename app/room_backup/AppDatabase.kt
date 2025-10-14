package com.modernmusicplayer.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.modernmusicplayer.app.data.local.dao.UserDao
import com.modernmusicplayer.app.data.local.dao.SongDao
import com.modernmusicplayer.app.data.local.dao.PlaylistDao
import com.modernmusicplayer.app.data.local.entity.UserEntity
import com.modernmusicplayer.app.data.local.entity.SongEntity
import com.modernmusicplayer.app.data.local.entity.PlaylistEntity

@Database(
    entities = [UserEntity::class, SongEntity::class, PlaylistEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_player_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
