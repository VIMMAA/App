package com.hits.app.data.remote.api

import com.hits.app.data.remote.dto.ApplicationDto
import com.hits.app.data.remote.dto.ApplicationShortDto
import com.hits.app.data.remote.dto.NewApplicationRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApplicationApi {
    @POST("Application")
    suspend fun createApplication(
        @Header("Authorization") token: String,
        @Body body: NewApplicationRequestDto
    ): Response<Unit>

    @GET("Application/{id}")
    suspend fun getApplication(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApplicationDto>

    @PUT("Application/{id}")
    suspend fun editApplication(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: NewApplicationRequestDto
    ): Response<Unit>

    @DELETE("Application/{id}")
    suspend fun deleteApplication(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @GET("Application/applicationList/{StudentId}")
    suspend fun getStudentApplicationList(
        @Header("Authorization") token: String,
        @Path("StudentId") id: String
    ): Response<List<ApplicationShortDto>>
}