package com.rockchip.settings.accounts;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.Intent;
import android.util.Log;

public class ManageAccountsSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public ManageAccountsSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	public void settingManageAccounts() {
		Intent intent = new Intent(mContext,
				ManageAccountsAlterDialogActivity.class);
		mContext.startActivity(intent);
	}
}
