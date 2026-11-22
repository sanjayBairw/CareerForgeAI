package com.pynexia.resumebuilder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var companyName: String = "",
    var jobTitle: String = "",
    var location: String = "",
    var salaryRange: String = "",
    var jobType: String = "Full-time",
    var status: String = "APPLIED", // INTERESTED, APPLIED, ASSESSMENT, INTERVIEW, OFFER, REJECTED
    var appliedDate: Long = System.currentTimeMillis(),
    var interviewDate: Long? = null,
    var jobDescription: String = "",
    var notes: String = "",
    var contactPerson: String = "",
    var contactEmail: String = "",
    var linkedResumeId: Long? = null,
    var followUpDate: Long? = null
)
