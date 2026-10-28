package android.window;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.util.Objects;

public final class WindowContainerTransaction {
    public WindowContainerTransaction createTaskFragment(
            TaskFragmentCreationParams taskFragmentOptions) { return this; }
    public WindowContainerTransaction startActivityInTaskFragment(
            IBinder fragmentToken, IBinder callerToken,
            Intent activityIntent, Bundle activityOptions) { return this; }
    public WindowContainerTransaction addTaskFragmentOperation(IBinder fragmentToken,
                                                               TaskFragmentOperation taskFragmentOperation) {
        return this;
    }
}
