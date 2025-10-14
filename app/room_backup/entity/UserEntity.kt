package com.modernmusicplayer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val favoritesSongIds: List<String> = emptyList()
)
