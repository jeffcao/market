package com.tblin.embedmarket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class PrintLogger {

	private static final boolean DEBUG = true;
	public static String LOG_FILE_PATH;
	private static File logger;
	private static Object locker = new Object();

	public static void i(String tag, String msg) {
		print("info:" + tag, msg);
		if (DEBUG) {
			Log.i(tag, msg);
		}
	}

	private static void print(final String tag, final String msg) {
		synchronized (locker) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					printToLog(tag, msg);
				}
			}).start();
		}
	}

	private static void printToLog(String tag, String msg) {
		checkFile();
		if (logger == null) {
			return;
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(logger);
			fw.write(tag + ":" + msg + "/n");
		} catch (IOException e) {
			if (DEBUG) {
				Log.e(PrintLogger.class.toString(),
						"can not write log to file!");
			}
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					if (DEBUG) {
						Log.e(PrintLogger.class.toString(),
								"can not close log stream!");
					}
				}
			}
		}
	}

	private static void checkFile() {
		if (logger == null) {
			if (LOG_FILE_PATH == null) {
				throw new IllegalArgumentException(
						"should set log path before user log");
			}
			logger = new File(LOG_FILE_PATH);
			if (!logger.exists()) {
				logger.getParentFile().mkdirs();
				try {
					logger.createNewFile();
				} catch (IOException e) {
					// if can not create file, the log will not print
					if (DEBUG) {
						Log.e(PrintLogger.class.toString(),
								"can not create log file!");
					}
					logger = null;
				}
			}
		}
	}

	public static void d(String tag, String msg) {
		print("debug:" + tag, msg);
		if (DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		print("error:" + tag, msg);
		if (DEBUG) {
			Log.e(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		print("warning:" + tag, msg);
		if (DEBUG) {
			Log.w(tag, msg);
		}
	}

	public static void w(String tag, Throwable t) {
		print("warning:" + tag, t.getMessage());
		if (DEBUG) {
			Log.w(tag, t);
		}
	}
}
