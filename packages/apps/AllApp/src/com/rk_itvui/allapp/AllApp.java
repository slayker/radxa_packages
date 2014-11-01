package com.rk_itvui.allapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import java.util.Collections;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.GridView;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.content.ComponentName;
import android.widget.AdapterView;
import android.view.View;
import android.content.SharedPreferences;
import android.content.Context;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.media.AudioManager;

import android.content.ActivityNotFoundException;
import android.widget.Toast;
import android.widget.Button;


public class AllApp extends Activity{
    private static final String TAG = "AllApp:Activity";

    private TextView mTextView = null;
    private GridView mGridView = null;
    private final static int UPDATA_UI = 0;
    private int TOPBAR_HEIGHT = 50;
    private int ITEM_SPACING = 20;
    private static ArrayList<PackageInformation> mApkInformation = null;
    private static LinkedList<PackageInformation> mApkRecent = null;

    private final int MAX_APK = 10;

    private SharedPreferences mSaveEditor = null;
    private final static String SHAREDNAME = "AppInfomation";
    private final static String AppCount = "Count";
    private final static String AppName = "AppName";
    private final static String PackageName = "PackageName";
    private final static String VersionCode = "VersionCode";
    private final static String VersionName = "VersionName";
    private final static String ActivityName = "ActivityName";
    private ViewGroup rootView = null;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "==================================Activity:onCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mApkRecent = new LinkedList<PackageInformation>();
		if(SDKConfig.getIsAndroid40()){
			//fullscreen
			//getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_SHOW_FULLSCREEN); //for pad
		}


    	setContentView(R.layout.app_activity);
				
        rootView = (ViewGroup)findViewById(R.id.app_layout);
        if(null != mClickListener){
            ((Button)findViewById(R.id.btnRecent)).setOnClickListener(mClickListener);
            ((Button)findViewById(R.id.btnDownload)).setOnClickListener(mClickListener);
        }
        mApkInformation = new ArrayList<PackageInformation>();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ScreenInfo.DENSITY = displayMetrics.densityDpi;
        ScreenInfo.WIDTH = displayMetrics.widthPixels;
        ScreenInfo.HEIGHT= displayMetrics.heightPixels;


        createTextView();
    }
	
    @Override
    public void onPause() 
    {
        Log.d(TAG, "==================================Activity:onPause");
        super.onPause();
        saveRecentApps();
    }
	
    @Override
    public void onResume() 
    {
        Log.d(TAG, "==================================Activity:onResume");
        super.onResume();
        restoreRecentApps();

        new Thread(getApkInfoRunnable).start();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

	
    private void createTextView()
    {
        mTextView = (TextView)findViewById(R.id.app_text); 

        if(null != mTextView){
            mTextView.setTextColor(Color.WHITE);
            mTextView.setBackgroundColor(Color.GRAY);
            String show = getResources().getString(R.string.application_scan);
            mTextView.setText("    "+show);
        }
    }

    private void createGridView()
    {
        mGridView = new GridView(this); 
        RelativeLayout.LayoutParams params = 
        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
        	RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW,R.id.app_text);
        params.setMargins(0, TOPBAR_HEIGHT/2, 0, 0);
        mGridView.setLayoutParams(params);
        AppGridViewAdpter adapter = new AppGridViewAdpter(this,mApkInformation);
        mGridView.setAdapter(adapter);
        mGridView.setNumColumns(6);
        mGridView.setHorizontalSpacing(ITEM_SPACING);
        mGridView.setVerticalSpacing(ITEM_SPACING);
        mGridView.setSelector(R.drawable.gridview_selector);
        mGridView.setGravity(Gravity.CENTER);

        if(rootView != null){
            rootView.addView(mGridView);
        }
        mGridView.requestFocus();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                if((mApkInformation != null)&&(arg2 < mApkInformation.size()))
                	startApplication(arg2);
            }

        });
    }

    void startApplication(int position){
        try{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            ComponentName componentName = new ComponentName(mApkInformation.get(position).getPackageName(),mApkInformation.get(position).getActivityName());
            intent.setComponent(componentName);
            
            addToRecent(mApkInformation.get(position));
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    void startRecentApp(int position){
        try{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            ComponentName componentName = new ComponentName(mApkRecent.get(position).getPackageName(),mApkRecent.get(position).getActivityName());
            intent.setComponent(componentName);
            
            addToRecent(mApkRecent.get(position));
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
	
    private void addToRecent(PackageInformation pack){
        /*if this pack is existed, remove!*/
        for(int i = 0; i < mApkRecent.size(); i++){
            if(0 == mApkRecent.get(i).getPackageName().compareTo(pack.getPackageName())){
                mApkRecent.remove(i);
                break;
            }
        }
        /*add to first postion of recent*/
        if(mApkRecent.size() >= MAX_APK){
            mApkRecent.removeLast();
            mApkRecent.addFirst(pack);
        }else{
            mApkRecent.addFirst(pack);
        }

    }

	// this function is used to filter Apk information. Return true means filter,then the Apk information do not showing in screen
	// return false means the Apk show in screen.
	public boolean filterApk(String packagenName){
		 if((packagenName.compareTo("com.rockchip.settings") == 0) ||
			(packagenName.compareTo("com.rk.setting") == 0) ||
			(packagenName.compareTo("com.twitter.android") == 0) ||
			(packagenName.compareTo("com.google.android.youtube") == 0) ||
			(packagenName.compareTo("com.android.browser") == 0) ||
			(packagenName.compareTo("com.google.android.apps.books") == 0) ||
			(packagenName.compareTo("com.adobe.flashplayer") == 0) ||
			(packagenName.compareTo("com.android.gallery3d") == 0) ||
			(packagenName.compareTo("com.google.android.apps.genie.geniewidget") == 0) ||
			(packagenName.compareTo("com.android.calculator2") == 0) ||
			(packagenName.compareTo("com.android.calendar") == 0) ||
			(packagenName.compareTo("com.android.videoeditor") == 0) ||
			(packagenName.compareTo("com.android.deskclock") == 0) ||
			(packagenName.compareTo("com.android.development") == 0) ||
			(packagenName.compareTo("com.android.providers.downloads.ui") == 0) ||
			(packagenName.compareTo("com.cooliris.media") == 0) ||
			(packagenName.compareTo("com.android.music") == 0) ||
			(packagenName.compareTo("com.android.quicksearchbox") == 0) ||
			(packagenName.compareTo("com.android.camera") == 0) ||
			(packagenName.compareTo("com.android.spare_parts") == 0)  ||
			(packagenName.compareTo("com.android.speechrecorder") == 0) ||
			(packagenName.compareTo("com.appside.android.VpadMonitor") == 0) ||
			(packagenName.compareTo("com.rk.youtube") == 0) ||
			(packagenName.compareTo("com.android.contacts") == 0) ||
			(packagenName.compareTo("com.google.android.talk") == 0) ||
			(packagenName.compareTo("com.google.android.apps.maps") == 0) ||
			(packagenName.compareTo("com.rk_itvui.allapp") == 0) ||
			(packagenName.compareTo("com.rk_itvui.rkxbmc") == 0) ||
			(packagenName.compareTo("com.android.soundrecorder") == 0) ||	
			(packagenName.compareTo("android.rk.RockVideoPlayer") == 0))
		{
			return true;
		}
		 
		 if(SDKConfig.getIsAndroid40()){
			 if((packagenName.compareTo("com.rockchip.settings") == 0)){
				 return true;
			 }
		 }
		 if(SDKConfig.getIsAndroid23()){
			 if((packagenName.compareTo("com.android.settings") == 0)){
				 return true;
			 }
		 }

		 return false;
	}
	
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	switch (v.getId()) {
			case R.id.btnRecent:
	        	openDialog(v.getId());
				break;
				
			case R.id.btnDownload:
				Intent intent = new Intent();
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction("android.intent.action.VIEW_DOWNLOADS");
				try {
					startActivity(intent);
				}catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(AllApp.this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
        }
    };
    
    private int openDialog(final int btnid){
        /*create a grid view*/
        ListView listview = new ListView(this);
        listview.setBackgroundColor(Color.WHITE);
        listview.setAdapter(new ListviewAdapterRecentApp(this, mApkRecent, mApkInformation)); 
        listview.setCacheColorHint(Color.TRANSPARENT);
        listview.setClickable(true);

        

        AlertDialog.Builder builder=  new AlertDialog.Builder(this);
        builder.setTitle(R.string.apprecent);
        builder.setView(listview);
        final AlertDialog dialog = builder.create();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		    // TODO Auto-generated method stub
		    if((mApkRecent != null)&&(arg2 < mApkRecent.size()))
				startRecentApp(arg2);
			      dialog.dismiss();
		    }
	});

    	/*show then set attributes*/
    	dialog.show();
    	
    	return 0;
    }
	
	
	public void getLauncherApk()
	{
		Intent intent = new Intent(Intent.ACTION_MAIN,null);
        	intent.addCategory(Intent.CATEGORY_LAUNCHER);

		PackageManager pm = getPackageManager();
   
		List<ResolveInfo> resolve = pm.queryIntentActivities(intent,0);
		Collections.sort(resolve, new ResolveInfo.DisplayNameComparator(pm));

		mApkInformation.clear();
		
		for(int i = 0; i < resolve.size(); i++)
		{
			ResolveInfo res = resolve.get(i);

			String packageName = res.activityInfo.packageName;
			if(res.activityInfo.name.compareTo("com.android.contacts.DialtactsActivity") == 0)
				continue;

			if(filterApk(packageName))
				continue;
			
			PackageInformation apkInfor = new PackageInformation();
			apkInfor.setAppName(res.loadLabel(pm).toString());
			apkInfor.setPackageName(packageName);
			apkInfor.setIcon(res.loadIcon(pm));
			apkInfor.setActivityName(res.activityInfo.name);
			mApkInformation.add(apkInfor);
		}
		
		/*check recent apk! if removed from system, then remove from recent*/
		for(int i = 0; i < mApkRecent.size(); i++){
			try {
				PackageInfo info = pm.getPackageInfo(mApkRecent.get(i).getPackageName(), 
											PackageManager.GET_ACTIVITIES);
				if(null == info){
					mApkRecent.remove(i);
				}
			} catch (NameNotFoundException e) {
				mApkRecent.remove(i);
				e.printStackTrace();
			}
		}
	}
	
	private Runnable getApkInfoRunnable = new Runnable()
	{
		public void run() 
		{
			getLauncherApk();
			
			Message msg=new Message();
			msg.what = UPDATA_UI;
			msg.arg1 = mApkInformation.size();
			mHandler.sendMessage(msg);
		}
	};

	private Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg)
		 {
			switch (msg.what)
			{
				case UPDATA_UI:
				{
					String str = getResources().getString(R.string.application_num);
					mTextView.setText("    "+str+" "+msg.arg1);
					if((rootView != null) && (mGridView != null)){
						rootView.removeView(mGridView);
					}
					createGridView();
				}
				break;
			}
		 }
	};

	void openSaveEditor()
	{
		if(mSaveEditor == null){
			mSaveEditor = this.getSharedPreferences(SHAREDNAME,Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
              }
	}

	public void restoreRecentApps()
	{
		mApkRecent.clear();
		
		openSaveEditor();
		int count = mSaveEditor.getInt(AppCount, 0);	
		for(int i = 0; i < count; i ++)
		{
			PackageInformation apkInfor = new PackageInformation();
			apkInfor.setAppName(mSaveEditor.getString(AppName+i, null));
			apkInfor.setPackageName(mSaveEditor.getString(PackageName+i, null));
			apkInfor.setVersionCode(mSaveEditor.getInt(VersionCode+i, 0));
			apkInfor.setVersionName(mSaveEditor.getString(VersionName+i, null));
			apkInfor.setActivityName(mSaveEditor.getString(ActivityName+i, null));
			
			mApkRecent.add(apkInfor);
		}
	}


	public void saveRecentApps()
	{
	       openSaveEditor();
		if((null==mApkRecent) || (null==mSaveEditor)){
			return ;
		}
              SharedPreferences.Editor editor = mSaveEditor.edit();
              editor.clear();
              editor.commit();

		saveAppCount(AppCount,mApkRecent.size());
		for(int i = 0; i < mApkRecent.size(); i++)
		{
			editor.putString(AppName+i, mApkRecent.get(i).getAppName());
			editor.putString(PackageName+i, mApkRecent.get(i).getPackageName());
			editor.putInt(VersionCode+i, mApkRecent.get(i).getVersionCode());
			editor.putString(VersionName+i, mApkRecent.get(i).getVersionName());
			editor.putString(ActivityName+i, mApkRecent.get(i).getActivityName());
		}
		editor.commit();
	}
	
	private void saveAppCount(String key,int value)
	{
		openSaveEditor();
		SharedPreferences.Editor editor = mSaveEditor.edit();
		editor.putInt(key, value);
		editor.commit();
	}
}


