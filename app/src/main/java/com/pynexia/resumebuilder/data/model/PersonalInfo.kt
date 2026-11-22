package com.pynexia.resumebuilder.data.model

data class PersonalInfo(
    var fullName: String = "",
    var headline: String = "",
    var email: String = "",
    var phone: String = "",
    var location: String = "",
    var linkedInUrl: String = "",
    var githubUrl: String = "",
    var portfolioUrl: String = "",
    var websiteUrl: String = "",
    var photoUri: String? = null
)
