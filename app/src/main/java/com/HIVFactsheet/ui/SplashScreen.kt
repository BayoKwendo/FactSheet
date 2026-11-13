package com.HIVFactsheet.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.HIVFactsheet.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoBounce",
    )
    LaunchedEffect(Unit) {
        started = true
        delay(1300)
        onDone()
    }
    Column(
        modifier = Modifier.fillMaxSize().background(BrandPurple),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            // Plain raster logo: painterResource can't load the adaptive-icon
            // XML that shadows @mipmap/ic_launcher on API 26+.
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(160.dp).scale(scale),
        )
        androidx.compose.material3.Text(
            text = "FactSheet",
            color = BrandCyan,
            fontSize = 30.sp,
            modifier = Modifier.padding(top = 16.dp),
        )
        androidx.compose.material3.Text(
            text = "BE INFORMED · BE FACTUAL · STOP STIGMA",
            color = BrandCyan,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp),
        )
    }
}
