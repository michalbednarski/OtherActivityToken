package com.example.otheractivitytoken;

import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.lang.reflect.Field;

public class SeqReceiver extends BroadcastReceiver {
    static {
        MainActivity.allowHiddenApis();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Field seqField = ActivityThread.class.getDeclaredField("mStartSeq");
            seqField.setAccessible(true);
            setResultData(String.valueOf(seqField.getLong(ActivityThread.currentActivityThread())));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        new Handler().post(() -> System.exit(0));
    }
}
