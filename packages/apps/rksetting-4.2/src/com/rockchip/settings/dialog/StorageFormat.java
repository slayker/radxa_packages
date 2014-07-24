package com.rockchip.settings.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.res.Configuration;
import android.content.Intent;
import android.util.Log;

public class StorageFormat
{
	private Context mContext = null;
	private Handler mHandler = null;
	
	
	public StorageFormat(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
	
	}
		
	public void FormatStorage()
	{

		Intent intent = new Intent(mContext, StorageFormatAlterDialogActivity.class); 		
		
		mContext.startActivity(intent);
	}

}

