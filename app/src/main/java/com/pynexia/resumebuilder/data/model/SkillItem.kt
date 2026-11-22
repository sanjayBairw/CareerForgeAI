package com.pynexia.resumebuilder.data.model

import java.util.UUID

data class SkillItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var category: String = "Technical", // Technical, Soft, Tools, Languages
    var level: String = "Advanced",     // Beginner, Intermediate, Advanced, Expert
    var rating: Int = 4                 // 1 to 5
)
