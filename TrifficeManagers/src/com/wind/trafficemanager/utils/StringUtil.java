package com.wind.trafficemanager.utils;
import android.util.Log;
/**
 * 流量数据格式转换（保留小数点后两位）
 *@author xiaobin  E-mail: xiaobin01@wind-mobi.com
 * @version 2016-12-22  下午5:10:44
 */
public class StringUtil {
	public static String  format (long num)
	{
		 float result = num;
		 String roundFormat;
		 String suffix ="B";
		if(result>1024){
			result = result /1024;
			suffix = "KB";
		}
		if(result>1024){
			result = result /1024;
			suffix = "MB";
		}
		if(result>1024){
			result = result /1024;
			suffix = "GB";
		}



		if (result>=100)
		{
			roundFormat = "%.0f";
		}
		else if (result<100&&result>0)
		{
			roundFormat = "%.2f";
		}else {
			roundFormat = "%.1f";
		}
		String total = String.format(roundFormat, result);
		Log.i("xb1", total+suffix);
		//Log.i("xxxb",df.format(result)+suffix);  
		return total+suffix;
	}
	
	  
	
	
}
