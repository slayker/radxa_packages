/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.ethernet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.ethernet.EthernetManager;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import android.net.EthernetDataTracker;

public class EthernetSettings extends SettingsPreferenceFragment {
    private static final String TAG = "EthernetSettings";

    private static final String KEY_ETH_IP_ADDRESS = "ethernet_ip_addr";
    private static final String KEY_ETH_HW_ADDRESS = "ethernet_hw_addr";
    private static final String KEY_ETH_NET_MASK = "ethernet_netmask";
    private static final String KEY_ETH_GATEWAY = "ethernet_gateway";
    private static final String KEY_ETH_DNS1 = "ethernet_dns1";
    private static final String KEY_ETH_DNS2 = "ethernet_dns2";

	EthernetManager mEthManager;
//$_rbox_$_modify_$_lijiehong: add to support checkbox
	private CheckBoxPreference mEthCheckBox;
//$_rbox_$_modify_$ end

        private  static String mEthHwAddress = null;
   	private  static String mEthIpAddress = null;
	private  static String mEthNetmask = null;
	private  static String mEthGateway = null;
	private  static String mEthdns1 = null;
	private  static String mEthdns2 = null;
	private final static String nullIpInfo = "0.0.0.0";
	
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_STATE, 0);
		Log.d(TAG, "BroadcastReceiver: Ethernet current state:" + state);
		getEthInfo(state);
	    }
//$_rbox_$_modify_$_lijiehong: add to support checkbox
            updateCheckbox();//add by ljh for adding a checkbox switch
//$_rbox_$_modify_$ end
        }
    };

//$_rbox_$_modify_$_lijiehong: add to support checkbox
    private void updateCheckbox(){//add by ljh for adding a checkbox switch
        if (mEthCheckBox== null) {
            mEthCheckBox = (CheckBoxPreference)findPreference("ethernet");
        }
        if(mEthManager==null){
            mEthCheckBox.setChecked(false);
        }else{
            mEthCheckBox.setChecked(mEthManager.getEthernetIfaceState()==EthernetDataTracker.ETHER_IFACE_STATE_UP);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {//add by ljh for adding a checkbox switch
        if(preference == mEthCheckBox){//sync wifi switch ,add by ljh
            mEthManager.setEthernetEnabled(mEthCheckBox.isChecked());
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }
//$_rbox_$_modify_$end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ethernet_settings);
		
		mEthManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
		if (mEthManager == null) {
			Log.e(TAG, "get ethernet manager failed");
			return;
		}
		
		getEthInfo(mEthManager.getEthernetConnectState());
		mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(EthernetDataTracker.ETHERNET_IFACE_STATE_CHANGED_ACTION);
	}
	
    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(mReceiver, mIntentFilter);
		if (mEthManager == null) return;

		getEthInfo(mEthManager.getEthernetConnectState());
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary("");
        }
    }

    public void getEthInfoFromCr() {

        ContentResolver contentResolver = getContentResolver();

        mEthIpAddress = System.getString(contentResolver, System.ETHERNET_STATIC_IP);
        mEthNetmask   = System.getString(contentResolver, System.ETHERNET_STATIC_NETMASK);
        mEthGateway   = System.getString(contentResolver, System.ETHERNET_STATIC_GATEWAY);
//        mEthdns1      = System.getString(contentResolver, System.ETHERNET_STATIC_DNS1);
//        mEthdns2      = System.getString(contentResolver, System.ETHERNET_STATIC_DNS2);
        mEthdns1      = SystemProperties.get("net.dns1");
        mEthdns2      = SystemProperties.get("net.dns2");
    }

	public void getEthInfoFromDhcp(){	
		String tempIpInfo;
		String iface = mEthManager.getEthernetIfaceName();
		
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".ipaddress");
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) ){ 
			mEthIpAddress = tempIpInfo;
    	} else {  
    		mEthIpAddress = nullIpInfo;
    	}
				
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".mask");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) ){
       		mEthNetmask = tempIpInfo;
    	} else {           		
    		mEthNetmask = nullIpInfo;
    	}
					
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".gateway");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
        	mEthGateway = tempIpInfo;
    	} else {
    		mEthGateway = nullIpInfo;        		
    	}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns1");
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
       		mEthdns1 = tempIpInfo;
    	} else {
    		mEthdns1 = nullIpInfo;      		
    	}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns2");
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
       		mEthdns2 = tempIpInfo;
    	} else {
    		mEthdns2 = nullIpInfo;       		
    	}
	}

	public void getEthInfo(int state){
//$_rbox_$_modify_$_lijiehong: add to support checkbox
            updateCheckbox();//add by ljh for adding a checkbox switch
//$_rbox_$_modify_$ end
            mEthHwAddress = mEthManager.getEthernetHwaddr(mEthManager.getEthernetIfaceName());
            if (mEthHwAddress == null) mEthHwAddress = nullIpInfo;

            if (state == EthernetDataTracker.ETHER_STATE_DISCONNECTED) {
                mEthIpAddress = nullIpInfo;
                mEthNetmask = nullIpInfo;
                mEthGateway = nullIpInfo;
                mEthdns1 = nullIpInfo;
                mEthdns2 = nullIpInfo;
            } else {
                ContentResolver contentResolver = getContentResolver();
                int useStaticIp = System.getInt(contentResolver, System.ETHERNET_USE_STATIC_IP, 0);
                if (useStaticIp == 1) {
                    getEthInfoFromCr();
                } else {
                    getEthInfoFromDhcp();
                }
            }

            setStringSummary(KEY_ETH_HW_ADDRESS, mEthHwAddress);
            setStringSummary(KEY_ETH_IP_ADDRESS, mEthIpAddress);
            setStringSummary(KEY_ETH_NET_MASK, mEthNetmask);
            setStringSummary(KEY_ETH_GATEWAY, mEthGateway);
            setStringSummary(KEY_ETH_DNS1, mEthdns1);
            setStringSummary(KEY_ETH_DNS2, mEthdns2);
        }
}

