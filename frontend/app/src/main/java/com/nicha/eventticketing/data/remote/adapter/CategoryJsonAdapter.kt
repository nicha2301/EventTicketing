package com.nicha.eventticketing.data.remote.adapter

import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.squareup.moshi.*
import okio.Buffer
import timber.log.Timber
import java.io.IOException

/**
 * Adapter tùy chỉnh để xử lý JSON response từ API categories
 */
class CategoryJsonAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): List<CategoryDto> {
        // Ghi lại JSON để debug
        val buffer = Buffer()
        try {
            reader.beginObject()
            
            // Tìm trường "data" trong JSON
            while (reader.hasNext()) {
                val name = reader.nextName()
                if (name == "data") {
                    // Kiểm tra xem data là mảng hay đối tượng
                    if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                        // Nếu là mảng, đọc trực tiếp
                        val moshi = Moshi.Builder().build()
                        val listType = Types.newParameterizedType(List::class.java, CategoryDto::class.java)
                        val adapter = moshi.adapter<List<CategoryDto>>(listType)
                        return adapter.fromJson(reader) ?: emptyList()
                    } else if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                        // Nếu là đối tượng, đọc đối tượng và tìm mảng bên trong
                        reader.beginObject()
                        while (reader.hasNext()) {
                            val innerName = reader.nextName()
                            if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                                // Tìm thấy mảng, đọc nó
                                val moshi = Moshi.Builder().build()
                                val listType = Types.newParameterizedType(List::class.java, CategoryDto::class.java)
                                val adapter = moshi.adapter<List<CategoryDto>>(listType)
                                return adapter.fromJson(reader) ?: emptyList()
                            } else {
                                // Bỏ qua trường không phải mảng
                                reader.skipValue()
                            }
                        }
                        reader.endObject()
                    } else {
                        // Nếu không phải mảng hoặc đối tượng, bỏ qua
                        reader.skipValue()
                    }
                } else {
                    // Bỏ qua các trường khác
                    reader.skipValue()
                }
            }
            
            reader.endObject()
            return emptyList()
        } catch (e: IOException) {
            Timber.e(e, "Lỗi khi đọc JSON")
            return emptyList()
        }
    }
} 