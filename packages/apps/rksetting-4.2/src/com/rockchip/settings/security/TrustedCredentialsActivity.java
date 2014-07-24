package com.rockchip.settings.security;

import android.app.Activity;
import android.os.Bundle;
import com.rockchip.settings.R;

public class TrustedCredentialsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trusted_credential_layout);
		this.setTitle(getString(R.string.trusted_credentials));
	}
}
