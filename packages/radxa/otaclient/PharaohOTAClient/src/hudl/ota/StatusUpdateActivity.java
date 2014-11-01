package hudl.ota;

import hudl.ota.model.UpdateInfo;
import hudl.ota.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusUpdateActivity extends Activity {

	private TextView mDescriptionText, mTitleText;
	private Button mCheckButton;

	private SharedPreferences mShared;

	private SimpleDateFormat hourFormatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
	private SimpleDateFormat dayFormatter = new SimpleDateFormat("FF MMMM yyyy", Locale.ENGLISH);

	private boolean mPressed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mShared = getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS);

		checkStatusOTA();

		setContentView(R.layout.main_status_update);

		mTitleText = (TextView) findViewById(R.id.title);
		mDescriptionText = (TextView) findViewById(R.id.description);

		mCheckButton = (Button) findViewById(R.id.button_check_new_version);
		mCheckButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (!mPressed) {
					mPressed = true;
					// Go to OtaClientChecker
					goToOtaClientChecker();
				}
			}

		});

	}

	private void checkStatusOTA() {

		UpdateInfo updateInfo;
		int otaStatus = mShared.getInt(Constants.SHARED_STATUS_OTA_UPDATE, 1);

		switch (otaStatus) {

		// No status
		case Constants.STATUS.STATUS_IDLE:
			// Nothing to do right now.
			Util.log(" STATUS IDLE ");
			break;

		case Constants.STATUS.STATUS_INFO_UPDATE_ACTIVITY:

			// We need the update info for starting the Activity

			updateInfo = new UpdateInfo(mShared.getString(Constants.SHARED_INFO_UPDATE, null));

			Intent intent = new Intent(this, InfoUpdateActivity.class);
			intent.putExtra(Constants.EXTRA_UPDATE_INFO, updateInfo);
			intent.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, false);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
			overridePendingTransition(0, 0);
			Util.log(" STATUS INFO ");

			break;

		// OTA downloaded - Ready for the checking
		case Constants.STATUS.STATUS_CLIENT_DOWNLOADER:

			// A new update is available there are already stored the
			// information about it.
			// Retrieve the information regarding the update and lunch the right
			// activity

			long requestId = mShared.getLong(Constants.SHARED_DOWNLOAD_ID, -1);

			// OPEN the right activity just if the requestId is valid
			if (requestId != -1) {
				// OPEN the right activity
				Intent intentNewActivity = new Intent(this, OTAClientDownloader.class);
				intentNewActivity.setAction(getString(R.string.intent_download_click));
				intentNewActivity.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, requestId);
				intentNewActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intentNewActivity);
				overridePendingTransition(0, 0);
				finish();
				overridePendingTransition(0, 0);
			}

			Util.log(" STATUS DOWNLOADER ");
			break;

		// The update is downloaded and needs to be verified before the
		// installation

		case Constants.STATUS.STATUS_VERIFY_OTA_UPDATE:

			String locationFile = mShared.getString(Constants.SHARED_LOCATION_UPDATE, "");

			updateInfo = new UpdateInfo(mShared.getString(Constants.SHARED_INFO_UPDATE, ""));

			Intent i = new Intent(this, VerifyOtaActivity.class);

			i.putExtra(Constants.EXTRA_UPDATE_FILE_PATH, locationFile);
			i.putExtra(Constants.EXTRA_UPDATE_INFO, updateInfo);
			i.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, false);
			i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(i);
			overridePendingTransition(0, 0);

			finish();
			overridePendingTransition(0, 0);

			Util.log(" STATUS VERIFY ");
			break;

		// The update is stored, checked and moved to the right place.
		// Everything is ready for the installation
		case Constants.STATUS.STATUS_INSTALL_UPDATE:

			Intent intentInstallUpdate = new Intent(this, VerifyOtaActivity.class);
			intentInstallUpdate.putExtra(VerifyOtaActivity.EXTRA_UPDATE_IS_READY, true);
			intentInstallUpdate.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intentInstallUpdate);
			overridePendingTransition(0, 0);

			finish();
			overridePendingTransition(0, 0);

			Util.log(" STATUS INSTALL ");
			break;

		//
		// case Constants.STATUS.STATUS_BATTERY_CHECK:
		//
		// Intent intentBatteryCheck = new Intent();
		// intentBatteryCheck.setClass(this, InsufficientBatteryActivity.class);
		// intentBatteryCheck.putExtra(Constants.EXTRA_UPDATE_FROM_HOME,
		// isFromHome());
		// intentBatteryCheck.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		// startActivity(intentBatteryCheck);
		// overridePendingTransition(0, 0);
		// finish();
		// overridePendingTransition(0, 0);
		// break;

		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		mPressed = false;
	}

	@Override
	protected void onStart() {
		mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();
		super.onStart();
	}

	@Override
	protected void onResume() {

		super.onResume();

		// MPV - TIP-1082
		// If we came here after pressing the check button, we show the message
		// telling that we are up to date.
		if (mShared.getBoolean(Constants.SHARED_FROM_CHECK_BUTTON, false)) {
			mShared.edit().putBoolean(Constants.SHARED_FROM_CHECK_BUTTON, false).commit();
			mTitleText.setText(getString(R.string.label_check_title_updated));
			long lastCheckTimestamp = mShared.getLong(Constants.SHARED_LAST_CHECK_TIMESTAMP, -1);

			// TIP-1229
			if (lastCheckTimestamp != -1) {

				if (DateUtils.isToday(lastCheckTimestamp)) {

					mDescriptionText.setText(getString(R.string.label_last_time_check_today) + " "
							+ hourFormatter.format(new Date(lastCheckTimestamp)));
				} else {
					mDescriptionText.setText(getString(R.string.label_last_time_check) + " "
							+ dayFormatter.format(new Date(lastCheckTimestamp)));
				}

			}
			mShared.edit().putLong(Constants.SHARED_LAST_CHECK_TIMESTAMP, lastCheckTimestamp);
		} else { // If we came here without pressing the check button, we show
					// the message: Check for updates.
			mTitleText.setText(getString(R.string.label_check_update_title));
			long lastUpdated = mShared.getLong(Constants.SHARED_LAST_UPDATED_TIME, -1);
			if (lastUpdated == -1) {
				lastUpdated = System.currentTimeMillis();
				mShared.edit().putLong(Constants.SHARED_LAST_UPDATED_TIME, lastUpdated).commit();
			}

			// TIP-1229
			if (DateUtils.isToday(lastUpdated)) {
				mDescriptionText.setText(getString(R.string.label_last_time_update_today) + " "
						+ hourFormatter.format(new Date(lastUpdated)));
			} else {
				mDescriptionText.setText(getString(R.string.label_last_time_updated) + " "
						+ dayFormatter.format(new Date(lastUpdated)));
			}

		}
	}

	private void goToOtaClientChecker() {

		Intent i = new Intent(this, OTAClientChecker.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(i);
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		LinearLayout root = (LinearLayout) findViewById(R.id.root);
		root.setPadding(getResources().getDimensionPixelSize(R.dimen.activity_margin_left), getResources()
				.getDimensionPixelSize(R.dimen.activity_margin_top),
				getResources().getDimensionPixelSize(R.dimen.activity_margin_right), getResources()
						.getDimensionPixelSize(R.dimen.activity_margin_bottom));

		for (int i = 1; i <= 4; i++) {
			Button button = (Button) root.findViewWithTag("button_" + i);
			if (button != null) {
				if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					button.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
							R.dimen.button_width), getResources().getDimensionPixelSize(R.dimen.button_height)));
				} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
					button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
							getResources().getDimensionPixelSize(R.dimen.button_height)));
				}
			}
		}

	}

}
