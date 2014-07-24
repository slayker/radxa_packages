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

package com.rk.setting.wifi;

import com.rk.setting.R;

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
import com.rk.setting.CallBackListenner;
import android.util.Log;
import android.os.Handler;

public class Wifi_Enabler  {
    private final Context mContext; 
    private final WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;
	private CallBackListenner mCallBack;
	
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


	public void setCallBack(CallBackListenner callback)
	{
		mCallBack = callback;
	}
	
    public Wifi_Enabler(Context context) {
        mContext = context;
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

	public boolean getWifiStatus()
	{
		Log.d("wifi Enabler","mOpen = "+mOpen);
		return mOpen;
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
		}
	}

    private void handleWifiStateChanged(int state) {
		LOGD("handleWifiStateChanged(),state = "+state);
		switch (state) 
		{
			case WifiManager.WIFI_STATE_ENABLING:
				mOpen = false;
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				mOpen = true;
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				mOpen = false;
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				mOpen = false;
				break;
			default:
				mOpen = false;
				break;
        }
		if(mCallBack != null)
		{
			mCallBack.onCallBack();
		}
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
		 //  		text = Summary.get(mContext, info.getSSID(),state);
		//		mOpen = true;
		/* 		if(mCallBack != null)
				{
					mCallBack.onCallBack();
				}*/
            }
        }
    }
}

