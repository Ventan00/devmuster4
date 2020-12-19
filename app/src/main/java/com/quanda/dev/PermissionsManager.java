package com.quanda.dev;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public interface PermissionsManager {
    int PERM_CAMERA_REQUEST_CODE = 100;
    int PERM_GALLERY_REQUEST_CODE = 99;

    default boolean hasGalleryPermissions(Activity context) {
        return checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, context, PERM_GALLERY_REQUEST_CODE);
    }

    default boolean hasCameraPermissions(Activity context) {
        return checkPermission(Manifest.permission.CAMERA, context, PERM_CAMERA_REQUEST_CODE);
    }

    default boolean checkPermission(String permission, Activity context, int REQUEST_CODE) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                    new String[]{permission}, REQUEST_CODE);
            return false;
        }
        return true;
    }

}
