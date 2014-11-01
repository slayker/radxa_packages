package hudl.ota;

import hudl.ota.model.UpdateInfo;
import hudl.ota.util.TrackingHelper;
import hudl.ota.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class VerifyOtaActivity extends OTAActivity {
	public static final String EXTRA_UPDATE_IS_READY = "extra_update_is_ready";
	private View mDividerView;
	private TextView mTitleText;
	private ProgressBar mProgressBar;
	private TextView mProgressText;
	private TextView mBatteryText;
	private String mUpdateLocalFile;
	private ViewGroup mButtonsContainer;
	private UpdateInfo mUpdateInfo;
	private String mUpdateChecksum;
	private Button mPerformUpdateButton;
	private Button mPerformLaterButton;
	private boolean mFromHome;
	// private DownloadManager mDownloadManager;

	private AsyncTask<Void, Void, Boolean> mFileAuthenticityCheckTask;
	private AsyncTask<Void, Void, Boolean> mFileIntegrityCheckTask;

	private BatteryCheckReceiver mBatteryCheckReceiver;

	private View.OnClickListener mResetDownloadClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {

			retryDownloadUpdate();

		}
	};

	private class BatteryCheckReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			checkBatteryLevel();

		}

	}

	protected void onCreate(Bundle savedInstanceState) {

		// Fixing TIP-1159
		super.onCreate(savedInstanceState);
		
		
		if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
			Toast.makeText(this, R.string.toast_notification_only_owner, Toast.LENGTH_LONG).show();
			overridePendingTransition(0, 0);
			finish();
			overridePendingTransition(0, 0);
			return;
		}

		setContentView(R.layout.main_verify_ota);

		TrackingHelper.configureAppMeasurement(getApplicationContext());
		TrackingHelper.trackScreenIsShown(getApplicationContext(), TrackingHelper.TRACKING_CHECKING_UPDATE_PAGE);

		// MPV - PYT-419
		mTitleText = (TextView) findViewById(R.id.title);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mDividerView = (View) findViewById(R.id.tam__header_divider);
		mProgressText = (TextView) findViewById(R.id.text_download);
		mBatteryText = (TextView) findViewById(R.id.text_battery);
		mButtonsContainer = (ViewGroup) findViewById(R.id.container_buttons);
		mButtonsContainer.setVisibility(View.GONE);

		// mDownloadManager = (DownloadManager)
		// getSystemService(DOWNLOAD_SERVICE);

		mPerformUpdateButton = (Button) findViewById(R.id.button_perform_update);
		mPerformUpdateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				launchUpdate();

			}
		});

		// MPV - TIP-1105
		mPerformLaterButton = (Button) findViewById(R.id.button_skip_restart);
		// TIP-1126
		mPerformLaterButton.setVisibility(View.GONE);

		Intent i = getIntent();
		if (i.getBooleanExtra(EXTRA_UPDATE_IS_READY, false)) {

			// Check battery before show "Ready to install"

			// GDX
			setReadyForRebootAndInstall();

		} else {

			mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_VERIFY_OTA_UPDATE)
					.commit();

			mUpdateLocalFile = i.getStringExtra(Constants.EXTRA_UPDATE_FILE_PATH);
			mFromHome = i.getBooleanExtra(Constants.EXTRA_UPDATE_FROM_HOME, false);
			mUpdateInfo = (UpdateInfo) i.getParcelableExtra(Constants.EXTRA_UPDATE_INFO);
			mUpdateChecksum = mUpdateInfo.getUpdateChecksum();

			checkFileIntegrity();
		}

		// Remove notification
		// Rollback to the previous step.
		// setReadyForRebootAndInstall();
		// MPV -- END PYT-419
	};

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();

		if(UserHandle.myUserId() == UserHandle.USER_OWNER) {
			if (!isFromHome()) {

				if (mFileIntegrityCheckTask != null) {

					mFileIntegrityCheckTask.cancel(false);
					mFileIntegrityCheckTask = null;
				}

				if (mFileAuthenticityCheckTask != null) {
					mFileAuthenticityCheckTask.cancel(false);
					mFileAuthenticityCheckTask = null;
				}

				finish();
				overridePendingTransition(0, 0);

				if (mShared.getInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE) == Constants.STATUS.STATUS_VERIFY_OTA_UPDATE) {

					Intent i = new Intent(this, VerifyOtaActivity.class);
					i.putExtra(Constants.EXTRA_UPDATE_FILE_PATH, mUpdateLocalFile);
					i.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, mFromHome);
					i.putExtra(Constants.EXTRA_UPDATE_INFO, mUpdateInfo);
					i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

					NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					Notification.Builder mBuilder = new Notification.Builder(this);
					mBuilder.setSmallIcon(R.drawable.hudl_notification_small);
					mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.hudl_notification_large));
					mBuilder.setContentTitle(getString(R.string.hudl_update_title));
					mBuilder.setContentText(getString(R.string.hudl_update_download_complete));
					mBuilder.setOngoing(true);
					// mBuilder.setAutoCancel(true);
					mBuilder.setPriority(Notification.PRIORITY_HIGH);
					PendingIntent pendingIntent = PendingIntent.getActivityAsUser(this, 0, i, 0, null, new UserHandle(
							UserHandle.USER_CURRENT));

					mBuilder.setContentIntent(pendingIntent);

					nm.notifyAsUser(null, Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID, mBuilder.build(), UserHandle.ALL);
					nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, UserHandle.ALL);
					nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, UserHandle.ALL);
					nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE, UserHandle.ALL);
				}

			}
		}
	}

	@Override
	protected void onPause() {

		super.onPause();
		TrackingHelper.stopActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		TrackingHelper.startActivity(this);
	}

	private void checkFileAuthenticity() {
		mFileAuthenticityCheckTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected void onPreExecute() {
				mTitleText.setText(getString(R.string.checking_authenticity));
				showProgress();
				mProgressText.setText("");
			}

			@Override
			protected Boolean doInBackground(Void... params) {

				try {

					// Thread.currentThread().sleep(2000);

					if (isCancelled()) {

						Util.log("Authenticity task cancelled");

						return null;
					}

					RecoverySystem.verifyPackage(new File(mUpdateLocalFile), null, null);

					if (isCancelled()) {
						Util.log("Authenticity task cancelled");
						return null;
					}

					// if no error, we can rename the file to the cache
					// directory
					final File src = new File(mUpdateLocalFile);
					src.renameTo(new File(Constants.CACHE_TARGET));

					return true;
				} catch (IOException ioe) {
					// the file is corrupted or no longer available
					Util.log(ioe);
					return false;
				} catch (GeneralSecurityException gse) {
					// the file is not valid
					Util.log(gse);

					return false;
				}
				// catch (InterruptedException e) {
				// return false;
				// }

			}
			
			@Override
			protected void onCancelled(Boolean result) {

				Util.log("On cancelled");
				mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_VERIFY_OTA_UPDATE)
				.commit();
				super.onCancelled(result);
			}

			@Override
			protected void onPostExecute(Boolean successful) {

				removeNotificationCompleteDownload();
				if (successful) {

					// Remove notification
					// UnlockFile
					//unlockFile(Constants.CACHE_TARGET);
					// MPV - PYT-419
					// launchUpdate();
					setReadyForRebootAndInstall();

				} else {

					Resources res = getResources();

					TrackingHelper.trackUpdateErrors(getApplicationContext(),
							TrackingHelper.TRACKING_VERIFICATION_ERROR_PAGE, res.getString(R.string.error_signature));

					showErrorViewPage(R.string.error_verification_title, R.string.error_signature);
				}

			}

		};
		mFileAuthenticityCheckTask.execute();

	}

	private void unlockFile(String filepath) {
		File file = new File(filepath);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			fis.getChannel().lock(0, file.length(), true).release();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void checkFileIntegrity() {
		mFileIntegrityCheckTask = new AsyncTask<Void, Void, Boolean>() {

			DigestInputStream digestInputStream = null;
			FileInputStream fileInputStream = null;
			String digest;

			@Override
			protected void onPreExecute() {
				mTitleText.setText(getString(R.string.checking_integrity));
				showProgress();
				mProgressText.setText("");
			}

			@Override
			protected Boolean doInBackground(Void... params) {

				try {
					// Thread.currentThread().sleep(3000);

					if (isCancelled()) {

						Util.log("Integrity task cancelled");

						return null;
					}

					MessageDigest md5 = MessageDigest.getInstance("MD5");
					fileInputStream = new FileInputStream(mUpdateLocalFile);
					digestInputStream = new DigestInputStream(fileInputStream, md5);

					int read;
					byte[] buffer = new byte[38192];
					while ((read = digestInputStream.read(buffer)) != -1) {

						if (isCancelled()) {
							Util.log("Integrity task cancelled");
							return null;
						}

					}
					String digest = Util.byteArrayToHexString(md5.digest());

					Util.log("MD5: " + digest + " " + this.toString());
					Util.log("CRC: " + mUpdateChecksum + " " + this.toString());
					if (isCancelled()) {
						Util.log("Integrity task cancelled");
						return null;
					}

					return digest.equalsIgnoreCase(mUpdateChecksum);

				} catch (FileNotFoundException e) {

					Util.log(e);

					if (digest != null && mUpdateChecksum != null) {
						return digest.equalsIgnoreCase(mUpdateChecksum);
					} else {
						return false;
					}

				} catch (Exception e) {

					Util.log(e);

					Util.log(e);

					return false;
				} finally {
					try {
						if (digestInputStream != null) {
							Util.log("digest stream cancelled ");
							digestInputStream.close();
						}
					} catch (Exception e2) {
						Util.log(e2);
					}

					try {
						if (fileInputStream != null) {
							Util.log("file Input Stream cancelled ");
							fileInputStream.close();
						}
					} catch (Exception e2) {
						Util.log(e2);
					}
				}

			}

			@Override
			protected void onCancelled(Boolean result) {

				Util.log("On cancelled");

				try {
					if (digestInputStream != null) {

						Util.log("digest stream cancelled ");
						digestInputStream.close();
					}
				} catch (Exception e2) {
					Util.log(e2);
				}

				try {
					if (fileInputStream != null) {
						Util.log("file Input Stream cancelled ");
						fileInputStream.close();
					}
				} catch (Exception e2) {
					Util.log(e2);
				}

				digestInputStream = null;
				fileInputStream = null;
				mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_VERIFY_OTA_UPDATE)
				.commit();
				super.onCancelled(result);
			}

			@Override
			protected void onPostExecute(Boolean successful) {

				if (successful) {
					checkFileAuthenticity();
				} else {

					Resources res = getResources();

					TrackingHelper.trackUpdateErrors(getApplicationContext(),
							TrackingHelper.TRACKING_VERIFICATION_ERROR_PAGE, res.getString(R.string.error_checksum));

					showErrorViewPage(R.string.error_verification_title, R.string.error_checksum);
					// showCheckSumError();
					removeNotificationCompleteDownload();

				}
			}
		};
		mFileIntegrityCheckTask.execute();

	}

	private void setReadyForRebootAndInstall() {

		TrackingHelper.trackUpdate(getApplicationContext(), TrackingHelper.TRACKING_RESTART_REQUEST_PAGE);

		mTitleText.setText(R.string.label_title_restart_hudl);
		mProgressText.setText(R.string.label_description_restart_hudl);

		hideProgress();

		mButtonsContainer.setVisibility(View.VISIBLE);
		if (!isFromHome()) {

		}
		mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_INSTALL_UPDATE).commit();
		mShared.edit().remove(Constants.SHARED_DOWNLOAD_ID).commit();

		// GDX
		checkBatteryLevel();
		mBatteryCheckReceiver = new BatteryCheckReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatteryCheckReceiver, filter);

		// SHow Notification TIP-1124
		showNotificationReisntallApplication();

	}

	private void showNotificationReisntallApplication() {

		// SHOW NOTIFICATION
		NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder mBuilder = new Notification.Builder(this);
		mBuilder.setSmallIcon(R.drawable.hudl_notification_small);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.hudl_notification_large));
		mBuilder.setContentTitle(this.getString(R.string.hudl_update_title));
		mBuilder.setContentText(this.getString(R.string.hudl_update_ready_to_install));
		// mBuilder.setAutoCancel(false);
		mBuilder.setOngoing(true);
		//
		Intent intentInstallUpdate = new Intent(this, VerifyOtaActivity.class);
		intentInstallUpdate.putExtra(VerifyOtaActivity.EXTRA_UPDATE_IS_READY, true);
		intentInstallUpdate.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

		PendingIntent pendingIntent = PendingIntent.getActivityAsUser(this, 0, intentInstallUpdate,
				PendingIntent.FLAG_CANCEL_CURRENT, null, new UserHandle(UserHandle.USER_CURRENT));

		mBuilder.setContentIntent(pendingIntent);

		nm.notifyAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, mBuilder.build(), UserHandle.ALL);

	}

	// GDX
	private void checkBatteryLevel() {
		if (Util.isBatteryLevelAcceptable(getBaseContext(), Util.minimumBatteryLevel)) {
			hideBatteryLevelWarning();
		} else {
			showBatteryLevelWarning();
		}
	}

	// GDX
	private void showBatteryLevelWarning() {
		TrackingHelper.trackUpdate(getApplicationContext(), TrackingHelper.TRACKING_UPDATE_BATTERY_REQUEST_PAGE);

		int batteryLevel = (int) (Util.getBatteryLevel(getBaseContext()) * 100);
		mBatteryText.setText(getResources().getString(R.string.label_battery_level_restart_hudl).replace("N%",
				Integer.toString(batteryLevel) + "%"));
		mBatteryText.setVisibility(View.VISIBLE);
		mPerformUpdateButton.setEnabled(false);
	}

	// GDX
	private void hideBatteryLevelWarning() {
		mBatteryText.setVisibility(View.INVISIBLE);
		mPerformUpdateButton.setEnabled(true);
	}

	// GDX
	@Override
	protected void onDestroy() {

		if (mBatteryCheckReceiver != null) {
			unregisterReceiver(mBatteryCheckReceiver);
		}
		super.onDestroy();
	}

	private void showErrorViewPage(int title, int message) {

		mTitleText.setText(title);
		mProgressText.setText(message);
		mProgressText.setVisibility(View.VISIBLE);

		mButtonsContainer.setVisibility(View.VISIBLE);

		hideProgress();

		mPerformUpdateButton.setText(R.string.try_again);
		mPerformUpdateButton.setOnClickListener(mResetDownloadClickListener);

		SharedPreferences shared = getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS);
		shared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();
		shared.edit().remove(Constants.SHARED_DOWNLOAD_ID).commit();

	}

	private void retryDownloadUpdate() {
		Intent i = new Intent();
		i.setClass(this, OTAClientChecker.class);
		i.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, mFromHome);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(i);
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);

	}

	private void removeNotificationInstallOTA() {
		NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, UserHandle.ALL);
	}

	private void removeNotificationCompleteDownload() {

		NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		// nm.cancel(Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID);

		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID, UserHandle.ALL);

	}

	private void showProgress() {

		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setIndeterminate(true);
		mDividerView.setVisibility(View.GONE);

	}

	private void hideProgress() {
		mProgressBar.setVisibility(View.GONE);
		mProgressBar.setIndeterminate(false);
		mDividerView.setVisibility(View.VISIBLE);

	}

	private void launchUpdate() {

		TrackingHelper.trackUpdate(getApplicationContext(), TrackingHelper.TRACKING_INSTALLING_PAGE);

		removeNotificationInstallOTA();
		File otaFile = new File(Constants.CACHE_TARGET);

		try {

			if (otaFile.exists()) {

				// UnlockFile
				unlockFile(Constants.CACHE_TARGET);

				RecoverySystem.installPackage(getApplicationContext(), new File(Constants.CACHE_TARGET));
				SharedPreferences shared = getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS);
				shared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();
		
			} else {
				throw new IOException("OTA file doesn't exist- It might be removed from the cache");
				
			}
		} catch (IOException e) {

			Util.log(e);
			showErrorViewPage(R.string.error_verification_title, R.string.error_signature);
		}
	}

}
