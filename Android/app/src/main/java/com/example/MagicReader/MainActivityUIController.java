package com.example.MagicReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Controller of main activity.
 */
public class MainActivityUIController {
    private final Activity activity;
    private final Handler mainThreadHandler;

    private Button resultView;
    private ImageView imageView;
    private String lastResult;

    public MainActivityUIController(Activity activity) {
        this.activity = activity;
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
        lastResult = activity.getString(R.string.result_placeholder);
    }

    public void announceRecognitionResult() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.recognition_dialog_title);
                builder.setMessage(lastResult);
                builder.setPositiveButton(R.string.error_dialog_dismiss_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                builder.show();
            }
        });
    }

    public void resume() {
        resultView = activity.findViewById(R.id.resultView);
        imageView = activity.findViewById(R.id.capturedImage);
        resultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                announceRecognitionResult();
            }
        });
    }

    public void updateResultText(final String text) {
        lastResult = text;
    }

    public void updateImageViewWithBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        imageView.setContentDescription(activity.getString(R.string.image_sent));
    }

    public void showErrorDialogWithMessage(int messageStringID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.error_dialog_title);
        builder.setMessage(messageStringID);
        builder.setPositiveButton(R.string.error_dialog_dismiss_button,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        builder.show();
    }

    public void showInternetError() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, R.string.internet_error_message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // decide whether we should show an explanation why permission is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                // this is called if the user has denied the permission before
                // in this case just ask for the permission again
                ActivityCompat.requestPermissions(activity, new String[] {permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(activity, new String[] {permission}, requestCode);
            }
        }
    }
}
