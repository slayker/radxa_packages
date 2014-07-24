package com.rockchip.settings.security;


import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.Intent;
import android.util.Log;

public class CredentialsInstallSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public CredentialsInstallSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		((RKSettings)mContext).updateSettingItem(R.string.credentials_install,-1,R.string.credentials_install_summary,-1);
	}

	public void settingCredentialsInstall() {
		Intent intent = new Intent("android.credentials.INSTALL");
		intent.setClassName("com.android.certinstaller", "com.android.certinstaller.CertInstallerMain");
		mContext.startActivity(intent);
	}
}
