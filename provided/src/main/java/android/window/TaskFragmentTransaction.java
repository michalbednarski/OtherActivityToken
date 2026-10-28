package android.window;

import android.os.IBinder;

import java.util.List;

public final class TaskFragmentTransaction {
    public List<Change> getChanges() {
        return null;
    }
    public static final int TYPE_ACTIVITY_REPARENTED_TO_TASK = 6;
    public static final class Change {
        public int getType() {
            return 0;
        }
        public IBinder getOtherActivityToken() {
            return null;
        }
    }
}
