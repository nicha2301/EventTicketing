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
        var quantitySold = 0
        var maxPerOrder: Int? = null
        var minPerOrder = 1
        var saleStartDate: String? = null
        var saleEndDate: String? = null
        
        reader.beginObject()
        while (reader.hasNext()) {
            when (val fieldName = reader.nextName()) {
                "id" -> id = reader.nextString()
                "eventId" -> eventId = reader.nextString()
                "name" -> name = reader.nextString()
                "description" -> description = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
                "price" -> {
                    try {
                        // Đọc giá trị price dưới dạng String để tránh mất độ chính xác
                        if (reader.peek() == JsonReader.Token.STRING) {
                            val priceStr = reader.nextString()
                            // Chuyển đổi từ String sang BigDecimal rồi sang Double để giữ độ chính xác
                            price = BigDecimal(priceStr).toDouble()
                            Timber.d("Đọc giá tiền từ String: $priceStr -> $price")
                        } else {
                            price = reader.nextDouble()
                            Timber.d("Đọc giá tiền trực tiếp: $price")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Lỗi khi đọc giá tiền")
                        price = 0.0
                    }
                }
                "quantity" -> quantity = reader.nextInt()
                "quantitySold" -> quantitySold = if (reader.peek() == JsonReader.Token.NULL) 0 else reader.nextInt()
                "maxPerOrder" -> maxPerOrder = if (reader.peek() == JsonReader.Token.NULL) null else reader.nextInt()
                "minPerOrder" -> minPerOrder = if (reader.peek() == JsonReader.Token.NULL) 1 else reader.nextInt()
                "saleStartDate" -> saleStartDate = if (reader.peek() == JsonReader.Token.NULL) null else reader.nextString()
                "saleEndDate" -> saleEndDate = if (reader.peek() == JsonReader.Token.NULL) null else reader.nextString()
                else -> {
                    Timber.d("Bỏ qua trường không xác định: $fieldName")
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        
        Timber.d("Tạo TicketTypeDto với giá: $price")
        return TicketTypeDto(
            id = id,
            eventId = eventId,
            name = name,
            description = description,
            price = price,
            quantity = quantity,
            quantitySold = quantitySold,
            maxPerOrder = maxPerOrder,
            minPerOrder = minPerOrder,
            saleStartDate = saleStartDate,
            saleEndDate = saleEndDate
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
        writer.name("quantitySold").value(value.quantitySold)
        writer.name("maxPerOrder").value(value.maxPerOrder)
        writer.name("minPerOrder").value(value.minPerOrder)
        writer.name("saleStartDate").value(value.saleStartDate)
        writer.name("saleEndDate").value(value.saleEndDate)
        writer.endObject()
    }
} 