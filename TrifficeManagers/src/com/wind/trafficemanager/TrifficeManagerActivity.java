package com.wind.trafficemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.provider.Settings;

import com.wind.trafficemanager.entity.AllAppInfos;
import com.wind.trafficemanager.entity.AppdataInfo;
import com.wind.trafficemanager.util.NetworkStatsHelper;
import com.wind.trafficemanager.util.SharedPreferencesUtil;
import com.wind.trafficemanager.util.StringUtil;
import com.wind.trifficemanager.R;
public class TrifficeManagerActivity extends AppCompatActivity {
	private ListView mListView ;
	private List<AppdataInfo> mApplist;
	private  ProgressDialog pDialog ;
	private SharedPreferences sp;
	private NetworkStatsHelper netStsHelper;
	private Editor editor;
	public String permission [] = new String[]{"android.permission.READ_PHONE_STATE"};
	public ArrayList<String> unsatisfiedPermissions = new ArrayList<>();
	final public static int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 123;
	private static final int READ_PHONE_STATE_REQUEST = 37;
	PackageManager pm;

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				mListView.setAdapter(new AppsAdapter());
				pDialog.dismiss();
				break;

			default:
				break;
			}
			
		};
	};
	
	private class LoadAppThread  extends Thread
	{
		@Override
		public void run()
		{
		    	 AllAppInfos mAllAppInfos = new AllAppInfos(TrifficeManagerActivity.this);
		    		mApplist = mAllAppInfos.getAllAppInfosByUid();
		    	    Message msg =Message.obtain();
				 	msg.what = 1;
				 	handler.sendMessage(msg);
		}
	};
	
	
	
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    mListView = (ListView) findViewById(R.id.list);
		sp = getSharedPreferences("isSelect",Context.MODE_PRIVATE );
		editor = sp.edit();//获取编辑器
		
		 requestPermissions();
		/* for (int i =0;i<permission.length;i++) {
	            if (checkSelfPermission(permission[i]) != PackageManager.PERMISSION_GRANTED) {
	                unsatisfiedPermissions.add(permission[i]);
	            }
	        }
		 if (unsatisfiedPermissions.size() != 0) {
	            requestPermissions(
	                    unsatisfiedPermissions.toArray(new String[unsatisfiedPermissions.size()]),
	                    PERMISSIONS_REQUEST_ALL_PERMISSIONS);
	        }else {
	        	pDialog = ProgressDialog.show(TrifficeManagerActivity.this, "", "正在加载应用");
	    	    loadAppThread.start();
			}*/
		 ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
	        Network[] networks = manager.getAllNetworks();  
	    for (Network item : networks) {  
	        NetworkInfo info = manager.getNetworkInfo(item);  
	    if (info.getType() == ConnectivityManager.TYPE_WIFI) {  
	        String wifi = manager.getLinkProperties(item)  
	                .getInterfaceName();  
	        Log.i("xb","wifi:" + wifi);  
	    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {  
	        String mobile = manager.getLinkProperties(item)  
	                .getInterfaceName();  
	        Log.i("xb","mobile:" + mobile);  
	    }  
	    }
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (hasPermissions()) {
		 	pDialog = ProgressDialog.show(TrifficeManagerActivity.this, "", "正在加载应用");
    	   new  LoadAppThread().start();
	 }
	}
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
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
                        Intent intent = new Intent(TrifficeManagerActivity.this, TrifficeManagerActivity.class);
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
	
	
	
	
	
	
	/*@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (PERMISSIONS_REQUEST_ALL_PERMISSIONS == requestCode) {
            if (permissions != null && permissions.length > 0) {
            	
            	for(int result :grantResults ){
            		if(result != PackageManager.PERMISSION_GRANTED){
            			 Toast.makeText(this, R.string.missing_required_permission, Toast.LENGTH_SHORT).show();
                         finish();
                         return;
            		}
            	}
                  
                }
                
		}  
		pDialog = ProgressDialog.show(TrifficeManagerActivity.this, "", "正在加载应用");
		loadAppThread.start();
	}*/
	
	
		
	
	
	
	
	
	
	
	
	
	
	
		public class AppsAdapter extends BaseAdapter{
			// 用来控制CheckBox的选中状况  
		    private HashMap<Integer, Boolean> isWifiSelected;  
		    private HashMap<Integer, Boolean> isMobileSelected;  
			
		   public  AppsAdapter()
		   {
			   //isWifiSelected = new HashMap<Integer, Boolean>(); 
			   //isMobileSelected = new HashMap<Integer, Boolean>(); 
			   isWifiSelected = (HashMap<Integer, Boolean>) SharedPreferencesUtil.getInfo(sp, "isWifiSelected");
			   isMobileSelected = (HashMap<Integer, Boolean>) SharedPreferencesUtil.getInfo(sp, "isMobileSelected");
			   Log.i("xb", isWifiSelected+"xxx");
			   if(isWifiSelected.size()==0)
			   {
				   Log.i("xb", "isWifiSelected==null");
				   isWifiSelected = new HashMap<Integer, Boolean>(); 
				   for (int i = 0; i < mApplist.size(); i++) {  
					   isWifiSelected.put(i, false);
			        }  
			   }
			   if(isMobileSelected.size()==0)
			   {
				   Log.i("xb", "isMobileSelected==null");
				   isMobileSelected = new HashMap<Integer, Boolean>(); 
				   for (int i = 0; i < mApplist.size(); i++) {  
					   isMobileSelected.put(i, false);
			        }  
			   }
			   setIsMobileSelected(isMobileSelected);
			   setIsWifiSelected(isWifiSelected);
		   }
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return mApplist.size();
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public View getView(final int position, View conventView, ViewGroup parent) {
				// TODO Auto-generated method stub
				ViewHolder viewHolder;
				if (conventView ==null)
				{
					conventView = LayoutInflater.from(TrifficeManagerActivity.this).inflate(R.layout.list_items, null);
					viewHolder= new ViewHolder();
					viewHolder.ivIcon = (ImageView) conventView.findViewById(R.id.iv_icon);
					viewHolder.tvAppName = (TextView) conventView.findViewById(R.id.tv_appName);
					viewHolder.tvMobileNum = (TextView) conventView.findViewById(R.id.tv_moblenum);
					viewHolder.tvWifiNum = (TextView) conventView.findViewById(R.id.tv_wifinum);
					viewHolder.wifiCbox = (CheckBox) conventView.findViewById(R.id.wifi_cbox);
					viewHolder.mobiCbox = (CheckBox) conventView.findViewById(R.id.mobi_cbox);
					conventView.setTag(viewHolder);
				}else {
					viewHolder = (ViewHolder) conventView.getTag();
					viewHolder.wifiCbox = (CheckBox) conventView.findViewById(R.id.wifi_cbox);
					viewHolder.mobiCbox = (CheckBox) conventView.findViewById(R.id.mobi_cbox);
					
				}
				viewHolder.ivIcon.setBackground(mApplist.get(position).getAppIcon());
				viewHolder.tvAppName.setText(mApplist.get(position).getTitle());
				viewHolder.tvMobileNum.setText("moble:"+StringUtil.format(mApplist.get(position).getTotalMobile()));
				viewHolder.tvWifiNum.setText("wifi:"+StringUtil.format(mApplist.get(position).getTotalWifi()));
				
				
		/*		viewHolder.wifiCbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
						// TODO Auto-generated method stub
						mApplist.get(position).setWifi(isChecked);
						editor.putBoolean("isWifi", isChecked).commit();
					}
				});*/
				
		        // 监听checkBox并根据原来的状态来设置新的状态  
				viewHolder.wifiCbox.setOnClickListener(new View.OnClickListener() {  
					
		            public void onClick(View v) {  
		            	
		                if (isWifiSelected.get(position)) {  
		                    isWifiSelected.put(position, false);  
		                    //setIsWifiSelected(isWifiSelected);  
		                } else {  
		                	isWifiSelected.put(position, true);  
		                    //setIsSelected(isSelected);  
		                }  
		                setIsWifiSelected(isWifiSelected);
		                SharedPreferencesUtil.saveInfo(sp, "isWifiSelected", isWifiSelected);
		            }  
		           
		        });  
		  
		        // 根据isSelected来设置checkbox的选中状况  
				if(getIsWifiSelected().get(position)!=null)
				{
					viewHolder.wifiCbox.setChecked(getIsWifiSelected().get(position));  
				}
				else{
					viewHolder.wifiCbox.setChecked(false);
				}
				Log.i("xb","isChecked"+getIsWifiSelected().get(position));
				/*viewHolder.mobiCbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
						// TODO Auto-generated method stub
						
						Log.i("xb","isChecked"+isChecked);
						
						
						mApplist.get(position).setMobile((isChecked));
						editor.putBoolean("isMobile", isChecked).commit();
					}
				});
				viewHolder.mobiCbox.setChecked(mApplist.get(position).isMobile());
				setIsSelected(isSelected);*/
		        viewHolder.mobiCbox.setOnClickListener(new View.OnClickListener() {  
		  		  
		            public void onClick(View v) {  
		            	
		                if (isMobileSelected.get(position)) {  
		                	isMobileSelected.put(position, false); 
		                   // setIsSelected(isSelected);  
		                } else {  
		                	isMobileSelected.put(position, true);   
		                   // setIsSelected(isSelected);  
		                }  
		                setIsMobileSelected(isMobileSelected);
		                SharedPreferencesUtil.saveInfo(sp, "isMobileSelected", isMobileSelected);
		            }  
		        });  
		  
		        // 根据isSelected来设置checkbox的选中状况  
		       
				if(getIsMobileSelected().get(position)!=null)
				{
					 viewHolder.mobiCbox.setChecked(getIsMobileSelected().get(position));  
				     // 根据isSelected来设置checkbox的选中状况  
				}
				else{
					viewHolder.wifiCbox.setChecked(false);
				}
		        
		        
		        
				return conventView;
			}
			
	
			 public  HashMap<Integer, Boolean> getIsWifiSelected() {  
			        return isWifiSelected;  
			    }  
			  
			    public  void setIsWifiSelected(HashMap<Integer, Boolean> isSelected) {  
			    	Iterator iter = isSelected.entrySet().iterator();
			    	while (iter.hasNext()) {
			    	Map.Entry entry = (Map.Entry) iter.next();
			    	int key = (int) entry.getKey();
			    	boolean b   = (boolean) entry.getValue();
			    	Log.i("xb", "int"+key+";"+"s:"+b);
			    	}
			        this.isWifiSelected = isSelected;  
			    }  
			    
			    public  HashMap<Integer, Boolean> getIsMobileSelected() {  
			        return isMobileSelected;  
			    }  
			  
			    public  void setIsMobileSelected(HashMap<Integer, Boolean> isSelected) {  
			    	Iterator iter = isSelected.entrySet().iterator();
			    	while (iter.hasNext()) {
			    	Map.Entry entry = (Map.Entry) iter.next();
			    	int key = (int) entry.getKey();
			    	boolean b   = (boolean) entry.getValue();
			    	Log.i("xb", "int"+key+";"+"s:"+b);
			    	}
			        this.isMobileSelected = isSelected;  
			    }  
			    
			 
		}
		
		
		class ViewHolder {
			ImageView ivIcon;
			TextView tvAppName;
			TextView tvMobileNum;
			TextView tvWifiNum;
			CheckBox wifiCbox;
			CheckBox mobiCbox;
	
			}
	}

