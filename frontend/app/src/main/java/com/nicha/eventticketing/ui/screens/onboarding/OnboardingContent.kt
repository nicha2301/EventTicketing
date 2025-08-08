package com.nicha.eventticketing.ui.screens.onboarding

import com.nicha.eventticketing.R

object OnboardingContent {
    val pages: List<OnboardingVisual> = listOf(
        OnboardingVisual(
            title = "Event App",
            description = "Discover and join the best events around you.",
            backgroundResId = null,
            isSolidBrandBackground = true
        ),
        OnboardingVisual(
            title = "Explore Upcoming and Nearby Events",
            description = "We bring artists and fans together through the power of live events.",
            backgroundResId = R.drawable.onboarding_bg_1
        ),
        OnboardingVisual(
            title = "Accumulate & Collect Loyalty Points",
            description = "300 Points for VIP, backstage and meet & greet free ticket upgrades.",
            backgroundResId = R.drawable.onboarding_bg_2_1
        ),
        OnboardingVisual(
            title = "Get Exclusive Access to tickets",
            description = "All users are guaranteed access to the most in demand event tickets.",
            backgroundResId = R.drawable.onboarding_bg_3
        )
    )
}


