package com.slackerkun.languagestudy

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException

object AssetsDataSource {

    private const val ASSET_FILE = "phrases.json"

    fun loadSituations(context: Context): List<Situation> {
        return try {
            val input = context.assets.open(ASSET_FILE)
            val json = input.bufferedReader().use { it.readText() }

            val gson = Gson()
            val arr = gson.fromJson(json, Array<Situation>::class.java)

            arr?.toList() ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
