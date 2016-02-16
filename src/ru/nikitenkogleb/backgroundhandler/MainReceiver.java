/*
 *	MainBroadcastReceiver.java
 *	BackgroundHandler
 *
 *  Copyright (c) 2016 Nikitenko Gleb.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package ru.nikitenkogleb.backgroundhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main Broadcast Receiver.
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Feb 16, 2016
 */
final class MainReceiver extends BroadcastReceiver {
    
    /** The Log-Cat Tag. */
    static final String TAG = MainReceiver.class.getName();
    
    /** The receiver intent-filter. */
    private static final IntentFilter INTENT_FILTER = new IntentFilter("Service Action");
    
    /** The fragment's current callback object, which is notified of broadcasts, etc... */
    private Callbacks mCallbacks = null;
    
    /** The queue of intents. */
    private Queue<Intent> mQueue = new LinkedList<Intent>();
    
    /**
     * Constructs a new MainBroadcastReceiver with host-activity and initial Intent.
     * 
     * @param context host-activities context
     * @param intent initial intent
     */
    MainReceiver(Context context, Intent intent) {
        onAttach(context);
        ((Context)mCallbacks).getApplicationContext()
        .registerReceiver(this, INTENT_FILTER);
        mCallbacks.onInit(intent.getAction(), intent.getExtras());
    }

    /** {@inheritDoc} */
    @Override
    public final void onReceive(Context context, Intent intent) {
        if (mQueue != null && mCallbacks == null) mQueue.offer(intent);
        else
            if (mQueue == null && mCallbacks != null)
                mCallbacks.onReceive(intent.getAction(), intent.getExtras());
            else
                Log.wtf(TAG, "One of [mQueue, mCallbacks] must be null, other - not null",
                        new IllegalStateException());
    }
    
    /** @param context current host-activity  */
    final void onAttach(Context context) {
        if (!(context instanceof Callbacks))
            throw new IllegalStateException("Activity must implement receiver's callbacks.");
        mCallbacks = (Callbacks) context;
        Intent intent = null;
        while (mQueue != null && (intent = mQueue.poll()) != null)
            mCallbacks.onReceive(intent.getAction(), intent.getExtras());
        mQueue = null;
    }
    
    /** Detach from host-activity. */
    final void onDetach() {
        mCallbacks = null;
        mQueue = new LinkedBlockingQueue<Intent>();
    }
    
    /** Release this receiver's resources. */
    final void onDestroy() {
        ((Context)mCallbacks).getApplicationContext()
        .unregisterReceiver(this);
        mCallbacks = null;
    }
    
    /**
     * Receiver Callback
     *
     * @version 3.3.9
     * @since Jan 20, 2016
     */
    public static interface Callbacks {
        
        /**
         * Calls after receiver initialize. 
         * 
         * @param action initial action
         * @param extras initial extras
         **/
        public void onInit(String action, Bundle extras);

        /**
         * Calls after receive new Intent.
         * 
         * @param action initial action
         * @param extras initial extras
         **/
        public void onReceive(String action, Bundle extras);
    }


}
