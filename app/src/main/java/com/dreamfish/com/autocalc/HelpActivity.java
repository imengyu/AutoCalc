package com.dreamfish.com.autocalc;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dreamfish.com.autocalc.utils.StatusBarUtils;
import com.dreamfish.com.autocalc.widgets.MyTitleBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        StatusBarUtils.setLightMode(this);

        MyTitleBar title_bar = findViewById(R.id.title_bar);
        title_bar.setTitle(getTitle());
        title_bar.setLeftIconOnClickListener((View v) -> finish());

        WebView myWebView = findViewById(R.id.webview_help);
        myWebView.loadUrl("file:///android_asset/help.html");

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
