package hudl.ota;

import hudl.ota.model.UpdateCheck;
import hudl.ota.model.UpdateInfo;
import hudl.ota.network.NetworkService;
import hudl.ota.util.TrackingHelper;
import hudl.ota.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/*
 * This class is responsible for checking update availability
 */
public class OTAClientChecker extends OTAActivity {

	private static final int AAA = 11;

	private UpdateInfo mUpdateInfo;
	private ProgressBar mProgressBar;
	private View mDividerView;

	private LinearLayout mContainerButton;

	private TextView mMessageText, mTitleText;
	private Button mGoToWifiButton;
	private SharedPreferences mShared;

	private AsyncTask<String, Void, UpdateCheck> mTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
			Toast.makeText(this, R.string.toast_notification_only_owner, Toast.LENGTH_LONG).show();
			overridePendingTransition(0, 0);
			finish();
			overridePendingTransition(0, 0);
			return;
		}
		
		TrackingHelper.configureAppMeasurement(getApplicationContext());
		TrackingHelper.trackScreenIsShown(getApplicationContext(),
				TrackingHelper.TRACKING_PAGE_NAME_CHECKING_FOR_UPDATES);

		setupUI();

		mShared = getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS);

	}

	@Override
	protected void onResume() {
		super.onResume();
		TrackingHelper.startActivity(this);
	}

	@Override
	protected void onStart() {

		super.onStart();

		if (isFromHome()) {

			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo infoNetwork = cm.getActiveNetworkInfo();
			if (infoNetwork == null || !infoNetwork.isConnectedOrConnecting()) {

				WifiManager wManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				wManager.setWifiEnabled(true);

				Util.log("No wifi connection");
				Intent wifiIntent = new Intent();

				wifiIntent.setComponent(new ComponentName("com.android.settings",
						"com.android.settings.wizard.fj.WifiSetupWizardFj"));

				wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivityForResult(wifiIntent, 11);
				overridePendingTransition(0, 0);

			} else {
				checkForUpdate();
			}
		} else {
			checkForUpdate();
		}
	}

	private void setupUI() {
		setContentView(R.layout.main_ota_client_checker);

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mDividerView = (View) findViewById(R.id.tam__header_divider);

		mContainerButton = (LinearLayout) findViewById(R.id.container_retry_button);

		mGoToWifiButton = (Button) findViewById(R.id.button_retry_wifi_connection);
		mGoToWifiButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setupWifiWizard();

			}
		});

		if (isFromHome()) {

			mGoToWifiButton.setVisibility(View.VISIBLE);

		} else {
			mGoToWifiButton.setVisibility(View.INVISIBLE);
		}

		Button retryButton = (Button) mContainerButton.findViewById(R.id.button_retry_download_json);

		retryButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mContainerButton.setVisibility(View.GONE);
				mTitleText.setText(R.string.label_check_title);
				mMessageText.setText(R.string.label_almost_ready);
				checkForUpdate();

			}
		});

		mMessageText = (TextView) findViewById(R.id.description);
		mTitleText = (TextView) findViewById(R.id.title);
		mTitleText.setText(R.string.label_check_title);
		mMessageText.setText(R.string.label_almost_ready);

	}

	private void checkForUpdate() {

		mTask = new AsyncTask<String, Void, UpdateCheck>() {

			@Override
			protected void onPreExecute() {

				showProgress();
			}

			@Override
			protected UpdateCheck doInBackground(String... params) {

				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					Util.log(e);
				}

				String version;
				Util.log("BUILD VERSION INCREMENTAL " + Build.VERSION.INCREMENTAL);

				version = Build.VERSION.INCREMENTAL;

				Util.log("Version to check " + version);

				return NetworkService.getInstance().getUpdateInformation(version, params[0], OTAClientChecker.this,
						isFromHome());

			}

			@Override
			protected void onPostExecute(UpdateCheck result) {

				hideProgress();

				if (result == null) {
					Util.log("result null");
					failedJsonFile();

				} else if (result.getStatus().equalsIgnoreCase("OK")) {

					mShared.edit().putLong(Constants.SHARED_LAST_CHECK_TIMESTAMP, System.currentTimeMillis()).commit();

					// UpdateInfo currentUpdateInfo = null ;

					if (result.getUpdateInfo() != null && result.getUpdateInfo().isValid()) {

						// there is an update
						mUpdateInfo = result.getUpdateInfo();

						// TIP-1137
						if (isFromHome()) {

							if (mUpdateInfo.isMandatory()) {
								showInfoUpdate();

							} else {
								skipAndDisable();
							}

						} else {
							showInfoUpdate();
						}
					} else {

						// there is no update available!
						if (isFromHome()) {

							skip();

						} else {

							showStatusUpdate();
						}
					}
				} else if (result.getStatus().equalsIgnoreCase("503")) {
					TrackingHelper.trackUpdateErrors(getApplicationContext(),
							TrackingHelper.TRACKING_UPDATE_BUSY_PAGE_NAME_PAGE, result.getStatus());
					failedServerBusy();
				} else {

					TrackingHelper.trackUpdateErrors(getApplicationContext(),
							TrackingHelper.TRACKING_UPDATE_ERROR_PAGE_NAME, result.getStatus());
					failedJsonFile();

				}

			}

			@Override
			protected void onCancelled() {
				hideProgress();
				super.onCancelled();
			}
		};

		if (Constants.DEBUG) {
			mTask.execute(Constants.URL_AMAZON_STAGING);
		} else {
			mTask.execute(Constants.URL_AMAZON);
		}
	}

	private void showInfoUpdate() {

		TrackingHelper.trackUpdate(getApplicationContext(), TrackingHelper.TRACKING_UPDATE_AVAILABLE_PAGE);

		final Intent intent = new Intent(this, InfoUpdateActivity.class);

		intent.putExtra(Constants.EXTRA_UPDATE_INFO, mUpdateInfo);
		intent.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, isFromHome());

		Util.log(" IS FROM HOME " + isFromHome());

		if (!isFromHome() && !mShared.getBoolean(Constants.SHARED_ACTIVE_NEW_DOWNLOAD_NOTIFICATION, false)) {

			showNotification();

		}

		mShared.edit().putString(Constants.SHARED_INFO_UPDATE, mUpdateInfo.writeToString()).commit();

		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);

	}

	private void showNotification() {

		// SHOW NOTIFICATION
		NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder mBuilder = new Notification.Builder(this);
		mBuilder.setSmallIcon(R.drawable.hudl_notification_small);
		mBuilder.setLargeIcon(getBitmap(R.drawable.hudl_notification_large));
		mBuilder.setContentTitle(this.getString(R.string.hudl_update_title));
		mBuilder.setContentText(this.getString(R.string.hudl_update_new_available));
		// mBuilder.setAutoCancel(true);
		mBuilder.setOngoing(true);
		mBuilder.setPriority(Notification.PRIORITY_HIGH);
 
		Intent intentOTAavailable = new Intent();
		intentOTAavailable.setClass(this, InfoUpdateActivity.class);
		intentOTAavailable.putExtra(Constants.EXTRA_UPDATE_INFO, mUpdateInfo);
		PendingIntent pendingIntent = PendingIntent.getActivityAsUser(this, 0, intentOTAavailable,
				PendingIntent.FLAG_CANCEL_CURRENT, null, new UserHandle(UserHandle.USER_CURRENT));

		mBuilder.setContentIntent(pendingIntent);

		// nm.notify(Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE,
		// mBuilder.build());
		mShared.edit().putBoolean(Constants.SHARED_ACTIVE_NEW_DOWNLOAD_NOTIFICATION, true).commit();

		nm.notifyAsUser(null, Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE, mBuilder.build(), UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID, UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, UserHandle.ALL);

	}

	private void failedJsonFile() {

		Util.log(" No Json File " + Util.isDeviceConnectedOrConnecting(this));

		if (!isFromHome() || Util.isDeviceConnectedOrConnecting(this)) {
			mContainerButton.setVisibility(View.VISIBLE);
			mMessageText.setText(R.string.error_json_file);

			mMessageText.setVisibility(View.VISIBLE);
			mTitleText.setText(R.string.label_check_title_network_error);

			Util.log(" Connection fine - Error for the server");

		} else {

			Util.log(" No connection waiting for Wifi error");

			Intent wifiIntent = new Intent(this, LossWifiIntoActivity.class);
			wifiIntent.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, isFromHome());

			wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(wifiIntent);

		}
	}

	private void failedServerBusy() {
		mContainerButton.setVisibility(View.VISIBLE);

		if (mGoToWifiButton != null) {

			mGoToWifiButton.setVisibility(View.GONE);
		} else {
			findViewById(R.id.button_retry_wifi_connection).setVisibility(View.GONE);
		}

		mMessageText.setText(R.string.error_server_busy);
		mMessageText.setVisibility(View.VISIBLE);
		mTitleText.setText(R.string.label_check_title_server_busy);

	}

	private void showStatusUpdate() {

		// MPV - TIP-1082
		mShared.edit().putBoolean(Constants.SHARED_FROM_CHECK_BUTTON, true).commit();

		Intent i = new Intent(this, StatusUpdateActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(i);
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);

	}

	private void showProgress() {

		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setIndeterminate(true);
		mDividerView.setVisibility(View.GONE);
		mContainerButton.setVisibility(View.GONE);
		mTitleText.setText(R.string.label_check_title);
		mMessageText.setText(R.string.label_almost_ready);
		if (isFromHome()) {
			mMessageText.setVisibility(View.VISIBLE);

		} else {
			mMessageText.setVisibility(View.INVISIBLE);
		}

	}

	private void hideProgress() {
		mProgressBar.setVisibility(View.GONE);
		mProgressBar.setIndeterminate(false);
		mDividerView.setVisibility(View.VISIBLE);

	}

	@Override
	protected void onPause() {

		if (!isFromHome() && mTask != null) {
			mTask.cancel(true);
		}

		super.onPause();

		TrackingHelper.stopActivity(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		Util.log(" Activity for result ");
		//Util.log(" Activity for result " + resultCode);
		//if (resultCode == RESULT_OK) {
			//checkForUpdate();
	    //} TIP-1211
		checkForUpdate();
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

	};

}
