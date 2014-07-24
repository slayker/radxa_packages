/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockchip.settings;

import android.net.ConnectivityManager;
import android.net.sip.SipManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.graphics.Bitmap;

import android.app.TabActivity;
import android.app.Activity;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.TabHost;
import android.content.res.Resources;
import android.view.LayoutInflater;
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import java.util.Map;
import java.util.HashMap;
import android.widget.Toast;
import android.widget.TabWidget;
import android.widget.AdapterView;
import android.os.AsyncTask;

import android.content.Intent;
import android.graphics.Color;
import android.bluetooth.BluetoothAdapter;
import android.view.Window;
import android.os.SystemClock;
import android.content.ComponentName;
import java.io.File; 
import android.graphics.drawable.Drawable;
import android.widget.Button;

import com.rockchip.settings.dialog.*;

import com.rockchip.settings.deviceinfo.UsbMode;
import com.rockchip.settings.ethernet.EthernetSettings;
import com.rockchip.settings.screen.ScreenSettings;
import com.rockchip.settings.sound.RingtoneSettings;
import com.rockchip.settings.sound.SoundSettings;
import com.rockchip.settings.sound.VolumeSettings;

import android.security.Credentials;
import android.security.KeyStore;
import com.rockchip.settings.bluetooth.BluetoothSettings;
import com.rockchip.settings.pppoe.PppoeSettings;
import com.rockchip.settings.vpn.VpnSettings;
import android.content.ComponentName;

import com.rockchip.settings.accounts.*;
import com.rockchip.settings.privacy.*;
import com.rockchip.settings.security.*;
import com.rockchip.settings.inputmethod.*;
import com.rockchip.settings.wifi.*;
import android.util.DisplayMetrics;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;


public class RKSettings extends TabActivity
{
	private String TAG = "RKSettings";
	private Bundle mBundle = null;
	RelativeLayout mLayout = null;
	private TextView mTextView = null;
	private ImageView mImageView = null;
	private TabHost mTabHost = null;
	private ListView mListView = null;
	private RelativeLayout  mScanCategoryView = null;
	// define for vpn
	private boolean mUnlocking = false;
	private final KeyStore mKeyStore = KeyStore.getInstance();

	private SettingItemAddManager mSettingItemManager = null; 
	
	// 以太网
	private EthernetSettings mEthernet = null;
    // 蓝牙
	private BluetoothSettings mBluetooth = null;
	// PPPOE
	private PppoeSettings mPPPOE = null;
	// vpn
	private VpnSettings mVpn = null;
	//wifi
	Wifi_Enabler mWifi_Enabler=null;
	/*
	 * @author cx
	 * Device class:usb,sound,display,screen,storage,application
	 */
//	private UsbSettings mUsbSettings = null;
	private DisplaySettings mDisplaySettings = null;
	private SoundSettings mSoundSettings = null;
	private ScreenSettings mScreenSettings = null;
	private UsbMode mUsbMode = null;

	/*
	 * end
	 */
	
	
	// 用于保存System,NetWork,Display,Others下的字符资源的ID,该ID用来提取出字符资源，并将该ID号设置为对应View的tag，根据View的tag来判断
	// 哪个view被点击
	private Map<String, ArrayList<SettingItem>> mMap = new HashMap<String, ArrayList<SettingItem>>();
	// ListView的Adapter
	private ListViewAdapter mListViewAdapter = null;

	private int[] mTextId = { R.string.header_category_system,
			R.string.header_category_wireless_networks, R.string.header_category_device,
			R.string.header_category_personal };
	private int[] mBitmap = { R.drawable.system, R.drawable.network,
			R.drawable.display, R.drawable.others };

	/*language  Setting*/
	LanguageSetting mLanguageSetting = null;
	/*keyboard Setting*/
	KeyBoardSetting mKeyBoardSetting = null;
	/*date and time*/
	DateAndTimeSetting mDateAndTime = null;
	/*usb mode setting*/
	UsbModeSettings mUsbModeSetting = null;
	/*event notification setting*/
	EventNotificationSetting mEventNotification = null;
	/*storage show*/
	StorageShow mStorageShow = null;
	/*storage format*/
	StorageFormat mStorageFormat = null;
	/*permit unknown sources*/
	UnknownSources mUnknownSources = null;
	/*usb debugging*/
	UsbDebugging mUsbDebugging = null;
	/*application manager*/
	ApplicationSetting mApplicationSetting = null;

	// 自动同步设置
	AutosyncSetting mAutosyncSetting = null;

	// 账户设置
	ManageAccountsSetting mManageAccountsSetting = null;

	// 隐私权设置
	PrivacySetting mPrivacySetting = null;
	
	// 备份设置
	BackDataSetting mBackDataSetting = null;
	
	// 自动还原设置
	AutoRestoreSetting mAutoRestoreSetting = null;
	
	// 备份账户设置
	ConfigureAccountSetting mConfigureAccountSetting = null;

	// 受信任的证书设置
	TrustedCredentialsSetting mTrustedCredentialsSetting = null;
	
	// 设备管理器设置
	DeviceAdminSetting mDeviceAdminSetting = null;
	
	// 安装证书设置
	CredentialsInstallSetting mCredentialsInstallSetting = null;
	
	// 删除证书设置
	CredentialsResetSetting mCredentialsResetSetting = null;

	// 语音搜索设置
	VoiceSearchSetting mVoiceSearchSetting = null;
	
	// 指针速度设置
	PointerSpeedSetting mPointerSpeedSetting = null;
	
	// TTS设置
	TextToSpeechSetting mTextToSpeechSetting = null;

		// 设备信息
	DeviceInfo mDeviceInfo = null;

	private void getScreenSize()	
	{		
		DisplayMetrics displayMetrics = new DisplayMetrics();	        
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);	        
		ScreenInformation.mScreenWidth = displayMetrics.widthPixels;	        
		ScreenInformation.mScreenHeight = displayMetrics.heightPixels;		 
		ScreenInformation.mDensityDpi = displayMetrics.densityDpi;		 
		ScreenInformation.mDpiRatio = ((float)ScreenInformation.mDefaultDpi)/(float)displayMetrics.densityDpi;	        
		Log.d("mainActivity","displayMetrics.densityDpi is: " + ScreenInformation.mDensityDpi);	        
		Log.d("mainActivity","displayMetrics.widthPixels is: " + ScreenInformation.mScreenWidth);	        
		Log.d("mainActivity","displayMetrics.heightPixels is: " + ScreenInformation.mScreenHeight);	
	}

	private void createHead()
	{
		ImageView image = (ImageView)findViewById(R.id.title_img);
		Bitmap resize = bitMapScale(R.drawable.setting,ScreenInformation.mDpiRatio);
		Log.d("Settings","createTitle(), resize = "+resize.getWidth()+",height = "+resize.getHeight());
		image.setScaleType(ImageView.ScaleType.CENTER);
		image.setImageBitmap(resize);

		TextView title = (TextView)findViewById(R.id.title_text);
		title.setTextSize(ScreenInformation.mScreenWidth/25f*ScreenInformation.mDpiRatio);
	}

	private Bitmap bitMapScale(int id,float scaleParameter)
	{
		Bitmap map = BitmapFactory.decodeResource(this.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*scaleParameter;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}

	private void createSpace()
	{
		TextView headSpace = (TextView)findViewById(R.id.head_space);
		TextView buttom = (TextView)findViewById(R.id.bottom_space);
		int height = (int)(ScreenInformation.mScreenWidth/20f);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,height);
		headSpace.setLayoutParams(params);
		buttom.setLayoutParams(params);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mBundle = savedInstanceState;
		int level = 0;
		int indicator = 0;
		int parent = -1;
		setContentView(R.layout.main);
		getScreenSize();
		createSpace();
		createHead();
		createShow();
		createTabHost();
		createScanDeviceCategory();
		mTabHost.requestFocus();

		
		if(savedInstanceState != null)
		{
			level = savedInstanceState.getInt("Level");
			indicator = savedInstanceState.getInt("Indicator");
			parent = savedInstanceState.getInt("Parent");
			LOGD("onCreate(), level = "+level+", indicator = "+indicator+",parentId = "+parent);
		}
		mTabHost.setCurrentTab(indicator);
		setTabHostFocus(indicator,level,parent);
		initContentStatus();
		mSettingItemManager = SettingItemAddManager.getInstance();
		mSettingItemManager.setContentMap(mMap);

	}

	private void getLanguageDefault() {
		mLanguageSetting = new LanguageSetting(RKSettings.this, mHandler);
		updateSettingItem(R.string.language_settings_category,
				mLanguageSetting.getDefaultLanguageSetting(), null, null);
	}

	private void getKeyBoardDefault() {
		mKeyBoardSetting = new KeyBoardSetting(RKSettings.this, mHandler,
				R.string.keyboard_settings_category);
		updateSettingItem(R.string.keyboard_settings_category,
				mKeyBoardSetting.getKeyBoardDefault(), null, null);
	}

	private void getDataAndTimeSettingDefault() {
		mDateAndTime = new DateAndTimeSetting(this, mHandler);
		if (mDateAndTime != null) {
			mDateAndTime.setAdapter(mListViewAdapter);
			mDateAndTime.resume();
			mDateAndTime.getDateAndTimeDefault();
		}
	}

	private void getEventNotificationDefault() {
		mEventNotification = new EventNotificationSetting(RKSettings.this,
				mHandler, mListViewAdapter);
	}

	private void getUsbModeSettingDefault() {
		mUsbModeSetting = new UsbModeSettings(RKSettings.this, mHandler, mListViewAdapter);
	}

	// zw(zw@rockchips.com)
	private void getStorageShowDefault() {
		mStorageShow = new StorageShow(RKSettings.this, mHandler);
		updateSettingItem(R.string.storage_setting, "", null, null);
	}

	private void getStorageFormatDefault() {
		mStorageFormat = new StorageFormat(RKSettings.this, mHandler);
		updateSettingItem(R.string.storage_format, "", null, null);
	}

	private void getUnknownSourcesDefault() {
		mUnknownSources = new UnknownSources(RKSettings.this, mHandler,
				mListViewAdapter);
	}

	private void getUsbDebuggingDefault() {
		mUsbDebugging = new UsbDebugging(RKSettings.this, mHandler,
				mListViewAdapter);
	}

	private void getManageApplicationDefault() {
		mApplicationSetting = new ApplicationSetting(RKSettings.this, mHandler);
		updateSettingItem(R.string.application_manage, "", null, null);
	}

	// 自动同步设置
	private void getAutosyncDefault() {
		mAutosyncSetting = new AutosyncSetting(this, mHandler);

	}

	// 账号设置
	private void getManageAccountsDefault() {
		mManageAccountsSetting = new ManageAccountsSetting(this, mHandler);
	}

	// 恢复出厂设置
	private void getPrivacyDefault() {
		mPrivacySetting = new PrivacySetting(this, mHandler);
	}
	//备份设置
	private void getBackDataDefault() {
		mBackDataSetting = new BackDataSetting(this, mHandler);
		mBackDataSetting.getDefaultBackDataSetting();
	}
	//自动还原
	private void getAutoRestoreDefault() {
		mAutoRestoreSetting = new AutoRestoreSetting(this, mHandler);
		mAutoRestoreSetting.getDefaultAutoRestoreSetting();
	}
	//备份账户
	private void getConfigureAccountDefault() {
		mConfigureAccountSetting = new ConfigureAccountSetting(this, mHandler);
		mConfigureAccountSetting.getDefaultConfigureAccountSetting();
	}

	 // 受信任的证书
	 private void getTrustedCredentialsDefault() {
		 mTrustedCredentialsSetting = new TrustedCredentialsSetting(this, mHandler);
	 }
	 //设备管理器
	 private void getDeviceAdminDefault() {
		 mDeviceAdminSetting = new DeviceAdminSetting(this, mHandler);
	 }
	//证书安装
	 private void getCredentialsInstallDefault() {
		 mCredentialsInstallSetting = new CredentialsInstallSetting(this, mHandler);
	 }
	 //证书删除
	 private void getCredentialsResetDefault() {
		 mCredentialsResetSetting = new CredentialsResetSetting(this, mHandler);
		 mCredentialsResetSetting.getDefaultCredentialsResetSetting();
	 }

	//语音搜索
	private void getVoiceSearchDefault() {
		mVoiceSearchSetting = new VoiceSearchSetting(this, mHandler);
	}
	
	//指针速度
	private void getPointerSpeedDefault() {
		mPointerSpeedSetting = new PointerSpeedSetting(this, mHandler);
	}
	
	//TTS
	private void getTextToSpeechDefault() {
		mTextToSpeechSetting = new TextToSpeechSetting(this, mHandler);
	}

	private void getDeviceInfoDefault() {
		mDeviceInfo = new DeviceInfo(this, mHandler);
		mDeviceInfo.getDeviceInfor();
	}
		//WIFI
	private void getWiFiDefault()
	{
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
		{
			mWifi_Enabler= new Wifi_Enabler(this,mListViewAdapter,mHandler);
			if(mWifi_Enabler != null)
			{
				mWifi_Enabler.resume();
				mWifi_Enabler.getWifiDefault();
			}
		}
	}
	Wifi_ApEnabler mWifi_ApEnabler;
	private void getWifiApDefault()
	{
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)
			&& !getPackageManager().hasSystemFeature("android.setting.portable_hotspot"))
		{
			mWifi_ApEnabler = new Wifi_ApEnabler(this,mHandler,mListViewAdapter);
			mWifi_ApEnabler.resume();
		}
	}
	
	Wifi_ApSettings mWifi_ApSettings;
	private void getWifiApSettingsDefault()
	{
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
		{
			if(!getPackageManager().hasSystemFeature("android.setting.portable_hotspot"))
			{
				mWifi_ApSettings = new Wifi_ApSettings(this,mHandler,mWifi_ApEnabler);
				if(mWifi_ApSettings != null)
				mWifi_ApSettings.onResume();
			}
		}
	}


	// 初始化Setting中每一项的状态值，应将Setting中每一项需要显示状态的项在这里设置状态
	// 状态值一般从数据库中获取或者根据硬件的初始化成功失败情况来设置
	private void initContentStatus() {
		Log.d(TAG, "OnCreate:initContentStatus()==================");
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... unused) {

				loadSystemResource();
				loadNetWorkResource();
				loadDeviceResource();
				loadPersonalResource();
				// add code here
				getLanguageDefault();
				getKeyBoardDefault();
				getDataAndTimeSettingDefault();
				getUsbModeSettingDefault();
				getEventNotificationDefault();
				getStorageShowDefault();
				getStorageFormatDefault();
				getUnknownSourcesDefault();
				getUsbDebuggingDefault();
				getManageApplicationDefault();

				netWorkInit();
				
				
				/*
				 * @author cx
				 * for device
				 */
//				usbSettiingsInit();
				SoundSettingsInit();
				DisplaySettingsInit();
				ScreenSettingsInit();
				UsbModeSettingsInit();
				/*
				 * end
				 */
				 
				// Personal
				getAutosyncDefault();
				getManageAccountsDefault();
				getPrivacyDefault();
				getBackDataDefault();
				getAutoRestoreDefault();
				getConfigureAccountDefault();
				getTrustedCredentialsDefault();
				getDeviceAdminDefault();
				getCredentialsInstallDefault();
				getCredentialsResetDefault();
				getVoiceSearchDefault();
				getPointerSpeedDefault();
				getTextToSpeechDefault();
				getDeviceInfoDefault();
				//WIFI
				getWiFiDefault();
				getWifiApDefault();

				mHandler.sendEmptyMessageDelayed(
						SettingMacroDefine.upDateListView, 10);
				return null;
			}
		}.execute();
	}


	private void netWorkInit()
	{
		// 以太网
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_ETHERNET))
		{
			mEthernet = new EthernetSettings(this,mHandler);
			if(mEthernet != null)
				mEthernet.Resume();
		}

		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
		{
			mBluetooth = new BluetoothSettings(this,mHandler);
			if(mBluetooth != null)
				mBluetooth.Resume();
				
			//  delete Blutooth resources
			if(!mBluetooth.BluetoothExist())
			{
				 deleteBluetoothResource();
			}
		}

		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_PPPOE))
		{
			mPPPOE = new PppoeSettings(this,mHandler);
			if(mPPPOE != null)
				mPPPOE.Resume();
		}

		// vpn
		if(!getPackageManager().hasSystemFeature("android.settings.vpn"))
		{
			mVpn = new VpnSettings(mBundle,this,mHandler,mKeyStore);
			if(mVpn != null)
				mVpn.Resume();
		}
	}

	public void deleteBluetoothResource()
	{
	   int[] id = {R.string.bluetooth_settings_title,
	   				R.string.bluetooth_settings,
	   				R.string.bluetooth_device_advanced_rename_device,
	                R.string.bluetooth_visibility,
	                R.string.bluetooth_preference_found_devices};
	   
		if(mMap == null)
			return;
		
		ArrayList<SettingItem> array = (ArrayList<SettingItem>)mMap.get("network");
		if(array != null)
		{
			for(int i = 0; i < 5; i++)
			{
				SettingItem item = findSettingItem("network",id[i]);
				if(item != null)
					array.remove(item);
			}
		}
	}
	
	private boolean vpnLock()
	{
//$_rbox_$_modify_$_chenxiao: remove keyguard protect
//$_rbox_$_modify_$_begin
//		if (mKeyStore.state() != KeyStore.State.UNLOCKED) 
//		{
//            if (!mUnlocking) {
//                // Let us unlock KeyStore. See you later!
//                Credentials.getInstance().unlock(this);
//            }
//			else 
//			{
//                // We already tried, but it is still not working!
//    //            finishFragment();
//            }
//            mUnlocking = !mUnlocking;
//            return false;
//        }
//$_rbox_$_modify_$_end

        // Now KeyStore is always unlocked. Reset the flag.
        mUnlocking = false;

		return true;
	}
	
	/*
	 * @author cx
	 * for device class init
	 */
//	private void usbSettiingsInit(){
//		mUsbSettings = new UsbSettings(this, mHandler);
//		mUsbSettings.Resume();
//	}
	
	private void SoundSettingsInit(){
		mSoundSettings = new SoundSettings(this, mHandler);
	}

	private void DisplaySettingsInit(){
		mDisplaySettings = new DisplaySettings(this, mHandler);
		mDisplaySettings.Resume();
	}
	
	private void ScreenSettingsInit(){
		mScreenSettings = new ScreenSettings(this, mHandler);
	}
	
	private void UsbModeSettingsInit(){
		mUsbMode = new UsbMode(this, mHandler);
		mUsbMode.Resume();
	}
	
	/*
	 * end
	 */
	
	private void createShow() {
		View mView = (View) findViewById(R.id.setting_show);
		mTextView = (TextView) mView.findViewById(R.id.setting_show_text);
		mTextView.setTextSize(ScreenInformation.mScreenWidth/25f*ScreenInformation.mDpiRatio);
		mImageView = (ImageView) mView.findViewById(R.id.setting_show_image);
	}

	private void setTabHostSize(View view,int imageId,int textId,int srcId)
	{
		ImageView image = (ImageView)view.findViewById(imageId);
		Bitmap map = bitMapScale(srcId,ScreenInformation.mDpiRatio);
		image.setImageBitmap(map);
		
		TextView text = (TextView)view.findViewById(textId);
		float fontsize = ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio;
		text.setTextSize(fontsize);
	}
	
	// create TabHost
	private void createTabHost() {
		mTabHost = getTabHost();
		if (mTabHost == null)
			return;

		LayoutInflater flater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Resources res = this.getResources();
		View system = flater.inflate(R.layout.setting_tabwidget_system, null);
		View network = flater.inflate(R.layout.setting_tabwidget_network, null);
		View device = flater.inflate(R.layout.setting_tabwidget_device, null);
		View person = flater.inflate(R.layout.setting_tabwidget_person, null);

		setTabHostSize(system,R.id.tabwidget_image,R.id.tabwidget_text,R.drawable.system_small);
		setTabHostSize(network,R.id.tabwidget_image,R.id.tabwidget_text,R.drawable.network_small);
		setTabHostSize(device,R.id.tabwidget_image,R.id.tabwidget_text,R.drawable.display_small);
		setTabHostSize(person,R.id.tabwidget_image,R.id.tabwidget_text,R.drawable.others_small);
		
		// system.
		mTabHost.addTab(mTabHost.newTabSpec("0").setIndicator(system)
				.setContent(mContentFactory));
		mTabHost.addTab(mTabHost.newTabSpec("1").setIndicator(network)
				.setContent(mContentFactory));
		mTabHost.addTab(mTabHost.newTabSpec("2").setIndicator(device)
				.setContent(mContentFactory));
		mTabHost.addTab(mTabHost.newTabSpec("3").setIndicator(person)
				.setContent(mContentFactory));
		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				LOGD("tabId = " + tabId);
				int id = Integer.parseInt(tabId);
				mTabHost.setCurrentTab(id);
				setTabHostFocus(id,0,-1);
			}
		});
	}

	// create ListView bind to TabHost
	private View createListView() {
		mListView = (ListView) findViewById(R.id.tabconent_list);
		mListViewAdapter = new ListViewAdapter(this, mMap);
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(mListItemClister);
		mListView.setOnItemLongClickListener(mLongListItemClickListener);
		return mListView;
	}

	void createScanDeviceCategory()
	{
		LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mScanCategoryView = (RelativeLayout)flater.inflate(R.layout.bluetooth_category,null);
		if(mScanCategoryView != null)
		{
			TextView view = (TextView)mScanCategoryView.findViewById(R.id.bluetooth_category_name);
			view.setText(R.string.bluetooth_preference_found_devices);
			view.setTextSize(ScreenInformation.mScreenWidth/50f*ScreenInformation.mDpiRatio);
		}
	}
	
	private TabHost.TabContentFactory mContentFactory = new TabHost.TabContentFactory() {
		public View createTabContent(String tag) {
			if(mListView == null)
				return createListView();
			else
				return mListView;
		}
	};

	// 加载System下的资源资源
	private void loadSystemResource() {
		// 定义system下的字符资源
		SettingItem[] resources = {
				new SettingItem(0, -1, R.string.language_settings_category),
				new SettingItem(0, -1, R.string.keyboard_settings_category),
				new SettingItem(0, -1, R.string.date_and_time),
				new SettingItem(1, R.string.date_and_time,
						R.string.date_time_auto),
				new SettingItem(1, R.string.date_and_time,
						R.string.date_time_set_date),
				new SettingItem(1, R.string.date_and_time,
						R.string.date_time_set_time),
				new SettingItem(1, R.string.date_and_time,
						R.string.date_time_set_timezone),
				new SettingItem(1, R.string.date_and_time,
						R.string.date_time_24hour),
				new SettingItem(1, R.string.date_and_time,
						R.string.date_time_date_format),
				// USB
				new SettingItem(0, -1, R.string.usb_setting),
				// Event Notification
//				new SettingItem(0, -1, R.string.event_notification),
				// storage
				new SettingItem(0, -1, R.string.storage_setting),
				new SettingItem(0, -1, R.string.storage_format),
				// Unknown Sources
				new SettingItem(0, -1, R.string.unknown_sources),
				// Usb Debugging
				new SettingItem(0, -1, R.string.usb_debugging),
				// Manage Application
				new SettingItem(0, -1, R.string.application_manage)
		};
		if (mMap != null) {
			ArrayList<SettingItem> array = new ArrayList<SettingItem>();
			for (int i = 0; i < resources.length; i++) {
				array.add(resources[i]);
			}
			mMap.put("system", array);
		}
	}

	private void showToast(String message){
		Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
	}
	
	// 加载NetWork下的资源资源
	private void loadNetWorkResource() {
		// 定义network下的字符资源
		SettingItem[] wifi = {
				//wifi
				new SettingItem(0,-1,R.string.wifi_settings),
				new SettingItem(1,R.string.wifi_settings,R.string.wifi_settings_title),
				new SettingItem(1,R.string.wifi_settings,R.string.wifi_notify_open_networks),
				new SettingItem(1,R.string.wifi_settings,R.string.ap_list),
		};

		SettingItem[] wifi_hotspot = 
		{
				//wifi ap
				new SettingItem(0,-1,R.string.tether_settings_title_wifi),
				new SettingItem(1,R.string.tether_settings_title_wifi,R.string.wifi_tether_checkbox_text),
				new SettingItem(1,R.string.tether_settings_title_wifi,R.string.wifi_tether_configure_ap_text)
		};

		SettingItem[] ethernet = {
				//ETHERNET
				new SettingItem(0,-1,R.string.ethernet_settings),
				new SettingItem(1,R.string.ethernet_settings,R.string.ethernet_settings_title),
				new SettingItem(1,R.string.ethernet_settings,R.string.ethernet_infor),
				new SettingItem(2,R.string.ethernet_infor,R.string.ethernet_mac_addr),
				new SettingItem(2,R.string.ethernet_infor,R.string.ethernet_ip_addr),
				new SettingItem(2,R.string.ethernet_infor,R.string.ethernet_netmask),
				new SettingItem(2,R.string.ethernet_infor,R.string.ethernet_gateway),
				new SettingItem(2,R.string.ethernet_infor,R.string.ethernet_dns1),
				new SettingItem(2,R.string.ethernet_infor,R.string.ethernet_dns2),
				new SettingItem(1,R.string.ethernet_settings,R.string.ethernet_use_static_ip),
				new SettingItem(1,R.string.ethernet_settings,R.string.ethernet_static_ip_setting),
				new SettingItem(2,R.string.ethernet_static_ip_setting,R.string.ethernet_static_ip),
				new SettingItem(2,R.string.ethernet_static_ip_setting,R.string.ethernet_static_gateway),
				new SettingItem(2,R.string.ethernet_static_ip_setting,R.string.ethernet_static_netmask),
				new SettingItem(2,R.string.ethernet_static_ip_setting,R.string.ethernet_static_dns1),
				new SettingItem(2,R.string.ethernet_static_ip_setting,R.string.ethernet_static_dns2)
		};

		SettingItem[] bluetooth = {
				// bluetooth
				new SettingItem(0,-1,R.string.bluetooth_settings_title),
				new SettingItem(1,R.string.bluetooth_settings_title,R.string.bluetooth_settings),
				new SettingItem(1,R.string.bluetooth_settings_title,R.string.bluetooth_device_advanced_rename_device),
				new SettingItem(1,R.string.bluetooth_settings_title,R.string.bluetooth_visibility),
				new SettingItem(1,R.string.bluetooth_settings_title,R.string.bluetooth_preference_found_devices).setView(mScanCategoryView),
		};

		SettingItem[] pppoe = {
				// PPPOE
				new SettingItem(0,-1,R.string.pppoe_settings),
				new SettingItem(1,R.string.pppoe_settings,R.string.pppoe_connect),
				new SettingItem(1,R.string.pppoe_settings,R.string.pppoe_phy_iface),
				new SettingItem(1,R.string.pppoe_settings,R.string.pppoe_add_account),
		};
		
		SettingItem[] mobileNetWork = 
		{
				// mobile setting
				new SettingItem(0,-1,R.string.network_settings_title),
		};

		
		SettingItem[] vpn = {
				// vpn
				new SettingItem(0,-1,R.string.vpn_title),
				new SettingItem(1,R.string.vpn_title,R.string.vpn_create),
				
			};

				
		if (mMap != null) {
			ArrayList<SettingItem> array = new ArrayList<SettingItem>();
			if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
			{
				for (int i = 0; i < wifi.length; i++) {
					array.add(wifi[i]);
				}
			}

			if(!getPackageManager().hasSystemFeature("android.setting.portable_hotspot"))
			{
				for (int i = 0; i < wifi_hotspot.length; i++) {
					array.add(wifi_hotspot[i]);
				}
			}
			
			// load ethernet resource
			if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_ETHERNET))
			{
				for (int i = 0; i < ethernet.length; i++) {
					array.add(ethernet[i]);
				}
			}

			if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
			{
				for (int i = 0; i < bluetooth.length; i++) {
					array.add(bluetooth[i]);
				}
			}

			if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_PPPOE))
			{
				for (int i = 0; i < pppoe.length; i++) {
					array.add(pppoe[i]);
				}
			}
			
			if(!getPackageManager().hasSystemFeature("android.settings.mobile"))
			{
				for (int i = 0; i < mobileNetWork.length; i++) {
					array.add(mobileNetWork[i]);
				}
			}

			if(!getPackageManager().hasSystemFeature("android.settings.vpn"))
			{
				for (int i = 0; i < vpn.length; i++) {
					array.add(vpn[i]);
				}
			}
			mMap.put("network", array);
		}
	}

	// 加载Device下的资源资源
	private void loadDeviceResource() {
		LOGD("device resources");
		// 定义device下的字符资源
		SettingItem[] resources = {
				// add resource here
				
				new SettingItem(0,-1,R.string.sound_settings),
				new SettingItem(1,R.string.sound_settings,R.string.all_volume_title),
				new SettingItem(1,R.string.sound_settings,R.string.notification_sound_title),
				new SettingItem(1,R.string.sound_settings,R.string.sound_effects_enable_title),
				new SettingItem(1,R.string.sound_settings,R.string.lock_sounds_enable_title),
				new SettingItem(1,R.string.sound_settings,R.string.sound_devices_manager_title),
				
				new SettingItem(0,-1,R.string.display_settings),
				
				new SettingItem(0,-1,R.string.screen_settings),
				new SettingItem(1,R.string.screen_settings,R.string.screenscale),
				new SettingItem(1,R.string.screen_settings,R.string.screen_interface),
				new SettingItem(1,R.string.screen_settings,R.string.screen_mode_title),
				
				new SettingItem(0, -1, R.string.storage_menu_usb)
				
//				new SettingItem(0,-1,R.string.storage_settings),
//				
//				new SettingItem(0,-1,R.string.applications_settings)
		};
		if (mMap != null) {
			ArrayList<SettingItem> array = new ArrayList<SettingItem>();
			for (int i = 0; i < resources.length; i++) {
				array.add(resources[i]);
			}
			mMap.put("device", array);
		}
	}

	// 加载Others下的资源资源
	private void loadPersonalResource() {
		// 定义others下的字符资源
		SettingItem[] resources = {
				// add resource here
				// Account
				new SettingItem(0, -1, R.string.sync_settings),
				new SettingItem(0, -1, R.string.account_sync_settings_title),

				// Security
				new SettingItem(0, -1, R.string.manage_device_admin),
				new SettingItem(0, -1, R.string.credentials_title),
				new SettingItem(1, R.string.credentials_title,
						R.string.trusted_credentials),
				new SettingItem(1, R.string.credentials_title,
						R.string.credentials_install),
				new SettingItem(1, R.string.credentials_title,
						R.string.credentials_reset),

				//inputmethod
				new SettingItem(0, -1, R.string.recognizer_settings_title),
				new SettingItem(0, -1, R.string.tts_settings_title),
				new SettingItem(0, -1, R.string.pointer_speed),
				
				// Privacy
				new SettingItem(0, -1, R.string.backup_section_title),
				new SettingItem(1, R.string.backup_section_title,
						R.string.backup_data_title),
				new SettingItem(1, R.string.backup_section_title,
						R.string.backup_configure_account_title),
				new SettingItem(1, R.string.backup_section_title,
						R.string.auto_restore_title),
				new SettingItem(0, -1, R.string.master_clear_title),
				new SettingItem(0, -1, R.string.about_settings),
				new SettingItem(1, R.string.about_settings, R.string.settings_license_activity_title),
				new SettingItem(1, R.string.about_settings, R.string.legal_information),
				new SettingItem(1, R.string.about_settings, R.string.device_status_activity_title),
				new SettingItem(1, R.string.about_settings, R.string.model_number),
				new SettingItem(1, R.string.about_settings, R.string.firmware_version),
				new SettingItem(1, R.string.about_settings, R.string.kernel_version),
				new SettingItem(1, R.string.about_settings, R.string.build_number), 	

				};
		if (mMap != null) {
			ArrayList<SettingItem> array = new ArrayList<SettingItem>();
			for (int i = 0; i < resources.length; i++) {
				array.add(resources[i]);
			}
			mMap.put("personal", array);
		}

		
	}

	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			LOGD("view's tag = " + view.getTag() + ",position = " + position
					+ ",arg3 = " + arg3);
			int id = ((Integer) view.getTag()).intValue();
			if (!haveChild(mListViewAdapter.getContent(),id)) {
				if(!settingItemClick(id))
				{
					onSystemItemClick(view, position);
					onNetWorkItemClick(view, position);
					onDeviceItemClick(view, position);
					onPersonalItemClick(view, position);
				}
			}
		}
	};

	private AdapterView.OnItemLongClickListener mLongListItemClickListener = new AdapterView.OnItemLongClickListener()
	{
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3)
		{
			int id = ((Integer) view.getTag()).intValue();
			if (!haveChild(mListViewAdapter.getContent(),id)) 
			{
				if(!settingItemLongClick(id))
				{
					// add code here
				}
			}


			return true;
		}
	};
	
	// 用于调用自己设置的点击事件处理函数
	private boolean settingItemClick(int id)
	{
		SettingItem item = null;
		if(((item = findSettingItem("system",id)) != null) || ((item = findSettingItem("network",id)) != null) ||
						((item = findSettingItem("device",id)) != null) || ((item = findSettingItem("personal",id)) != null))
		{
			return item.onSettingItemClick(id);
		}

		return false;
	}


	private boolean settingItemLongClick(int id)
	{
		SettingItem item = null;
		if(((item = findSettingItem("system",id)) != null) || ((item = findSettingItem("network",id)) != null) ||
						((item = findSettingItem("device",id)) != null) || ((item = findSettingItem("personal",id)) != null))
		{
			return item.onSettingItemLongClick(id);
		}

		return false;
	}
	
	private boolean haveChild(String name, int id) {
		LOGD("havaChild(),name = " + name + ",id = " + id);
		if ((mMap != null) && (name != null)) {
			ArrayList array = (ArrayList<SettingItem>) mMap.get(name);
			if (array != null) {
				for (int i = 0; i < array.size(); i++) {
					SettingItem item = (SettingItem) array.get(i);
					if (item.mParentId == id) {
						if((id == R.string.vpn_title) && (!vpnLock()))
						{
							return false;
						}
						else
						{
							mListViewAdapter.setParentId(id);
							mListViewAdapter.setLevel(item.mLevel);
							mListViewAdapter.invalidate();
						}
						return true;
					}
				}
			}
		}

		return false;
	}

	private SettingItem findParent(String name, int Id) {
		if ((mMap != null) && (name != null)) {
			ArrayList array = (ArrayList<SettingItem>) mMap.get(name);
			if (array != null) {
				for (int i = 0; i < array.size(); i++) {
					SettingItem item = (SettingItem) array.get(i);
					if (item.mId == Id) {
						return item;
					}
				}
			}
		}

		return null;
	}

	// System目录下的选项被点击时的响应函数
	private void onSystemItemClick(View view, int position) {
		if (view == null)
			return;

		int tag = ((Integer) view.getTag()).intValue();
		switch (tag) {
		// add code here
		case R.string.language_settings_category:
			LOGD("language_settings id = "
					+ R.string.language_settings_category);
			mLanguageSetting.settingLanguage();
			break;
		case R.string.keyboard_settings_category:
			mKeyBoardSetting.settingKeyBoard();
			LOGD("keyboard_settings id = "
					+ R.string.keyboard_settings_category);
			break;
		case R.string.date_time_auto:
		case R.string.date_time_set_date:
		case R.string.date_time_set_timezone:
		case R.string.date_time_24hour:
		case R.string.date_time_date_format:
		case R.string.date_time_set_time:
			if (mDateAndTime != null) {
				mDateAndTime.onClick(tag);
			}
			break;
		case R.string.usb_setting:
			mUsbModeSetting.onUsbModeClick();
			break;
//		case R.string.event_notification:
//			mEventNotification.onEventNotificationClick();
//			break;
		case R.string.storage_setting:
			mStorageShow.showStorage();
			break;
		case R.string.storage_format:
			mStorageFormat.FormatStorage();
			break;
		case R.string.unknown_sources:
			mUnknownSources.SourcesUnknown();
			break;
		case R.string.usb_debugging:
			mUsbDebugging.DebuggingUsb();
			break;
		case R.string.application_manage:
			mApplicationSetting.SettingApplication();
			break;			
		default:break;
		}
	}

	// NetWork目录下的选项被点击时的响应函数
	private void onNetWorkItemClick(View view, int position) {
		if (view == null)
			return;

		int tag = ((Integer) view.getTag()).intValue();
		switch (tag) {
		// add code here
		    // ethernet
			case R.string.ethernet_settings_title:
			case R.string.ethernet_use_static_ip:
			case R.string.ethernet_static_ip_setting:
			case R.string.ethernet_static_ip:
			case R.string.ethernet_static_gateway:
			case R.string.ethernet_static_netmask:
			case R.string.ethernet_static_dns1:
			case R.string.ethernet_static_dns2:
 				if(mEthernet != null)
					mEthernet.onClick(tag);
				break;
			// bluetooth
			case R.string.bluetooth_settings:
			case R.string.bluetooth_visibility:
			case R.string.bluetooth_device_advanced_rename_device:
				if(mBluetooth != null)
					mBluetooth.handleClick(tag);
				break;
			// mobile
			case R.string.network_settings_title:
				try
				{
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.setComponent(new ComponentName("com.android.phone", 
							"com.android.phone.Settings"));
					startActivity(intent);
				}
				catch (ActivityNotFoundException e)
				{
					Toast.makeText(this, R.string.activity_not_found,Toast.LENGTH_SHORT).show();
				} 
				catch (SecurityException e)
				{
					Toast.makeText(this, R.string.activity_not_found,Toast.LENGTH_SHORT).show();
				}
				break;
			// pppoe
			case R.string.pppoe_connect:
			case R.string.pppoe_phy_iface:
			case R.string.pppoe_add_account:
				if(mPPPOE != null)
					mPPPOE.onClick(tag);
				break;

			// vpn
			case R.string.vpn_create:
				if(!getPackageManager().hasSystemFeature("android.settings.vpn"))
				{
					if(mVpn != null)
						mVpn.onClick();
				}
				break;
			case R.string.wifi_settings_title:
				if(mWifi_Enabler != null)
				mWifi_Enabler.onWiFiClick();
				break;
			case R.string.wifi_notify_open_networks:
				if(mWifi_Enabler != null)
					mWifi_Enabler.onNetWorkNotificaiton();
				break;
			case R.string.ap_list:
				String action = StorageUtils.getWiFiAction();
				if(action != null)
				{
					Intent wifisettingintent = new Intent(action); 		
					this.startActivity(wifisettingintent);
				}
				else
				{
					Toast.makeText(this,R.string.version_error,Toast.LENGTH_LONG).show();
				}
				break;
			case R.string.wifi_tether_checkbox_text:
				if(!getPackageManager().hasSystemFeature("android.setting.portable_hotspot"))
				{
					mWifi_ApEnabler.onPreferenceChange();
				}
				break;
			case R.string.wifi_tether_configure_ap_text:
				if(!getPackageManager().hasSystemFeature("android.setting.portable_hotspot"))
				{
					getWifiApSettingsDefault();
					if(mWifi_ApSettings != null)
						mWifi_ApSettings.showDialog();
				}
				break;
			default:break;
		}
	}

	// Device目录下的选项被点击时的响应函数
	private void onDeviceItemClick(View view, int position) {
		LOGD("DeviceItemClick");
		if (view == null)
			return;

		int tag = ((Integer) view.getTag()).intValue();
		switch (tag) {
		// add code here
//		case R.string.usb_settings:
//			if (mUsbSettings == null){
//				mUsbSettings = new UsbSettings(this, mHandler);
//			}
//			mUsbSettings.OnClick();
//			break;
			
//		case R.string.sound_settings:
//			if (mSoundSettings == null){
//				mSoundSettings = new SoundSettings(this, mHandler);
//			}
//			mSoundSettings.OnClick(R.string.sound_settings);
//			break;
			
		case R.string.all_volume_title:
			if (mSoundSettings == null){
				mSoundSettings = new SoundSettings(this, mHandler);
			}
			mSoundSettings.OnClick(R.string.all_volume_title);
			break;
			
		case R.string.notification_sound_title:
			if (mSoundSettings == null){
				mSoundSettings = new SoundSettings(this, mHandler);
			}
			mSoundSettings.OnClick(R.string.notification_sound_title);
			break;
			
		case R.string.sound_effects_enable_title:
			if (mSoundSettings == null){
				mSoundSettings = new SoundSettings(this, mHandler);
			}
			mSoundSettings.OnClick(R.string.sound_effects_enable_title);
			break;
			
		case R.string.lock_sounds_enable_title:
			if (mSoundSettings == null){
				mSoundSettings = new SoundSettings(this, mHandler);
			}
			mSoundSettings.OnClick(R.string.lock_sounds_enable_title);
			break;
			
		case R.string.sound_devices_manager_title:
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setComponent(new ComponentName("com.rockchip.settings",
					"com.rockchip.settings.sound.SoundDevicesManager"));
			
			StartActivitySafely(intent);
			break;
			
		case R.string.display_settings:
			if (mDisplaySettings == null){
				mDisplaySettings = new DisplaySettings(this, mHandler);
			}
			mDisplaySettings.OnClick();
			break;
			
//		case R.string.screen_settings:
//			if (mScreenSettings == null){
//				mScreenSettings = new ScreenSettings(this, mHandler);				
//			}
//			mScreenSettings.OnClick(R.string.screen_settings);
//			break;
			
		case R.string.screenscale:
			if (mScreenSettings == null){
				mScreenSettings = new ScreenSettings(this, mHandler);				
			}
			mScreenSettings.OnClick(R.string.screenscale);
			break;
			
		case R.string.screen_interface:
			if (mScreenSettings == null){
				mScreenSettings = new ScreenSettings(this, mHandler);				
			}
			mScreenSettings.OnClick(R.string.screen_interface);
			break;
			
		case R.string.screen_mode_title:
			if (mScreenSettings == null){
				mScreenSettings = new ScreenSettings(this, mHandler);				
			}
			mScreenSettings.OnClick(R.string.screen_mode_title);
			break;
			
		case R.string.storage_menu_usb:
			StartActivityForResultSafely(new Intent(this, com.rockchip.settings.deviceinfo.UsbSettings.class),
					UsbMode.REQUEST_CODE_USB_CONNECT_MODE);
			break;
			
		case R.string.storage_settings:
			
			break;
			
		case R.string.applications_settings:
			
			break;
			
		default:
			break;
		}
	}

	// Personal目录下的选项被点击时的响应函数
	private void onPersonalItemClick(View view, int position) {
		if (view == null)
			return;

		int tag = ((Integer) view.getTag()).intValue();
		switch (tag) {
		// add code here
		case R.string.account_sync_settings_title:
			mAutosyncSetting.settingAutosync();
			getAutosyncDefault();
			mListViewAdapter.invalidate();
			break;
		case R.string.sync_settings:
			mManageAccountsSetting.settingManageAccounts();
			break;
		case R.string.master_clear_title:
			// add function to process
			mPrivacySetting.settingPrivacy();
			break;
		case R.string.backup_data_title:
			// add function to process
			mBackDataSetting.settingBackData();
			break;
		case R.string.auto_restore_title:
			// add function to process
			mAutoRestoreSetting.settingAutoRestore();
			break;
		case R.string.backup_configure_account_title:
			// add function to process
			mConfigureAccountSetting.settingConfigureAccount();
			break;
		case R.string.trusted_credentials:
			mTrustedCredentialsSetting.settingTrustedCredentials();
			break;
		case R.string.manage_device_admin:
			mDeviceAdminSetting.settingDeviceAdmin();
			break;
		case R.string.credentials_install:
			mCredentialsInstallSetting.settingCredentialsInstall();
			break;
		case R.string.credentials_reset:
			mCredentialsResetSetting.settingCredentialsReset();
			break;
		case R.string.recognizer_settings_title:
			mVoiceSearchSetting.settingVoiceSearch();
			break;			
		case R.string.pointer_speed:
			mPointerSpeedSetting.settingPointerSpeed();
			break;
		case R.string.tts_settings_title:
			mTextToSpeechSetting.settingTextToSpeech();
			break;
		case R.string.device_status_activity_title:
			mDeviceInfo.getDeviceStatus();
			break;
		case R.string.settings_license_activity_title:
			mDeviceInfo.getLicense();
			break;
		case R.string.legal_information:
			mDeviceInfo.getTos();
			break;
			default:break;
		}
	}

	private void LOGD(String msg) {
		if (true)
			Log.d("Setting", msg);
	}

	// 设置当前TabHost的选中项
	private void setTabHostFocus(int current,int level, int parent) {
		mListViewAdapter.setLevel(level);
		mListViewAdapter.setParentId(parent);
		mTabHost.setCurrentTab(current);
		mListViewAdapter.setSelection(current);
		mListViewAdapter.invalidate();
		mListView.requestFocus();
		mListView.setSelection(0);
		mListView.invalidate();
		updateShowView(current);
		for (int i = 0; i < 4; i++) {
			if (i != current) {
				View view = mTabHost.getTabWidget().getChildAt(i);
				view.setBackgroundResource(R.drawable.background_small);
				TextView text = (TextView) view
						.findViewById(R.id.tabwidget_text);
				text.setTextColor(Color.GRAY);
			} else {
				View view = mTabHost.getTabWidget().getChildAt(i);
				view.setBackgroundResource(R.drawable.background_small_selected);
				TextView text = (TextView) view
						.findViewById(R.id.tabwidget_text);
				text.setTextColor(Color.WHITE);
			}
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			LOGD("mHandler,handleMessage() msg.what = " + msg.what);
			switch (msg.what) {
			case SettingMacroDefine.upDateListView:
				if (mListViewAdapter != null) {
					mListViewAdapter.invalidate();
				}
				break;
			case SettingMacroDefine.upKeyBoardSetting:
				updateSettingItem(msg.arg1, (String) msg.obj, null, null);
				mListViewAdapter.invalidate();
				break;
			case SettingMacroDefine.setSettingItemClickable:
				{
					int id = (int)msg.arg1;
					boolean canclick = ((int)msg.arg2 == 1);
					setSettingItemClickable(id,canclick);

					if (mListViewAdapter != null) 
					{
						mListViewAdapter.invalidate();
					}
					break;
				}
			case SettingMacroDefine.setSettingItemView:
				{
					int id = (int)msg.arg1;
					View view = (View)msg.obj;
					setSettingItemView(id,view);

					if (mListViewAdapter != null) 
					{
						mListViewAdapter.invalidate();
					}
				}
				break;
		//	case SettingMacroDefine.upSettingItemStatus:
		//		updateSettingItem();
		//		break;
			case SettingMacroDefine.setSettingItemFucntion:
				setSettingItemCallBackFunction(msg.arg1,(SettingItemClick)msg.obj);
				break;
				
			//刷新备份状态	
			case SettingMacroDefine.upBackDataSetting:
				mBackDataSetting.getDefaultBackDataSetting();
				mAutoRestoreSetting.getDefaultAutoRestoreSetting();
				mConfigureAccountSetting.getDefaultConfigureAccountSetting();
				mListViewAdapter.invalidate();
				break;

			case SettingMacroDefine.upCredentialStatusSetting:
				mCredentialsResetSetting.getDefaultCredentialsResetSetting();
				mListViewAdapter.invalidate();
				break;
			}
		}
	};

	private void updateShowView(int position) {
		if ((position < 0) || (position >= mTextId.length)
				|| (position >= mBitmap.length))
			return;

		String text = this.getResources().getString(mTextId[position]);
		mTextView.setText(text);
		Bitmap map = bitMapScale(mBitmap[position],ScreenInformation.mDpiRatio);
		mImageView.setImageBitmap(map);
	}

	private void moveRight() {
		int position = mTabHost.getCurrentTab();
		// 右移widget的焦点
		position++;
		LOGD("moveRight(), current position = " + position);
		int total = mTextId.length;
		if (position >= total)
			position = 0;

		mTabHost.setCurrentTab(position);
		// 更新Adapter
		mListViewAdapter.setSelection(position);
		// 设置焦点
		mListView.requestFocus();
		mListView.setSelection(0);

		updateShowView(position);
	}

	private void moveLeft() {
		int position = mTabHost.getCurrentTab();
		// 左移widget的焦点
		position--;
		LOGD("moveLeft(), current position = " + position);
		int total = mTextId.length;
		// 如果到达最左端，设置焦点为最后一个
		if (position < 0)
			position = total - 1;
		// 设置TabHost的选中项
		mTabHost.setCurrentTab(position);
		// 更新Adapter
		mListViewAdapter.setSelection(position);
		// 设置焦点
		mListView.requestFocus();
		mListView.setSelection(0);

		updateShowView(position);
	}

	public SettingItem findSettingItem(String content,int id)
	{
		if(mMap == null)
			return null;
		
		ArrayList<SettingItem> list = (ArrayList<SettingItem>)mMap.get(content);
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				SettingItem item = list.get(i);
				if(item.mId == id)
				{
					return item;
				}
			}
		}

		return null;
	}

	public void updateSettingItem(int id,String status, String summary, Bitmap draw)
	{
		SettingItem item = null;
		if(((item = findSettingItem("system",id)) != null) || ((item = findSettingItem("network",id)) != null) ||
						((item = findSettingItem("device",id)) != null) || ((item = findSettingItem("personal",id)) != null))
		{
			if(status != null)
				item.setStatus(status);
			if(summary != null)
				item.setSummary(summary);
			if(draw != null)
				item.setDrawable(draw);
		}
	}

	public void  updateSettingItem(int id,int statusId, int summaryId,int bitmapId)
	{
		String status = null;
		String summary = null;
		Bitmap drawable = null;
		if(statusId != -1)
			status = this.getResources().getString(statusId);
		if(summaryId != -1)
			summary = this.getResources().getString(summaryId);
		if(bitmapId != -1)
			drawable = BitmapFactory.decodeResource(this.getResources(),bitmapId);;//this.getResources().getDrawable(bitmapId);

		updateSettingItem(id,status,summary,drawable);
	}

	public void setSettingItemView(int id,View view)
	{
		if(mMap != null)
		{
			SettingItem item = null;
			if(((item = findSettingItem("system",id)) != null) || ((item = findSettingItem("network",id)) != null) ||
							((item = findSettingItem("device",id)) != null) || ((item = findSettingItem("personal",id)) != null))
			{
				item.setView(view);
			}
			
		}
	}
	
	public boolean setSettingItemClickable(int id,boolean click)
	{
		if(mMap != null)
		{
			SettingItem item = null;
			if(((item = findSettingItem("system",id)) != null) || ((item = findSettingItem("network",id)) != null) ||
							((item = findSettingItem("device",id)) != null) || ((item = findSettingItem("personal",id)) != null))
			{
				item.setClickable(click);
				return true;
			}
			
		}
		return false;
	}

	

	public void setSettingItemCallBackFunction(int id, SettingItemClick function)
	{
		if(mMap != null)
		{
			SettingItem item = null;
			if(((item = findSettingItem("system",id)) != null) || ((item = findSettingItem("network",id)) != null) ||
							((item = findSettingItem("device",id)) != null) || ((item = findSettingItem("personal",id)) != null))
			{
				LOGD("setSettingItemCallBackFunction(), setFunction, id = "+id);
				item.setOnSettingItemClick(function);
			}
			
		}
	}
	// 某些目录在退出时，需要做某些处理	
	private void exitFromParent(int id)	
	{		
		if(id == R.string.ethernet_static_ip_setting)		
		{			
			if(mEthernet != null)			
			{				
				mEthernet.saveStaticIP();			
			}		
		}	
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();

		LOGD("dispatchKeyEvent(),keyCode = " + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				mListViewAdapter.setLevel(0);
				mListViewAdapter.setParentId(-1);
				moveRight();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				mListViewAdapter.setLevel(0);
				mListViewAdapter.setParentId(-1);
				moveLeft();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				int level = mListViewAdapter.getLevel();
				if (level > 0) {
					level--;
					mListViewAdapter.setLevel(level);
					String name = mListViewAdapter.getContent();
					int parent = mListViewAdapter.getParentId();
					exitFromParent(parent);
					SettingItem item = findParent(name, parent);
					LOGD("dispatchKey,Key_Back, name = " + name
							+ ",parent id = " + parent);
					if (item == null) {
						mListViewAdapter.setParentId(-1);
						// mListViewAdapter.setLevel(0);
					} else {
						mListViewAdapter.setParentId(item.mParentId);
					}
					mListViewAdapter.invalidate();

					return true;
				}
			}
			break;
		}

		return super.dispatchKeyEvent(event);
	}

	public void onSaveInstanceState(Bundle savedState) 
	{
		if(mListViewAdapter != null)
		{
			LOGD("onSaveInstanceState(),Level = "+mListViewAdapter.getLevel()+", Indicator = "+mListViewAdapter.getSelection()
				+",ParentId = "+mListViewAdapter.getParentId());
			savedState.putInt("Level", mListViewAdapter.getLevel());
	        savedState.putInt("Indicator", mListViewAdapter.getSelection());
			savedState.putInt("Parent", mListViewAdapter.getParentId());
		}
		if(!getPackageManager().hasSystemFeature("android.settings.vpn"))
		{
			if(mVpn != null)
				mVpn.onSaveInstanceState(savedState);
		}
	}
	public void onResume() {
		Log.d(TAG, "OnResume:==================");
		super.onResume();
	}

	public void onPause() {
		super.onPause();
		
		if (mSoundSettings != null){
			mSoundSettings.Pause();
		}
	}


	public void onDestroy()
	{
		super.onDestroy();
		if (mDateAndTime != null)
		    mDateAndTime.pause();

		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_ETHERNET))
		{
			if(mEthernet != null)
				mEthernet.Pause();
		}
		
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
		{
			if(mBluetooth != null)
				mBluetooth.Pause();
		}

		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_PPPOE))
		{
			if(mPPPOE != null)
				mPPPOE.Pause();
		}

		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
		{
		if(mWifi_Enabler != null)
			mWifi_Enabler.pause();

		if(!getPackageManager().hasSystemFeature("android.setting.portable_hotspot"))
		{
			if(mWifi_ApEnabler != null)
				mWifi_ApEnabler.pause();
			
			if(mWifi_ApSettings != null)
				mWifi_ApSettings.onPause();
		}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
		LOGD("onActivityResult requestcode:"+requestCode);
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case RingtoneSettings.REQUEST_CODE_RINGTONE_PICKER:
			if (mSoundSettings != null){
				mSoundSettings.onActivityResult(requestCode, resultCode, data);
				mSoundSettings.Resume();
			}
			break;
			
		case UsbMode.REQUEST_CODE_USB_CONNECT_MODE:
			String usbmode = data.getStringExtra(UsbMode.USB_MODE);
			LOGD("usbmode:"+usbmode);
			if (mUsbMode != null){
				mUsbMode.updateMode(usbmode);
			}
			break;

		default:
			break;
		}
		
	}
	
	public void StartActivitySafely(Intent intent){
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e("RKsettings", "Unable to launch intent=" + intent, e);
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e("RKsettings",
							"Launcher does not have the permission to launch "
									+ intent
									+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
									+ "or use the exported attribute for this activity. "
									+ " intent=" + intent, e);
		}
	}
	
	
	public void StartActivityForResultSafely(Intent intent , int requestCode){
		try {
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e("RKsettings", "Unable to launch intent=" + intent, e);
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e("RKsettings",
							"Launcher does not have the permission to launch "
									+ intent
									+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
									+ "or use the exported attribute for this activity. "
									+ " intent=" + intent, e);
		}
	}
	/**
     * Unregister a receiver, but eat the exception that is thrown if the
     * receiver was never registered to begin with. This is a little easier
     * than keeping track of whether the receivers have actually been
     * registered by the time onDestroy() is called.
     */
    public void unregisterReceiverSafe(BroadcastReceiver receiver) {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }
	
	
}
