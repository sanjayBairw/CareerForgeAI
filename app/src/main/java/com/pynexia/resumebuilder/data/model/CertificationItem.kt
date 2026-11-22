package com.pynexia.resumebuilder.data.model

import java.util.UUID

data class CertificationItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var issuingOrganization: String = "",
    var issueDate: String = "",
    var credentialId: String = "",
    var credentialUrl: String = ""
)
