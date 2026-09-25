package com.studentcloudhub.api;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiResponseListener {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    public void fetchUrl(String urlString, ApiResponseListener listener) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10 sec
                connection.setReadTimeout(10000);    // 10 sec
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();

                    String result = builder.toString();
                    mainHandler.post(() -> listener.onSuccess(result));
                } else {
                    final String err = "HTTP Error " + statusCode;
                    mainHandler.post(() -> listener.onError(err));
                }
            } catch (Exception e) {
                final String err = e.getMessage() != null ? e.getMessage() : "Network error occurred";
                mainHandler.post(() -> listener.onError("Connection failed: " + err));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
