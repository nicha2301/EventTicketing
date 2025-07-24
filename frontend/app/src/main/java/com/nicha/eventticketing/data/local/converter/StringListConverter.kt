package com.nicha.eventticketing.data.local.converter

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }
    @TypeConverter
    fun listToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }
} 