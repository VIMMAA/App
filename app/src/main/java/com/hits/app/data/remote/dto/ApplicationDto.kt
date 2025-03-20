package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto (
    val id: String,
    val status: String,
    val submissionDate: String
)