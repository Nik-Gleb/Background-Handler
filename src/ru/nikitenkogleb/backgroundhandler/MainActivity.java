
package ru.nikitenkogleb.backgroundhandler;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity implements MainReceiver.Callbacks {
    
    private static final int IDN_DEFAULT = 0;
    private static final int IDA_DEFAULT = 0;
    
    private MainFragment mResidentFragment = null;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("MainActivity.onCreate()");
        
        //new Notification.Builder(this)
        
        final Intent intent = new Intent(this, MainActivity.class).setAction("notification");
        
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        .notify(IDN_DEFAULT, new Notification.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Title")
                .setContentText("Notification")
                .setContentIntent(PendingIntent.getActivity(this, IDA_DEFAULT,
                        intent, PendingIntent.FLAG_ONE_SHOT))
                .build());
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mResidentFragment == null) {
            
            final Bundle extras = new Bundle();
            extras.putString("action", getIntent().getAction());
            mResidentFragment = MainFragment.newInstance(getFragmentManager(),
                    getIntent());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mResidentFragment = (MainFragment) getFragmentManager().
                getFragment(savedInstanceState, MainFragment.TAG);
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onSaveInstanceState(Bundle outState) {
        getFragmentManager().putFragment(outState, MainFragment.TAG, mResidentFragment);
        super.onSaveInstanceState(outState);
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onDestroy() {
        mResidentFragment = null;
        System.out.println("MainActivity.onDestroy()");
        super.onDestroy();
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mResidentFragment.onNewIntent(intent);
    }

    /** {@inheritDoc} */
    @Override
    public final void onInit(String action, Bundle extras) {
        System.out.println("MainActivity.onInit() - " + action);
        MyIntentService.startActionFoo(this, "a1", "a2");
    }

    /** {@inheritDoc} */
    @Override
    public final void onReceive(String action, Bundle extras) {
        System.out.println("MainActivity.onReceive() - " + action);
    }
    
}
