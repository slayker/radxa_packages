package com.rockchip.settings.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.res.Configuration;
import android.content.Intent;
import android.util.Log;

public class LanguageSetting
{
	private Context mContext = null;
	private Handler mHandler = null;
	
	private void LOGD(String msg)
	{
		if(true)
			Log.d("LanguageSetting",msg);
	}
	
	public LanguageSetting(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
	}

	public String getDefaultLanguageSetting()
	{
		Configuration conf = mContext.getResources().getConfiguration();
		String locale = conf.locale.getDisplayName(conf.locale);
		if (locale != null && locale.length() > 1) 
		{
			locale = Character.toUpperCase(locale.charAt(0)) + locale.substring(1);
			LOGD("getDefaultLanguageSetting(),local = "+locale);
			return locale;
		}

		return "";
	}

	//popup setting dialog
	public void settingLanguage()
	{
		Intent intent = new Intent(mContext, LanguageSettingAlterDialogActivity.class); 		
		mContext.startActivity(intent);
	}
}

