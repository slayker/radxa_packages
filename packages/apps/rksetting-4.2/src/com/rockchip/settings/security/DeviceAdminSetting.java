package com.rockchip.settings.security;


import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.Intent;
import android.util.Log;

public class DeviceAdminSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public DeviceAdminSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		((RKSettings)mContext).updateSettingItem(R.string.manage_device_admin,-1,R.string.manage_device_admin_summary,-1);
	}

	public void settingDeviceAdmin() {
		Intent intent = new Intent(mContext,
				DeviceAdminActivity.class);
		mContext.startActivity(intent);
	}
}
