/*
 *  MainActivity.java
 *  BackgroundHandler
 *  
 *  The MIT License (MIT)
 *  
 *  Copyright (c) 2016 Gleb Nikitenko
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package ru.nikitenkogleb.backgroundhandler.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import ru.nikitenkogleb.backgroundhandler.BuildConfig;
import ru.nikitenkogleb.backgroundhandler.R;
import ru.nikitenkogleb.backgroundhandler.utils.Utils;

/**
 * Main Activity.
 * First activity for launch application.
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Jan 28, 2016
 */
public final class MainActivity extends Activity {
    
    /** The activity root view. */
    private View mRootView = null;

    /** {@inheritDoc} */
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG)
            mRootView = getWindow().getDecorView()
            .findViewById(android.R.id.content);
        
    }

    /** {@inheritDoc} */
    @Override
    protected final void onDestroy() {
        
        if (mRootView != null) {
            Utils.unbindReferences(mRootView);
            mRootView = null;
            System.gc();
            System.runFinalization();
            System.gc();
        }
        super.onDestroy();
    }

}
