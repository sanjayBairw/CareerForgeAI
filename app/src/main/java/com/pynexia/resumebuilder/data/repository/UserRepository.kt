package com.pynexia.resumebuilder.data.repository

import androidx.lifecycle.LiveData
import com.pynexia.resumebuilder.data.database.UserProfileDao
import com.pynexia.resumebuilder.data.model.UserProfileEntity

class UserRepository(
    private val dao: UserProfileDao
) {
    val userProfile: LiveData<UserProfileEntity?> = dao.getProfile()

    suspend fun getProfileSync(): UserProfileEntity? = dao.getProfileSync()

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.insertOrUpdate(profile)
    }
}
