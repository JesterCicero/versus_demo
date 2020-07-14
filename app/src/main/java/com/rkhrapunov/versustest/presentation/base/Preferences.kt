package com.rkhrapunov.versustest.presentation.base

import android.content.SharedPreferences
import org.koin.core.KoinComponent
import org.koin.core.inject

class Preferences : KoinComponent {

    private val mSharedPreferences by inject<SharedPreferences>()

    fun saveQuiz(quiz: String, contestants: Set<String>) = mSharedPreferences.edit().putStringSet(quiz, contestants).apply()
    fun getQuiz(quiz: String): MutableSet<String>? = mSharedPreferences.getStringSet(quiz, emptySet())
    fun removeQuiz(quiz: String) = mSharedPreferences.edit().remove(quiz).apply()

    var quizzes: Set<String>?
        get() = mSharedPreferences.getStringSet(PreferencesKeys.QUIZZES, emptySet())
        set(value) = mSharedPreferences.edit().putStringSet(PreferencesKeys.QUIZZES, value).apply()
}