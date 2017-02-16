package com.wind.trafficemanager.receiver;
import com.wind.trafficemanager.utils.DatabaseHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import android.util.Log;
public class BootBroadCastReciever extends BroadcastReceiver
{
	private static final String DATABASE_NAME="trafficemanager.db";
    private INetworkManagementService networkManagementService;
	@Override
	public void onReceive(Context context, Intent intent)
	{
         Log.i("BroadcastReceiver","onReceive");
		 DatabaseHelper databaseHelper = new DatabaseHelper(context,DATABASE_NAME,null,1);
		 List<Integer> wifiList = databaseHelper.queryByWifi(1);
		 List<Integer> mobileList = databaseHelper.queryBymobile(1);
         networkManagementService = INetworkManagementService.Stub.asInterface(
				ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
		if (wifiList.size()!=0)
		{
			for (int id :wifiList)
            {
                Log.i("BroadcastReceiver","wifi"+id);
                try {
                    networkManagementService.setFirewallUidChainRule(id,1,true);
                }catch (Exception e)
                {
                }
            }
		}
		if (mobileList.size()!=0)
		{
			for (int id :mobileList)
            {   Log.i("BroadcastReceiver","mobile"+id);
                try {
                    networkManagementService.setFirewallUidChainRule(id,2,true);
                }catch (Exception e)
                {
                }
            }
		}

	}

}
