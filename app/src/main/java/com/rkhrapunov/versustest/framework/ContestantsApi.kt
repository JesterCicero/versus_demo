package com.rkhrapunov.versustest.framework

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContestantsApi {
    @GET("/public_quizess")
    fun getAllQuizzes(): Call<List<QuizShortInfo>>
    @GET("/public_quizess/{contestantsType}")
    fun getQuiz(@Path("contestantsType") contestantsType: String): Call<List<ContestantsInfo>>
    @GET("/public_quizess_stats/{contestantsType}")
    fun getQuizStats(@Path("contestantsType") contestantsType: String): Call<List<ContestantsStatsInfo>>
    @POST("/winner/")
    fun postWinnerInfo(@Body data: String): Call<String>
}