package com.rockchip.settings.privacy;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.app.backup.IBackupManager;
import android.os.ServiceManager;

public class AutoRestoreSetting {
	private Context mContext = null;
	private Handler mHandler = null;
	private IBackupManager mBackupManager;

	public AutoRestoreSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mBackupManager = IBackupManager.Stub.asInterface(ServiceManager
				.getService(Context.BACKUP_SERVICE));
	}

	public void getDefaultAutoRestoreSetting() {
		ContentResolver res = mContext.getContentResolver();
		Boolean mAutoRestore = (Settings.Secure.getInt(res,
				Settings.Secure.BACKUP_AUTO_RESTORE, 1) == 1);
		if(mAutoRestore)
			((RKSettings)mContext).updateSettingItem(R.string.auto_restore_title,R.string.turn_on,R.string.auto_restore_summary,-1);
		else
			((RKSettings)mContext).updateSettingItem(R.string.auto_restore_title,R.string.turn_off,R.string.auto_restore_summary,-1);
		
		boolean mBackup= false;
		try {
			mBackup = mBackupManager.isBackupEnabled();
		} catch (RemoteException e) {
			// leave it 'false' and disable the UI; there's no backup manager
			mBackup = false;
		}

			((RKSettings)mContext).setSettingItemClickable(R.string.auto_restore_title,mBackup);

	}

	public void settingAutoRestore() {
		ContentResolver res = mContext.getContentResolver();
		Boolean mAutoRestore = (Settings.Secure.getInt(res,
				Settings.Secure.BACKUP_AUTO_RESTORE, 1) == 1);
        try {
            mBackupManager.setAutoRestore(!mAutoRestore);
        } catch (RemoteException e) {
        }
        getDefaultAutoRestoreSetting();
		Message msg = new Message();
		msg.what = SettingMacroDefine.upDateListView;
		mHandler.sendMessage(msg);    
	}
}
