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

package com.android.settings.ethernet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.net.EthernetDataTracker;
import android.net.NetworkUtils;

import com.android.settings.R;
import android.util.Log;
import android.net.ethernet.EthernetManager;

/**
 * EthernetEnabler is a helper to manage the Ethernet on/off checkbox
 * preference. It turns on/off Ethernet and ensures the summary of the
 * preference reflects the current state.
 */
public final class EthernetEnabler implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "EthernetEnabler";
    private final Context mContext;
    private Switch mSwitch;
    private final IntentFilter mIntentFilter;
	private EthernetManager mEthManager;
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(EthernetDataTracker.ETHERNET_IFACE_STATE_CHANGED_ACTION)) {
				int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_IFACE_STATE, 0);
				Log.d(TAG, "BroadcastReceiver: Ethernet interface current state:" + state);
				handleStateChanged(state);
			}
		}
    };

    public EthernetEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        mEthManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);
		if (mEthManager == null) Log.e(TAG, "get ethernet manager failed");
        mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_IFACE_STATE_CHANGED_ACTION);
    }

    public void resume() {
        if (mEthManager == null) {
            mSwitch.setEnabled(false);
            return;
        }

        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);			
        handleStateChanged(mEthManager.getEthernetIfaceState());
    }

    public void pause() {
        if (mEthManager == null) {
            return;
        }

        mContext.unregisterReceiver(mReceiver);
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
		int state = EthernetDataTracker.ETHER_IFACE_STATE_DOWN;
		if (mEthManager != null) {
			state = mEthManager.getEthernetIfaceState();
		}
        boolean isOn = state == EthernetDataTracker.ETHER_IFACE_STATE_UP;
        boolean isOff = state == EthernetDataTracker.ETHER_IFACE_STATE_DOWN;
        mSwitch.setChecked(isOn);
        mSwitch.setEnabled(isOn || isOff);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {		
		Log.d(TAG, "onCheckedChanged: isChecked:"+isChecked);

		if (mEthManager != null) {
			mEthManager.setEthernetEnabled(isChecked);
		}
        mSwitch.setEnabled(false);
    }

    void handleStateChanged(int state) {	
		Log.d(TAG, "handleStateChanged: state:"+state);
        switch (state) {
            case EthernetDataTracker.ETHER_IFACE_STATE_UP:
                mSwitch.setChecked(true);
                mSwitch.setEnabled(true);
                break;
            case EthernetDataTracker.ETHER_IFACE_STATE_DOWN:
                mSwitch.setChecked(false);
                mSwitch.setEnabled(true);
                break;
            default:
                mSwitch.setChecked(false);
                mSwitch.setEnabled(true);
        }      
    }
}

