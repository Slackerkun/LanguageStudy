package com.slackerkun.languagestudy

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// This is the shape MainActivity expects
data class RemoteResult(
    val data: List<Situation>,
    val status: RemoteStatus
)

object RemoteDataSource {

    // GitHub raw JSON (must stay public)
    private const val REMOTE_URL =
        "https://raw.githubusercontent.com/Slackerkun/LanguageStudy/main/app/src/main/assets/phrases.json"

    fun fetchSituations(context: Context): RemoteResult {
        // 1) try remote
        return try {
            val connection = URL(REMOTE_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == 200) {
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
                // non-200 → fallback to assets
                val local = AssetsDataSource.loadSituations(context)
                RemoteResult(
                    data = local,
                    status = RemoteStatus(
                        source = "Local (HTTP ${connection.responseCode})",
                        ok = false,
                        code = connection.responseCode
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()

            // 2) remote failed hard → fallback to assets
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
