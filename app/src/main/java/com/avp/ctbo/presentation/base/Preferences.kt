package com.avp.ctbo.presentation.base

import android.content.SharedPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Preferences : KoinComponent {

    private val mSharedPreferences by inject<SharedPreferences>()

    fun saveSuperCategory(superCategoryName: String, categories: Set<String>) = mSharedPreferences.edit().putStringSet(superCategoryName, categories).apply()
    fun getSuperCategory(superCategoryName: String): MutableSet<String>? = mSharedPreferences.getStringSet(superCategoryName, emptySet())
    fun removeSuperCategory(superCategoryName: String) = mSharedPreferences.edit().remove(superCategoryName).apply()

    fun saveCategory(categoryName: String, quizzes: Set<String>) = mSharedPreferences.edit().putStringSet(categoryName, quizzes).apply()
    fun getCategory(categoryName: String): MutableSet<String>? = mSharedPreferences.getStringSet(categoryName, emptySet())
    fun removeCategory(categoryName: String) = mSharedPreferences.edit().remove(categoryName).apply()

    fun saveQuiz(quiz: String, contestants: Set<String>) = mSharedPreferences.edit().putStringSet(quiz, contestants).apply()
    fun getQuiz(quiz: String): MutableSet<String>? = mSharedPreferences.getStringSet(quiz, emptySet())
    fun removeQuiz(quiz: String) = mSharedPreferences.edit().remove(quiz).apply()

    var superCategories: Set<String>?
        get() = mSharedPreferences.getStringSet(PreferencesKeys.SUPER_CATEGORIES, emptySet())
        set(value) = mSharedPreferences.edit().putStringSet(PreferencesKeys.SUPER_CATEGORIES, value).apply()
}