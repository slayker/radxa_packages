package com.rockchip.settings.privacy;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import android.content.Intent;
import android.util.Log;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.os.ServiceManager;

public class BackDataSetting {
	private Context mContext = null;
	private Handler mHandler = null;
	private IBackupManager mBackupManager;
	private Dialog mConfirmDialog;

	public BackDataSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mBackupManager = IBackupManager.Stub.asInterface(ServiceManager
				.getService(Context.BACKUP_SERVICE));

	}

	public void getDefaultBackDataSetting() {
		boolean mBackup= false;
		try {
			mBackup = mBackupManager.isBackupEnabled();
		} catch (RemoteException e) {
			// leave it 'false' and disable the UI; there's no backup manager
			mBackup = false;
		}
		
		if(mBackup)
			((RKSettings)mContext).updateSettingItem(R.string.backup_data_title,R.string.turn_on,R.string.backup_data_summary,-1);
		else
			((RKSettings)mContext).updateSettingItem(R.string.backup_data_title,R.string.turn_off,R.string.backup_data_summary,-1);
	}

	
	public void settingBackData() {
		boolean mBackup= false;
		try {
			mBackup = mBackupManager.isBackupEnabled();
		} catch (RemoteException e) {
			// leave it 'false' and disable the UI; there's no backup manager
			mBackup = false;
		}

		if (mBackup) {
			showEraseBackupDialog();
		} else {
			setBackupEnabled(true);
			 //需要更新界面，以及另外俩个开关的状态
			Message msg = new Message();
			msg.what = SettingMacroDefine.upBackDataSetting;
			mHandler.sendMessage(msg);    
		}
	}

	private void showEraseBackupDialog() {
		CharSequence msg = mContext.getResources().getText(
				R.string.backup_erase_dialog_message);

		// TODO: DialogFragment?
		mConfirmDialog = new AlertDialog.Builder(mContext)
			/*	.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
				.setMessage(msg)
				.setTitle(R.string.backup_erase_dialog_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setBackupEnabled(false);
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
	}

	private void setBackupEnabled(boolean enable) {
        if (mBackupManager != null) {
            try {
                mBackupManager.setBackupEnabled(enable);
            } catch (RemoteException e) {
                return;
            }
        }
        //需要更新界面,以及另外俩个开关的状态
		Message msg = new Message();
		msg.what = SettingMacroDefine.upBackDataSetting;
		mHandler.sendMessage(msg);  
    }
}
