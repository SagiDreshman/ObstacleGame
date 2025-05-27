package com.example.targil1

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScoreManager {
    data class Score(val name: String, val coins: Int, val distance: Int, val lat: Double,
                     val lng: Double)

    private val scores = mutableListOf<Score>()
    private lateinit var sharedPrefs: android.content.SharedPreferences
    private val gson = Gson()
    private const val PREFS_NAME = "highscore_prefs"
    private const val SCORES_KEY = "scores_list"

    fun init(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadScores()
    }

    private fun loadScores() {
        val json = sharedPrefs.getString(SCORES_KEY, null) ?: return
        val type = object : TypeToken<List<Score>>() {}.type
        val loadedScores: List<Score> = gson.fromJson(json, type)
        scores.clear()
        scores.addAll(loadedScores)
    }

    private fun saveScores() {
        val json = gson.toJson(scores)
        sharedPrefs.edit().putString(SCORES_KEY, json).apply()
    }

    fun addScore(score: Score) {
        scores.add(score)
        scores.sortByDescending { it.distance }
        if (scores.size > 10) {
            scores.subList(10, scores.size).clear()
        }

        saveScores()
    }

    fun getScores(): List<Score> {
        return scores
    }

    fun clearScores() {
        scores.clear()
        saveScores()
    }
}
