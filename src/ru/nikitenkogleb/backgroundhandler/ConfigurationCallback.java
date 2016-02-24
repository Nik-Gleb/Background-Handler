/*
 *	ConfigurationCallback.java
 *	BackgroundHandler
 *
 *	Copyright (C) 2016, Webzilla Apps Inc. All Rights Reserved.
 *	
 *	NOTICE:  All information contained herein is, and remains the 
 *	property of Webzilla Apps Incorporated and its SUPPLIERS, if any. 
 *	
 *	The intellectual and technical concepts contained herein are 
 *	proprietary to Webzilla Apps Incorporated and its suppliers and 
 *	may be covered by United States and Foreign Patents, patents 
 *	in process, and are protected by trade secret or copyright law.
 *	
 *	Dissemination of this information or reproduction of this material 
 *	is strictly forbidden unless prior written permission is obtained 
 *	from Webzilla Apps Incorporated.
 */
package ru.nikitenkogleb.backgroundhandler;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler.Callback;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Configuration Change Callback.
 *
 * @author Gleb Nikitenko
 * @since 3.5.2, 17/02/16
 */
public final class ConfigurationCallback implements Callback {
    
    /** The name of the Activity Manager Native Class. */
    private static final String ACTIVITY_MANAGER_NATIVE_CLASS_NAME = "android.app.ActivityManagerNative";
    /** The name of getDefault() method of the Activity Manager Native. */
    private static final String GET_DEFAULT_METHOD_NAME = "getDefault";
    /** The name of getConfiguration() method of the Activity Manager Native. */
    private static final String GET_CONFIGURATION_METHOD_NAME = "getConfiguration";
    /** The name of updateConfiguration() method of the Activity Manager Native. */
    private static final String UPDATE_CONFIGURATION_METHOD_NAME = "updateConfiguration";
    
    /** The pause not calls yet. */
    private static final long NO_PAUSE_TIMESTAMP = -1;
    /** The delay between onPause of first Activity and onResume of second Activity. */
    private static final long DEFAULT_RECREATE_DELAY = 100;
    /** The garbage collector sleep interval. */
    private static final long GC_SLEEP_INTERVAL = 60;
    /** The number of calls of System.gc (only for debug). */
    private static final byte GC_PASSES = 1;
    /** The delay for display screen. */
    private static final long SHOW_SCREEN_DELAY = 200;
    /** The time intervals between configuration changes. */
    private static final long CHANGE_TIME_INTERVALS = GC_SLEEP_INTERVAL * GC_PASSES * 4 + SHOW_SCREEN_DELAY;
    
    /** The delay between onPause of first Activity and onResume of second Activity. */
    private long mRecreateDelay = DEFAULT_RECREATE_DELAY;
    /** The time by {@link #onPause()}. */
    private long mPauseTimeStamp = NO_PAUSE_TIMESTAMP;
    
    /** Strict Mode initialization. */
    static {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().detectCustomSlowCalls().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().detectActivityLeaks().build());
        System.setErr(new PrintStreamStrictModeKills(System.err));
    }
    /** The calls timer. */
    private final Timer mTimer = new Timer();
    
    /** The Application Context. */
    private final WeakReference<Context> mContext;
    
    /**
     * Constructs a new ConfigurationCallback with Context.
     * 
     * @param context {@link #mContext}
     */
    ConfigurationCallback(Context context) {
        if (context == null)
            throw new IllegalArgumentException("Context cannot be null!");
        mContext = new WeakReference<Context>(context);
        //mHandler = new Handler(this);
        
    }

    /** {@inheritDoc} */
    @Override
    public final boolean handleMessage(Message msg) {
        final Context context = mContext.get();
        if (context == null) return false;
        
        final int keyboardConfiguration = context.getResources().getConfiguration()
                .keyboardHidden == Configuration.KEYBOARDHIDDEN_YES ?
                        Configuration.KEYBOARDHIDDEN_NO : Configuration.KEYBOARDHIDDEN_YES;
        try {
            final Class<?> activityManagerNativeClass = Class.forName(ACTIVITY_MANAGER_NATIVE_CLASS_NAME);
            final Object activityManagerNativeInstance = activityManagerNativeClass
                    .getMethod(GET_DEFAULT_METHOD_NAME).invoke(activityManagerNativeClass);
            final Configuration configuration = (Configuration) activityManagerNativeInstance.getClass()
                    .getMethod(GET_CONFIGURATION_METHOD_NAME).invoke(activityManagerNativeInstance);
            configuration.keyboardHidden = keyboardConfiguration;
            activityManagerNativeInstance.getClass().getMethod(UPDATE_CONFIGURATION_METHOD_NAME,
                    android.content.res.Configuration.class).invoke(activityManagerNativeInstance,
                            configuration);
            
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException |
                InvocationTargetException | NoSuchMethodException e) {e.printStackTrace(); return false;}
        
        return true;
    }
    
    /** Stops measure recreate delay. Schedule next recreate task. */
    final void onResume() {
        
        if (mPauseTimeStamp != NO_PAUSE_TIMESTAMP)
            mRecreateDelay = System.currentTimeMillis() - mPauseTimeStamp;
        
        /*if (mHandler == null || !mHandler.sendEmptyMessageDelayed(0,
                CHANGE_TIME_INTERVALS + mRecreateDelay))
            throw new RuntimeException("Cannot send message!");*/
        
        mTimer.schedule(new TimerTask() {
            @Override  public void run() {handleMessage(null);}},
                CHANGE_TIME_INTERVALS + mRecreateDelay);
        
    }
    
    /** Starts measure recreate delay. */
    final void onPause() {mPauseTimeStamp = System.currentTimeMillis();}
    
    /** Release resources. */
    final void onDestroy() {
        mContext.clear();
        mTimer.cancel();
    }
    
    /** Force call garbage collector. */
    static final void gc() {
        try {
            for (byte i = 0; i < GC_PASSES; i++) {
                System.gc(); Thread.sleep(GC_SLEEP_INTERVAL);
                System.runFinalization(); Thread.sleep(GC_SLEEP_INTERVAL);
            }
        } catch (InterruptedException e) {e.printStackTrace();}
    }
    
    /** @return current usage heap memory. */
    static final long getMemoryUsage() {
        gc(); final long totalMemory = Runtime.getRuntime().totalMemory();
        gc(); final long freeMemory = Runtime.getRuntime().freeMemory();
        return totalMemory - freeMemory; 
    }
    
    /**
     * Releases all references in Root-View to avoid memory leaks.
     * 
     * @param view Root View
     */
    static final void unbindReferences(View view) {
        /*
         * whatever exception is thrown just ignore it because a crash is
         * always worse than this method not doing what it's supposed to do.
         */
        try {unbindViewReferences(view);
            if (view instanceof ViewGroup)
                unbindViewGroupReferences((ViewGroup) view);}
        catch (Throwable extention) {}
    }

    /**
     * Unbinds all references in ViewGroup.
     * 
     * @param viewGroup some View Group
     */
    private static final void unbindViewGroupReferences(ViewGroup viewGroup) {
        final int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = viewGroup.getChildAt(i);
            unbindViewReferences(view);
            if (view instanceof ViewGroup)
                unbindViewGroupReferences((ViewGroup) view);
        }
        
        /*
         * AdapterViews, ListViews and potentially other ViewGroups don't
         * support the removeAllViews operation.
         */
        try {viewGroup.removeAllViews();} catch (Throwable exception) {}
    }

    /**
     * Unbinds all references in View.
     * 
     * @param view some View
     */
    private static final void unbindViewReferences(View view) {
        /*
         * set all listeners to null (not every view and not every API level
         * supports the methods)
         */
        try {view.setOnClickListener(null);}                catch (Throwable exception) {};
        try {view.setOnCreateContextMenuListener(null);}    catch (Throwable exception) {};
        try {view.setOnFocusChangeListener(null);}          catch (Throwable exception) {};
        try {view.setOnKeyListener(null);}                  catch (Throwable exception) {};
        try {view.setOnLongClickListener(null);}            catch (Throwable exception) {};
        try {view.setOnClickListener(null);}                catch (Throwable exception) {};

        // set background to null
        Drawable d = view.getBackground();
        if (d != null)
            d.setCallback(null);
        if (view instanceof ImageView) {
            final ImageView imageView = (ImageView) view;
            d = imageView.getDrawable();
            if (d != null)
                d.setCallback(null);
            imageView.setImageDrawable(null);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                clearImageViewBackground_16andLater(imageView);
            else
                clearImageViewBackground_before16(imageView);

        }

        // destroy webview
        if (view instanceof WebView) {
            ((WebView) view).destroyDrawingCache();
            ((WebView) view).destroy();
        }

        if (view instanceof AbsListView) {
            ((AbsListView) view).setSelector(null);
            ((AbsListView) view).setOnItemClickListener(null);
            ((AbsListView) view).setOnItemLongClickListener(null);
            ((AbsListView) view).setOnItemSelectedListener(null);
        }

        if (view instanceof ListView) {
            ((ListView) view).setOverscrollHeader(null);
            ((ListView) view).setOverscrollFooter(null);
            ((ListView) view).setDivider(null);
        }
    }

    /** @param imageView to cleaning background for API less 16. */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.BASE)
    private static final void clearImageViewBackground_before16(ImageView imageView) {
        imageView.setBackgroundDrawable(null);
    }

    /** @param imageView to cleaning background for API larger or equals 16. */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static final void clearImageViewBackground_16andLater(ImageView imageView) {
        imageView.setBackground(null);
    }

    
    /**
     * Dump memory trace.
     *
     * @author Gleb Nikitenko
     * @version 1.0
     * @since Feb 05, 2016
     */
    private static final class PrintStreamStrictModeKills extends PrintStream {
        /** {@inheritDoc} */
        public PrintStreamStrictModeKills(OutputStream out) {super(out);}
        /** {@inheritDoc} */
        public PrintStreamStrictModeKills(File file) throws FileNotFoundException {super(file);}
        /** {@inheritDoc} */
        public PrintStreamStrictModeKills(String fileName) throws FileNotFoundException {super(fileName);}
        /** {@inheritDoc} */
        public PrintStreamStrictModeKills(OutputStream out, boolean autoFlush) {super(out, autoFlush);}
        /** {@inheritDoc} */
        public PrintStreamStrictModeKills(File file, String charsetName)
                throws FileNotFoundException, UnsupportedEncodingException {super(file, charsetName);}
        /** {@inheritDoc} */
        public PrintStreamStrictModeKills(String fileName, String charsetName)
                throws FileNotFoundException, UnsupportedEncodingException {super(fileName, charsetName);}
        /** {@inheritDoc} */
        public PrintStreamStrictModeKills(OutputStream out, boolean autoFlush, String charsetName)
                throws UnsupportedEncodingException {super(out, autoFlush, charsetName);}

        /** {@inheritDoc} */
        @Override
        synchronized public final void println(String str) {
            super.println(str);
            if (!str.startsWith("StrictMode VmPolicy violation with POLICY_DEATH;")) return;

            // StrictMode is about to terminate us... do a heap dump!
            final File dir = Environment.getExternalStorageDirectory();
            final File dumpFile = new File(dir, "strictmode-violation.hprof"); 
            super.println("Dumping HPROF to: " + dumpFile);
            try {Debug.dumpHprofData(dumpFile.getAbsolutePath());}
            catch (IOException e) {e.printStackTrace();}
            super.println("Dumping " + dumpFile + " succesfull!");
        }
    }


}
