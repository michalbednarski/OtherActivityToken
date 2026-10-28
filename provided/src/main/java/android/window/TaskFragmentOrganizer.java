package android.window;

import java.util.concurrent.Executor;

public class TaskFragmentOrganizer {
    public TaskFragmentOrganizer(Executor executor) {}
    public void registerOrganizer() {}
    public void applyTransaction(WindowContainerTransaction wct, int transitionType, boolean shouldApplyIndependently) {}
    public TaskFragmentOrganizerToken getOrganizerToken() { return null; }
    public void onTransactionReady(TaskFragmentTransaction transaction) {}
}
