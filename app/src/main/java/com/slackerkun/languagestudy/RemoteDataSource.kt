package com.slackerkun.languagestudy

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class RemoteResult(
    val data: List<Situation>,
    val status: RemoteStatus
)

object RemoteDataSource {

    // base URL to your GitHub raw file (no query here)
    private const val BASE_REMOTE_URL =
        "https://raw.githubusercontent.com/Slackerkun/LanguageStudy/main/app/src/main/assets/phrases.json"

    fun fetchSituations(context: Context): RemoteResult {
        // add a cache-buster so GitHub can’t hand us an old copy
        val fullUrl = "$BASE_REMOTE_URL?ts=${System.currentTimeMillis()}"

        return try {
            val connection = URL(fullUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Cache-Control", "no-cache")
            connection.connect()

            val code = connection.responseCode

            if (code == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val json = reader.readText()
                reader.close()

                val gson = Gson()
                val arr = gson.fromJson(json, Array<Situation>::class.java)
                val list = arr?.toList() ?: emptyList()

                RemoteResult(
                    data = list,
                    status = RemoteStatus(
                        source = "GitHub",
                        ok = true,
                        code = 200
                    )
                )
            } else {
                // non-200 → fall back to assets
                val local = AssetsDataSource.loadSituations(context)
                RemoteResult(
                    data = local,
                    status = RemoteStatus(
                        source = "Local (HTTP $code)",
                        ok = false,
                        code = code
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()

            // network/parse/etc → fall back to assets
            val local = AssetsDataSource.loadSituations(context)
            RemoteResult(
                data = local,
                status = RemoteStatus(
                    source = "Local by exception",
                    ok = local.isNotEmpty(),
                    code = null
                )
            )
        }
    }
}
