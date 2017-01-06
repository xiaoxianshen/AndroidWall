package com.wind.trafficemanager.util;
/*package com.android.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

public class Test {
	@TargetApi(23)
	private void showData(int type) {
	   Log.e("showData", "sdk:" + Integer.toString(Build.VERSION.SDK_INT));
	   if (23 > Build.VERSION.SDK_INT) {
	      stringBuffer.append("设备不支持统计,请使用安卓6.0以上系统");
	      tvshowdata.setText(stringBuffer.toString());
	      return;
	   }
	   try {
	//设置统计时间
	      Calendar calendar = Calendar.getInstance();
	      calendar.set(Calendar.HOUR_OF_DAY, 0);
	      calendar.set(Calendar.MINUTE, 0);
	      calendar.set(Calendar.SECOND, 0);
	      etime = System.currentTimeMillis();
	      stime = calendar.getTimeInMillis();
	      calendar.clear();
	      NetworkStatsManager nsm = (NetworkStatsManager) MainActivity.this.getSystemService(Context.NETWORK_STATS_SERVICE);   
	      PackageManager packageManager = getPackageManager();     
	      List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
	      for (PackageInfo p : packageInfos)
	      {         if ("com.huawei.health".equals(p.packageName)) 
	      {            healthuid = p.applicationInfo.uid;        
	      showappicon.setImageDrawable((Drawable) p.applicationInfo.loadIcon(getPackageManager()));
	      showappname.setText(p.applicationInfo.loadLabel(getPackageManager()).toString());        
	      }         
	      Log.e("安装包：", p.packageName + "  uid:" + p.applicationInfo.uid);    
	      } 
	      stringBuffer.delete(0, stringBuffer.length());    
	      NetworkStats networkStats = nsm.querySummary(type, null,            stime, etime);   
	      ArrayList<NetworkStats.Bucket> bucketList = new ArrayList<NetworkStats.Bucket>();  
	      while (networkStats.hasNextBucket()) {        
	    	  NetworkStats.Bucket bucket = new NetworkStats.Bucket();     
	    	  if (networkStats.getNextBucket(bucket)) {    
	    		  Log.e("bucket:", bucket.toString() + " uid:" + bucket.getUid());    
	    		  if (bucket.getUid() == healthuid) {               bucketList.add(bucket);        
	    		  }         }      }     
	      stringBuffer.append("详细信息：\r\n\r\n");    
	      for (int i = 0; i < bucketList.size(); i++)
	      {         rxbytes += bucketList.get(i).getRxBytes();    
	      txbytes += bucketList.get(i).getTxBytes();     
	      stringBuffer.append("uid:" + Integer.toString(healthuid) +   
	    		  "\r\n 接收： " + Long.toString(bucketList.get(i).getRxBytes() / 1000) +  
	    		  "kb \r\n 发送：" + Long.toString(bucketList.get(i).getTxBytes() / 1000) +               "kb\r\n\r\n");      }    
	      if (txbytes > 1048576)
	      {         showTx.setText("Tx: \r\n" + log.df((((double) txbytes) / 1048576.0), "0.00") + " Mb");      } 
	      else {         showTx.setText("Tx: \r\n" + Long.toString(txbytes / 1024) + " Kb");      }  
   if (rxbytes > 1048576) {         showRx.setText("Rx: \r\n" + log.df((((double) rxbytes) / 1048576.0), "0.00") + " Mb"); 
   } else {         showRx.setText("Rx: \r\n" + Long.toString(rxbytes / 1024) + " Kb");      }      
   if ((rxbytes + txbytes)> 1048576) {         showtotal.setText("Total: \r\n" + log.df((((double) (rxbytes + txbytes)) / 1048576.0), "0.00") + " Mb");      } 
   else {         showtotal.setText("Total: \r\n" + Long.toString((rxbytes + txbytes) / 1024) + " Kb");      }    
   tvshowdata.setText(stringBuffer.toString());      rxbytes = 0;      txbytes = 0;   } 
	   catch (RemoteException e) {      log.e(tag,"出现异常RemoteException",isshowlog);    
	   e.printStackTrace();   }catch (Exception e1){      log.e(tag,"出现异常Exception",isshowlog);      e1.printStackTrace();   }}
	}
}
*/