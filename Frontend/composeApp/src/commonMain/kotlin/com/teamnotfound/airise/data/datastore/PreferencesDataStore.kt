package com.teamnotfound.airise.data.datastore

import android.content.Context
import androidx.databinding.tool.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

class PreferencesDataStore {

}