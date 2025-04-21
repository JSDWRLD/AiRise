package com.teamnotfound.airise.room

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toStringList(s: String): List<String> {
        return if (s.isEmpty()) emptyList() else s.split(",")
    }
}