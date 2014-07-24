package com.rockchip.settings.dialog;

import com.rockchip.settings.R;
import com.rockchip.settings.ListViewAdapter;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;

public class EventNotificationSetting
{
	private Context mContext;
	private Handler mHandler;
	private ListViewAdapter mListViewAdapter;

	private String mMode = null;
	public EventNotificationSetting(Context context,Handler handler,ListViewAdapter adapter)
	{
		mContext = context;
		mHandler = handler;
		mListViewAdapter = adapter;
		mMode = SystemProperties.get("persist.sys.EventNotification", "open");
		Log.d("EventNotificationSetting","mMode = "+mMode);
		int state = mMode.equals("open")?R.string.open:R.string.off;
		((RKSettings)mContext).updateSettingItem(R.string.event_notification,state,-1,-1);
	}

	public void onEventNotificationClick()
	{
		if(mMode.equals("open"))
			SystemProperties.set("persist.sys.EventNotification", "close");
		else if(mMode.equals("close"))
			SystemProperties.set("persist.sys.EventNotification", "open");

		mMode = SystemProperties.get("persist.sys.EventNotification", "open");
		int state = mMode.equals("open")?R.string.open:R.string.off;
		((RKSettings)mContext).updateSettingItem(R.string.event_notification,state,-1,-1);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}
}
