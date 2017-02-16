package com.wind.trafficemanager.adpter;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.wind.trafficemanager.R;
import com.wind.trafficemanager.entity.AppdataInfo;
import com.wind.trafficemanager.utils.StringUtil;

public class TrafficeListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<AppdataInfo> mAppList;

    public TrafficeListViewAdapter(Context context, List list) {
        mContext = context;
        mAppList = list;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.triffice_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvAppName = (TextView) convertView.findViewById(R.id.tv_appName);
            viewHolder.tvMobileNum = (TextView) convertView.findViewById(R.id.tv_moblenum);
            viewHolder.tvWifiNum = (TextView) convertView.findViewById(R.id.tv_wifinum);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.ivIcon.setBackground(mAppList.get(position).getAppIcon());
        viewHolder.tvAppName.setText(mAppList.get(position).getTitle());
        viewHolder.tvMobileNum.setText("moble:"+ StringUtil.format(mAppList.get(position).getTotalMobile()));
        viewHolder.tvWifiNum.setText("wifi:"+StringUtil.format(mAppList.get(position).getTotalWifi()));
        return convertView;
    }

     class ViewHolder {

            ImageView ivIcon;
            TextView tvAppName;
            TextView tvMobileNum;
            TextView tvWifiNum;
        }

}