package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LessonDto(
    val id: String,
    val name: String,
    val startTime: String,
    val endTime: String,
)
