package com.quanda.dev;

import android.view.View;
import com.google.android.material.snackbar.Snackbar;

public class Alert {
    public static void show(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, 2000);
        snackbar.show();
    }
}
