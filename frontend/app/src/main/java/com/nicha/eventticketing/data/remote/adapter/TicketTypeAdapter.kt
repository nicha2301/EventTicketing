package com.nicha.eventticketing.data.remote.adapter

import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import timber.log.Timber
import java.math.BigDecimal

/**
 * Adapter tùy chỉnh cho TicketTypeDto để xử lý trường hợp thiếu trường quantitySold
 * và chuyển đổi chính xác giá tiền từ BigDecimal sang Double
 */
class TicketTypeAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): TicketTypeDto {
        var id = ""
        var eventId = ""
        var name = ""
        var description: String? = null
        var price = 0.0
        var quantity = 0
        var availableQuantity = 0
        var quantitySold = 0
        var maxTicketsPerCustomer: Int? = null
        var minTicketsPerOrder = 1
        var salesStartDate: String? = null
        var salesEndDate: String? = null
        var isEarlyBird = false
        var isVIP = false
        var isActive = true
        var createdAt: String? = null
        var updatedAt: String? = null
        
        reader.beginObject()
        while (reader.hasNext()) {
            val peek = reader.peek()
            if (peek == JsonReader.Token.NULL) {
                reader.skipValue()
                continue
            }
            
            val fieldName = reader.nextName()
            when (fieldName) {
                "id" -> id = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<String>(); "" } else reader.nextString()
                "eventId" -> eventId = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<String>(); "" } else reader.nextString()
                "name" -> name = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<String>(); "" } else reader.nextString()
                "description" -> description = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
                "price" -> {
                    try {
                        when (reader.peek()) {
                            JsonReader.Token.NULL -> {
                                reader.nextNull<String>()
                                price = 0.0
                            }
                            JsonReader.Token.STRING -> {
                            val priceStr = reader.nextString()
                                price = try {
                                    BigDecimal(priceStr).toDouble()
                                } catch (e: Exception) {
                                    Timber.e(e, "Lỗi khi chuyển đổi giá tiền từ String: $priceStr")
                                    0.0
                                }
                            }
                            else -> price = reader.nextDouble()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Lỗi khi đọc giá tiền")
                        price = 0.0
                    }
                }
                "quantity" -> quantity = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Int>(); 0 } else reader.nextInt()
                "availableQuantity" -> availableQuantity = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Int>(); 0 } else reader.nextInt()
                "quantitySold" -> quantitySold = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Int>(); 0 } else reader.nextInt()
                "maxTicketsPerCustomer" -> maxTicketsPerCustomer = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Int>(); null } else reader.nextInt()
                "minTicketsPerOrder" -> minTicketsPerOrder = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Int>(); 1 } else reader.nextInt()
                "salesStartDate" -> salesStartDate = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
                "salesEndDate" -> salesEndDate = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
                "isEarlyBird" -> isEarlyBird = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Boolean>(); false } else reader.nextBoolean()
                "isVIP" -> isVIP = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Boolean>(); false } else reader.nextBoolean()
                "isActive" -> isActive = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Boolean>(); true } else reader.nextBoolean()
                "createdAt" -> createdAt = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
                "updatedAt" -> updatedAt = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
                else -> {
                    Timber.d("Bỏ qua trường không xác định: $fieldName")
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        
        return TicketTypeDto(
            id = id,
            eventId = eventId,
            name = name,
            description = description,
            price = price,
            quantity = quantity,
            availableQuantity = availableQuantity,
            quantitySold = quantitySold,
            maxTicketsPerCustomer = maxTicketsPerCustomer,
            minTicketsPerOrder = minTicketsPerOrder,
            salesStartDate = salesStartDate,
            salesEndDate = salesEndDate,
            isEarlyBird = isEarlyBird,
            isVIP = isVIP,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    @ToJson
    fun toJson(writer: JsonWriter, value: TicketTypeDto) {
        writer.beginObject()
        writer.name("id").value(value.id)
        writer.name("eventId").value(value.eventId)
        writer.name("name").value(value.name)
        writer.name("description").value(value.description)
        writer.name("price").value(value.price)
        writer.name("quantity").value(value.quantity)
        writer.name("availableQuantity").value(value.availableQuantity)
        writer.name("quantitySold").value(value.quantitySold)
        writer.name("maxTicketsPerCustomer").value(value.maxTicketsPerCustomer)
        writer.name("minTicketsPerOrder").value(value.minTicketsPerOrder)
        writer.name("salesStartDate").value(value.salesStartDate)
        writer.name("salesEndDate").value(value.salesEndDate)
        writer.name("isEarlyBird").value(value.isEarlyBird)
        writer.name("isVIP").value(value.isVIP)
        writer.name("isActive").value(value.isActive)
        writer.name("createdAt").value(value.createdAt)
        writer.name("updatedAt").value(value.updatedAt)
        writer.endObject()
    }
} 