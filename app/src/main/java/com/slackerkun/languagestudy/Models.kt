package com.slackerkun.languagestudy

data class SituationalPhrase(
    val jp: String,
    val romaji: String,
    val en: String,
    val note: String? = null
)

data class Situation(
    val id: String,
    val title: String,
    val phrases: List<SituationalPhrase>
)
