/*
 *	HomeFragment.java
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Resident Fragment.
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Feb 15, 2016
 */
public final class HomeFragment extends Fragment {
    
    static final String TAG = HomeFragment.class.getName();
    
    /** The Activities context. */
    private Context mContext = null;
    
    /** The application initial intent. */
    private static final String ARG_INTENT = "intent";
    
    /** The Main Broadcast receiver. */
    private MainReceiver mMainBroadcastReceiver = null;
    
 
    
    /** Constructs a new BaseFragment. */
    public HomeFragment() {setRetainInstance(true);}
    
    /** {@inheritDoc} */
    @Override
    public void onAttach(Activity activity) {
        onAttachHoneycomb(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) return;
        onAttachInternal(activity);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onAttach(Context context) {
        onAttachMarshmallow(context);
        onAttachInternal(context);
    }
    
    /**
     * Causes Honeycomb Super.
     * 
     * @param context activity context
     */
    @SuppressWarnings("deprecation")
    private final void onAttachHoneycomb(Activity activity) {super.onAttach(activity);}
    
    /**
     * Causes Marshmallow Super.
     * 
     * @param context activity context
     */
    @TargetApi(23)
    private final void onAttachMarshmallow(Context context) {super.onAttach(context);}

    /**
     * OnAttach for all API's
     * 
     * @param context context or activity
     */
    private final void onAttachInternal(Context context) {
        mContext = context;
        
        if (mMainBroadcastReceiver != null)
            mMainBroadcastReceiver.onAttach(mContext);
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onDetach() {
        
        if (mMainBroadcastReceiver != null)
            mMainBroadcastReceiver.onDetach();
        
        mContext = null;
        super.onDetach();
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainBroadcastReceiver = new MainReceiver(mContext,
                (Intent) getArguments().getParcelable(ARG_INTENT));
        
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onDestroy() {
        
         
        mMainBroadcastReceiver.onDestroy();
        mMainBroadcastReceiver = null;
        
        super.onDestroy();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onResume() {
        super.onResume();
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onPause() {
         super.onPause();
    }
    
    /**
     * @param fragmentManager current fragment manager
     * @return fragment instance
     */
    static final HomeFragment newInstance(FragmentManager fragmentManager, Intent intent) {
        final HomeFragment result = new HomeFragment();
        final Bundle arguments = new Bundle();
        arguments.putParcelable(ARG_INTENT, intent);
        result.setArguments(arguments);
        fragmentManager.beginTransaction().add(result, TAG).commit();
        return result;
    }
    
    
    /** @param intent new intent from host-activity  */
    final void onNewIntent(Intent intent) {
        mMainBroadcastReceiver.onReceive(mContext, intent);
    }
    

    
}
