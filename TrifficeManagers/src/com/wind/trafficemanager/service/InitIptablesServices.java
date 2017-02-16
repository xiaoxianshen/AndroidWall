package com.wind.trafficemanager.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
//import com.wind.trafficemanager.TrifficeManagerActivity.LoadAppThread;
import com.wind.trafficemanager.entity.AllAppInfos;
import com.wind.trafficemanager.entity.AppdataInfo;
import com.wind.trafficemanager.utils.SharedPreferencesUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.IpPrefix;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class InitIptablesServices extends Service
{
	private SharedPreferences sp;
	private HashMap<Integer, Boolean> isWifiSelected;
	private HashMap<Integer, Boolean> isMobileSelected;
	private List<AppdataInfo> mApplist;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				/*if(isWifiSelected.size()!=0)
				{
					for (Entry<Integer, Boolean> entry : isWifiSelected.entrySet()) {

					    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
					    //networkManagementService.setWifiDataUidRule(mApplist.get(entry.getKey()).getUid(), entry.getValue());

					}
				}*/
				/*if(isMobileSelected.size()!=0)
				{
					for (Entry<Integer, Boolean> entry : isMobileSelected.entrySet()) {

					    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
					   // networkManagementService.setMobileDataUidRule(mApplist.get(entry.getKey()).getUid(), entry.getValue());

					}
				}*/
				break;

			default:
				break;
			}

		};
	};
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	@Override  
    public void onCreate() {  
        super.onCreate();

    }  
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
	
	
	

}
