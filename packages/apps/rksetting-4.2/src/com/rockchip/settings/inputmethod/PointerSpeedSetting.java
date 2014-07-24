package com.rockchip.settings.inputmethod;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.rockchip.settings.StorageUtils;
import android.widget.Toast;
import com.rockchip.settings.R;

public class PointerSpeedSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public PointerSpeedSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	public void settingPointerSpeed() {
		String action = StorageUtils.getPointerSpeedAction();
		if(action != null)
		{
			Intent i = new Intent(action);
			mContext.startActivity(i);
		}
		else
		{
			Toast.makeText(mContext,R.string.version_error,Toast.LENGTH_LONG).show();
		}
	}
}
