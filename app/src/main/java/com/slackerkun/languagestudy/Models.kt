package com.slackerkun.languagestudy

// Basic structure for each phrase
data class Phrase(
    val jp: String,
    val romaji: String,
    val en: String
)

// A group of phrases under one topic
data class Situation(
    val id: String,
    val title: String,
    val phrases: List<Phrase>
)

// Used to show if remote GitHub pull succeeded
data class RemoteStatus(
    val source: String,
    val ok: Boolean,
    val code: Int?
)
