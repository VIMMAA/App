package com.hits.app.data.remote.api

import com.hits.app.data.remote.dto.LessonDto
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface ApplicationApi {
    @POST("")
    suspend fun postApplication(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
    ): Response<List<LessonDto>>
}