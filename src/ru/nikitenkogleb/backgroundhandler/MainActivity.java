
package ru.nikitenkogleb.backgroundhandler;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.Locale;

public class MainActivity extends Activity implements MainReceiver.Callbacks {
    
    private static final int IDN_DEFAULT = 0;
    private static final int IDA_DEFAULT = 0;
    
    private HomeFragment mMainFragment = null;
    
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LeaksDetector.scheduleConfigUpdates(200);
        LeaksDetector.enableStrictMode();
        LeaksDetector.onCreate(this);
        
        /*if (BuildConfig.DEBUG)
            mRootView = getWindow().getDecorView()
            .findViewById(android.R.id.content);*/
        
        //new Notification.Builder(this)
        
        final Intent intent = new Intent(this, MainActivity.class).setAction("notification");
        
        
        /*((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        .notify(IDN_DEFAULT, new Notification.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Title")
                .setContentText("Notification")
                .setContentIntent(PendingIntent.getActivity(this, IDA_DEFAULT,
                        intent, PendingIntent.FLAG_ONE_SHOT))
                .build());*/
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mMainFragment == null) {
            
            final Bundle extras = new Bundle();
            extras.putString("action", getIntent().getAction());
            mMainFragment = HomeFragment.newInstance(getFragmentManager(),
                    getIntent());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMainFragment = (HomeFragment) getFragmentManager().
                getFragment(savedInstanceState, HomeFragment.TAG);
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onSaveInstanceState(Bundle outState) {
        getFragmentManager().putFragment(outState, HomeFragment.TAG, mMainFragment);
        super.onSaveInstanceState(outState);
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onDestroy() {
        mMainFragment = null;
       
        /*if (mRootView != null) {
            ConfigurationCallback.unbindReferences(mRootView);
            mRootView = null;
            Log.println(Log.VERBOSE, "MainActivity", String.format(Locale.getDefault(),
                    "Heap after activity %s:  %.2f MB", Integer.toHexString(hashCode()),
                    ConfigurationCallback.getMemoryUsage() / 1048576.0f));
        }*/
        
        LeaksDetector.onDestroy();
        super.onDestroy();
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mMainFragment.onNewIntent(intent);
    }

    /** {@inheritDoc} */
    @Override
    public final void onInit(String action, Bundle extras) {
        System.out.println("MainActivity.onInit() - " + action);
        //MyIntentService.startActionFoo(this, "a1", "a2");
        /*final Activity fuckingActivity = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {Thread.sleep(8000);} catch (InterruptedException e) {e.printStackTrace();}
                Log.println(Log.VERBOSE, "MainActivity", String.format(Locale.getDefault(),
                        "Keep activity %s", Integer.toHexString(fuckingActivity.hashCode())));
            }
        }).start();*/
    }

    /** {@inheritDoc} */
    @Override
    public final void onReceive(String action, Bundle extras) {
        System.out.println("MainActivity.onReceive() - " + action);
    }
    
}
