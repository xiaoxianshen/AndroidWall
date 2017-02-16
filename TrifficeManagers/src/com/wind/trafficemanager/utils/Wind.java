package com.wind.trafficemanager.utils;

public class Wind {
	public final static String NAME = "wind/";

	public static void Log(String tag, String msg) {
		android.util.Log.i(NAME + tag, msg);
	}

	public static void getAppInfo(android.content.Context context) {
		android.util.Log.i(NAME, "	getAppInfo ");
		android.content.pm.PackageManager manager;
		android.content.pm.PackageInfo info = null;
		manager = context.getPackageManager();
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (info != null) {
			android.util.Log.i(NAME, "	versionCode = " + info.versionCode
					+ "	versionName = " + info.versionName + " 	packageName = "
					+ info.packageName + "	signatures = " + info.signatures);
		}
	}
}
