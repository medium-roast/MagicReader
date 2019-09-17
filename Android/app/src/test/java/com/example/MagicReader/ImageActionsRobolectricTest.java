package com.example.MagicReader;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP)
public class ImageActionsRobolectricTest {

    @Test
    public void cameraActionSendsSystemCameraIntent() {
    }

    @Test
    public void galleryActionSendsSystemPickIntent() {

    }
}
