package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttachedFileDto (
    val name: String,
    val data: String
)