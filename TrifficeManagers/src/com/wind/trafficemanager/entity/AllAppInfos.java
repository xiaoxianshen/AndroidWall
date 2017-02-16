package com.wind.trafficemanager.entity;

import java.util.ArrayList;
import java.util.List;

import com.wind.trafficemanager.R;
import com.wind.trafficemanager.utils.NetworkStatsHelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.wind.trafficemanager.utils.StringUtil;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.wind.trafficemanager.utils.TimeUtils;
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
		List<AppdataInfo> lists = new ArrayList<>();
		Log.i("sss", list.size() + "");
        List<AppdataInfo> listss = new ArrayList<>();
        if(list.size()!=0)
        {
		AppdataInfo a = new AppdataInfo();
        a.setUid(1000);
		a.setmAppDrawable(pm.getDefaultActivityIcon());
		a.setmTitle(mContext.getResources().getString(R.string.system_title));
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUid() < 10000) {
				resultTotalMobile += list.get(i).getTotalMobile();
				resultTotalWifi += list.get(i).getTotalWifi();
				a.setTotalMobile(resultTotalMobile);
				a.setTotalWifi(resultTotalWifi);
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
				}
			}
		}
        }
		return listss;
	}
	public List<AppdataInfo> getInstallAppliction() {
		List<AppdataInfo> installApplictionList = new ArrayList<AppdataInfo>();
		pm = mContext.getPackageManager();
		List<ApplicationInfo> installedApplications = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		AppdataInfo appdataInfo = null;

		long s1 = System.currentTimeMillis();
		for (ApplicationInfo applicationInfo : installedApplications) {

			if (applicationInfo != null && PackageManager.PERMISSION_GRANTED == pm
					.checkPermission(Manifest.permission.INTERNET,
							applicationInfo.packageName)) {

				if (applicationInfo.uid > 10000) {
					AppdataInfo b = new AppdataInfo();
					b.setUid(applicationInfo.uid);
					try {
						b.setmAppDrawable(pm.getApplicationIcon(pm
								.getPackagesForUid(applicationInfo.uid)[0]));
						b.setmTitle(pm.getApplicationLabel(
								pm.getApplicationInfo(
										pm.getPackagesForUid(applicationInfo.uid)[0],
										PackageManager.GET_META_DATA)).toString());
						installApplictionList.add(b);
					} catch (NameNotFoundException e) {
						b.setmAppDrawable(pm.getDefaultActivityIcon());
					}

				}

			}

		}
		AppdataInfo a = new AppdataInfo();
		a.setUid(1000);
		a.setmAppDrawable(pm.getDefaultActivityIcon());
		a.setmTitle(mContext.getResources().getString(
				R.string.system_app));
		installApplictionList.add(a);
		return installApplictionList;
	}

	public long getTodayData(int type)
	{
		long data =0;
		switch (type)
		{
			case 1:
				data = networkStatsHelper.getAllTodayMobile(TimeUtils.getTimesmorning(),System.currentTimeMillis());
				break;
			case 2:
				data = networkStatsHelper.getAllTodayWifi(TimeUtils.getTimesmorning(),System.currentTimeMillis());
				break;
			default:break;
		}
		return  data;

	}

}
