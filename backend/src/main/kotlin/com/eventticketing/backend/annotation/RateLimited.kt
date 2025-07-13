package com.eventticketing.backend.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RateLimited(
    val maxRequests: Int = 5,
    val windowSeconds: Int = 60
) 