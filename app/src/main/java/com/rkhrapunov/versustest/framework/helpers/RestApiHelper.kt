package com.rkhrapunov.versustest.framework.helpers

import com.rkhrapunov.versustest.framework.ContestantsApi
import com.rkhrapunov.versustest.framework.ContestantsInfo
import com.rkhrapunov.versustest.framework.ContestantsStatsInfo
import com.rkhrapunov.versustest.framework.QuizShortInfo
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class RestApiHelper {

    enum class RequestType {
        QUIZ,
        ALL_QUIZZES,
        QUIZ_STATS,
        WINNER
    }

    private lateinit var mRetrofit: Retrofit

    inner class ServerResponseCallback<T>(
        private val mOnSuccessAction: (response: Response<T>) -> Unit,
        private val mOnFailAction: (t: Throwable) -> Unit) : Callback<T> {

        override fun onResponse(call: Call<T>, response: Response<T>) = mOnSuccessAction.invoke(response)

        override fun onFailure(call: Call<T>, t: Throwable) = mOnFailAction.invoke(t)
    }

    companion object {
        private const val BASE_URL = "https://185.177.114.72"
    }

    init {
        setupRetrofit()
    }

    fun makeRequest(requestType: RequestType, onSuccessAction: (response: Response<*>) -> Unit,
                    onFailAction: (t: Throwable) -> Unit, requestData: String = EMPTY_STRING) {
        Timber.d("makeRequest()")
        val creationPart = mRetrofit.create(ContestantsApi::class.java)
        when (requestType) {
            RequestType.QUIZ -> creationPart.getQuiz(requestData).enqueue(ServerResponseCallback<List<ContestantsInfo>>(onSuccessAction, onFailAction))
            RequestType.ALL_QUIZZES -> creationPart.getAllQuizzes().enqueue(ServerResponseCallback<List<QuizShortInfo>>(onSuccessAction, onFailAction))
            RequestType.QUIZ_STATS -> creationPart.getQuizStats(requestData).enqueue(ServerResponseCallback<List<ContestantsStatsInfo>>(onSuccessAction, onFailAction))
            RequestType.WINNER -> creationPart.postWinnerInfo(requestData).enqueue(ServerResponseCallback<String>(onSuccessAction, onFailAction))
        }
    }
    private fun setupRetrofit() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor)
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
    }
}