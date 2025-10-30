package com.slackerkun.languagestudy

import android.content.Context
import android.widget.Toast
import android.util.Log
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

object RemoteDataSource {

    private const val TAG = "RemoteDataSource"

    // your public raw URL
    private const val REMOTE_URL =
        "https://raw.githubusercontent.com/Slackerkun/LanguageStudy/main/app/src/main/assets/phrases.json"

    fun fetchSituations(context: Context): List<Situation> {
        return try {
            val connection = URL(REMOTE_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.connect()

            val code = connection.responseCode
            Log.d(TAG, "HTTP response code: $code")

            if (code == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                Log.d(TAG, "Loaded JSON from remote, length=${response.length}")
                Toast.makeText(context, "Loaded from GitHub ✅", Toast.LENGTH_SHORT).show()

                Gson().fromJson(response, Array<Situation>::class.java).toList()
            } else {
                Log.w(TAG, "Remote fetch failed, code=$code, using assets")
                Toast.makeText(context, "Loaded from local (remote HTTP $code)", Toast.LENGTH_SHORT).show()
                AssetsDataSource.loadSituations(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Remote fetch error: ${e.message}", e)
            Toast.makeText(context, "Loaded from local (exception)", Toast.LENGTH_SHORT).show()
            AssetsDataSource.loadSituations(context)
        }
    }
}
