package com.pynexia.resumebuilder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    var fullName: String = "",
    var email: String = "",
    var phone: String = "",
    var headline: String = "",
    var photoUri: String? = null,
    var isLoggedIn: Boolean = false,
    var authProvider: String = "LOCAL",
    var lastSyncTime: Long = System.currentTimeMillis()
)
