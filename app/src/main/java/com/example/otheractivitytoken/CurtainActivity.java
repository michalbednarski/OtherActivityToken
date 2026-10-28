package com.example.otheractivitytoken;

import android.app.Activity;

import java.util.function.Consumer;

public class CurtainActivity extends Activity {
    static Consumer<CurtainActivity> sPauseListener;

    @Override
    protected void onPause() {
        Consumer<CurtainActivity> listener = sPauseListener;
        if (listener != null) {
            sPauseListener = null;
            listener.accept(this);
        }
        super.onPause();
    }
}
