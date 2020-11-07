package com.rkhrapunov.versustest.framework.helpers

import android.annotation.SuppressLint
import com.rkhrapunov.versustest.framework.ContestantsApi
import com.rkhrapunov.versustest.framework.ContestantsInfo
import com.rkhrapunov.versustest.framework.ContestantsStatsInfo
import com.rkhrapunov.versustest.framework.QuizShortInfo
import com.rkhrapunov.versustest.framework.SuperCategory
import com.rkhrapunov.versustest.framework.Category
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext

class RestApiHelper {

    enum class RequestType {
        SUPER_CATEGORIES,
        CATEGORIES,
        QUIZ,
        ALL_QUIZZES,
        QUIZ_STATS,
        WINNER
    }

    private lateinit var mRetrofit: Retrofit

    private val mTrustAllCerts = arrayOf(
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) { }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) { }

            override fun getAcceptedIssuers() = emptyArray<X509Certificate>()
        }
    )

    inner class ServerResponseCallback<T>(
        private val mOnSuccessAction: (response: Response<T>) -> Unit,
        private val mOnFailAction: (t: Throwable) -> Unit) : Callback<T> {

        override fun onResponse(call: Call<T>, response: Response<T>) = mOnSuccessAction.invoke(response)

        override fun onFailure(call: Call<T>, t: Throwable) = mOnFailAction.invoke(t)
    }

    companion object {
        private const val BASE_URL = "https://185.177.114.72/"
    }

    init {
        setupRetrofit()
    }

    fun makeRequest(requestType: RequestType,
                    onSuccessAction: (response: Response<*>) -> Unit,
                    onFailAction: (t: Throwable) -> Unit,
                    lang: String = EMPTY_STRING,
                    public_super_category: String = EMPTY_STRING,
                    public_category: String = EMPTY_STRING,
                    public_quiz: String = EMPTY_STRING,
                    bodyData: String = EMPTY_STRING) {
        Timber.d("makeRequest()")
        val creationPart = mRetrofit.create(ContestantsApi::class.java)
        when (requestType) {
            RequestType.SUPER_CATEGORIES -> creationPart.getSuperCategories(lang).enqueue(ServerResponseCallback<List<SuperCategory>>(onSuccessAction, onFailAction))
            RequestType.CATEGORIES -> creationPart.getCategories(lang, public_super_category).enqueue(ServerResponseCallback<List<Category>>(onSuccessAction, onFailAction))
            RequestType.ALL_QUIZZES -> creationPart.getAllQuizzes(lang, public_super_category, public_category).enqueue(ServerResponseCallback<List<QuizShortInfo>>(onSuccessAction, onFailAction))
            RequestType.QUIZ -> creationPart.getQuiz(lang, public_super_category, public_category, public_quiz).enqueue(ServerResponseCallback<List<ContestantsInfo>>(onSuccessAction, onFailAction))
            RequestType.QUIZ_STATS -> creationPart.getQuizStats(lang, public_super_category, public_category, public_quiz).enqueue(ServerResponseCallback<List<ContestantsStatsInfo>>(onSuccessAction, onFailAction))
            RequestType.WINNER -> creationPart.postWinnerInfo(lang, public_super_category, public_category, public_quiz, bodyData).enqueue(ServerResponseCallback<String>(onSuccessAction, onFailAction))
        }
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, mTrustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .sslSocketFactory(sslSocketFactory, mTrustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
    }

    private fun setupRetrofit() {
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getUnsafeOkHttpClient().build())
            .build()
    }
}