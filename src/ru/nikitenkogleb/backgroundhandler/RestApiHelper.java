/*
 *	RestApiHelper.java
 *	SecureVPN_Network
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.Locale;

/**
 * REST API Helper
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Feb 17, 2016
 */
final class RestApiHelper {
    
    /** The name of network package. */
    private static final String PACKAGE_NAME = "com.securevpn.network";
    /** The name of rest api service. */
    private static final String SERVICE_NAME = "RestApiService";
    /** The full name of service component. */
    private static final String FULL_SERVICE_NAME = PACKAGE_NAME + "." + SERVICE_NAME;
    /** The action names preffix. */
    private static final String ACTION_PREFFIX = PACKAGE_NAME + ".action.";


    static final void foo(Activity activity, String fileName) {
        execute(activity, getFooArgs(fileName));
    }
    
    private static final Bundle getFooArgs(String fileName) {
        return new Bundle();
    }
    
    private static final void execute(Activity activity, Bundle bundle) {
        System.out.println(activity.startService(
                new Intent(methodToAction(Thread.currentThread()
                .getStackTrace()[3].getMethodName())).putExtras(bundle)
                .addCategory("category")
                .setClassName(PACKAGE_NAME, FULL_SERVICE_NAME)));
    }
    
    /**
     * @param methodName the name of method
     * @return converted the java-name of method to Intent-Action
     */
    private static final String methodToAction(String methodName) {
        return ACTION_PREFFIX + methodName.replaceAll("(?=\\p{Upper})", "_")
                .toUpperCase(Locale.getDefault());
    }


}
