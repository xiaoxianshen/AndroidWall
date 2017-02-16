package com.wind.trafficemanager.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wind.trafficemanager.R;
import com.wind.trafficemanager.adpter.TrafficeListViewAdapter;
import com.wind.trafficemanager.entity.AllAppInfos;
import com.wind.trafficemanager.entity.AppdataInfo;
import com.wind.trafficemanager.utils.StringUtil;
public class TrafficeListViewFragment extends Fragment
{
	private ListView mTrafficeListview;
	private List<AppdataInfo> mApplist;
	private static ProgressDialog pDialog;
    private TextView mTodayWifi;
	private TextView mTodaymobile;
    private long mTodayWifiNum = 0;
	private long mTodaymobileNum = 0;
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case 1:  
					if (mApplist.size()==0)
					{
                        
						View view = mRootView.findViewById(R.id.layout_empty);
                        Log.i("xb","xiaoxiansen");
						mTrafficeListview.setEmptyView(view);
					}
					TrafficeListViewAdapter listAdapter = new TrafficeListViewAdapter(getActivity(), mApplist);
                    mTodayWifi.setText("今日wifi："+StringUtil.format(mTodayWifiNum));
					mTodaymobile.setText("今日流量："+StringUtil.format(mTodaymobileNum));
					mTrafficeListview.setAdapter(listAdapter);
					pDialog.dismiss();
					break;

			default:
				break;
			}

		};
	};
	private  View mRootView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pDialog = ProgressDialog.show(getActivity(), "", "正在加载应用");
		new TrafficeListViewFragment.LoadAppThread().start();

	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if(mRootView==null) {
			mRootView = inflater.inflate(R.layout.fragment_traffice, container,
					false);
		}
		mTrafficeListview = (ListView) mRootView.findViewById(R.id.lv);
        mTodaymobile = (TextView) mRootView.findViewById(R.id.td_mobile);
		mTodayWifi = (TextView) mRootView.findViewById(R.id.td_wifi);
		ViewGroup parent = (ViewGroup) mRootView.getParent();
		if (parent != null) {
			parent.removeView(mRootView);
		}
		return mRootView;
	}

	class LoadAppThread extends Thread
	{
		@Override
		public void run()
		{

			AllAppInfos mAllAppInfos = new AllAppInfos(getActivity());
			Message msg = Message.obtain();
			mApplist = mAllAppInfos.getAllAppInfosByUid();
            mTodayWifiNum= mAllAppInfos.getTodayData(2);
			mTodaymobileNum = mAllAppInfos.getTodayData(1);
			msg.what = 1;
			handler.sendMessage(msg);
		}
	};
}
