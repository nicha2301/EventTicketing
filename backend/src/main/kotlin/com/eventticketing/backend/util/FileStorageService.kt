package com.eventticketing.backend.util

import com.eventticketing.backend.exception.FileUploadException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileStorageService(
    @Value("\${app.upload.dir:\${user.home}/event-ticketing/uploads}") private val uploadDir: String
) {
    private var fileStorageLocation: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    init {
        try {
            Files.createDirectories(fileStorageLocation)
        } catch (e: Exception) {
            throw FileUploadException("Không thể tạo thư mục để lưu trữ file tải lên.", e)
        }
    }

    /**
     * Lưu trữ file và trả về tên file đã được lưu
     */
    fun storeFile(file: MultipartFile, subfolder: String = ""): String {
        // Chuẩn hóa tên file
        val originalFilename = file.originalFilename ?: "file"
        val fileExtension = originalFilename.substringAfterLast('.', "")
        val uniqueFilename = UUID.randomUUID().toString() + if (fileExtension.isNotEmpty()) ".$fileExtension" else ""

        // Tạo thư mục con nếu cần
        val targetLocation = if (subfolder.isNotEmpty()) {
            val subfolderPath = fileStorageLocation.resolve(subfolder).normalize()
            Files.createDirectories(subfolderPath)
            subfolderPath.resolve(uniqueFilename)
        } else {
            fileStorageLocation.resolve(uniqueFilename)
        }

        try {
            // Copy file vào thư mục lưu trữ
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            // Trả về đường dẫn tương đối để lưu vào database
            return if (subfolder.isNotEmpty()) "$subfolder/$uniqueFilename" else uniqueFilename
        } catch (e: IOException) {
            throw FileUploadException("Không thể lưu trữ file $originalFilename. Vui lòng thử lại!", e)
        }
    }

    /**
     * Lấy file dựa trên tên file
     */
    fun loadFileAsResource(filename: String): Resource {
        try {
            val filePath = fileStorageLocation.resolve(filename).normalize()
            val resource = UrlResource(filePath.toUri())

            if (resource.exists()) {
                return resource
            } else {
                throw FileUploadException("File $filename không tồn tại")
            }
        } catch (e: MalformedURLException) {
            throw FileUploadException("File $filename không tồn tại", e)
        }
    }

    /**
     * Xóa file dựa trên tên file
     */
    fun deleteFile(filename: String): Boolean {
        try {
            val filePath = fileStorageLocation.resolve(filename).normalize()
            return Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            throw FileUploadException("Không thể xóa file $filename", e)
        }
    }

    /**
     * Xác thực loại file ảnh
     */
    fun isImageFile(file: MultipartFile): Boolean {
        val contentType = file.contentType
        return contentType != null && contentType.startsWith("image/")
    }

    /**
     * Tạo URL truy cập tương đối cho file đã tải lên
     */
    fun getFileUrl(filename: String, subfolder: String = ""): String {
        return if (subfolder.isNotEmpty()) "/api/files/$subfolder/$filename" else "/api/files/$filename"
    }
} 