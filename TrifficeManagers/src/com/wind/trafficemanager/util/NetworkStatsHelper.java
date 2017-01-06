package com.wind.trafficemanager.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.wind.trafficemanager.entity.AppdataInfo;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;

/**
 * 流量数据获取的帮助类，主要应用了6.0以后系统提供的NetworkStatsManager类（特别注意一定要在6.0及以上的版本上使用）
 * 该类对源码中NetworkStatsService的方法进行封装，分别可以获取wifi和手机流量
 * 
 * @author xiaobin E-mail: xiaobin01@wind-mobi.com
 * @version 2016-12-22 下午5:12:32
 */
@TargetApi(Build.VERSION_CODES.M)
public class NetworkStatsHelper
{
	private NetworkStatsManager networkStatsManager;
	private Context context;

	public NetworkStatsHelper(Context context)
	{
		this.context = context;
		this.networkStatsManager = (NetworkStatsManager) context
				.getSystemService(Context.NETWORK_STATS_SERVICE);
	}

	/**
	 * 获取某时间段的手机流量使用总和
	 * 
	 * @return
	 */
	public long getAllTodayMobile(long startTime,long endTime)
	{
		NetworkStats.Bucket bucket;
		try {
			bucket = networkStatsManager.querySummaryForDevice(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE),
					startTime, endTime);
		} catch (RemoteException e) {
			return -1;
		}
		return bucket.getTxBytes() + bucket.getRxBytes();
	}

	/**
	 * 返回某时间段各个应用流量使用情况 int networkType 网络类型（wifi或者mobile） String subscriberId
	 * sim卡subid（当为wifi时为“”，当为mobile时通过getSubscriberId()获取） long startTime 开始时间戳
	 * long endTime 结束时间戳
	 * 
	 * @return
	 */
	public List<AppdataInfo> queryTodayMobile(long startTime,long endTime)
	{
		NetworkStats networkStats = null;
		ArrayList<AppdataInfo> mobileDataInfosList = null;
		try {
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE),
					startTime, endTime);
			mobileDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo mobleInfo = new AppdataInfo();
					mobleInfo.setUid(bucket.getUid());
					mobleInfo.setTotalMobile(bucket.getRxBytes()
							+ bucket.getTxBytes());
					mobileDataInfosList.add(mobleInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			networkStats.close();
		}
		return mobileDataInfosList;

	}

	public List<AppdataInfo> queryTodayWifi()
	{
		NetworkStats networkStats = null;
		ArrayList<AppdataInfo> wifiDataInfosList = null;
		try {
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_WIFI, "", TimeUtils.getTimesmorning(),
					System.currentTimeMillis());
			wifiDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo wifiDataInfo = new AppdataInfo();
					wifiDataInfo.setUid(bucket.getUid());
					wifiDataInfo.setTotalWifi(bucket.getRxBytes()
							+ bucket.getTxBytes());
					wifiDataInfosList.add(wifiDataInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			networkStats.close();
		}
		return wifiDataInfosList;

	}

	ArrayList<AppdataInfo> wifiDataInfosList = null;
	ArrayList<AppdataInfo> mobileDataInfosList = null;

	/**
	 * 查询今天所有的数据流量
	 * 
	 * @return
	 */
	public List<AppdataInfo> queryTodayData()
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_WIFI, "", TimeUtils.getTimesmorning(),
					System.currentTimeMillis());
			wifiDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo wifiDataInfo = new AppdataInfo();
					wifiDataInfo.setUid(bucket.getUid());
					wifiDataInfo.setTotalWifi(bucket.getRxBytes()
							+ bucket.getTxBytes());
					wifiDataInfosList.add(wifiDataInfo);
					Log.i("xxxb",
							bucket.getUid()
									+ "id"
									+ "total:"
									+ StringUtil.format(bucket.getRxBytes()
											+ bucket.getTxBytes()));
				}
			}
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE),
					TimeUtils.getTimesmorning(), System.currentTimeMillis());
			mobileDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo mobleInfo = new AppdataInfo();
					mobleInfo.setUid(bucket.getUid());
					mobleInfo.setTotalMobile(bucket.getRxBytes()
							+ bucket.getTxBytes());
					Log.i("xxxb",
							bucket.getUid()
									+ "id1"
									+ "total1:"
									+ StringUtil.format((bucket.getRxBytes() + bucket
											.getTxBytes())));
					mobileDataInfosList.add(mobleInfo);
				}
			}
			for (AppdataInfo b : mobileDataInfosList) {
				if (!isExistInA(b)) {
					wifiDataInfosList.add(b);
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			networkStats.close();
		}
		return wifiDataInfosList;
	}

	/**
	 * 获取某个时间区间的流量数据（mobile+wifi）
	 * 
	 * @param startTime
	 *            开始时间戳
	 * @param endTime
	 *            结束时间戳
	 * @return
	 */
	public List<AppdataInfo> queryData(long startTime, long endTime)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_WIFI, "", startTime, endTime);
			wifiDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo wifiDataInfo = new AppdataInfo();
					wifiDataInfo.setUid(bucket.getUid());
					wifiDataInfo.setTotalWifi(bucket.getRxBytes()
							+ bucket.getTxBytes());
					wifiDataInfosList.add(wifiDataInfo);
					Log.i("xxxb",
							bucket.getUid()
									+ "id"
									+ "total"
									+ StringUtil.format(bucket.getRxBytes()
											+ bucket.getTxBytes()));
				}
			}
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE),
					startTime, endTime);
			mobileDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo mobleInfo = new AppdataInfo();
					mobleInfo.setUid(bucket.getUid());
					mobleInfo.setTotalMobile(bucket.getRxBytes()
							+ bucket.getTxBytes());
					Log.i("xxxb",
							bucket.getUid()
									+ "id1"
									+ "total1"
									+ StringUtil.format((bucket.getRxBytes() + bucket
											.getTxBytes())));
					mobileDataInfosList.add(mobleInfo);
				}
			}
			for (AppdataInfo b : mobileDataInfosList) {
				if (!isExistInA(b)) {
					wifiDataInfosList.add(b);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			networkStats.close();
		}
		return wifiDataInfosList;
	}
	/*
	 * 获取某个时间区间的流量数据（mobile+wifi）
	 * 
	 * @param startTime
	 *            开始时间戳
	 * @param endTime
	 *            结束时间戳
	 * @return
	 */
	public List<AppdataInfo> queryMonthData(long startTime,long endTime)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_WIFI, "", startTime, endTime);
			wifiDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo wifiDataInfo = new AppdataInfo();
					wifiDataInfo.setUid(bucket.getUid());
					wifiDataInfo.setTotalWifi(bucket.getRxBytes()
							+ bucket.getTxBytes());
					wifiDataInfosList.add(wifiDataInfo);
					Log.i("xxxb",
							bucket.getUid()
									+ "id"
									+ "total"
									+ StringUtil.format(bucket.getRxBytes()
											+ bucket.getTxBytes()));
				}
			}
			networkStats = networkStatsManager.querySummary(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE),
					startTime, endTime);
			mobileDataInfosList = new ArrayList<AppdataInfo>();
			while (networkStats.hasNextBucket()) {
				NetworkStats.Bucket bucket = new NetworkStats.Bucket();
				if (networkStats.getNextBucket(bucket)) {
					AppdataInfo mobleInfo = new AppdataInfo();
					mobleInfo.setUid(bucket.getUid());
					mobleInfo.setTotalMobile(bucket.getRxBytes()
							+ bucket.getTxBytes());
					Log.i("xxxb",
							bucket.getUid()
									+ "id1"
									+ "total1"
									+ StringUtil.format((bucket.getRxBytes() + bucket
											.getTxBytes())));
					mobileDataInfosList.add(mobleInfo);
				}
			}
			for (AppdataInfo b : mobileDataInfosList) {
				if (!isExistInA(b)) {
					wifiDataInfosList.add(b);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			networkStats.close();
		}
		return wifiDataInfosList;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	boolean isExistInA(AppdataInfo b)
	{
		for (AppdataInfo a : wifiDataInfosList) {

			if (a.getUid() == b.getUid()) {
				a.setTotalMobile(b.getTotalMobile()+a.getTotalMobile());
				a.setTotalWifi(b.getTotalWifi()+a.getTotalWifi());
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取本月已使用的流量
	 * 
	 * @return
	 */
	public long getAllMonthMobile(long startTime,long endTime)
	{
		NetworkStats.Bucket bucket;
		try {
			bucket = networkStatsManager.querySummaryForDevice(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE),
					startTime, endTime);
		} catch (RemoteException e) {
			return -1;
		}
		return bucket.getRxBytes() + bucket.getTxBytes();
	}

	public long getAllRxBytesWifi()
	{
		NetworkStats.Bucket bucket;
		try {
			bucket = networkStatsManager.querySummaryForDevice(
					ConnectivityManager.TYPE_WIFI, "", 0,
					System.currentTimeMillis());
		} catch (RemoteException e) {
			return -1;
		} finally {

		}
		return bucket.getRxBytes();
	}

	public long getAllTxBytesWifi(long startTime ,long endTime)
	{
		NetworkStats.Bucket bucket;
		try {
			bucket = networkStatsManager.querySummaryForDevice(
					ConnectivityManager.TYPE_WIFI, "", startTime,
					endTime);
			Log.i("xb4", bucket.getUid() + "");
		} catch (RemoteException e) {
			return -1;
		} finally {
		}
		return bucket.getTxBytes() + bucket.getRxBytes();
	}

	public long getPackageRxBytesMobile(int packageUid)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.queryDetailsForUid(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE), 0,
					System.currentTimeMillis(), packageUid);
			NetworkStats.Bucket bucket = new NetworkStats.Bucket();
			networkStats.getNextBucket(bucket);
			Log.i("22", "bucket.getRxBytes() = " + bucket.getRxBytes());
			return bucket.getRxBytes();
		} catch (RemoteException e) {
			return -1;
		} finally {
			if (networkStats != null) {
				networkStats.close();
			}
		}

	}

	public long getPackageRxBytesMobileTotal(int packageUid)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.queryDetailsForUid(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE),TimeUtils.getTimesMonthmorning(),
					System.currentTimeMillis(), packageUid);
			NetworkStats.Bucket bucket = new NetworkStats.Bucket();
			networkStats.getNextBucket(bucket);
			Log.i("xb2", (bucket.getRxBytes() + bucket.getTxBytes()) + "");
			return bucket.getRxBytes() + bucket.getTxBytes();
		} catch (RemoteException e) {
			return -1;
		} finally {
			if (networkStats != null) {
				networkStats.close();
			}
		}

	}

	public long getPackageTxBytesMobile(int packageUid)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.queryDetailsForUid(
					ConnectivityManager.TYPE_MOBILE,
					getSubscriberId(ConnectivityManager.TYPE_MOBILE), 0,
					System.currentTimeMillis(), packageUid);
			NetworkStats.Bucket bucket = new NetworkStats.Bucket();
			networkStats.getNextBucket(bucket);
			return bucket.getTxBytes();
		} catch (RemoteException e) {
			return -1;
		} finally {
			if (networkStats != null) {
				networkStats.close();
			}
		}

	}

	public long getPackageRxBytesWifi(int packageUid)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.queryDetailsForUid(
					ConnectivityManager.TYPE_WIFI, "", TimeUtils.getTimesMonthmorning(),
					System.currentTimeMillis(), packageUid);
			NetworkStats.Bucket bucket = new NetworkStats.Bucket();
			networkStats.getNextBucket(bucket);
			Log.i("xb3", "bucket.getRxBytes()" + bucket.getRxBytes());
			return bucket.getRxBytes();
		} catch (RemoteException e) {
			return -1;
		} finally {
			networkStats.close();
		}

	}

	public long getPackageRxBytesWifiTotal(int packageUid)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.queryDetailsForUid(
					ConnectivityManager.TYPE_WIFI, "", TimeUtils.getTimesMonthmorning(),
					System.currentTimeMillis(), packageUid);
			NetworkStats.Bucket bucket = new NetworkStats.Bucket();
			networkStats.getNextBucket(bucket);
			return bucket.getRxBytes() + bucket.getTxBytes();
		} catch (RemoteException e) {
			return -1;
		} finally {
			networkStats.close();
		}

	}

	public long getPackageTxBytesWifi(int packageUid)
	{
		NetworkStats networkStats = null;
		try {
			networkStats = networkStatsManager.queryDetailsForUid(
					ConnectivityManager.TYPE_WIFI, "", 0,
					System.currentTimeMillis(), packageUid);
		} catch (RemoteException e) {
			return -1;
		}
		NetworkStats.Bucket bucket = new NetworkStats.Bucket();
		networkStats.getNextBucket(bucket);
		return bucket.getTxBytes();
	}

	private String getSubscriberId(int networkType)
	{
		if (ConnectivityManager.TYPE_MOBILE == networkType) {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			// final String actualSubscriberId = tm.getSubscriberId();
			// return SystemProperties.get(TEST_SUBSCRIBER_PROP,
			// actualSubscriberId);
			return tm.getSubscriberId();
		}
		return "";
	}
}