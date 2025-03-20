package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationShortDto(
    val id: String,
    val status: String,
    val submissionDate: String,
)
