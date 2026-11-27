package com.example.medibook.utils;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.services.CloudinaryService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** Picks an image and uploads it to Cloudinary. Create it in onCreate; callbacks run on the main thread. */
public final class PhotoPicker {
    public interface Listener {
        void onUploading(Uri preview);
        void onUploaded(String url);
        void onFailed(String error);
    }

    private final AppCompatActivity activity;
    private final String folder;
    private final Listener listener;
    private final ActivityResultLauncher<String> launcher;
    private final Handler main = new Handler(Looper.getMainLooper());

    public PhotoPicker(AppCompatActivity activity, String folder, Listener listener) {
        this.activity = activity;
        this.folder = folder;
        this.listener = listener;
        launcher = activity.registerForActivityResult(new ActivityResultContracts.GetContent(), this::upload);
    }

    public void pick() {
        launcher.launch("image/*");
    }

    private void upload(Uri uri) {
        if (uri == null) return; // picker cancelled
        listener.onUploading(uri);
        File file;
        try {
            file = copyToCache(uri);
        } catch (Exception e) {
            listener.onFailed(e.getMessage());
            return;
        }
        new CloudinaryService().uploadImage(file, folder, new CloudinaryService.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                file.delete();
                main.post(() -> listener.onUploaded(imageUrl));
            }

            @Override
            public void onFailure(String error) {
                file.delete();
                main.post(() -> listener.onFailed(error));
            }
        });
    }

    // shortcut: copies on the main thread, fine for phone photos; move off-thread if large files stutter.
    private File copyToCache(Uri uri) throws Exception {
        File file = File.createTempFile("upload_", ".jpg", activity.getCacheDir());
        try (InputStream in = activity.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
            if (in == null) throw new IllegalStateException("Can't read the selected image");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        }
        return file;
    }
}
