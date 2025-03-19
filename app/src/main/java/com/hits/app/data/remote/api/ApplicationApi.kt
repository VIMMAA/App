package com.hits.app.data.remote.api

import com.hits.app.data.remote.dto.LessonDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApplicationApi {
    @POST("Application")
    suspend fun createApplication(
        @Body lessons: String,
        @Body files: String
    ): Response<List<LessonDto>>

    @GET("Application/{id}")
    suspend fun getApplication (

    )

    @PUT("Application/{id}")
    suspend fun editApplication (

    )

    @DELETE("Application/{id}")
    suspend fun deleteApplication (

    )

    @GET("Application/applicationList/{StudentId}")
    suspend fun getStudentApplicationList (

    )
}