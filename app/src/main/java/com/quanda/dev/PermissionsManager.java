package com.quanda.dev;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public interface PermissionsManager {
    int PERM_CAMERA_REQUEST_CODE = 100;
    int PERM_GALLERY_REQUEST_CODE = 99;

    default void requestGalleryPermission(Activity context) {

    }

    default boolean hasCameraPermissions(Activity context) {
        System.out.println(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA));
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.CAMERA}, PERM_CAMERA_REQUEST_CODE);
            return false;
        }
        return true;
    }

}
