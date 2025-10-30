package com.slackerkun.languagestudy

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AssetsDataSource {

    private const val FILE_NAME = "phrases.json"

    fun loadSituations(context: Context): List<Situation> {
        return try {
            // read the JSON text from /assets/phrases.json
            val json = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }

            // tell Gson what type we want (List<Situation>)
            val type = object : TypeToken<List<Situation>>() {}.type

            Gson().fromJson<List<Situation>>(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
