package com.wind.trafficemanager;

import android.Manifest;
import android.app.AppOpsManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import com.wind.trafficemanager.fragment.ForbirdAppListViewFragment;
import com.wind.trafficemanager.fragment.TrafficeListViewFragment;

public class TrafficeManagerActivity extends AppCompatActivity {
    private static final int READ_PHONE_STATE_REQUEST = 37;
    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        if (hasPermissions()) {
            initActionBar();
            initTab();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
     @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
            return super.onOptionsItemSelected(item);
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
            return;
        }
    }
    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }
    private boolean hasPermissionToReadNetworkHistory() {
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(TrafficeManagerActivity.this, TrafficeManagerActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
        return false;
    }

    private void initActionBar(){
        mActionBar = getSupportActionBar();//获得ActionBar
        mActionBar.setDisplayShowHomeEnabled(true);//显示home区域
        mActionBar.setDisplayHomeAsUpEnabled(true);//显示返回图片
        //去除默认的ICON图标
        Drawable colorDrawable=new
                ColorDrawable(getResources().getColor(android.R.color.transparent));
        mActionBar.setIcon(colorDrawable);
        //设置自定义View
        mActionBar.setDisplayShowCustomEnabled(true);
        //mActionBar.setCustomView(R.layout.head_logo);
        //设置导航模式为Tabs方式
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }
    private void initTab(){
        ActionBar.Tab  trafficTab = mActionBar.newTab().setText("流量详情");
        ActionBar.Tab  forbirdAppTab = mActionBar.newTab().setText("网络控制");
        trafficTab.setTabListener(new TabListener<Fragment>(new TrafficeListViewFragment()));
        forbirdAppTab.setTabListener(new TabListener<Fragment>(new ForbirdAppListViewFragment()));
        mActionBar.addTab(trafficTab);
        mActionBar.addTab(forbirdAppTab);

    }


   class TabListener<T extends Fragment>implements ActionBar.TabListener{
        private Fragment mFragment;
        public TabListener(Fragment fragment) {
            mFragment = fragment;
        }

       @Override
       public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
           if (!mFragment.isAdded())
           {
               ft.add(R.id.content, mFragment, null);
           }else
           {
               ft.show(mFragment);
           }
       }

       @Override
       public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

           ft.hide(mFragment);
       }

       @Override
       public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

       }
   }
}
