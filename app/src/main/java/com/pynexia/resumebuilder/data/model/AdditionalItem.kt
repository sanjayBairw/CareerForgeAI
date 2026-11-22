package com.pynexia.resumebuilder.data.model

import java.util.UUID

data class AdditionalItem(
    val id: String = UUID.randomUUID().toString(),
    var category: String = "Award", // Award, Publication, Volunteer, Language, Extracurricular, Reference, Custom
    var title: String = "",
    var subtitle: String = "",
    var date: String = "",
    var description: String = ""
)
