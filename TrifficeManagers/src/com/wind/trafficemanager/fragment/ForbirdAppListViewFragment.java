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
import android.widget.ListView;

import com.wind.trafficemanager.R;
import com.wind.trafficemanager.adpter.ForbirdAppListViewAdapter;
import com.wind.trafficemanager.entity.AllAppInfos;
import com.wind.trafficemanager.entity.AppdataInfo;

public class ForbirdAppListViewFragment extends Fragment
{
	private ListView mForbridAppList;
	private List<AppdataInfo> mApplist;
	private static ProgressDialog pDialog;
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case 1:
				if (mApplist.size() == 0) {
					pDialog.dismiss();
				} else {
					ForbirdAppListViewAdapter listAdapter = new ForbirdAppListViewAdapter(getActivity(), mApplist);
					mForbridAppList.setAdapter(listAdapter);
					pDialog.dismiss();
				}
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
		new LoadAppThread().start();
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if(mRootView==null) {
			mRootView = inflater.inflate(R.layout.fragment_forbirdapp, container,
					false);
		}
		mForbridAppList = (ListView) mRootView.findViewById(R.id.lv);
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
			long start = System.currentTimeMillis();
			mApplist = mAllAppInfos.getInstallAppliction();
			long end = System.currentTimeMillis();
			Log.d("qzw","time = " + (end-start));
			msg.what = 1;
			handler.sendMessage(msg);
		}
	}


}
