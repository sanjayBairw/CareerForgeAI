package com.pynexia.resumebuilder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cover_letters")
data class CoverLetterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var resumeId: Long? = null,
    var title: String = "Application Cover Letter",
    var recipientName: String = "Hiring Manager",
    var companyName: String = "",
    var jobTitle: String = "",
    var date: String = "",
    var salutation: String = "Dear Hiring Team,",
    var bodyText: String = "",
    var signOff: String = "Sincerely,",
    var senderName: String = "",
    var senderContact: String = "",
    var templateId: String = "modern",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
