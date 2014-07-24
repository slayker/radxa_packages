package com.rk.setting.ethernet;

import android.net.ethernet.EthernetManager;
import android.net.EthernetDataTracker;
import android.net.NetworkUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import com.rk.setting.CallBackListenner;
import android.util.Log;


public class EthernetEnabler
{
	private Context mContext = null;
	private EthernetManager mEthManager = null;
	private IntentFilter mIntentFilter = null;
	private int mStatus = EthernetDataTracker.ETHER_STATE_DISCONNECTED;
	private CallBackListenner mCallBack;

	public void setCallBack(CallBackListenner callback)
	{
		mCallBack = callback;
	}

//	private boolean mConnect = false;
	
	public EthernetEnabler(Context context)
	{
		mContext = context;
        mEthManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
		if(mEthManager != null)
			mStatus = mEthManager.getEthernetConnectState();
		mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
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

	public int getEthernetStatus()
	{
		return mStatus;
	}
	
	void handleStateChanged(int state) 
	{
		mStatus = state;
		if(mCallBack != null)
			mCallBack.onCallBack();
	}

	private final BroadcastReceiver mEthernetEnablerReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (action.equals(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION)) 
			{
				int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_STATE, 0);
				LOG("BroadcastReceiver: Ethernet current state:" + state);
				handleStateChanged(state);
			}
	/*		else if(action.equals(EthernetDataTracker.ETHERNET_IFACE_STATE_CHANGED_ACTION)) 
			{
				int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_IFACE_STATE, 0);
				LOG("BroadcastReceiver: Ethernet interface current state:" + state);
				handleStateChanged(state);
			}*/
		}
	};

	private void LOG(String msg)
	{
		if(true)
			Log.d("EthernetEnabler",msg);
	}
}
