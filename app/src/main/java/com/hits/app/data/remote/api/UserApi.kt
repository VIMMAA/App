package com.hits.app.data.remote.api

import com.hits.app.data.remote.dto.LoginResponseDto
import com.hits.app.data.remote.dto.ProfileResponseDto
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApi {
    @Serializable
    data class LoginParams(
        val email: String,
        val password: String
    )

    @Serializable
    data class RegisterParams(
        val firstName: String,
        val middleName: String,
        val lastName: String,
        val birthday: String,
        val email: String,
        val password: String
    )

    @POST("User/register")
    suspend fun register(@Body body: RegisterParams): Response<LoginResponseDto>

    @POST("User/login")
    suspend fun login(@Body body: LoginParams): Response<LoginResponseDto>

    @POST("User/logout")
    suspend fun logout(@Header("Authorization") token: String)

    @GET("User/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponseDto>
}
