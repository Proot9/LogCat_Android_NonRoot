package com.logcat.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LoggerService extends Service {
    private final IBinder binder = new LoggerBinder();
    private LoggerCallback callback;

    public class LoggerBinder extends Binder {
        public LoggerService getService() {
            return LoggerService.this;
        }
    }
    
    private android.content.BroadcastReceiver logReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, android.content.Intent intent) {
            if ("com.logcat.android.LOG_EVENT".equals(intent.getAction())) {
                String tag = intent.getStringExtra("tag");
                String message = intent.getStringExtra("message");
  
                // Kirim ke logMessage
                logMessage(tag, message);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Daftar receiver untuk LOG_EVENT
        android.content.IntentFilter filter = new android.content.IntentFilter("com.logcat.android.LOG_EVENT");
        registerReceiver(logReceiver, filter);
    }
    
    public interface LoggerCallback {
        void onLogReceived(String tag, String message);
    }

    public void setLoggerCallback(LoggerCallback cb) {
        callback = cb;
    }

    public void logMessage(String tag, String message) {
        saveLogToFile(tag + ": " + message);
    
        String emoji = "";
        if (message.toUpperCase().contains("[ERROR]")) {
            emoji = "❌ ";
        } else if (message.toUpperCase().contains("[SUCCESS]")) {
            emoji = "✅ ";
        } else if (message.toUpperCase().contains("[WARN]")) {
            emoji = "⚠️ ";
        }

        // JANGAN hapus tag, cukup trim() spasi:
        String displayMessage = message.trim(); 
    
        if (callback != null) {
            callback.onLogReceived(tag, emoji + displayMessage); 
        }
    }

    private void saveLogToFile(String log) {
        try {
            File logFile = new File(getFilesDir(), "logs.txt");
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write((log + "\n").getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("LoggerApp", "Error saving log", e);
        }
    }

    public void clearLogFile() {
        try {
            File logFile = new File(getFilesDir(), "logs.txt");
            if (logFile.exists()) {
                FileOutputStream fos = new FileOutputStream(logFile);
                fos.write("".getBytes()); // Kosongkan file
                fos.close();
            }
            // Panggil callback untuk update UI
            if (callback != null) {
                callback.onLogReceived("Logger", "Log cleared.");
            }
        } catch (IOException e) {
            Log.e("LoggerApp", "Error clearing log file", e);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(logReceiver); // Unregister receiver saat service dihentikan
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}