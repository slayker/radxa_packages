package com.rockchip.settings.ethernet;

import com.rockchip.settings.RKSettings;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.Context;

import android.net.ethernet.EthernetManager;
import com.rockchip.settings.R;
import android.net.EthernetDataTracker;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.SystemProperties;
import com.rockchip.settings.SettingMacroDefine;

import android.content.pm.ResolveInfo;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.System;

import android.util.Log;

public class EthernetSettings
{
	private Context mContext = null;
	private Handler mHandler = null;

	EthernetManager mEthManager;
	EthernetStaticIPSettings mEthStatic;
	
	private  static String mEthMacAddress = null;
   	private  static String mEthIpAddress = null;
	private  static String mEthNetmask = null;
	private  static String mEthGateway = null;
	private  static String mEthdns1 = null;
	private  static String mEthdns2 = null;
	private final static String nullIpInfo = "0.0.0.0";

	private IntentFilter mIntentFilter;
	private EthernetEnabler mEthernetEnabler = null;
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
	
	public EthernetSettings(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;

		mEthManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
		mEthernetEnabler = new EthernetEnabler(mContext,mHandler);
		mEthStatic = new EthernetStaticIPSettings(context,handler,mEthManager);
		if (mEthManager == null) {
			LOG("get ethernet manager failed");
			return;
		}
		
		getEthInfo(mEthManager.getEthernetConnectState());
		mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
	}
	
	// 打开或者关闭以太网
	public void onClick(int id)
	{
		switch(id)
		{
			case R.string.ethernet_settings_title:
				if(mEthernetEnabler != null)
				{
					mEthernetEnabler.onClick();
				}
				break;
			case R.string.ethernet_use_static_ip:
				ContentResolver contentResolver = mContext.getContentResolver();
				int useStaticIp = System.getInt(contentResolver, System.ETHERNET_USE_STATIC_IP, 0);
				Log.d("EthernetSetting","onClick(), useStaticIp = "+useStaticIp);
				if (useStaticIp == 1) 
				{
					System.putInt(contentResolver, System.ETHERNET_USE_STATIC_IP,0);
					((RKSettings)mContext).updateSettingItem(R.string.ethernet_use_static_ip,R.string.turn_off,-1,-1);
					((RKSettings)mContext).setSettingItemClickable(R.string.ethernet_static_ip_setting,false);
				}
				else
				{
					System.putInt(contentResolver, System.ETHERNET_USE_STATIC_IP,1);
					((RKSettings)mContext).updateSettingItem(R.string.ethernet_use_static_ip,R.string.turn_on,-1,-1);
					((RKSettings)mContext).setSettingItemClickable(R.string.ethernet_static_ip_setting,true);
				}
				int preState = mEthManager.getEthernetIfaceState();
                                mEthManager.setEthernetEnabled(false);
				if (preState == EthernetDataTracker.ETHER_IFACE_STATE_UP) {
                                    mEthManager.setEthernetEnabled(true);
                                }
				getEthInfo(mEthManager.getEthernetConnectState());
	//			mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
				break;
				
			case R.string.ethernet_static_ip:
			case R.string.ethernet_static_gateway:
			case R.string.ethernet_static_netmask:
			case R.string.ethernet_static_dns1:
			case R.string.ethernet_static_dns2:
				if(mEthStatic != null)
					mEthStatic.onClick(id);
				break;
		}
	}

	public void saveStaticIP()
	{
		if(mEthStatic != null)
		{
			if(mEthStatic.saveIP())
			{
				getEthInfo(mEthManager.getEthernetConnectState());
			}
		}
	}
	
	
	// 获取保存到数据库中的静态IP
	public void getEthInfoFromCr() 
	{

        ContentResolver contentResolver = mContext.getContentResolver();

        mEthIpAddress = System.getString(contentResolver, System.ETHERNET_STATIC_IP);
        mEthNetmask   = System.getString(contentResolver, System.ETHERNET_STATIC_NETMASK);
        mEthGateway   = System.getString(contentResolver, System.ETHERNET_STATIC_GATEWAY);
 //       mEthdns1      = System.getString(contentResolver, System.ETHERNET_STATIC_DNS1);
  //      mEthdns2      = System.getString(contentResolver, System.ETHERNET_STATIC_DNS2);
  		mEthdns1      = SystemProperties.get("net.dns1");
        mEthdns2      = SystemProperties.get("net.dns2");
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
			mEthIpAddress = nullIpInfo;
			mEthNetmask = nullIpInfo;
			mEthGateway = nullIpInfo;        		
			mEthdns1 = nullIpInfo;      		
			mEthdns2 = nullIpInfo;       		
			((RKSettings)mContext).updateSettingItem(R.string.ethernet_settings_title,R.string.disconncect,-1,-1);
		} 
		else 
		{
			((RKSettings)mContext).updateSettingItem(R.string.ethernet_settings_title,R.string.connected,-1,-1);
			if (useStaticIp == 1) 
			{
				getEthInfoFromCr();
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_use_static_ip,R.string.turn_on,-1,-1);
				((RKSettings)mContext).setSettingItemClickable(R.string.ethernet_static_ip_setting,true);
			} 
			else 
			{
				getEthInfoFromDhcp();
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_use_static_ip,R.string.turn_off,-1,-1);
				((RKSettings)mContext).setSettingItemClickable(R.string.ethernet_static_ip_setting,false);
			}
		}

		if (useStaticIp == 1) 
		{
			((RKSettings)mContext).updateSettingItem(R.string.ethernet_use_static_ip,R.string.turn_on,-1,-1);
			((RKSettings)mContext).setSettingItemClickable(R.string.ethernet_static_ip_setting,true);
		} 
		else 
		{
			((RKSettings)mContext).updateSettingItem(R.string.ethernet_use_static_ip,R.string.turn_off,-1,-1);
			((RKSettings)mContext).setSettingItemClickable(R.string.ethernet_static_ip_setting,false);
		}
		
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_mac_addr,mEthMacAddress,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_ip_addr,mEthIpAddress,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_netmask,mEthNetmask,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_gateway,mEthGateway,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_dns1,mEthdns1,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_dns2,mEthdns2,null,null);
		
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}
}
