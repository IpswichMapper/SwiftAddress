package com.mapitall.SwiftAddress

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class OAuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        setContentView(webView)

        val oAuthManager = OAuthManager(webView)
    }
}

class OAuthManager(webView: WebView) {
    init {
        webView.webViewClient = OSMOAuthWebViewClient
    }
}