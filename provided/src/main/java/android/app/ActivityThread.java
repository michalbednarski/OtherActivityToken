package android.app;

public final class ActivityThread {
    public static ActivityThread currentActivityThread() {return null;}
    public class ApplicationThread extends IApplicationThread.Stub {}
    public ApplicationThread getApplicationThread() {return null;}
}
