package com.HIVFactsheet.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * The only remaining WebView: external pages (YouTube channel, NASCOP viral-load
 * dashboard) that aren't part of the native fact content.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(title: String, url: String, onBack: () -> Unit) {
    AppScaffold(title = title, onBack = onBack) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    loadUrl(url)
                }
            },
        )
    }
}
