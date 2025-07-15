package com.nicha.eventticketing.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Lớp tiện ích chứa các hàm hỗ trợ animation
 */
object AnimationUtils {
    
    /**
     * Giải pháp thay thế cho animateDpAsState
     * @param targetValue Giá trị đích của animation
     * @param animationSpec Thông số animation
     * @return State<Dp> chứa giá trị hiện tại của animation
     */
    @Composable
    fun safeAnimateDp(
        targetValue: Dp,
        animationSpec: AnimationSpec<Dp> = spring(stiffness = Spring.StiffnessLow),
        label: String = "DpAnimation"
    ): State<Dp> {
        val animatable = remember { Animatable(targetValue, Dp.VectorConverter) }
        
        LaunchedEffect(targetValue) {
            animatable.animateTo(
                targetValue = targetValue,
                animationSpec = animationSpec
            )
        }
        
        return animatable.asState()
    }
    
    /**
     * Giải pháp thay thế cho animateFloatAsState
     * @param targetValue Giá trị đích của animation
     * @param animationSpec Thông số animation
     * @return State<Float> chứa giá trị hiện tại của animation
     */
    @Composable
    fun safeAnimateFloat(
        targetValue: Float,
        animationSpec: AnimationSpec<Float> = tween(300),
        label: String = "FloatAnimation"
    ): State<Float> {
        val animatable = remember { Animatable(targetValue) }
        
        LaunchedEffect(targetValue) {
            animatable.animateTo(
                targetValue = targetValue,
                animationSpec = animationSpec
            )
        }
        
        return animatable.asState()
    }
    
    /**
     * Tạo hiệu ứng pulse (nhấp nháy) cho một thành phần
     * @param initialValue Giá trị ban đầu (thường là 1f)
     * @param targetValue Giá trị đích (thường là 1.05f)
     * @param duration Thời gian của một chu kỳ animation (ms)
     * @param repeatForever Có lặp lại vô hạn hay không
     * @return State<Float> chứa giá trị hiện tại của animation
     */
    @Composable
    fun pulseAnimation(
        initialValue: Float = 1f,
        targetValue: Float = 1.05f,
        duration: Int = 1000,
        repeatForever: Boolean = true
    ): State<Float> {
        val animatable = remember { Animatable(initialValue) }
        var isAnimatingToTarget by remember { mutableStateOf(true) }
        
        LaunchedEffect(Unit) {
            if (repeatForever) {
                while (true) {
                    val target = if (isAnimatingToTarget) targetValue else initialValue
                    launch {
                        animatable.animateTo(
                            targetValue = target,
                            animationSpec = tween(duration)
                        )
                    }
                    isAnimatingToTarget = !isAnimatingToTarget
                }
            } else {
                animatable.animateTo(
                    targetValue = targetValue,
                    animationSpec = tween(duration)
                )
                animatable.animateTo(
                    targetValue = initialValue,
                    animationSpec = tween(duration)
                )
            }
        }
        
        return animatable.asState()
    }
    
    /**
     * Tạo hiệu ứng bounce (nảy) cho một thành phần
     * @param initialValue Giá trị ban đầu
     * @param bounceHeight Độ cao nảy
     * @return State<Float> chứa giá trị hiện tại của animation
     */
    @Composable
    fun bounceAnimation(
        initialValue: Float = 0f,
        bounceHeight: Float = 20f
    ): State<Float> {
        val animatable = remember { Animatable(initialValue) }
        
        LaunchedEffect(Unit) {
            while (true) {
                animatable.animateTo(
                    targetValue = bounceHeight,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                animatable.animateTo(
                    targetValue = initialValue,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
        
        return animatable.asState()
    }
    
    /**
     * Tạo hiệu ứng fade (mờ dần) cho một thành phần
     * @param initialValue Giá trị ban đầu (thường là 0f)
     * @param targetValue Giá trị đích (thường là 1f)
     * @param duration Thời gian của animation (ms)
     * @return State<Float> chứa giá trị hiện tại của animation
     */
    @Composable
    fun fadeAnimation(
        initialValue: Float = 0f,
        targetValue: Float = 1f,
        duration: Int = 500
    ): State<Float> {
        val animatable = remember { Animatable(initialValue) }
        
        LaunchedEffect(Unit) {
            animatable.animateTo(
                targetValue = targetValue,
                animationSpec = tween(duration)
            )
        }
        
        return animatable.asState()
    }
} 