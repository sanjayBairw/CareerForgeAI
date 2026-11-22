package com.pynexia.resumebuilder.data.model

import java.util.UUID

data class EducationItem(
    val id: String = UUID.randomUUID().toString(),
    var institution: String = "",
    var degree: String = "",
    var fieldOfStudy: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var grade: String = "",
    var achievements: String = "",
    var coursework: String = ""
)
