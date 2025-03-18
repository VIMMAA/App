package com.hits.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val birthday: String,
    val email: String,
    val role: String,
    val password: String
)
