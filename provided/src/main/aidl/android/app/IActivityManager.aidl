package android.app;

import android.app.IApplicationThread;

interface IActivityManager {
    void attachApplication(IApplicationThread app, long startSeq);
}