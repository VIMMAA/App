package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseDto(
    val profile: ProfileDto,
    val message: String
)
