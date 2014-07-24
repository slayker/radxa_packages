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

public class UnknownSources 
{

	private static final String TAG = "UnknownSources";
	private Context mContext ;
	private Handler mHandler ;
	private ListViewAdapter mListViewAdapter;
	private DialogInterface mWarnInstallApps;
	private boolean mselect=false;
	public UnknownSources(Context context,Handler handler,ListViewAdapter adapter)
	{	
		mContext = context;
		mHandler = handler;
		mListViewAdapter = adapter;
		updateItemStatus();
	}

	public void SourcesUnknown()
	{
		int state = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
		mselect = (state==1)?true:false;
		if (!mselect) {
			warnAppInstallation();
		} else {
			mselect=false;
			setNonMarketAppsAllowed(false);
			updateItemStatus();
		}
	}

	private void updateItemStatus(){
		int state = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
		int stringID = (state==1)?R.string.source_yes:R.string.source_no;
		((RKSettings)mContext).updateSettingItem(R.string.unknown_sources,stringID,-1,-1);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}

	private boolean isNonMarketAppsAllowed() {
		return Settings.Secure.getInt(mContext.getContentResolver(), 
						Settings.Secure.INSTALL_NON_MARKET_APPS, 0) > 0;
	}

	private void setNonMarketAppsAllowed(boolean enabled) {
		// Change the system setting
		Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 
		                        enabled ? 1 : 0);
	}
    

	private void warnAppInstallation() {
	   mWarnInstallApps = new AlertDialog.Builder(mContext)
			   .setTitle(mContext.getString(R.string.error_title))
			   .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
			   .setMessage(mContext.getResources().getString(R.string.install_all_warning))
		/*	   .setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
			   .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
			   {
			   		 public void onClick(DialogInterface dialog, int which)
		   			{
						setNonMarketAppsAllowed(true);
						mselect=true;
						updateItemStatus();
		   			}
			   })
			   .setNegativeButton(android.R.string.no, null)
			   .show();
   }
}

