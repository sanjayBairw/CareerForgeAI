package com.pynexia.resumebuilder.data.model

import java.util.UUID

data class ProjectItem(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var role: String = "",
    var techStack: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var description: String = "",
    var liveUrl: String = "",
    var repoUrl: String = ""
)
