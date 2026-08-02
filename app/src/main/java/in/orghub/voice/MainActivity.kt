package com.orghub.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var audioManager: AudioManager

    // CHANGE THIS LINE to switch which page opens:
    // Employee = /employee.html   Admin = /admin.html   Customer = /
    private val APP_URL = "https://voice.orghub.in/employee.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        webView = WebView(this)
        setContentView(webView)

        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.mediaPlaybackRequiresUserGesture = false
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.setSupportZoom(false)

        webView.addJavascriptInterface(NativeAudio(), "NativeAudio")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webView.loadUrl(APP_URL)
    }

    inner class NativeAudio {
        @JavascriptInterface
        fun setSpeaker() {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
        }

        @JavascriptInterface
        fun setEarpiece() {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = false
        }

        @JavascriptInterface
        fun setBluetooth() {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            try {
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
            } catch (e: Exception) {}
        }

        @JavascriptInterface
        fun isBluetoothConnected(): Boolean {
            return audioManager.isBluetoothScoAvailableOffCall
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
