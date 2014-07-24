package com.rockchip.settings.privacy;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.Intent;
import android.util.Log;

public class PrivacySetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public PrivacySetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	public void settingPrivacy() {
		Intent intent = new Intent(mContext,
				PrivacySettingAlterDialogActivity.class);
		mContext.startActivity(intent);
	}
}
