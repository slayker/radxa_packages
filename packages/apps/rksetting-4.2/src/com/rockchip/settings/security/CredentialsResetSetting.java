package com.rockchip.settings.security;


import android.security.KeyStore;
import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.dialog.KeyBoardSettingAlterDialogActivity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import android.content.Intent;
import android.util.Log;

public class CredentialsResetSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public CredentialsResetSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		((RKSettings)mContext).updateSettingItem(R.string.credentials_reset,-1,R.string.credentials_reset_summary,-1);
	}
	public void getDefaultCredentialsResetSetting() {
        KeyStore.State state = KeyStore.getInstance().state();
			((RKSettings) mContext).setSettingItemClickable(
					R.string.credentials_reset, state != KeyStore.State.UNINITIALIZED);

	}
	public void settingCredentialsReset() {
		CredentialStorage.setHandler(mHandler);
		Intent intent = new Intent("com.rockchip.credentials.RESET");
		intent.setClassName("com.rockchip.settings", "com.rockchip.settings.security.CredentialStorage");
		mContext.startActivity(intent);
	}
}
