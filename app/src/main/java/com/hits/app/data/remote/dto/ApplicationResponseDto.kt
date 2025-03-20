package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationResponseDto (
    val status: String,
    val message: String
)