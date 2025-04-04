package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private WebView mWebView;
    private EditText mChatInput;
    private ImageButton mSendButton; // Đổi từ Button thành ImageButton
    private LinearLayout chatContainer;

    private ImageView splashImage;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the splash screen layout
        setContentView(R.layout.activity_splash);  // Set splash layout

        // Initialize the splash image view
        splashImage = findViewById(R.id.splashImage);
        splashImage.setScaleType(ImageView.ScaleType.FIT_CENTER);


        // Start a thread to delay for splash screen
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); // Wait for 2 seconds (Splash screen duration)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // After splash, transition to the main activity
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Switch to the main layout
                        setContentView(R.layout.activity_main);
                        initializeWebView();
                    }
                });
            }
        }).start();
    }

    // Initialize WebView and chat interface
    private void initializeWebView() {
        mWebView = findViewById(R.id.activity_main_webview);
        mChatInput = findViewById(R.id.chat_input);
        mSendButton = findViewById(R.id.chat_send_button);
        chatContainer = findViewById(R.id.chat_container);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());

        mWebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void showChatInput(boolean show) {
                runOnUiThread(() -> {
                    if (show) {
                        chatContainer.setVisibility(View.VISIBLE);
                        mChatInput.setVisibility(View.VISIBLE);
                        mSendButton.setVisibility(View.VISIBLE);
                    } else {
                        chatContainer.setVisibility(View.GONE);
                        mChatInput.setVisibility(View.GONE);
                        mSendButton.setVisibility(View.GONE);
                    }
                });
            }
        }, "Android");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String jsCode = "javascript:(function() { " +
                        "    function checkChatInput() { " +
                        "        var chatDiv = document.querySelector('div.relative.flex.w-full.flex-col'); " +
                        "        var textArea = document.querySelector('textarea[placeholder=\"Send a message...\"]'); " +
                        "        if (textArea) { " +
                        "            window.Android.showChatInput(true); " +  // Hiển thị input trên Android
                        "            if (chatDiv) { chatDiv.style.display = 'none'; } " + // Ẩn chat div
                        "        } else { " +
                        "            window.Android.showChatInput(false); " + // Ẩn input trên Android
                        "            if (chatDiv) { chatDiv.style.display = 'block'; } " + // Hiển thị lại chat div nếu cần
                        "        } " +
                        "    } " +
                        "    var intervalId = setInterval(checkChatInput, 500); " + // Kiểm tra mỗi 500ms
                        "})();";

                mWebView.evaluateJavascript(jsCode, null);
            }
        });

        mWebView.loadUrl("https://eva-fe-git-develop-ai16evas-projects.vercel.app?_vercel_share=WYBezVMxSI7cibGuKcpwP7PwsFD369yJ");

        mSendButton.setOnClickListener(v -> insertMessageToWebView());
    }

    private void insertMessageToWebView() {
        String message = mChatInput.getText().toString().trim();
        if (!message.isEmpty()) {
            String jsCode = "javascript:(function() { " +
                    "var textArea = document.querySelector('textarea.flex.w-full.rounded-md');" +
                    "if (textArea) { " +
                    "    var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;" +
                    "    nativeInputValueSetter.call(textArea, '" + message + "');" +
                    "    textArea.dispatchEvent(new Event('input', { bubbles: true }));" +
                    "    textArea.dispatchEvent(new Event('change', { bubbles: true }));" +
                    "    var event = new KeyboardEvent('keydown', { key: 'Enter', bubbles: true });" +
                    "    textArea.dispatchEvent(event);" +
                    "    setTimeout(function() { " +
                    "        var sendButton = document.querySelector('button.bg-primary');" +
                    "        if (sendButton) { " +
                    "            sendButton.removeAttribute('disabled');" +
                    "            sendButton.classList.remove('disabled');" +
                    "        } " +
                    "    }, 500); " +
                    "} " +
                    "})();";
            mWebView.evaluateJavascript(jsCode, null);
            mChatInput.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
