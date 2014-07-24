package com.rockchip.settings.inputmethod;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Build;
import android.content.ActivityNotFoundException;
import com.rockchip.settings.R;
import android.widget.Toast;

public class VoiceSearchSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public VoiceSearchSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	public void settingVoiceSearch() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		try
		{
			if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				i.setComponent(new ComponentName("com.google.android.googlequicksearchbox",
					"com.google.android.voicesearch.VoiceSearchPreferences"));
			}
			else
			{
				i.setComponent(new ComponentName("com.google.android.voicesearch",
						"com.google.android.voicesearch.VoiceSearchPreferences"));
			}
			
			mContext.startActivity(i);
		}
		catch (ActivityNotFoundException e)
		{
			Toast.makeText(mContext, R.string.activity_not_found,Toast.LENGTH_SHORT).show();
		}
	}
}
