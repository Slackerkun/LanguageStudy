package com.slackerkun.languagestudy

import android.content.Context
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

data class RemoteResult(
    val data: List<Situation>,
    val status: RemoteStatus
)

object RemoteDataSource {

    private const val BASE_REMOTE_URL =
        "https://raw.githubusercontent.com/Slackerkun/LanguageStudy/main/app/src/main/assets/phrases.json"

    fun fetchSituations(context: Context): RemoteResult {
        // add both timestamp + random nonce to really force a new URL
        val nonce = Random.nextInt(0, Int.MAX_VALUE)
        val fullUrl = "$BASE_REMOTE_URL?ts=${System.currentTimeMillis()}&n=$nonce"

        return try {
            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"

                // aggressive no-cache hints
                useCaches = false
                setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                setRequestProperty("Pragma", "no-cache")
                setRequestProperty("Expires", "0")
            }

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
