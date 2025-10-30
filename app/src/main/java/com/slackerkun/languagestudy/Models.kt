package com.slackerkun.languagestudy

data class Situation(
    val id: String,
    val title: String,
    val phrases: List<Phrase>
)

data class Phrase(
    val jp: String,
    val romaji: String,
    val en: String
)
