package com.rockchip.settings.dialog;

import com.rockchip.settings.R;
import com.rockchip.settings.ListViewAdapter;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.provider.Settings;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;
import android.os.SystemProperties;
import android.app.Dialog;


public class UsbDebugging
{
	private Context mContext = null;
	private Handler mHandler = null;
	private ListViewAdapter mListViewAdapter;

	private boolean mOkClicked;
	private DialogInterface mOkDialog;
	private int mstate;
	private boolean mselect=false;
	private boolean mreflash=false;
    

   /* private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Usb.ACTION_USB_STATE)) {
                handleUsbStateChanged(intent);
            }
        }
    };
	*/
	public UsbDebugging(Context context,Handler handler,ListViewAdapter adapter)
	{
		mContext = context;
		mHandler = handler;
		mListViewAdapter = adapter;
		mstate = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
		int status = (mstate==1)?R.string.open:R.string.off;
		((RKSettings)mContext).updateSettingItem(R.string.usb_debugging,status,-1,-1);
	}

      public static boolean isMonkeyRunning() {
          return SystemProperties.getBoolean("ro.monkey", false);
      }
		
	public void DebuggingUsb()
	{
		if (isMonkeyRunning()) {
			return ;
		}
		mselect = (mstate==1)?true:false;
       
            if (!mselect) {
                mOkDialog = new AlertDialog.Builder(mContext)
			   .setMessage(mContext.getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
          /*              .setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
								mstate = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
								mselect=true;
								int status = (mstate==1)?R.string.open:R.string.off;
								((RKSettings)mContext).updateSettingItem(R.string.usb_debugging,status,-1,-1);
								mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
							}
			   			})
						.setNegativeButton(android.R.string.no, null)
                        .show();
                //mOkDialog.setOnDismissListener(mContext.this);
            } else {
		Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
		mstate = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
		mselect=false;
		int status = (mstate==1)?R.string.open:R.string.off;
		((RKSettings)mContext).updateSettingItem(R.string.usb_debugging,status,-1,-1);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);		
            }
	}
}

