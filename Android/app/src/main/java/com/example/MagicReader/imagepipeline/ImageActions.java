package com.example.MagicReader.imagepipeline;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * Actions to get images from either the device back camera or photo gallery
 */
// These functions are public utility functions: should write Javadoc for them
public class ImageActions {

    private static Uri photoURI;
    private static File photoFile;

    /**
     * Prepare a temp file for the camera image.
     * @param activity origin activity in which the intent will be from.
     */
    private static void createImageFile(Activity activity) throws IOException {
        String imageFileName = "captured_picture";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Delete the previously stored file to save space.
        if (photoFile != null) {
            photoFile.delete();
        }
        // Create a new temp file for the image.
        photoFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /**
     * Return the URI of the image.
     * @return URI of the captured photo.
     */
    public static Uri getPhotoURI() {
        return photoURI;
    }

    /**
     * Start the built-in back camera to capture a still image.
     * @param activity origin activity in which the intent will be from.
     * @param requestCode request code to get result when the camera activity is dismissed.
     */
    public static void startCameraActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        try {
            createImageFile(activity);
        } catch (IOException e) {
            // Error occurred while creating the File
            e.printStackTrace();
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(activity,
                    "com.example.MagicReader.fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Start photo gallery image picker to select a saved image.
     * @param activity origin activity in which the intent will be from.
     * @param requestCode request code to get result when the gallery activity is dismissed.
     */
    public static void startGalleryActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, requestCode);
    }
}
