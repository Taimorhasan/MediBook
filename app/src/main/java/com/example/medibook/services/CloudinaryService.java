package com.example.medibook.services;

import android.util.Log;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class CloudinaryService {
    private static final String TAG = "CloudinaryService";
    private static final String CLOUD_NAME = "dda7wmxvw";
    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
    
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    /**
     * Upload image file to Cloudinary asynchronously
     * Uses multipart form data with HttpURLConnection (no external library needed)
     */
    public void uploadImage(File imageFile, String folder, ImageUploadCallback callback) {
        new Thread(() -> {
            try {
                if (!imageFile.exists()) {
                    callback.onFailure("File does not exist");
                    return;
                }

                // Create multipart boundary
                String boundary = "===" + System.currentTimeMillis() + "===";
                URL url = new URL(UPLOAD_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);

                try (OutputStream out = connection.getOutputStream()) {
                    // Write file part
                    String fileHeader = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + imageFile.getName() + "\"\r\n" +
                        "Content-Type: image/jpeg\r\n\r\n";
                    out.write(fileHeader.getBytes());

                    byte[] buffer = new byte[8192];
                    try (InputStream fileIn = new java.io.FileInputStream(imageFile)) {
                        int bytesRead;
                        while ((bytesRead = fileIn.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }

                    // Write upload preset
                    String presetParam = "\r\n--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n" +
                        "medibook_upload";
                    out.write(presetParam.getBytes());

                    // Write folder parameter
                    String folderParam = "\r\n--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"folder\"\r\n\r\n" +
                        "medibook/" + folder;
                    out.write(folderParam.getBytes());

                    // End boundary
                    String endBoundary = "\r\n--" + boundary + "--\r\n";
                    out.write(endBoundary.getBytes());
                    out.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    callback.onFailure("Upload failed with code: " + responseCode);
                    return;
                }

                // Read response
                String responseBody;
                try (InputStream in = connection.getInputStream();
                     Scanner scanner = new Scanner(in).useDelimiter("\\A")) {
                    responseBody = scanner.hasNext() ? scanner.next() : "";
                }

                JSONObject jsonResponse = new JSONObject(responseBody);
                String imageUrl = jsonResponse.getString("secure_url");
                
                Log.d(TAG, "Image uploaded successfully: " + imageUrl);
                callback.onSuccess(imageUrl);

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image", e);
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Get optimized image URL with transformations
     */
    public String getOptimizedImageUrl(String imageUrl, int width, int height) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        try {
            String transformation = String.format("w_%d,h_%d,c_thumb,g_face", width, height);
            return imageUrl.replace("/upload/", "/upload/" + transformation + "/");
        } catch (Exception e) {
            Log.e(TAG, "Error generating optimized URL", e);
            return imageUrl;
        }
    }
}
