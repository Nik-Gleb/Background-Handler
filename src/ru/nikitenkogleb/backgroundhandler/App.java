/*
 *  App.java
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
package ru.nikitenkogleb.backgroundhandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import android.app.Application;
import android.os.Debug;
import android.os.Environment;
import android.os.Process;
import android.os.StrictMode;
import android.widget.Toast;

/**
 * Main Application.
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Jan 28, 2016
 */
public final class App extends Application {
	
    /** The number of device cores. */
    public static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
	/** The index of Linux Thread Priority, that will use for Main Thread. */
	private static final int MAIN_THREAD_PRIORITY_INDEX = -20;

	/** {@inheritDoc} */
	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.DEBUG)
		    Toast.makeText(this, "DEBUG", Toast.LENGTH_SHORT).show();
		initStrictMode(BuildConfig.DEBUG);
	}
	
	/** @param isDebugMode build config mode. */
	private static final void initStrictMode(boolean isDebugMode) {
		if (isDebugMode) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					.penaltyLog().detectCustomSlowCalls().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects()
					.detectLeakedClosableObjects()
					.penaltyLog().penaltyDeath().detectActivityLeaks().build());

			System.setErr(new PrintStreamStrictModeKills(System.err));
		} else
			Process.setThreadPriority(Process.myTid(), MAIN_THREAD_PRIORITY_INDEX);
	}

    /**
     * Dump memory trace.
     *
	 * @author Gleb Nikitenko
	 * @version 1.0
	 * @since Jan 28, 2016
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
			if (str.startsWith("StrictMode VmPolicy violation with POLICY_DEATH;")) {
				// StrictMode is about to terminate us... do a heap dump!
				try {
					final File dir = Environment.getExternalStorageDirectory();
					final File file = new File(dir, "strictmode-violation.hprof");
					super.println("Dumping HPROF to: " + file);
					Debug.dumpHprofData(file.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
