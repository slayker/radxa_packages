package com.rockchip.settings.security;


import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.Intent;
import android.util.Log;

public class TrustedCredentialsSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public TrustedCredentialsSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		((RKSettings)mContext).updateSettingItem(R.string.trusted_credentials,-1,R.string.trusted_credentials_summary,-1);
	}

	public void settingTrustedCredentials() {
		Intent intent = new Intent(mContext,
				TrustedCredentialsActivity.class);
		mContext.startActivity(intent);
	}
}
