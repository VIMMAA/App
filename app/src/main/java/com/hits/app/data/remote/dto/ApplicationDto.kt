package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto(
    val id: String,
    val studentId: String,
    val submissionDate: String,
    val status: String,
    val lessons: List<LessonDto>,
    val attachedFiles: List<AttachedFileWithIdDto>,
    val comment: String?,
)