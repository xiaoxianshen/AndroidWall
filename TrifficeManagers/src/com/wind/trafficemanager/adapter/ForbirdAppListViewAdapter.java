package com.wind.trafficemanager.adpter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.wind.trafficemanager.R;
import com.wind.trafficemanager.entity.AppdataInfo;
import com.wind.trafficemanager.entity.Appinfo;
import com.wind.trafficemanager.utils.DatabaseHelper;
import com.wind.trafficemanager.utils.StringUtil;

import java.util.List;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
public class ForbirdAppListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<AppdataInfo> mAppList;
    private DatabaseHelper databaseHelper;
    private INetworkManagementService networkManagementService;
    private static final String DATABASE_NAME="trafficemanager.db";
    public ForbirdAppListViewAdapter(Context context, List list) {
        mContext = context;
        mAppList = list;
        databaseHelper = new DatabaseHelper(context,DATABASE_NAME,null,1);
        networkManagementService = INetworkManagementService.Stub.asInterface(
        ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.forbirdapp_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvAppName = (TextView) convertView.findViewById(R.id.tv_appName);
            convertView.setTag(viewHolder);
            viewHolder.wifiCbox = (CheckBox) convertView.findViewById(R.id.wifi_cbox);
            viewHolder.mobiCbox = (CheckBox) convertView.findViewById(R.id.mobi_cbox);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.ivIcon.setBackground(mAppList.get(position).getAppIcon());
        viewHolder.tvAppName.setText(mAppList.get(position).getTitle());
        final int mUid = mAppList.get(position).getUid();
        Appinfo appinfo = databaseHelper.queryByUid(mUid);
        viewHolder.wifiCbox.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Appinfo appinfo = databaseHelper.queryByUid(mUid);
                if (appinfo.getUid()!=0) {
                    if(appinfo.getWifi() ==1) {
                        Log.e("db","appinfo.getWifi()"+(appinfo.getWifi() ==1));
                        setFireWallEnable(appinfo.getUid(),1,false);
                        databaseHelper.update(appinfo.getUid(), 1, 0);
                    }
                    else {
                        databaseHelper.update(appinfo.getUid(),1,1);
                        setFireWallEnable(appinfo.getUid(),1,true);
                    }
                }else {
                   databaseHelper.insert(mUid,1,1);
                   setFireWallEnable(mUid,1,true);
                }

            }

        });

        viewHolder.wifiCbox.setChecked(appinfo.getWifi() == 1);

        viewHolder.mobiCbox.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Appinfo appinfo = databaseHelper.queryByUid(mUid);
                if (appinfo.getUid()!=0) {
                    if(appinfo.getMobile() ==1) {
                        Log.e("db", "appinfo.getWifi()" + (appinfo.getMobile() == 1));
                        setFireWallEnable(appinfo.getUid(),2,false);
                        databaseHelper.update(appinfo.getUid(), 2, 0);
                    }
                    else {
                        databaseHelper.update(appinfo.getUid(),2,1);
                        setFireWallEnable(appinfo.getUid(),2,true);
                    }
                }else {
                    databaseHelper.insert(mUid,2,1);
                    setFireWallEnable(mUid,2,true);
                }
            }
        });

        viewHolder.mobiCbox.setChecked(appinfo.getMobile()==1);

        return convertView;
    }

     class ViewHolder {

            ImageView ivIcon;
            TextView tvAppName;
            CheckBox wifiCbox;
            CheckBox mobiCbox;
        }
    /**
     * uid  appllication uid
     * type 1:wifi;other:mobile
     * allow true:fordbrd app ,false:allow app
     */
    public void setFireWallEnable(int uid,int type,boolean allow)
    {
           if (networkManagementService != null) { 
                try { 
                     networkManagementService.setFirewallUidChainRule(uid,type,allow);
                    }catch(Exception e)
                    {
                     
                    }
          }    
    
    }
        
        
        
}