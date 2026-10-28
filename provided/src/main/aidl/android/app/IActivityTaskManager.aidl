package android.app;

import android.app.IApplicationThread;
import android.app.ProfilerInfo;

interface IActivityTaskManager {
    int startActivityAsCaller(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int flags, in ProfilerInfo profilerInfo, in Bundle options,
            boolean ignoreTargetSecurity, int userId);
}
