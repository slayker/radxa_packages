package com.rockchip.settings.security;

import android.app.Activity;
import android.os.Bundle;
import com.rockchip.settings.R;

public class DeviceAdminActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_admin_layout);
		this.setTitle(getString(R.string.manage_device_admin));
	}
}
