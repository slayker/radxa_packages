package com.rockchip.settings.inputmethod;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.rockchip.settings.R;

public class TextToSpeechActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tts_layout);
		this.setTitle(getString(R.string.tts_settings_title));
	}
}
