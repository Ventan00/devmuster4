package com.quanda.dev;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public interface BottomNavManager {
    void home(View view);
    void questions(View view);
    void ranking(View view);
    void profile(View view);

    default void openActivity(Activity context, Class<?> c){
        Intent intent = new Intent(context, c);
        context.startActivity(intent);
        context.overridePendingTransition(0, 0);
    }
}
