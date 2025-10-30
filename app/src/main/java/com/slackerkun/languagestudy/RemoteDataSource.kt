package com.slackerkun.languagestudy

import android.content.Context
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

object RemoteDataSource {

    private const val REMOTE_URL =
        "https://raw.githubusercontent.com/Slackerkun/LanguageStudy/main/app/src/main/assets/phrases.json"

    fun fetchSituations(context: Context): List<Situation> {
        return try {
            val connection = URL(REMOTE_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                Gson().fromJson(response, Array<Situation>::class.java).toList()
            } else {
                AssetsDataSource.loadSituations(context) // fallback
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AssetsDataSource.loadSituations(context)
        }
    }
}
