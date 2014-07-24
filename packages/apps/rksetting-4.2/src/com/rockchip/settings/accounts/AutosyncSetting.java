package com.rockchip.settings.accounts;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.renderscript.RenderScript.Priority;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

public class AutosyncSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public AutosyncSetting(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
		if(mHandler != null)
		{
			boolean masterSyncAutomatically = ContentResolver
					.getMasterSyncAutomatically();
			if(masterSyncAutomatically)
				((RKSettings)mContext).updateSettingItem(R.string.account_sync_settings_title,R.string.sync_enabled,-1,-1);
			else
				((RKSettings)mContext).updateSettingItem(R.string.account_sync_settings_title,R.string.sync_disabled,-1,-1);
		}
	}

	// 设置自动更新
	public void settingAutosync() {
		boolean oldmasterSyncAutomatically = ContentResolver
				.getMasterSyncAutomatically();
		ContentResolver.setMasterSyncAutomatically(!oldmasterSyncAutomatically);
		// Log.d("hhq", "oldmasterSyncAutomatically:" +
		// oldmasterSyncAutomatically);
	}
}
