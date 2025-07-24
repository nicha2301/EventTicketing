package com.nicha.eventticketing.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto

class TicketTypeListConverter {
    @TypeConverter
    fun fromString(value: String?): List<TicketTypeDto>? {
        if (value == null) return null
        val listType = object : TypeToken<List<TicketTypeDto>>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun listToString(list: List<TicketTypeDto>?): String? {
        return Gson().toJson(list)
    }
} 