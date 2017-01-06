package com.wind.trafficemanager.util;
import android.util.Log;
/**
 * 流量数据格式转换（保留小数点后两位）
 *@author xiaobin  E-mail: xiaobin01@wind-mobi.com
 * @version 2016-12-22  下午5:10:44
 */
public class StringUtil {
	public static String  format (long num)
	{
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("0.00");  
		Log.i("xb1", "ss"+num);
		 float result = num;
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
		Log.i("xb1", result+suffix);
		//Log.i("xxxb",df.format(result)+suffix);  
		return df.format(result)+suffix;
	}
	
	  
	
	
}
