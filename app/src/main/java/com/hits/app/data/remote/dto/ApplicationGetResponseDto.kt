package com.hits.app.data.remote.dto

import com.hits.app.application.ApplicationItem
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationGetResponseDto (
    val id: String,
    val studentId: String,
    val submissionDate: String,
    val status: String,
    val lessons: List<LessonDto>,
    val attachedFiles: List<AttachedFileWithIdDto>,
    val comment: String
)