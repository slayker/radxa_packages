package com.rockchip.settings.ethernet;

import com.rockchip.settings.RKSettings;
import android.net.ethernet.EthernetManager;
import android.net.EthernetDataTracker;
import android.net.NetworkUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.R;
import android.util.Log;


public class EthernetEnabler
{
	private Context mContext = null;
	private Handler mHandler = null;
	private EthernetManager mEthManager = null;
	private IntentFilter mIntentFilter = null;

	private boolean mConnect = false;
	
	public EthernetEnabler(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
        mEthManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
		if(mHandler != null)
		{
			mConnect = (mEthManager.getEthernetConnectState() == EthernetDataTracker.ETHER_STATE_CONNECTED);
			if(mConnect)
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_settings_title,R.string.connected,-1,-1);
			else
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_settings_title,R.string.disconncect,-1,-1);
		}
//		mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
//		mIntentFilter.addAction(EthernetDataTracker.ETHERNET_IFACE_STATE_CHANGED_ACTION);
		  mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_IFACE_STATE_CHANGED_ACTION);
	}

	public void Resume()
	{
		if (mEthManager != null)
		{
			mContext.registerReceiver(mEthernetEnablerReceiver, mIntentFilter);
		}
	}

	public void Pause()
	{
		if(mIntentFilter != null)
		{
			mContext.unregisterReceiver(mEthernetEnablerReceiver);
		}
	}

	public void onClick()
	{
		if (mEthManager != null) 
		{
			mConnect = (mEthManager.getEthernetConnectState() == EthernetDataTracker.ETHER_STATE_CONNECTED);
			mConnect = !mConnect;
			mEthManager.setEthernetEnabled(mConnect); 
		}
	}
	

	void handleStateChanged(int state) 
	{
		LOG("handleStateChanged: state = "+state);
		switch (state) 
		{
			case EthernetDataTracker.ETHER_IFACE_STATE_UP:
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_settings_title,R.string.turn_off,-1,-1);

		                break;
		            case EthernetDataTracker.ETHER_IFACE_STATE_DOWN:
		              ((RKSettings)mContext).updateSettingItem(R.string.ethernet_settings_title,R.string.turn_on,-1,-1);
				break;
				
		}      
		
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}

	private final BroadcastReceiver mEthernetEnablerReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
	/*		if (action.equals(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION)) 
			{
				int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_STATE, 0);
				LOG("BroadcastReceiver: Ethernet current state:" + state);
				handleStateChanged(state);
			}
			else */
			if(action.equals(EthernetDataTracker.ETHERNET_IFACE_STATE_CHANGED_ACTION)) 
			{
				int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_IFACE_STATE, 0);
				LOG("BroadcastReceiver: Ethernet interface current state:" + state);
				handleStateChanged(state);
			}
		}
	};

	private void LOG(String msg)
	{
		if(true)
			Log.d("EthernetEnabler",msg);
	}
}
