package com.wind.trafficemanager.entity;

import java.util.List;

import com.wind.trafficemanager.util.NetworkStatsHelper;
import com.wind.trafficemanager.util.StringUtil;
import com.wind.trafficemanager.util.Wind;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class AppdataInfo
{
	private static final String TAG = "AppInfo";
	private Context mContext;

	private Drawable mAppDrawable;

	private PackageInfo mInfo;
	private int Uid;
	public ComponentName mComponentName;
	private String mTitle;
	private PackageManager mPm;
	private boolean wifi;
	private boolean mobile;

	private long totalMobile;
	private long totalWifi;


	public AppdataInfo()
	{
		/*
		 * this.mContext = context; this.mInfo = info; mPm =
		 * mContext.getPackageManager(); final String packageName
		 * =info.applicationInfo.packageName; //this.mComponentName = new
		 * ComponentName(packageName, // info.activityInfo.name); //statsHelper
		 * = new NetworkStatsHelper(mContext); Uid = info.applicationInfo.uid;
		 * mTitle =mPm.getApplicationLabel(info.applicationInfo).toString();
		 * mAppDrawable =mPm.getApplicationIcon(info.applicationInfo);
		 * mAppDrawable = zoomDrawable(mAppDrawable,192,192);
		 */

		// totalMobile =
		// StringUtil.getTotal(statsHelper.getPackageRxBytesMobileTotal(info.applicationInfo.uid));
		// totalWifi =
		// StringUtil.getTotal(statsHelper.getPackageRxBytesWifiTotal(info.applicationInfo.uid));
	}

	//

	public long getTotalMobile()
	{
		return totalMobile;
	}

	public void setTotalMobile(long totalMobile)
	{
		this.totalMobile = totalMobile;
	}

	public long getTotalWifi()
	{
		return totalWifi;
	}

	public void setTotalWifi(long totalWifi)
	{
		this.totalWifi = totalWifi;
	}

	public int getUid()
	{
		return Uid;
	}

	public void setUid(int uid)
	{
		Uid = uid;
	}

	public boolean isMobile()
	{
		return mobile;
	}

	public void setMobile(boolean mobile)
	{
		this.mobile = mobile;
	}

	public boolean isWifi()
	{
		return wifi;
	}

	public void setWifi(boolean wifi)
	{
		this.wifi = wifi;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public Drawable getAppIcon()
	{
		return mAppDrawable;
	}

	public Drawable getmAppDrawable()
	{
		return mAppDrawable;
	}

	public void setmAppDrawable(Drawable mAppDrawable)
	{
		this.mAppDrawable = zoomDrawable(mAppDrawable, 192, 192);
	}

	public String getmTitle()
	{
		return mTitle;
	}

	public void setmTitle(String mTitle)
	{
		this.mTitle = mTitle;
	}

	public Drawable getFullResIcon(ActivityInfo info)
	{
		Resources res;
		try {
			res = mPm.getResourcesForApplication(info.applicationInfo);
		} catch (PackageManager.NameNotFoundException e) {
			res = null;
			Wind.Log(TAG, "getFullResIcon" + e.toString());
		}

		if (res != null) {
			int iconId = info.getIconResource();
			if (iconId != 0) {
				return getFullResIcon(res, iconId);
			}
		}
		return getFullResDefaultActivityIcon();
	}

	public Drawable getFullResIcon(Resources res, int iconId)
	{
		Wind.Log(TAG, "getFullResIcon");
		Drawable d;
		try {
			d = res.getDrawable(iconId);
			// d = res.getDrawableForDensity(iconId, mIcon);
		} catch (Resources.NotFoundException e) {
			Wind.Log(TAG, "getFullResIcon" + e.toString());
			d = null;
		}
		return (d != null) ? d : getFullResDefaultActivityIcon();
	}

	public Drawable getFullResDefaultActivityIcon()
	{
		Wind.Log(TAG, "getFullResDefaultActivityIcon");
		return getFullResIcon(Resources.getSystem(),
				android.R.mipmap.sym_def_app_icon);
	}

	private Drawable zoomDrawable(Drawable drawable, int w, int h)
	{
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable);
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return new BitmapDrawable(null, newbmp);
	}

	private Bitmap drawableToBitmap(Drawable drawable)
	{
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	@Override
	public String toString()
	{
		return "AppdataInfo [mContext=" + mContext + ", mAppDrawable="
				+ mAppDrawable + ", mInfo=" + mInfo + ", Uid=" + Uid
				+ ", mComponentName=" + mComponentName + ", mTitle=" + mTitle
				+ ", mPm=" + mPm + ", wifi=" + wifi + ", mobile=" + mobile
				+ ", totalMobile=" + totalMobile + ", totalWifi=" + totalWifi;
	}

}
