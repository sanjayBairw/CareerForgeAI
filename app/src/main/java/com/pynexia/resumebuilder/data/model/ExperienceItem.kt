package com.pynexia.resumebuilder.data.model

import java.util.UUID

data class ExperienceItem(
    val id: String = UUID.randomUUID().toString(),
    var company: String = "",
    var jobTitle: String = "",
    var employmentType: String = "Full-time",
    var location: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var isCurrent: Boolean = false,
    var responsibilities: String = "",
    var measurableResults: String = ""
)
