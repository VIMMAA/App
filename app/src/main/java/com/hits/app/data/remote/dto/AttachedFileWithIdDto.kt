package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttachedFileWithIdDto (
    val id: String,
    val name: String,
    val data: String
)