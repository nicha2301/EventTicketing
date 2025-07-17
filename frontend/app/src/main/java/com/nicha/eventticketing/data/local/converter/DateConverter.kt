package com.nicha.eventticketing.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Converter cho kiểu Date để sử dụng với Room
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 