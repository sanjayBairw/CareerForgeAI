package com.pynexia.resumebuilder.data.model

data class SectionConfig(
    val key: String,
    var title: String,
    var isVisible: Boolean = true,
    var order: Int = 0
)
