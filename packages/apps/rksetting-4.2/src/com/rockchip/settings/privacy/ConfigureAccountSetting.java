package com.rockchip.settings.privacy;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import android.content.Intent;
import android.util.Log;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class ConfigureAccountSetting {
	private Context mContext = null;
	private Handler mHandler = null;
	private IBackupManager mBackupManager;

	public ConfigureAccountSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mBackupManager = IBackupManager.Stub.asInterface(ServiceManager
				.getService(Context.BACKUP_SERVICE));
	}

	public void getDefaultConfigureAccountSetting() {
		boolean backupEnabled = false;
		Intent configIntent = null;
		String configSummary = null;
		try {
			backupEnabled = mBackupManager.isBackupEnabled();
			String transport = mBackupManager.getCurrentTransport();
			configIntent = mBackupManager.getConfigurationIntent(transport);
			configSummary = mBackupManager.getDestinationString(transport);
		} catch (RemoteException e) {
		}

		final boolean configureEnabled = (configIntent != null)
				&& backupEnabled;

		if (configSummary != null)
			((RKSettings) mContext).updateSettingItem(
					R.string.backup_configure_account_title, null,
					configSummary, null);
		else
			((RKSettings) mContext).updateSettingItem(
					R.string.backup_configure_account_title, -1,
					R.string.backup_configure_account_default_summary, -1);

		boolean mBackup = false;
		try {
			mBackup = mBackupManager.isBackupEnabled();
		} catch (RemoteException e) {
			// leave it 'false' and disable the UI; there's no backup manager
			mBackup = false;
		}
		if (mBackup == false)
			((RKSettings) mContext).setSettingItemClickable(
					R.string.backup_configure_account_title, mBackup);
		else
			((RKSettings) mContext).setSettingItemClickable(
					R.string.backup_configure_account_title, configureEnabled);

	}

	public void settingConfigureAccount() {
		Intent configIntent = null;
		try {
			String transport = mBackupManager.getCurrentTransport();
			configIntent = mBackupManager.getConfigurationIntent(transport);
		} catch (RemoteException e) {
		}
		mContext.startActivity(configIntent);

		getDefaultConfigureAccountSetting();
	}
}
