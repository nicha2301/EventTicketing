package com.eventticketing.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Ngoại lệ khi không tìm thấy tài nguyên
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * Ngoại lệ khi tài nguyên đã tồn tại
 */
@ResponseStatus(HttpStatus.CONFLICT)
class ResourceAlreadyExistsException(message: String) : RuntimeException(message)

/**
 * Ngoại lệ khi yêu cầu không hợp lệ
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(message: String) : RuntimeException(message)

/**
 * Ngoại lệ khi người dùng không được xác thực
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException(message: String) : RuntimeException(message)

/**
 * Ngoại lệ khi xử lý thanh toán
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class PaymentProcessingException(message: String) : RuntimeException(message)

/**
 * Ngoại lệ khi quá số lượng vé cho phép
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class TicketLimitExceededException(message: String) : RuntimeException(message)

/**
 * Ngoại lệ khi tài nguyên đã được sử dụng
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ResourceInUseException(message: String) : RuntimeException(message)

/**
 * Ngoại lệ khi có lỗi xảy ra trong quá trình tải lên file
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class FileUploadException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
/**
 * Ngoại lệ khi gặp lỗi trong quá trình gửi email
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class EmailSendingException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class TicketSoldOutException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidQRCodeException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class TicketAlreadyCheckedException(message: String) : RuntimeException(message) 