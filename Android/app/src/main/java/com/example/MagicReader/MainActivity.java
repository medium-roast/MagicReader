package com.example.MagicReader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.MagicReader.imagepipeline.ImageActions;
import com.example.MagicReader.utilities.HttpUtilities;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String UPLOAD_HTTP_URL = "your url";

    private static final int IMAGE_CAPTURE_CODE = 1;
    private static final int SELECT_IMAGE_CODE = 2;


    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    private MainActivityUIController mainActivityUIController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the user interface layout for this activity.
        setContentView(R.layout.activity_main);
        // Create a UI controller instance for this activity.
        mainActivityUIController = new MainActivityUIController(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // UI controller resumes the UI and this activity is visible in the foreground.
        mainActivityUIController.resume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_capture:
                mainActivityUIController.updateResultText(getString(R.string.result_placeholder));
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    mainActivityUIController.askForPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST);
                } else {
                    ImageActions.startCameraActivity(this, IMAGE_CAPTURE_CODE);
                }
                return true;
            case R.id.action_gallery:
                mainActivityUIController.updateResultText(getString(R.string.result_placeholder));
                ImageActions.startGalleryActivity(this, SELECT_IMAGE_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            switch (requestCode) {
                case IMAGE_CAPTURE_CODE:
                    Uri capturedImage = ImageActions.getPhotoURI();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), capturedImage);
                        mainActivityUIController.updateImageViewWithBitmap(bitmap);
                    } catch (IOException e) {
                        mainActivityUIController.showErrorDialogWithMessage(R.string.capturing_error_message);
                    }
                    break;
                case SELECT_IMAGE_CODE:
                    Uri selectedImage = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        mainActivityUIController.updateImageViewWithBitmap(bitmap);
                    } catch (IOException e) {
                        mainActivityUIController.showErrorDialogWithMessage(R.string.reading_error_message);
                    }
                    break;
                default:
                    break;
            }
            // Upload the image in a new thread to handle network components.
            if (bitmap != null) {
                final Bitmap bitmapToUpload = bitmap;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadImage(bitmapToUpload);
                    }
                });
                thread.start();
            }
        }
        ImageActions.deletePhotoFile();
    }

    private void uploadImage(Bitmap bitmap) {
        try {
            // Upload image using HTTP.
            HttpURLConnection conn = HttpUtilities.makeHttpPostConnectionToUploadImage(bitmap, UPLOAD_HTTP_URL);
            conn.connect();
            // Get the HTTP response and update the result text if HTTP status is OK.
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                mainActivityUIController.updateResultText(HttpUtilities.parseOCRResponse(conn));
            } else {
                mainActivityUIController.showInternetError();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mainActivityUIController.showInternetError();
        } catch (IOException e) {
            e.printStackTrace();
            mainActivityUIController.showInternetError();
        } catch (JSONException e) {
            e.printStackTrace();
            mainActivityUIController.showInternetError();
        }
    }
}