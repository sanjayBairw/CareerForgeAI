package com.pynexia.resumebuilder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    var fullName: String = "Alex Johnson",
    var email: String = "alex.johnson@example.com",
    var phone: String = "+1 (555) 019-2834",
    var headline: String = "Senior Full Stack & Mobile Engineer",
    var photoUri: String? = null,
    var isLoggedIn: Boolean = true,
    var authProvider: String = "LOCAL",
    var lastSyncTime: Long = System.currentTimeMillis()
)
