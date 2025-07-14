package com.logcat.android;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class PanduanActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panduan);

        webView = findViewById(R.id.webview);

        // Aktifkan fitur dasar WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        
        // Load file HTML dari assets
        loadHtmlGuide();
    }

    private void loadHtmlGuide() {
        try {
            // Baca file HTML dari assets
            String htmlPath = "file:///android_asset/panduan.html";
            webView.loadUrl(htmlPath);
        } catch (Exception e) {
            webView.loadData(
                "<h1>Gagal memuat panduan</h1><p>" + e.getMessage() + "</p>",
                "text/html",
                "UTF-8"
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}