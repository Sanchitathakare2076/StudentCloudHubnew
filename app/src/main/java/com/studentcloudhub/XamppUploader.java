package com.studentcloudhub;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class XamppUploader {

    private static final String TAG = "XAMPP_UPLOAD";

    /*
     * Android Emulator:
     * 10.0.2.2 = computer localhost
     */
    public static final String DEFAULT_XAMPP_URL =
            "http://10.61.149.231/studentcloudhub/upload_pdf.php";


    public interface UploadCallback {

        void onSuccess(String pdfUrl);

        void onError(String errorMessage);
    }


    public static void uploadPdfToXampp(
            Context context,
            Uri pdfUri,
            String fileName,
            String uploadUrl,
            UploadCallback callback
    ) {

        new Thread(() -> {

            HttpURLConnection connection = null;
            InputStream fileInputStream = null;
            DataOutputStream outputStream = null;

            try {

                Log.d(TAG, "================================");
                Log.d(TAG, "Starting XAMPP PDF upload");
                Log.d(TAG, "Upload URL: " + uploadUrl);
                Log.d(TAG, "File name: " + fileName);
                Log.d(TAG, "PDF URI: " + pdfUri);
                Log.d(TAG, "================================");


                // -----------------------------------------
                // CREATE CONNECTION
                // -----------------------------------------

                URL url = new URL(uploadUrl);

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);

                connection.setRequestProperty(
                        "Connection",
                        "Keep-Alive"
                );


                // -----------------------------------------
                // MULTIPART BOUNDARY
                // -----------------------------------------

                String boundary =
                        "----StudentCloudHubBoundary"
                                + System.currentTimeMillis();

                connection.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary="
                                + boundary
                );


                // -----------------------------------------
                // OUTPUT STREAM
                // -----------------------------------------

                outputStream =
                        new DataOutputStream(
                                connection.getOutputStream()
                        );


                // -----------------------------------------
                // SEND FILE NAME
                // -----------------------------------------

                outputStream.writeBytes(
                        "--" + boundary + "\r\n"
                );

                outputStream.writeBytes(
                        "Content-Disposition: form-data; "
                                + "name=\"file_name\"\r\n\r\n"
                );

                outputStream.writeBytes(
                        fileName + "\r\n"
                );


                // -----------------------------------------
                // SEND PDF
                // -----------------------------------------

                outputStream.writeBytes(
                        "--" + boundary + "\r\n"
                );

                outputStream.writeBytes(
                        "Content-Disposition: form-data; "
                                + "name=\"pdf\"; "
                                + "filename=\""
                                + fileName
                                + "\"\r\n"
                );

                outputStream.writeBytes(
                        "Content-Type: application/pdf\r\n"
                );

                outputStream.writeBytes("\r\n");


                // -----------------------------------------
                // READ PDF
                // -----------------------------------------

                fileInputStream =
                        context.getContentResolver()
                                .openInputStream(pdfUri);

                if (fileInputStream == null) {

                    throw new Exception(
                            "Android could not read the selected PDF."
                    );
                }


                // -----------------------------------------
                // SEND PDF BYTES
                // -----------------------------------------

                byte[] buffer = new byte[8192];

                int bytesRead;

                long totalBytes = 0;

                while (
                        (bytesRead =
                                fileInputStream.read(buffer)) != -1
                ) {

                    outputStream.write(
                            buffer,
                            0,
                            bytesRead
                    );

                    totalBytes += bytesRead;
                }

                Log.d(
                        TAG,
                        "PDF bytes sent: "
                                + totalBytes
                );


                // -----------------------------------------
                // END MULTIPART REQUEST
                // -----------------------------------------

                outputStream.writeBytes("\r\n");

                outputStream.writeBytes(
                        "--" + boundary + "--\r\n"
                );

                outputStream.flush();

                outputStream.close();

                outputStream = null;


                // -----------------------------------------
                // SERVER RESPONSE
                // -----------------------------------------

                int responseCode =
                        connection.getResponseCode();

                Log.d(
                        TAG,
                        "HTTP response code: "
                                + responseCode
                );


                InputStream responseStream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    responseStream =
                            connection.getInputStream();

                } else {

                    responseStream =
                            connection.getErrorStream();
                }


                if (responseStream == null) {

                    callback.onError(
                            "XAMPP returned HTTP "
                                    + responseCode
                                    + " but no response."
                    );

                    return;
                }


                // -----------------------------------------
                // READ RESPONSE
                // -----------------------------------------

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        responseStream
                                )
                        );

                StringBuilder responseBuilder =
                        new StringBuilder();

                String line;

                while (
                        (line = reader.readLine()) != null
                ) {

                    responseBuilder.append(line);
                }

                reader.close();

                String response =
                        responseBuilder
                                .toString()
                                .trim();


                Log.d(
                        TAG,
                        "================================"
                );

                Log.d(
                        TAG,
                        "XAMPP SERVER RESPONSE:"
                );

                Log.d(
                        TAG,
                        response
                );

                Log.d(
                        TAG,
                        "================================"
                );


                // -----------------------------------------
                // HTTP ERROR
                // -----------------------------------------

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    callback.onError(
                            "XAMPP server error HTTP "
                                    + responseCode
                                    + "\n"
                                    + response
                    );

                    return;
                }


                // -----------------------------------------
                // EMPTY RESPONSE
                // -----------------------------------------

                if (response.isEmpty()) {

                    callback.onError(
                            "XAMPP returned an empty response."
                    );

                    return;
                }


                // -----------------------------------------
                // PARSE JSON
                // -----------------------------------------

                try {

                    JSONObject json =
                            new JSONObject(response);


                    boolean success =
                            json.optBoolean(
                                    "success",
                                    false
                            );


                    String pdfUrl =
                            json.optString(
                                    "pdf_url",
                                    ""
                            );


                    String message =
                            json.optString(
                                    "message",
                                    ""
                            );


                    Log.d(
                            TAG,
                            "success = "
                                    + success
                    );

                    Log.d(
                            TAG,
                            "pdf_url = "
                                    + pdfUrl
                    );

                    Log.d(
                            TAG,
                            "message = "
                                    + message
                    );


                    // -----------------------------------------
                    // SUCCESS
                    // -----------------------------------------

                    if (success &&
                            !pdfUrl.trim().isEmpty()) {

                        callback.onSuccess(
                                pdfUrl.trim()
                        );

                    } else {

                        if (message.isEmpty()) {

                            message =
                                    "PHP upload failed. "
                                            + "No error message was returned.";
                        }

                        callback.onError(
                                message
                        );
                    }


                } catch (Exception e) {

                    Log.e(
                            TAG,
                            "JSON parsing failed",
                            e
                    );

                    callback.onError(
                            "XAMPP returned invalid JSON:\n"
                                    + response
                    );
                }


            } catch (Exception e) {

                Log.e(
                        TAG,
                        "XAMPP upload exception",
                        e
                );


                String error = e.getMessage();

                if (error == null ||
                        error.trim().isEmpty()) {

                    error =
                            e.getClass()
                                    .getSimpleName();
                }


                callback.onError(
                        "XAMPP upload failed:\n"
                                + error
                );


            } finally {

                try {

                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }

                } catch (Exception ignored) {
                }


                try {

                    if (outputStream != null) {
                        outputStream.close();
                    }

                } catch (Exception ignored) {
                }


                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }
}