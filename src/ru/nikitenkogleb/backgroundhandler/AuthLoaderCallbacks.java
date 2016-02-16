/*
 *	AuthLoaderCallbacks.java
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

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

/**
 * Auth-Data Loader Callback
 *
 * @author Gleb Nikitenko
 * @version 3.6, 16/02/16
 * @since 3.6
 */
final class AuthAdapter implements LoaderCallbacks<Cursor> {

    /** {@inheritDoc} */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO Auto-generated method stub
        //new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder)
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub
        
    }

}
