package com.hits.app.data.remote.api

import com.hits.app.data.remote.dto.LessonDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ScheduleApi {

    @GET("Schedule")
    suspend fun getSchedule(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
    ): Response<List<LessonDto>>
}