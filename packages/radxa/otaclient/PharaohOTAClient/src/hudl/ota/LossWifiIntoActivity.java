package hudl.ota;

import hudl.ota.util.Util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LossWifiIntoActivity extends Activity {

	private boolean mIsFromHome = true;

	@Override
	protected void onCreate(Bundle arg0) {

		overridePendingTransition(0, 0);
		setContentView(R.layout.activity_loss_connectivity);

		TextView textView = (TextView) findViewById(R.id.title);
		textView.setText(R.string.hudl_title_wifi_loss);

		Button nextButton = (Button) findViewById(R.id.button_go_to_wifi);

		mIsFromHome = getIntent().getBooleanExtra(Constants.EXTRA_UPDATE_FROM_HOME, true);

		nextButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Show WIFI FJ setting
				setupWifiWizard();

			}
		});

		if (mIsFromHome) {

			nextButton.setVisibility(View.VISIBLE);

		} else {

			nextButton.setVisibility(View.INVISIBLE);
		}

		SharedPreferences shared = getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS);
		shared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();

		super.onCreate(arg0);
	}

	private void setupWifiWizard() {
		// Enable Wifi
		WifiManager wManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wManager.setWifiEnabled(true);

		Util.log("No wifi connection");
		Intent wifiIntent = new Intent();

		wifiIntent.setComponent(new ComponentName("com.android.settings",
				"com.android.settings.wizard.fj.WifiSetupWizardFj"));

		wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(wifiIntent);
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);
	};

}
