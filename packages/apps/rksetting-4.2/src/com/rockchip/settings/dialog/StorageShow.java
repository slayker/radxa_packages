package com.rockchip.settings.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.res.Configuration;
import android.content.Intent;
import android.util.Log;

public class StorageShow
{
	private Context mContext = null;
	private Handler mHandler = null;
	
	

	public StorageShow(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
	
	}
		
	public void showStorage()
	{

		Intent intent = new Intent(mContext, StorageShowAlterDialogActivity.class); 		
		
		mContext.startActivity(intent);
	}

}

