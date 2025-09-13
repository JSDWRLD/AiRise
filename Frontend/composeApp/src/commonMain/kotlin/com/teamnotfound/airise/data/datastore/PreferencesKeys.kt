package com.teamnotfound.airise.data.datastore

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore

object PreferencesKeys {
    val LAST_COMPLETION_TIMESTAMP = longPreferencesKey("last_completion_time")

}


private suspend fun saveCompletionTime(time: Long) {
    dataStore.edit { preferences -> prefs[ChallengePrefs.LAST_COMPLETED_TIMESTAMP] = time }
}


private suspend fun canIncrementStreak(): Boolean {
    val preferences = dataStore.data.first()
}