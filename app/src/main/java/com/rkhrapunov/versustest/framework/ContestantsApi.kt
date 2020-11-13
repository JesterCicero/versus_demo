package com.rkhrapunov.versustest.framework

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContestantsApi {
    @GET(PUBLIC_SUPER_CATEGORIES_PATH)
    fun getSuperCategories(@Path(LANG_PATH_NAME) lang: String): Call<List<SuperCategory>>

    @GET(PUBLIC_SUPER_CATEGORY_PATH)
    fun getCategories(@Path(LANG_PATH_NAME)
                      lang: String,
                      @Path(PUBLIC_SUPER_CATEGORY_PATH_NAME)
                      public_super_category: String): Call<List<Category>>

    @GET(PUBLIC_CATEGORY_PATH)
    fun getQuizzes(@Path(LANG_PATH_NAME)
                      lang: String,
                      @Path(PUBLIC_SUPER_CATEGORY_PATH_NAME)
                      public_super_category: String,
                      @Path(PUBLIC_CATEGORY_PATH_NAME)
                      public_category: String): Call<List<QuizShortInfo>>

    @GET(ALL_PUBLIC_QUIZZES_PATH)
    fun getAllQuizzes(@Path(LANG_PATH_NAME) lang: String): Call<List<QuizShortInfo>>

    @GET(PUBLIC_QUIZ_PATH)
    fun getQuiz(@Path(LANG_PATH_NAME)
                lang: String,
                @Path(PUBLIC_SUPER_CATEGORY_PATH_NAME)
                public_super_category: String,
                @Path(PUBLIC_CATEGORY_PATH_NAME)
                public_category: String,
                @Path(PUBLIC_QUIZ_TITLE_PATH_NAME)
                public_quiz_title: String): Call<List<ContestantsInfo>>

    @GET(PUBLIC_QUIZ_STATS_PATH)
    fun getQuizStats(@Path(LANG_PATH_NAME)
                     lang: String,
                     @Path(PUBLIC_SUPER_CATEGORY_PATH_NAME)
                     public_super_category: String,
                     @Path(PUBLIC_CATEGORY_PATH_NAME)
                     public_category: String,
                     @Path(PUBLIC_QUIZ_TITLE_PATH_NAME)
                     public_quiz_title: String): Call<List<ContestantsStatsInfo>>

    @POST(PUBLIC_QUIZ_WINNER_PATH)
    fun postWinnerInfo(@Path(LANG_PATH_NAME)
                       lang: String,
                       @Path(PUBLIC_SUPER_CATEGORY_PATH_NAME)
                       public_super_category: String,
                       @Path(PUBLIC_CATEGORY_PATH_NAME)
                       public_category: String,
                       @Path(PUBLIC_QUIZ_TITLE_PATH_NAME)
                       public_quiz_title: String,
                       @Body data: String): Call<String>

    companion object {
        private const val LANG_PATH_NAME = "lang"
        private const val PUBLIC_SUPER_CATEGORY_PATH_NAME = "public_super_category"
        private const val PUBLIC_CATEGORY_PATH_NAME = "public_category"
        private const val PUBLIC_QUIZ_TITLE_PATH_NAME = "title"

        private const val PUBLIC_SUPER_CATEGORIES_PATH = "/public_supercategories/{$LANG_PATH_NAME}"
        private const val PUBLIC_SUPER_CATEGORY_PATH = "$PUBLIC_SUPER_CATEGORIES_PATH/{$PUBLIC_SUPER_CATEGORY_PATH_NAME}"
        private const val PUBLIC_CATEGORY_PATH = "$PUBLIC_SUPER_CATEGORY_PATH/{$PUBLIC_CATEGORY_PATH_NAME}"
        private const val ALL_PUBLIC_QUIZZES_PATH = "${PUBLIC_SUPER_CATEGORIES_PATH}/all_public_quizzes/"
        private const val PUBLIC_QUIZ_PATH = "$PUBLIC_CATEGORY_PATH/{$PUBLIC_QUIZ_TITLE_PATH_NAME}"
        private const val PUBLIC_QUIZ_STATS_PATH = "${PUBLIC_QUIZ_PATH}/stats/"
        private const val PUBLIC_QUIZ_WINNER_PATH = "${PUBLIC_QUIZ_PATH}/winner/"
    }
}