package com.wind.trafficemanager.entity;

import java.util.ArrayList;
import java.util.List;

import com.wind.trafficemanager.TrifficeManagerActivity;
import com.wind.trafficemanager.util.NetworkStatsHelper;
import com.wind.trafficemanager.util.TimeUtils;
import com.wind.trafficemanager.util.Wind;
import com.wind.trifficemanager.R;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class AllAppInfos
{
	public static final String APP_LOCK_PACKAGE_NAME = "com.wind.trifficemanager";

	private static final String TAG = "AllAppInfos";

	private Context mContext;
	private NetworkStatsHelper networkStatsHelper;
	private PackageManager pm;

	public AllAppInfos(Context context)
	{
		mContext = context;
	}

	@SuppressWarnings("deprecation")
	public List<AppdataInfo> getAllAppInfosByUid()
	{
		networkStatsHelper = new NetworkStatsHelper(mContext);
		List<AppdataInfo> list = networkStatsHelper.queryTodayData();
		Log.i("xxxxb", networkStatsHelper.getPackageRxBytesWifiTotal(10119)+"netEase");
		pm = mContext.getPackageManager();
		long resultTotalWifi = 0;
		long resultTotalMobile = 0;
		int uid = 0;
		List<AppdataInfo> lists = new ArrayList<>();
		Log.i("sss", list.size() + "");
		AppdataInfo a = new AppdataInfo();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUid() < 10000) {
				resultTotalMobile += list.get(i).getTotalMobile();
				resultTotalWifi += list.get(i).getTotalWifi();
				uid = 1000;
				a.setTotalMobile(resultTotalMobile);
				a.setTotalWifi(resultTotalWifi);
				a.setUid(uid);
				a.setmAppDrawable(pm.getDefaultActivityIcon());
				a.setmTitle(mContext.getResources().getString(
						R.string.system_title));
				continue;
			}
			AppdataInfo b = new AppdataInfo();
			b.setTotalMobile(list.get(i).getTotalMobile());
			b.setTotalWifi(list.get(i).getTotalWifi());
			b.setUid(list.get(i).getUid());
			try {
				b.setmAppDrawable(pm.getApplicationIcon(pm
						.getPackagesForUid(list.get(i).getUid())[0]));
				b.setmTitle(pm.getApplicationLabel(
						pm.getApplicationInfo(
								pm.getPackagesForUid(list.get(i).getUid())[0],
								PackageManager.GET_META_DATA)).toString());
			} catch (NameNotFoundException e) {
				b.setmAppDrawable(pm.getDefaultActivityIcon());
			}

			lists.add(b);
		}
		lists.add(a);
		for (AppdataInfo ap : lists) {
			Log.i("xx", "1="+ap.toString());
		}
		List<AppdataInfo> listss = new ArrayList<>();
		for (int i = 0; i < lists.size(); i++) {

			for (int j = i + 1; j < lists.size(); j++) {
				if (lists.get(i).getUid() == lists.get(j).getUid()) {
					lists.get(i).setTotalMobile(
							lists.get(j).getTotalMobile()
									+ lists.get(i).getTotalMobile());
					lists.get(i).setTotalWifi(
							lists.get(j).getTotalWifi()
									+ lists.get(i).getTotalWifi());
				}
			}
			listss.add(lists.get(i));
		}

		for (int i = listss.size() - 1; i >= 0; i--) {
			for (int j = i - 1; j >= 0; j--) {
				if (listss.get(i).getUid() == listss.get(j).getUid()) {
					listss.remove(i);
					Log.d("xx", "i=" + i);
				}
			}
		}
		for (AppdataInfo ap : listss) {
			Log.i("xx", ap.toString());
		}
		return listss;
	}

}
