/*
 *	LeaksDetector.java
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
import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * https://bitbucket.org/Nik-Gleb/secure-vpn/pull-requests/new?source=v3.5.2&t=1
 * Memory Leaks Detector.
 * 
 * Provides any useful Java-Heap Management utils. 
 * 
 * <p><i>Uses Android <b>StrictMode</b> also.</i></p>
 *
 * @author Gleb Nikitenko
 * @since 3.5.2, 17/02/16
 */
final class LeaksDetector {
    
    /** The TAG for LOG-CAT. */
    private static final String TAG = "Leaks Detector";
    /** The LOCALE for LOG-CAT. */
    private static final Locale LOCALE = Locale.ENGLISH;
    /** The size of MegaByte. */
    private static final int MB_SIZE = 1048576; // 1024 * 1024
    
    /** The format of onDestroy log. */
    private static final String LOG_FORMAT_ON_DESTROY = "Heap before destroy:  %.2f MB";
    
    /** The LOG-LEVEL for LOG-CAT. */
    private static int mLogLevel = Log.DEBUG;
    
    /** The garbage collector's sleep interval. */
    private static long mGcSleepInterval = 60;
    /** The number of calls of <i>System.gc()</i> */
    private static byte mGcPasses = 1;
    
    /** The Configuration Updater Fragment for testing Runtime Configuration Changes. */
    private static Fragment mConfigUpdater = null;
    
    /** The activity's root view. */
    private static View mRootView = null;
    
    /** @param level {@link #mLogLevel}, {@code Log.Debug} by default.  */
    static final void setLogLevel(int level) {mLogLevel = level;}
    /** @param interval {@link #mGcSleepInterval}, {@code 60 ms} by default.  */
    static final void setGcSleepInterval(int interval) {mGcSleepInterval = interval;}
    /** @param passes {@link #mGcPasses}, {@code 1 pass} by default.  */
    static final void setGcPasses(byte passes) {mGcPasses = passes;}
    
    /**
     * Turn on strict mode development.
     * 
     * This method require to declare <i>android.permission.WRITE_EXTERNAL_STORAGE</i>
     */
    static final void enableStrictMode() {enableStrictMode(null);}
    
    /**
     * Turn on strict mode development.
     * 
     * This method require to declare <i>android.permission.WRITE_EXTERNAL_STORAGE</i>
     * 
     * @param dumpFileName the name of file for dumping heap, after VmPolicy Crashes.
     */
    static final void enableStrictMode(String dumpFileName) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog()
                .detectResourceMismatches().detectCustomSlowCalls().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectCleartextNetwork()
                .detectLeakedClosableObjects().detectFileUriExposure()
                .detectLeakedRegistrationObjects()
                .penaltyLog().penaltyDeath().detectActivityLeaks().build());
        System.setErr(new PrintStreamStrictModeKills(System.err, dumpFileName));

    }
    
    /** Allows execute periodically System Configuration Update for testing LifeCycles of your App.  */
    //static final void scheduleConfigUpdates() {mConfigUpdater = Fragment.}

    /**
     * This method must be causes after {@link Activity#setContentView(int)} in {@link Activity#onCreate(android.os.Bundle)}
     * @param activity observable activity, usually <i>MainActivity</i> of project
     **/
    static final void onCreate(Activity activity) {
        if (activity == null) return; 
        mRootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        if (activity.getFragmentManager().findFragmentByTag(null) != null) return;
        //Fragment.instantiate(context, fname)
    }
    
    /** This method must be causes before <i>super.onDestroy()</i> in {@link Activity#onDestroy} */
    static final void onDestroy() {
        if (mRootView == null) return;
        unbindReferences(mRootView);
        mRootView = null;
        Log.println(mLogLevel, TAG, String.format(LOCALE, LOG_FORMAT_ON_DESTROY,
                (float) getMemoryUsage() / MB_SIZE));
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
    
    /** Force call garbage collector. */
    static final void gc() {
        try {
            for (byte i = 0; i < mGcPasses; i++) {
                System.gc(); Thread.sleep(mGcSleepInterval);
                System.runFinalization(); Thread.sleep(mGcSleepInterval);}}
        catch (InterruptedException e) {Log.println(Log.WARN, TAG, e.getLocalizedMessage());}
    }
    
    /** @return current usage heap memory. */
    static final long getMemoryUsage() {
        gc(); final long totalMemory = Runtime.getRuntime().totalMemory();
        gc(); final long freeMemory = Runtime.getRuntime().freeMemory();
        return totalMemory - freeMemory;
    }
    
    /**
     * Dump memory trace.
     *
     * @author Gleb Nikitenko
     * @version 1.0
     * @since Feb 05, 2016
     */
    private static final class PrintStreamStrictModeKills extends PrintStream {
        
        /** The LOG-CAT TAG. */
        private static final String TAG = "Strict Mode";
        
        /** The start of Strict Mode VM Policy Crash-Message. */
        private static final String STRICT_MODE_VM_POLICY_MESSAGE =
                "StrictMode VmPolicy violation with POLICY_DEATH;";
        
        /** The name of heap-dump<i>(hprof)</i> file by default. */
        private static final String DEFAULT_DUMP_FILE_NAME =
                "strictmode-violation.hprof";
        
        /** The name of heap-dump<i>(hprof)</i> file. */
        private final String mDumpFileName;
        
        /** {@inheritDoc} */
        private PrintStreamStrictModeKills(OutputStream out, String dumpFileName) {
            super(out);
            mDumpFileName = dumpFileName;
        }
        
        /** {@inheritDoc} */
        private PrintStreamStrictModeKills(File file, String dumpFileName)
                throws FileNotFoundException {
            super(file);
            mDumpFileName = dumpFileName;
        }
        
        /** {@inheritDoc} */
        private PrintStreamStrictModeKills(String fileName, String dumpFileName)
                throws FileNotFoundException {
            super(fileName);
            mDumpFileName = dumpFileName;
        }
        
        /** {@inheritDoc} */
        private PrintStreamStrictModeKills(OutputStream out, boolean autoFlush, String dumpFileName) {
            super(out, autoFlush);
            mDumpFileName = dumpFileName;
        }
        
        /** {@inheritDoc} */
        private PrintStreamStrictModeKills(File file, String charsetName, String dumpFileName)
                throws FileNotFoundException, UnsupportedEncodingException {
            super(file, charsetName);
            mDumpFileName = dumpFileName;
        }
        
        /** {@inheritDoc} */
        private PrintStreamStrictModeKills(String fileName, String charsetName, String dumpFileName)
                throws FileNotFoundException, UnsupportedEncodingException {
            super(fileName, charsetName);
            mDumpFileName = dumpFileName;
        }
        
        /** {@inheritDoc} */
        private PrintStreamStrictModeKills(OutputStream out, boolean autoFlush, String charsetName,
                String dumpFileName) throws UnsupportedEncodingException {
            super(out, autoFlush, charsetName);
            mDumpFileName = dumpFileName;
        }

        /** {@inheritDoc} */
        @Override
        synchronized public final void println(String str) {
            super.println(str);
            if (!str.startsWith(STRICT_MODE_VM_POLICY_MESSAGE)) return;
            // StrictMode is about to terminate us... do a heap dump!
            final File dir = Environment.getExternalStorageDirectory();
            final File dumpFile = new File(dir, TextUtils.isEmpty(mDumpFileName) ?
                    DEFAULT_DUMP_FILE_NAME : mDumpFileName); 
            //super.println("Dumping HPROF to: " + dumpFile);
            try {Debug.dumpHprofData(dumpFile.getAbsolutePath());}
            catch (IOException e) {Log.println(Log.WARN, TAG, e.getLocalizedMessage());}
            //super.println("Dumping " + dumpFile + " succesfull!");
        }
    }

}
