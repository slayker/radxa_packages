/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.rockchip.settings.wifi;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.wifi.Summary;
import com.rockchip.settings.ListViewAdapter;
import com.rockchip.settings.SettingMacroDefine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;
import android.os.Message;

import android.util.Log;
import android.os.Handler;

//import com.android.settings.wifi.Summary;
//import com.android.settings.rockchip.*;
public class Wifi_Enabler  {
    private final Context mContext; 
    private final ListViewAdapter mListViewAdapter;
//    private String mCurrentState = "close";
    private final WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;
    private final Handler mHandler;
	// 用于表明wifi是否已打开 ,true:wifi打开， false:wifi没有打开
	private boolean mOpen = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
			LOGD("BroadcastReceiver onReceive(), action = "+action);
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) 
			{
                handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } 
			else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) 
            {
                handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            } 
			else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) 
            {
                handleStateChanged(((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
            }
        }
	
    };
    private void LOGD(String msg)
	{
		if(true)
			Log.d("Wifi Enabler",msg);
	}
	
    public Wifi_Enabler(Context context, ListViewAdapter listview,Handler handler) {
        mContext = context;
        mListViewAdapter = listview;
		mHandler = handler;
//		mCurrentState = null;
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

	public void getWifiDefault()
	{
		boolean bool = (Settings.Secure.getInt(mContext.getContentResolver(), 
						Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
		int hint = (bool?R.string.open:R.string.off);
		((RKSettings)mContext).updateSettingItem(R.string.wifi_notify_open_networks,hint,-1,-1);
		if(mOpen)
		{
			((RKSettings)mContext).updateSettingItem(R.string.wifi_settings_title,R.string.turn_on,-1,-1);
			((RKSettings)mContext).setSettingItemClickable(R.string.wifi_notify_open_networks, true);
			((RKSettings)mContext).setSettingItemClickable(R.string.ap_list, true);
		}
		else
		{
			((RKSettings)mContext).updateSettingItem(R.string.wifi_settings_title,R.string.turn_off,-1,-1);
			((RKSettings)mContext).setSettingItemClickable(R.string.wifi_notify_open_networks, false);
			((RKSettings)mContext).setSettingItemClickable(R.string.ap_list, false);
		}
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);	
	}

	private void upDateWifiStatus(String title)
	{
		boolean bool = (Settings.Secure.getInt(mContext.getContentResolver(), 
						Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
		int hint = (bool?R.string.open:R.string.off);
		((RKSettings)mContext).updateSettingItem(R.string.wifi_notify_open_networks,hint,-1,-1);

		((RKSettings)mContext).updateSettingItem(R.string.wifi_settings_title,title,null,null);
		
		if(mOpen)
		{
			((RKSettings)mContext).setSettingItemClickable(R.string.wifi_notify_open_networks, true);
			((RKSettings)mContext).setSettingItemClickable(R.string.ap_list, true);
		}
		else
		{
			((RKSettings)mContext).setSettingItemClickable(R.string.wifi_notify_open_networks, false);
			((RKSettings)mContext).setSettingItemClickable(R.string.ap_list, false);
		}
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);	
	}
	
    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }
    
    public void pause() {
        mContext.unregisterReceiver(mReceiver);
    }

	public void onWiFiClick()
	{
		boolean enable = !mOpen;
		int wifiApState = mWifiManager.getWifiApState();
        if (enable && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) 
        {
            mWifiManager.setWifiApEnabled(null, false);
        }

		if(mWifiManager.setWifiEnabled(enable))
		{
			mOpen = enable;
			String open = mContext.getResources().getString(R.string.turn_on);
			upDateWifiStatus(open);
		}
		else
		{
			((RKSettings)mContext).updateSettingItem(R.string.wifi_settings_title,R.string.wifi_error,-1,-1);
			((RKSettings)mContext).setSettingItemClickable(R.string.wifi_notify_open_networks, false);
			((RKSettings)mContext).setSettingItemClickable(R.string.wifi_settings, false);
			mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);	
		}
	}

	public void onNetWorkNotificaiton()
	{
		boolean bool = (Settings.Secure.getInt(mContext.getContentResolver(), 
						Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
		bool = !bool;
		Settings.Secure.putInt(mContext.getContentResolver(), 
					Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
					bool ? 1 : 0);
		int hint = (bool?R.string.open:R.string.off);
		((RKSettings)mContext).updateSettingItem(R.string.wifi_notify_open_networks,hint,-1,-1);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);	
	}

    private void handleWifiStateChanged(int state) {
		LOGD("handleWifiStateChanged(),state = "+state);
		int hint = 0;
		switch (state) 
		{
			case WifiManager.WIFI_STATE_ENABLING:
				mOpen = false;
				hint = R.string.wifi_starting;
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				mOpen = true;
				hint = R.string.turn_on;
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				mOpen = false;
				hint = R.string.wifi_stopping;
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				mOpen = false;
				hint = R.string.turn_off;
				break;
			default:
				mOpen = false;
				hint = R.string.wifi_error;
				break;
        }
		upDateWifiStatus(mContext.getResources().getString(hint));
    }

    private void handleStateChanged(NetworkInfo.DetailedState state) {
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the check box as an optimization.
        String text = null;
        if (state != null ) 
		{
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) 
			{
				if(info.getSSID() != null)
		   			text = Summary.get(mContext, info.getSSID(),state);
				else
					text = mContext.getString(R.string.turn_on);
		   		
				Log.d("Wifi_Enabler","info.getSSID() = "+info.getSSID()+",text = "+text);
//				mOpen = true;
				upDateWifiStatus(text);
				return ;
            }
        }
    }
   
	public void get_wifisetting()
	{

		//Intent intent = new Intent(mContext, Wifi_setting.class); 		
			
		//mContext.startActivity(intent);
	}
}

