package com.example.otheractivitytoken;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.FileDescriptor;

class ForceOneWay implements IBinder {
    final IBinder mTarget;

    ForceOneWay(IBinder target) {
        mTarget = target;
    }

    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return mTarget.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return mTarget.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return mTarget.isBinderAlive();
    }

    @Override
    public IInterface queryLocalInterface(String descriptor) {
        return mTarget.queryLocalInterface(descriptor);
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        mTarget.dump(fd, args);
    }

    @Override
    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
        mTarget.dumpAsync(fd, args);
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return mTarget.transact(code, data, reply, FLAG_ONEWAY);
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
        mTarget.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return mTarget.unlinkToDeath(recipient, flags);
    }
}
