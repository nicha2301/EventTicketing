package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.util.FileStorageService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/files")
class FileController(
    private val fileStorageService: FileStorageService
) {

    /**
     * Truy cập file từ thư mục lưu trữ
     */
    @GetMapping("/{folder}/{filename:.+}")
    fun getFileFromFolder(
        @PathVariable folder: String,
        @PathVariable filename: String,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        return getFileResponse("$folder/$filename", request)
    }

    /**
     * Truy cập file từ thư mục gốc
     */
    @GetMapping("/{filename:.+}")
    fun getFile(
        @PathVariable filename: String,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        return getFileResponse(filename, request)
    }

    /**
     * Phương thức hỗ trợ để phản hồi file
     */
    private fun getFileResponse(filename: String, request: HttpServletRequest): ResponseEntity<Resource> {
        try {
            val resource = fileStorageService.loadFileAsResource(filename)
            
            // Xác định loại nội dung của file
            var contentType: String? = null
            try {
                contentType = request.servletContext.getMimeType(resource.file.absolutePath)
            } catch (e: IOException) {
                // Bỏ qua nếu không xác định được loại nội dung
            }
            
            // Mặc định là loại nội dung octet-stream
            if (contentType == null) {
                contentType = "application/octet-stream"
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.filename + "\"")
                .body(resource)
        } catch (e: ResourceNotFoundException) {
            // Trả về file mặc định nếu không tìm thấy
            return ResponseEntity.notFound().build()
        }
    }
} 