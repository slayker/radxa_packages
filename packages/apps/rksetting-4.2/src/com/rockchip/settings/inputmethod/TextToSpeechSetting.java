package com.rockchip.settings.inputmethod;


import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.Intent;
import android.util.Log;

public class TextToSpeechSetting {
	private Context mContext = null;
	private Handler mHandler = null;

	public TextToSpeechSetting(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	public void settingTextToSpeech() {
		Intent intent = new Intent(mContext,
				TextToSpeechActivity.class);
		mContext.startActivity(intent);
	}
}
