package com.example.otheractivitytoken;

import android.app.Activity;

import java.util.function.Consumer;

public class PipActivity extends Activity {
    static Consumer<PipActivity> sResumeListener;
    static Consumer<PipActivity> sPauseListener;

    @Override
    protected void onResume() {
        Consumer<PipActivity> listener = sResumeListener;
        if (listener != null) {
            sResumeListener = null;
            listener.accept(this);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Consumer<PipActivity> listener = sPauseListener;
        if (listener != null) {
            sPauseListener = null;
            listener.accept(this);
        }
        super.onPause();
    }
}
