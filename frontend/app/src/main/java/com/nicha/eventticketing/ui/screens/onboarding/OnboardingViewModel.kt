package com.nicha.eventticketing.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    /**
     * Mark onboarding as completed
     */
    suspend fun setOnboardingCompleted() {
        try {
            preferencesManager.setOnboardingCompleted(true)
            Timber.d("Onboarding đã được đánh dấu là hoàn thành")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đánh dấu onboarding hoàn thành")
        }
    }
} 