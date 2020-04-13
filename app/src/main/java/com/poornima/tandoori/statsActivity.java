package com.poornima.tandoori;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class statsActivity extends AppCompatActivity {
    private WebView web;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        web=(WebView)findViewById(R.id.webview);
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        web.setWebChromeClient(new WebChromeClient());
        web.loadUrl("https://www.covid19india.org/");
    }

    @Override
    public void onBackPressed() {
        if(web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
