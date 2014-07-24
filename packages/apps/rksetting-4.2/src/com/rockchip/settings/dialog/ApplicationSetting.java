package com.rockchip.settings.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.res.Configuration;
import android.content.Intent;
import android.util.Log;

import com.rockchip.settings.R;

public class ApplicationSetting
{
	private Context mContext = null;
	private Handler mHandler = null;
	
	
	public ApplicationSetting(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
	
	}

	/*create popup window*/
	public void SettingApplication()
	{

		//Intent intent = new Intent(mContext, ManageApplications.class);
		Intent intent = new Intent(Intent.ACTION_VIEW);
        	intent.setClass(mContext, com.rockchip.settings.dialog.ManageApplications.class);
		mContext.startActivity(intent);
	}

}

