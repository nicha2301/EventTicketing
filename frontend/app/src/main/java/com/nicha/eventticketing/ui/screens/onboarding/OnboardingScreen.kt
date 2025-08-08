package com.nicha.eventticketing.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.R
import com.nicha.eventticketing.viewmodel.OnboardingViewModel
import com.nicha.eventticketing.ui.screens.onboarding.OnboardingContent
import kotlinx.coroutines.launch
import com.nicha.eventticketing.ui.theme.BrandOrange

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = OnboardingContent.pages

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            OnboardingBackground(page = pages[index])
        }

        // Overlay content at bottom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val current = pages[pagerState.currentPage]

            Text(
                text = current.title,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = Color(0xFFFAFAFA)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = current.description,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFFFAFAFA).copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(18.dp))

            // Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { i ->
                    val dotColor = if (i == pagerState.currentPage) BrandOrange else BrandOrange.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Buttons row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch { viewModel.setOnboardingCompleted() }
                        onGetStarted()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandOrange)
                ) {
                    Text(text = "Get Started")
                }

                Button(
                    onClick = {
                        scope.launch { viewModel.setOnboardingCompleted() }
                        onLogin()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White)
                ) {
                    Text(text = "Log in")
                }
            }
        }
    }
}

@Composable
private fun OnboardingBackground(page: OnboardingVisual) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (page.isSolidBrandBackground) {
            Box(modifier = Modifier.fillMaxSize().background(BrandOrange))
        } else if (page.backgroundResId != null) {
            Image(
                painter = painterResource(id = page.backgroundResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.72f),
                            Color.Black.copy(alpha = 1.0f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}