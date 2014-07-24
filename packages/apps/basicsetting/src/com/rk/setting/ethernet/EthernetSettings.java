package com.rk.setting.ethernet;

import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.Context;

import android.net.ethernet.EthernetManager;
import android.net.EthernetDataTracker;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.SystemProperties;
import com.rk.setting.CallBackListenner;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.System;
import com.rk.setting.R;
import android.util.Log;

public class EthernetSettings
{
	private Context mContext = null;

	EthernetManager mEthManager;
	
	private String mEthStatus = null;
	private String mEthMacAddress = null;
   	private  String mEthIpAddress = null;
	private  String mEthNetmask = null;
	private  String mEthGateway = null;
	private  String mEthdns1 = null;
	private  String mEthdns2 = null;
	private  String nullIpInfo = "0.0.0.0";

	private IntentFilter mIntentFilter;
	private EthernetEnabler mEthernetEnabler = null;

	private CallBackListenner mCallBack;
	private void LOG(String msg)
	{
		if(true)
			Log.d("EthernetSettings",msg);
	}
	
    private final BroadcastReceiver mEthernetSettingsReceiver = new BroadcastReceiver() 
	{
        public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (action.equals(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION)) 
			{
				int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_STATE, 0);
				LOG("BroadcastReceiver: Ethernet current state:" + state);
				getEthInfo(state);
			}
        }
    };

	public void Resume()
	{
		mContext.registerReceiver(mEthernetSettingsReceiver, mIntentFilter);
		if(mEthernetEnabler != null)
			mEthernetEnabler.Resume();
		getEthInfo(mEthManager.getEthernetConnectState());
	}

	public void Pause()
	{
		mContext.unregisterReceiver(mEthernetSettingsReceiver);
		if(mEthernetEnabler != null)
			mEthernetEnabler.Pause();
	}
	
	public EthernetSettings(Context context)
	{
		mContext = context;

		mEthManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
		mEthernetEnabler = new EthernetEnabler(mContext);
		if (mEthManager == null) {
			LOG("get ethernet manager failed");
			return;
		}
		nullIpInfo = mContext.getResources().getString(R.string.status_unavailable);
		mEthIpAddress = nullIpInfo;
		mEthNetmask = nullIpInfo;
		mEthGateway = nullIpInfo;
		mEthdns1 = nullIpInfo;
		mEthdns2 = nullIpInfo;
		getEthInfo(mEthManager.getEthernetConnectState());
		mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
	}
/*
	// 打开或者关闭以太网
	public void onClick()
	{
		if(mEthernetEnabler != null)
		{
			mEthernetEnabler.onClick();
		}
	}*/
	
	// 获取保存到数据库中的静态IP
	public void getEthInfoFromCr() 
	{

        ContentResolver contentResolver = mContext.getContentResolver();

        mEthIpAddress = System.getString(contentResolver, System.ETHERNET_STATIC_IP);
        mEthNetmask   = System.getString(contentResolver, System.ETHERNET_STATIC_NETMASK);
        mEthGateway   = System.getString(contentResolver, System.ETHERNET_STATIC_GATEWAY);
        mEthdns1      = System.getString(contentResolver, System.ETHERNET_STATIC_DNS1);
        mEthdns2      = System.getString(contentResolver, System.ETHERNET_STATIC_DNS2);
    }

	// 动态获取网络信息
	public void getEthInfoFromDhcp()
	{	
		if(mEthManager == null)
			return ;
		
		String tempIpInfo;
		String iface = mEthManager.getEthernetIfaceName();

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".ipaddress");
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) )
		{ 
			mEthIpAddress = tempIpInfo;
		} 
		else 
		{  
			mEthIpAddress = nullIpInfo;
		}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".mask");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) )
		{
			mEthNetmask = tempIpInfo;
		} 
		else 
		{           		
			mEthNetmask = nullIpInfo;
		}
			
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".gateway");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")))
		{
			mEthGateway = tempIpInfo;
		} 
		else
		{
			mEthGateway = nullIpInfo;        		
		}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns1");
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")))
		{
			mEthdns1 = tempIpInfo;
		} 
		else 
		{
			mEthdns1 = nullIpInfo;      		
		}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns2");
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")))
		{
			mEthdns2 = tempIpInfo;
		} 
		else 
		{
			mEthdns2 = nullIpInfo;       		
		}
	}

	public void getEthInfo(int state)
	{	
		LOG("getEthInfo(), state = "+state);

		mEthMacAddress = mEthManager.getEthernetHwaddr(mEthManager.getEthernetIfaceName());
        if (mEthMacAddress == null) 
			mEthMacAddress = nullIpInfo;

		ContentResolver contentResolver = mContext.getContentResolver();
		int	useStaticIp = System.getInt(contentResolver, System.ETHERNET_USE_STATIC_IP, 0);
		
		if (state == EthernetDataTracker.ETHER_STATE_DISCONNECTED) 
		{
			mEthStatus = mContext.getString(R.string.disconnected);
			mEthIpAddress = nullIpInfo;
			mEthNetmask = nullIpInfo;
			mEthGateway = nullIpInfo;        		
			mEthdns1 = nullIpInfo;      		
			mEthdns2 = nullIpInfo;       		
		} 
		else 
		{
			mEthStatus = mContext.getString(R.string.connected);
			if (useStaticIp == 1) 
			{
				getEthInfoFromCr();
			} 
			else 
			{
				getEthInfoFromDhcp();
			}
		}

		if (useStaticIp == 1) 
		{
			mEthStatus += mContext.getString(R.string.using_static_ip);
		} 
		
		if(mCallBack != null)
			mCallBack.onCallBack();
 	}

	public String getEthernetStatus()
	{
		return mEthStatus;
	}

	public String getMacAddress()
	{	
		return mEthMacAddress;
	}

	public String getIPAddress()
	{
		LOG("mEthIpAddress = "+mEthIpAddress);
		return mEthIpAddress;
	}

	public String getNetMask()
	{
		LOG("mEthNetmask = "+mEthNetmask);
		return mEthNetmask;
	}

	public String getGateWay()
	{
		LOG("mEthGateway = "+mEthGateway);
		return mEthGateway;
	}
	
	public String getDNS1()
	{
		LOG("mEthdns1 = "+mEthdns1);
		return mEthdns1;
	}

	public String getDNS2()
	{
		LOG("mEthdns2 = "+mEthdns2);
		return mEthdns2;
	}
	
	public void setCallBack(CallBackListenner callback)
	{
		mCallBack = callback;
		if(mEthernetEnabler != null)
			mEthernetEnabler.setCallBack(callback);
	}
}
