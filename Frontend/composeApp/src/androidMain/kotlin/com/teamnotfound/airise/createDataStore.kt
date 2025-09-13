package com.teamnotfound.airise

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.teamnotfound.airise.data.datastore.createDataStore
import com.teamnotfound.airise.data.datastore.dataStoreFileName

fun createDataStore(context: Context): DataStore<Preferences> = createDataStore (
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)


/*
fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)

 */