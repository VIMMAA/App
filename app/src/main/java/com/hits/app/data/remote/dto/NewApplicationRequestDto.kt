package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewApplicationRequestDto (
    val lessons: List<String>,
    val files: List<AttachedFileDto>,
)