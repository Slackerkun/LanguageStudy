package com.slackerkun.languagestudy

import android.content.Context

// SharedPreferences file and keys
private const val PREFS_NAME = "nihongo_prefs"
private const val KEY_HIDDEN_PHRASES = "hidden_phrases"
private const val KEY_HIDDEN_CATEGORIES = "hidden_categories"

/* =======================
   PHRASE FUNCTIONS
   ======================= */

/**
 * Retrieve all hidden phrase keys.
 */
fun getHiddenPhrases(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getStringSet(KEY_HIDDEN_PHRASES, emptySet()) ?: emptySet()
}

/**
 * Add a phrase to the hidden list.
 */
fun addHiddenPhrase(context: Context, phraseKey: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet(KEY_HIDDEN_PHRASES, emptySet()) ?: emptySet()
    val updated = current.toMutableSet().apply { add(phraseKey) }
    prefs.edit().putStringSet(KEY_HIDDEN_PHRASES, updated).apply()
}

/**
 * Remove a single phrase from the hidden list.
 */
fun removeHiddenPhrase(context: Context, phraseKey: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet(KEY_HIDDEN_PHRASES, emptySet()) ?: emptySet()
    if (!current.contains(phraseKey)) return
    val updated = current.toMutableSet().apply { remove(phraseKey) }
    prefs.edit().putStringSet(KEY_HIDDEN_PHRASES, updated).apply()
}

/**
 * Clear all hidden phrases.
 */
fun clearHiddenPhrases(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putStringSet(KEY_HIDDEN_PHRASES, emptySet()).apply()
}

/* =======================
   CATEGORY FUNCTIONS
   ======================= */

/**
 * Retrieve all hidden category IDs.
 */
fun getHiddenCategories(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getStringSet(KEY_HIDDEN_CATEGORIES, emptySet()) ?: emptySet()
}

/**
 * Add a category ID to the hidden list.
 */
fun addHiddenCategory(context: Context, categoryId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet(KEY_HIDDEN_CATEGORIES, emptySet()) ?: emptySet()
    val updated = current.toMutableSet().apply { add(categoryId) }
    prefs.edit().putStringSet(KEY_HIDDEN_CATEGORIES, updated).apply()
}

/**
 * Remove a single category ID from the hidden list.
 */
fun removeHiddenCategory(context: Context, categoryId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet(KEY_HIDDEN_CATEGORIES, emptySet()) ?: emptySet()
    if (!current.contains(categoryId)) return
    val updated = current.toMutableSet().apply { remove(categoryId) }
    prefs.edit().putStringSet(KEY_HIDDEN_CATEGORIES, updated).apply()
}

/**
 * Overwrite the hidden category set with a new one (used for checkbox-style UI).
 */
fun saveHiddenCategories(context: Context, categories: Set<String>) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putStringSet(KEY_HIDDEN_CATEGORIES, categories).apply()
}

/**
 * Clear all hidden categories.
 */
fun clearHiddenCategories(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putStringSet(KEY_HIDDEN_CATEGORIES, emptySet()).apply()
}

/* =======================
   RESET ALL
   ======================= */

/**
 * Clear all hidden data (phrases + categories).
 */
fun clearAllHidden(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
        .putStringSet(KEY_HIDDEN_PHRASES, emptySet())
        .putStringSet(KEY_HIDDEN_CATEGORIES, emptySet())
        .apply()
}
