package com.example.MagicReader;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.MagicReader.imagepipeline.ImageActions;
import com.example.MagicReader.utilities.HttpUtilities;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String UPLOAD_HTTP_URL = "http://34.82.236.138/vizassist/annotate";

    private static final int IMAGE_CAPTURE_CODE = 1;
    private static final int SELECT_IMAGE_CODE = 2;


    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    private MainActivityUIController mainActivityUIController;
    private MainActivityVoiceController mainActivityVoiceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the user interface layout for this activity
        // the layout file is defined in the project res/layout/main_activity.xml file
        setContentView(R.layout.activity_main);
        // create a UI controller instance for this activity
        // this UI controller should be associated with only this activity
        mainActivityUIController = new MainActivityUIController(this);
//        mainActivityVoiceController = new MainActivityVoiceController(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // UI controller resumes the UI and this activity is visible in the foreground.
        mainActivityUIController.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityVoiceController.shutdown();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_capture:
//                mainActivityUIController.updateResultView(getString(R.string.result_placeholder));
                mainActivityUIController.askForPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST);
                ImageActions.startCameraActivity(this, IMAGE_CAPTURE_CODE);
                return true;
            case R.id.action_gallery:
//                mainActivityUIController.updateResultView(getString(R.string.result_placeholder));
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
                    bitmap = (Bitmap) data.getExtras().get("data");
                    mainActivityUIController.updateImageViewWithBitmap(bitmap);
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
    }

    private void uploadImage(Bitmap bitmap) {
        try {
            HttpURLConnection conn = HttpUtilities.makeHttpPostConnectionToUploadImage(bitmap, UPLOAD_HTTP_URL);
            conn.connect();
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