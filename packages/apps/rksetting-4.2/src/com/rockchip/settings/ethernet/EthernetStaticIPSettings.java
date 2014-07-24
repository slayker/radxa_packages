package com.rockchip.settings.ethernet;

import com.rockchip.settings.RKSettings;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.Context;
import android.net.ethernet.EthernetManager;
import com.rockchip.settings.R;
import com.rockchip.settings.SettingMacroDefine;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.net.EthernetDataTracker;
import android.util.Log;

public class EthernetStaticIPSettings
{
	private Context mContext = null;
	private Handler mHandler = null;
	private EthernetManager mEthManager; 

	private EditText mEditText;
	private int mId = 0;
	
	private String[] mSettingNames = {
        System.ETHERNET_STATIC_IP, 
        System.ETHERNET_STATIC_GATEWAY,
        System.ETHERNET_STATIC_NETMASK,
        System.ETHERNET_STATIC_DNS1, 
        System.ETHERNET_STATIC_DNS2
    };

	private  String mIpAddress = null;
	private  String mNetmask = null;
	private  String mGateway = null;
	private  String mDns1 = null;
	private  String mDns2 = null;
	
	public EthernetStaticIPSettings(Context context,Handler handler,EthernetManager manager)
	{
		mContext = context;
		mHandler = handler;
		mEthManager = manager;

		upDateStaticIP();
	}

	public void upDateStaticIP()
	{
		ContentResolver contentResolver = mContext.getContentResolver();

		mIpAddress = System.getString(contentResolver, mSettingNames[0]);
		mGateway = System.getString(contentResolver, mSettingNames[1]);
		mNetmask = System.getString(contentResolver, mSettingNames[2]);
		mDns1 = System.getString(contentResolver, mSettingNames[3]);
		mDns2 = System.getString(contentResolver, mSettingNames[4]);
		
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_ip,mIpAddress,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_gateway,mGateway,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_netmask,mNetmask,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_dns1,mDns1,null,null);
		((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_dns2,mDns2,null,null);

		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}

	public void onClick(int id)
	{
		switch(id)
		{
			case R.string.ethernet_static_ip:
				mId = R.string.ethernet_static_ip;
				createSettingEditor(id,mIpAddress);
				break;
			case R.string.ethernet_static_gateway:
				mId = R.string.ethernet_static_gateway;
				createSettingEditor(id,mGateway);
				break;
			case R.string.ethernet_static_netmask:
				mId = R.string.ethernet_static_netmask;
				createSettingEditor(id,mNetmask);
				break;
			case R.string.ethernet_static_dns1:
				mId = R.string.ethernet_static_dns1;
				createSettingEditor(id,mDns1);
				break;
			case R.string.ethernet_static_dns2:
				mId = R.string.ethernet_static_dns2;
				createSettingEditor(id,mDns2);
				break;
		}
	}


	private void createSettingEditor(int id,String msg)
	{
		mEditText = new EditText(mContext); 
		mEditText.setText(msg);
		String title = mContext.getResources().getString(id);
		AlertDialog  dialogBuilder = new AlertDialog.Builder(mContext)
			.setTitle(title)
		/*	.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
			.setView(mEditText)
			.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
			 {
				public void onClick(DialogInterface dialoginterfacd,int i)
				{
					settingIP(mId);
					dialoginterfacd.dismiss();
				}
			 })
			.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener()
   		 	{
    			public void onClick(DialogInterface dialoginterfacd,int i)
    			{
   					dialoginterfacd.dismiss();
    			}
   		 	})
			.create();
			dialogBuilder.show();
	}

	private void settingIP(int id)
	{
		switch(id)
		{
			case R.string.ethernet_static_ip:
				mIpAddress = mEditText.getText().toString();
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_ip,mIpAddress,null,null);
				break;
			case R.string.ethernet_static_netmask:
				mNetmask = mEditText.getText().toString();
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_netmask,mNetmask,null,null);
				break;
			case R.string.ethernet_static_gateway:
				mGateway = mEditText.getText().toString();
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_gateway,mGateway,null,null);
				break;
			case R.string.ethernet_static_dns1:
				mDns1 = mEditText.getText().toString();
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_dns1,mDns1,null,null);
				break;
			case R.string.ethernet_static_dns2:
				mDns2 = mEditText.getText().toString();
				((RKSettings)mContext).updateSettingItem(R.string.ethernet_static_dns2,mDns2,null,null);
				break;
		}

		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}

	 // 判断是否是合法的地址
	private boolean isValidAddress(String value) 
	{
		if(value == null)
			return false;
		
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;
        
        while (start < value.length()) {
            
            if ( -1 == end ) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
            
            numBlocks++;
            
            start = end + 1;
            end = value.indexOf('.', start);
        }
        
        return numBlocks == 4;
    }

	private boolean saveAddress(String address,String saveString,int error)
	{
		if(isValidAddress(address))
		{
			ContentResolver resolver = mContext.getContentResolver();
			System.putString(resolver, saveString, address);
		}
		else if(error != 0)
		{
			String errorString = mContext.getResources().getString(error);
			Toast.makeText(mContext,errorString,Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}


	// 判断手动设置IP是否完整
	public boolean saveIP()
	{  
		if(isEmpty(mIpAddress))
		{
			Toast.makeText(mContext,R.string.ip_address_empty,Toast.LENGTH_LONG).show();
			return false;
		}
		if(isEmpty(mNetmask))
		{
			Toast.makeText(mContext,R.string.netmask_empty,Toast.LENGTH_LONG).show();
			return false;
		}
		if(isEmpty(mGateway))
		{
			Toast.makeText(mContext,R.string.gateway_empty,Toast.LENGTH_LONG).show();
			return false;
		}
		if(isEmpty(mDns1))
		{
			Toast.makeText(mContext,R.string.dns1_empty,Toast.LENGTH_LONG).show();
			return false;
		}
/*
		if(isEmpty(mDns2))
		{
			Toast.makeText(mContext,R.string.dns2_empty,Toast.LENGTH_LONG).show();
			return false;
		}*/
			
		boolean result = false;

		if(saveAddress(mIpAddress,System.ETHERNET_STATIC_IP,R.string.ip_address_error) 
				&& saveAddress(mNetmask,System.ETHERNET_STATIC_NETMASK,R.string.netmask_error)
				&& saveAddress(mGateway,System.ETHERNET_STATIC_GATEWAY,R.string.gateway_error) 
				&& saveAddress(mDns1,System.ETHERNET_STATIC_DNS1,R.string.dns1_error)
				&& saveAddress(mDns2,System.ETHERNET_STATIC_DNS2,0))
		{
			String hint = mContext.getResources().getString(R.string.save_sucess);
			Toast.makeText(mContext,hint,Toast.LENGTH_LONG).show();
			result =  true;
		}
			
			int preState = mEthManager.getEthernetIfaceState();
            mEthManager.setEthernetEnabled(false);
			if (preState == EthernetDataTracker.ETHER_IFACE_STATE_UP) 
			{
				mEthManager.setEthernetEnabled(true);
			}
		
		return result;
	}

	private boolean isEmpty(String text)
	{
		if(null == text || TextUtils.isEmpty(text)) 
		{
            return true;
        }

		return false;
	}
}
