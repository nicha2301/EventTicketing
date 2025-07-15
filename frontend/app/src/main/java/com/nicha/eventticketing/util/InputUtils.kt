package com.nicha.eventticketing.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow

/**
 * Lớp tiện ích chứa các hàm xử lý input
 */
object InputUtils {
    
    /**
     * Tạo một debounced callback cho input
     * @param delayMillis Thời gian trễ (ms)
     * @param onDebounced Callback được gọi sau khi debounce
     * @return (T) -> Unit: Hàm nhận input và xử lý debounce
     */
    @Composable
    fun <T> debounce(
        delayMillis: Long = 300,
        onDebounced: (T) -> Unit
    ): (T) -> Unit {
        var latestInput by remember { mutableStateOf<T?>(null) }
        var debounceJob by remember { mutableStateOf(false) }
        
        LaunchedEffect(latestInput) {
            if (latestInput != null && !debounceJob) {
                debounceJob = true
                delay(delayMillis)
                latestInput?.let { onDebounced(it) }
                debounceJob = false
            }
        }
        
        return { input ->
            latestInput = input
        }
    }
    
    /**
     * Tạo một throttled callback cho input
     * @param delayMillis Thời gian giữa các lần gọi (ms)
     * @param onThrottled Callback được gọi sau khi throttle
     * @return (T) -> Unit: Hàm nhận input và xử lý throttle
     */
    @Composable
    fun <T> throttle(
        delayMillis: Long = 300,
        onThrottled: (T) -> Unit
    ): (T) -> Unit {
        var throttleTime by remember { mutableStateOf(0L) }
        
        return { input ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - throttleTime >= delayMillis) {
                throttleTime = currentTime
                onThrottled(input)
            }
        }
    }
    
    /**
     * Tạo một Flow với debounce
     * @param delayMillis Thời gian trễ (ms)
     * @return Flow<T>: Flow đã được áp dụng debounce
     */
    @OptIn(FlowPreview::class)
    fun <T> Flow<T>.debounced(delayMillis: Long = 300): Flow<T> {
        return this.debounce(delayMillis)
    }
} 