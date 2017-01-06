package com.wind.trafficemanager.util;

import java.util.Calendar;

import android.util.Log;

public class TimeUtils
{

	/**
	 * 获取当天的零点时间
	 * 
	 * @return
	 */
	public static long getTimesmorning()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return (cal.getTimeInMillis());
	}

	// 获得本月第一天0点时间
	public static int getTimesMonthmorning()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_MONTH,
				cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		Log.i("xxxxb", cal.getTimeInMillis()+"");
		return (int) (cal.getTimeInMillis());
	}
	
	
}
