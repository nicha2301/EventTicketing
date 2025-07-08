package com.eventticketing.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class PaymentException(message: String) : RuntimeException(message) 