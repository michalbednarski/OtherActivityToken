package com.example.otheractivitytoken;

import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Set;

class AppThreadRelay extends Binder {
    final Set<Integer> mRelayedCodes = new ArraySet<>();

    AppThreadRelay() {
        addRelayedTransaction("TRANSACTION_scheduleCreateService");
        addRelayedTransaction("TRANSACTION_scheduleServiceArgs");
        addRelayedTransaction("TRANSACTION_scheduleBindService");
        addRelayedTransaction("TRANSACTION_scheduleUnbindService");
        addRelayedTransaction("TRANSACTION_scheduleTaskFragmentTransaction");
    }

    private void addRelayedTransaction(String fieldName) {
        try {
            Field field = IApplicationThread.Stub.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            mRelayedCodes.add(field.getInt(null));
        } catch (IllegalAccessException|NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        boolean doRelay = mRelayedCodes.contains(code);
        Log.v("OtherToken", "FakeThread code=" + code + " doRelay=" + doRelay);
        if (doRelay) {
            ActivityThread.currentActivityThread().getApplicationThread().onTransact(code, data, reply, flags);
        }
        return true;
    }
}
