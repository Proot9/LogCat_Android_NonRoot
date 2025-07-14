package com.logcat.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.IBinder;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ScrollView;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private LoggerService loggerService;
    private boolean isBound = false;
    private TextView txt_log;
    private Button btn_log, btn_info;
    private ScrollView vscroll1;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private Typeface hackBoldFont;

    // Warna untuk level log berbeda
    private int COLOR_ERROR;
    private int COLOR_SUCCESS;
    private int COLOR_WARNING;
    private int COLOR_INFO;
    private int COLOR_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        nameText();
        // Load custom font
        hackBoldFont = Typeface.createFromAsset(getAssets(), "fonts/hack_bold.ttf");

        // Inisialisasi warna
        COLOR_DEFAULT = ContextCompat.getColor(this, R.color.dark_green);
        COLOR_ERROR = ContextCompat.getColor(this, R.color.terminal_red);
        COLOR_SUCCESS = ContextCompat.getColor(this, R.color.terminal_green);
        COLOR_WARNING = ContextCompat.getColor(this, R.color.terminal_yellow);
        COLOR_INFO = ContextCompat.getColor(this, R.color.terminal_blue);
        
        txt_log = findViewById(R.id.txt_log);
        btn_log = findViewById(R.id.btn_log);
        btn_info = findViewById(R.id.btn_info);
        vscroll1 = findViewById(R.id.vscroll1);

        // Set tampilan terminal dengan font custom
        txt_log.setTypeface(hackBoldFont);
        txt_log.setTextIsSelectable(true);
        txt_log.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        txt_log.setTextColor(COLOR_DEFAULT);
        txt_log.setTextSize(12); // Ukuran font dalam sp

        // Tombol-tombol
        btn_log.setOnClickListener(v -> clearLogs());
        btn_info.setOnClickListener(v -> showInfo());

        // Bind service
        Intent intent = new Intent(this, LoggerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    
    private void nameText() {
        // Bind Toolbar bawaan Sketchware
        Toolbar toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(toolbar); // Pasang Toolbar sebagai ActionBar

        // Ambil Title TextView dari Toolbar
        for (int i = 0; i < toolbar.getChildCount(); i++) {
             View view = toolbar.getChildAt(i);
             if (view instanceof TextView) {
                 TextView titleTextView = (TextView) view;

                 // Apply font Hack Bold
                 Typeface hackBold = Typeface.createFromAsset(getAssets(), "fonts/hack_bold.ttf");
                 titleTextView.setTypeface(hackBold);

                 // Set warna dan ukuran
                 titleTextView.setTextColor(Color.parseColor("#00FF00")); // Hijau hacker
                 titleTextView.setTextSize(20); // Ukuran teks
              break;
             }
        }

        // Set title text
        getSupportActionBar().setTitle("LogCat Android");
    }

    private void clearLogs() {
        if (loggerService != null) {
            loggerService.clearLogFile();
            txt_log.setText("");
        }
    }

    private void showInfo() {
        Intent intent = new Intent(this, PanduanActivity.class);
        startActivity(intent);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LoggerService.LoggerBinder binder = (LoggerService.LoggerBinder) service;
            loggerService = binder.getService();

            // Setup auto-scroll
            layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    vscroll1.fullScroll(View.FOCUS_DOWN);
                }
            };
            vscroll1.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

            // Setup callback dengan warna
            loggerService.setLoggerCallback((tag, message) -> runOnUiThread(() -> {
                SpannableString coloredLog = colorizeLog(tag, message);
                txt_log.append(coloredLog);
                txt_log.append("\n");
            }));

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (vscroll1 != null && layoutListener != null) {
                vscroll1.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
            }
            isBound = false;
            loggerService = null;
        }
    };

    private SpannableString colorizeLog(String tag, String message) {
        String fullLog = tag + ": " + message;
        SpannableString spannable = new SpannableString(fullLog);
        int color = COLOR_DEFAULT;

        // Tentukan warna berdasarkan konten log
        if (message.toUpperCase().contains("[ERROR]")) {
            color = COLOR_ERROR;
        } else if (message.toUpperCase().contains("[SUCCESS]")) {
            color = COLOR_SUCCESS;
        } else if (message.toUpperCase().contains("[WARN]")) {
            color = COLOR_WARNING;
        } else if (message.toUpperCase().contains("[INFO]")) {
            color = COLOR_INFO;
        }

        spannable.setSpan(new ForegroundColorSpan(color), 
                        0, fullLog.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return spannable;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vscroll1 != null && layoutListener != null) {
            vscroll1.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        }
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}