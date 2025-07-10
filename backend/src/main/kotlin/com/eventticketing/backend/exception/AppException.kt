package com.eventticketing.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Enum định nghĩa các loại lỗi trong ứng dụng
 */
enum class ErrorType(val status: HttpStatus, val prefix: String) {
    // Lỗi liên quan đến Resource
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ERR_RESOURCE_NOT_FOUND"),
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "ERR_RESOURCE_ALREADY_EXISTS"),
    RESOURCE_IN_USE(HttpStatus.BAD_REQUEST, "ERR_RESOURCE_IN_USE"),
    
    // Lỗi liên quan đến xác thực và phân quyền
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ERR_UNAUTHORIZED"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ERR_ACCESS_DENIED"),
    
    // Lỗi liên quan đến dữ liệu và yêu cầu
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "ERR_BAD_REQUEST"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "ERR_VALIDATION"),
    
    // Lỗi liên quan đến Event
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ERR_EVENT_NOT_FOUND"),
    
    // Lỗi liên quan đến Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ERR_COMMENT_NOT_FOUND"),
    COMMENT_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "ERR_COMMENT_VALIDATION"),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ERR_COMMENT_ACCESS_DENIED"),
    COMMENT_ALREADY_REPORTED(HttpStatus.CONFLICT, "ERR_COMMENT_ALREADY_REPORTED"),
    
    // Lỗi liên quan đến Rating
    RATING_NOT_FOUND(HttpStatus.NOT_FOUND, "ERR_RATING_NOT_FOUND"),
    RATING_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "ERR_RATING_VALIDATION"),
    RATING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ERR_RATING_ACCESS_DENIED"),
    RATING_ALREADY_EXISTS(HttpStatus.CONFLICT, "ERR_RATING_ALREADY_EXISTS"),
    RATING_ALREADY_REPORTED(HttpStatus.CONFLICT, "ERR_RATING_ALREADY_REPORTED"),
    
    // Lỗi liên quan đến Payment
    PAYMENT_ERROR(HttpStatus.BAD_REQUEST, "ERR_PAYMENT"),
    
    // Lỗi liên quan đến Ticket
    TICKET_ERROR(HttpStatus.BAD_REQUEST, "ERR_TICKET"),
    TICKET_SOLD_OUT(HttpStatus.BAD_REQUEST, "ERR_TICKET_SOLD_OUT"),
    TICKET_ALREADY_CHECKED(HttpStatus.BAD_REQUEST, "ERR_TICKET_ALREADY_CHECKED"),
    TICKET_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ERR_TICKET_LIMIT_EXCEEDED"),
    INVALID_QR_CODE(HttpStatus.BAD_REQUEST, "ERR_INVALID_QR_CODE"),
    
    // Lỗi liên quan đến tác vụ hệ thống
    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "ERR_FILE_UPLOAD"),
    EMAIL_SENDING_ERROR(HttpStatus.BAD_REQUEST, "ERR_EMAIL_SENDING"),
    
    // Lỗi khác
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ERR_INTERNAL_SERVER");
}

/**
 * Lớp exception cơ sở cho tất cả các exception trong ứng dụng
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
open class AppException(
    val errorType: ErrorType,
    override val message: String,
    val errorCode: String = errorType.prefix,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// Các lớp con cụ thể cho từng loại lỗi

// Lỗi không tìm thấy tài nguyên
class ResourceNotFoundException(message: String) : 
    AppException(ErrorType.RESOURCE_NOT_FOUND, message)

// Lỗi tài nguyên đã tồn tại
class ResourceAlreadyExistsException(message: String) : 
    AppException(ErrorType.RESOURCE_ALREADY_EXISTS, message)

// Lỗi liên quan đến Event
class EventNotFoundException(message: String) : 
    AppException(ErrorType.EVENT_NOT_FOUND, message)

// Lỗi liên quan đến Comment
class CommentNotFoundException(message: String) : 
    AppException(ErrorType.COMMENT_NOT_FOUND, message)

class CommentValidationException(message: String) : 
    AppException(ErrorType.COMMENT_VALIDATION_ERROR, message)

class CommentAccessDeniedException(message: String) : 
    AppException(ErrorType.COMMENT_ACCESS_DENIED, message)

class CommentAlreadyReportedException(message: String) : 
    AppException(ErrorType.COMMENT_ALREADY_REPORTED, message)

// Lỗi liên quan đến Rating
class RatingNotFoundException(message: String) : 
    AppException(ErrorType.RATING_NOT_FOUND, message)

class RatingValidationException(message: String) : 
    AppException(ErrorType.RATING_VALIDATION_ERROR, message)

class RatingAccessDeniedException(message: String) : 
    AppException(ErrorType.RATING_ACCESS_DENIED, message)

class RatingAlreadyExistsException(message: String) : 
    AppException(ErrorType.RATING_ALREADY_EXISTS, message)

class RatingAlreadyReportedException(message: String) : 
    AppException(ErrorType.RATING_ALREADY_REPORTED, message)

// Lỗi liên quan đến Payment
class PaymentException(message: String) : 
    AppException(ErrorType.PAYMENT_ERROR, message)

// Lỗi liên quan đến Ticket
class TicketException(message: String) : 
    AppException(ErrorType.TICKET_ERROR, message)

class TicketSoldOutException(message: String) : 
    AppException(ErrorType.TICKET_SOLD_OUT, message)

class TicketAlreadyCheckedException(message: String) : 
    AppException(ErrorType.TICKET_ALREADY_CHECKED, message)

class TicketLimitExceededException(message: String) : 
    AppException(ErrorType.TICKET_LIMIT_EXCEEDED, message)

class InvalidQRCodeException(message: String) : 
    AppException(ErrorType.INVALID_QR_CODE, message)

// Lỗi khác
class BadRequestException(message: String) : 
    AppException(ErrorType.BAD_REQUEST, message)

class UnauthorizedException(message: String) : 
    AppException(ErrorType.UNAUTHORIZED, message)

class ForbiddenException(message: String) : 
    AppException(ErrorType.ACCESS_DENIED, message)

class FileUploadException(message: String, cause: Throwable? = null) : 
    AppException(ErrorType.FILE_UPLOAD_ERROR, message, cause = cause)

class EmailSendingException(message: String) : 
    AppException(ErrorType.EMAIL_SENDING_ERROR, message) 