package hudl.ota;

import hudl.ota.model.UpdateInfo;
import hudl.ota.util.TrackingHelper;
import hudl.ota.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Timer;
import java.util.TimerTask;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.UserHandle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OTAClientDownloader extends OTAActivity {

	private static final int LIMIT_MINUTE_BOUND = 20000;
	private UpdateInfo mUpdateInfo;
	private boolean mFromHome;
	private DownloadManager mDownloadManager;
	private Cursor mCursor;
	private int mStatusColumnIndex = 0;
	private int mReasonColumnIndex = 0;
	private int mBytesSoFarColumnIndex = 0;
	private int mTotalBytesColumnIndex = 0;
	private int mFilenameColumnIndex = 0;
	private int mLastTimeStampIndex = 0;
	private long mRequestID;
	private long mPreviusID;
	private DownloadContentObserver mDownloadContentObserver;
	private ProgressBar mProgressBar;
	private TextView mProgressText, mErrorDescription, mTitleView;
	private ViewGroup mButtonsContainer;
	private String mUpdateLocalFile;
	private Button mButtonCancelUpdate;
	private View mDivider;
	private SharedPreferences mShared;


	// GDX:watchdog
	private boolean mWatchdogEnabled = true;
	private long mLastObservedTimeMillis;
	private long mLastObserverdDataTransfer;
	private Timer mTimer;
	private TimerTask mWatchdogTimerTask;

	private View.OnClickListener mTryAgainClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			// setupDownloadUi();
			mDownloadManager.remove(mRequestID);
			retryDownloadUpdate();
		}
	};

	private View.OnClickListener mDeleteDownloadClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			NotificationManager nm = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
			// nm.cancel(Constants.NOTIFICATION_DOWNLOAD_RUNNING);
			nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, UserHandle.ALL);
			mDownloadManager.remove(mRequestID);

			showInfoUpdate();
		}
	};

	private class DownloadContentObserver extends ContentObserver {
		public DownloadContentObserver() {
			super(new Handler());
			return;
		}

		public void onChange(boolean selfChange) {
			updateProgressBar();
		}
	}

	@Override
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


		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		TrackingHelper.configureAppMeasurement(getApplicationContext());

		setupUi();
		
		mShared = getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS);

		mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

		mDownloadContentObserver = new DownloadContentObserver();

		Intent i = getIntent();

		String action = i.getAction();

		if (action != null) {

			mRequestID = i.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, (long) -1);
			// setupCursor();
			handleAction(i, false);

		} else {

			// We came from Intent without any action
			// it has been called by InfoUpdateActivity

			Util.log(" intent without action");
			Intent callingIntent = getIntent();

			mUpdateInfo = (UpdateInfo) callingIntent.getParcelableExtra(Constants.EXTRA_UPDATE_INFO);

			mFromHome = callingIntent.getBooleanExtra(Constants.EXTRA_UPDATE_FROM_HOME, false);

			Util.log("NORMAL FLOWs");

            if (mUpdateInfo != null && !mUpdateInfo.isMandatory()) {
            	mButtonsContainer.setVisibility(View.VISIBLE);
            } else {
            	mButtonsContainer.setVisibility(View.GONE);
          	}
            
			downloadFile();

		}

	}

	private void setupUi() {
		setContentView(R.layout.main_downloader);

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mProgressBar.setIndeterminate(true);
		mProgressBar.setVisibility(View.VISIBLE);
		mDivider = findViewById(R.id.tam__header_divider);
		mDivider.setVisibility(View.GONE);

		mProgressText = (TextView) findViewById(R.id.text_download);
		mErrorDescription = (TextView) findViewById(R.id.error_description);
		mTitleView = (TextView) findViewById(R.id.title);

		mButtonsContainer = (ViewGroup) findViewById(R.id.container_buttons);

		mButtonCancelUpdate = (Button) findViewById(R.id.button_cancel_update);
		mButtonCancelUpdate.setOnClickListener(mDeleteDownloadClickListener);

	}

	private void setupFailDownloadUi() {
		mButtonCancelUpdate.setText(R.string.try_again);
		mDivider.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		mButtonCancelUpdate.setText(R.string.try_again);
		mButtonCancelUpdate.setOnClickListener(mTryAgainClickListener);
		mButtonCancelUpdate.setVisibility(View.VISIBLE);

		mErrorDescription.setVisibility(View.VISIBLE);
		mProgressText.setVisibility(View.GONE);

		mTitleView.setText(R.string.label_download_stop);

		mButtonsContainer.setVisibility(View.VISIBLE);

		mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();

	}

	private void showNotificationDownloadAvailable() {

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

		// Snm.notify(Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE,
		// mBuilder.build());
		nm.notifyAsUser(null, Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE, mBuilder.build(), UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID, UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, UserHandle.ALL);

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

	private void downloadFile() {

		mProgressBar.setIndeterminate(true);

		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUpdateInfo.getUpdateURL()));

		request.setDestinationToSystemCache();

		// request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
		// "ota");
		request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
		request.setTitle(getString(R.string.ota_notification_title));
		request.setDescription(getString(R.string.ota_notification_description));
		request.setVisibleInDownloadsUi(false);

		if (mFromHome) {
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
		} else {

			// Implement custom notification
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		}

		// Setup new requestID
		try {
			mRequestID = mDownloadManager.enqueue(request);
			Editor editor = mShared.edit();
			editor.putLong(Constants.SHARED_DOWNLOAD_ID, mRequestID).commit();
			requestNewCursor();
		} catch (IllegalArgumentException e) {

			// The system download manager is disabled. There is no choice to
			// start the download.
			// content://downloads/my_downloads is not available.
			// Force to close the app as the Google Play Store does.
			Util.log(e);
			Util.log("Error in the download- DownloadManager not available");
			finish();
			overridePendingTransition(0, 0);

		}

		startWatchdog();

	}

	private void requestNewCursor() {

		try {
			if (mCursor != null) {
				mCursor.unregisterContentObserver(mDownloadContentObserver);
				mCursor.close();

			}
		} catch (java.lang.IllegalStateException e) {
			Util.log(e);
		} finally {
			mCursor = null;
		}

		mCursor = mDownloadManager.query(new DownloadManager.Query().setFilterById(mRequestID));
		if (mCursor != null && !mCursor.isClosed() && mCursor.getCount() == 1) {
			mCursor.moveToFirst();

			mStatusColumnIndex = mCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
			mReasonColumnIndex = mCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
			mBytesSoFarColumnIndex = mCursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
			mTotalBytesColumnIndex = mCursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
			mFilenameColumnIndex = mCursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
			mLastTimeStampIndex = mCursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
			mCursor.registerContentObserver(mDownloadContentObserver);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {

		if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
			Toast.makeText(this, R.string.toast_notification_only_owner, Toast.LENGTH_LONG).show();
			overridePendingTransition(0, 0);
			finish();
			overridePendingTransition(0, 0);
		}
		else{
			setupUi();
			handleAction(intent, true);			
		}

		super.onNewIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		Util.log("Saved current state");

		Editor edit = mShared.edit();
		edit.putString(Constants.SHARED_INFO_UPDATE, mUpdateInfo.writeToString());
		edit.putBoolean(Constants.EXTRA_UPDATE_FROM_HOME, mFromHome);
		edit.commit();
		super.onSaveInstanceState(outState);
	}

	private void updateProgressBar() {
		// needed by new API
		requestNewCursor();

		if (mCursor != null && !mCursor.isClosed() && mCursor.getCount() == 1) {
			mCursor.moveToFirst();

			int status = mCursor.getInt(mStatusColumnIndex);
			long totalBytes = mCursor.getLong(mTotalBytesColumnIndex);

			long timestamp = mCursor.getLong(mLastTimeStampIndex);

			if (status == DownloadManager.STATUS_RUNNING) {
				long bytesSoFar = mCursor.getLong(mBytesSoFarColumnIndex);

				if (mWatchdogEnabled) {
					mLastObserverdDataTransfer = bytesSoFar;
					mLastObservedTimeMillis = System.currentTimeMillis();
				}

				if (totalBytes <= 0 || bytesSoFar < 0) {
					mProgressBar.setIndeterminate(true);
				} else {
					mProgressBar.setIndeterminate(false);
					mProgressBar.setMax((int) totalBytes);
					mProgressBar.setProgress((int) bytesSoFar);

					int mb = (int) (totalBytes / 1024 / 1024);
					int perc = (int) (100 * (float) bytesSoFar / (float) totalBytes);

					long currentTimestamp = System.currentTimeMillis();

					long timeSoFar = currentTimestamp - timestamp;

					long timeToEnd = (long) ((100 * (float) timeSoFar) / (float) perc);

					long remaningTime = timeToEnd - timeSoFar;
					long secondsToEnd = remaningTime / (1000);

					Util.log("Time so far " + timeSoFar);
					Util.log("remainging time " + remaningTime);
					Util.log("MB " + mb);
					Util.log("Percentae " + perc);

					String progressText = "Downloading " + mb + "MB (" + perc + "%)\n";

					long minutesToEnd = (secondsToEnd / 60);

					if (minutesToEnd > 1 && minutesToEnd < 200000) {
						progressText = progressText + " " + minutesToEnd + " minutes left";
					} else if (minutesToEnd == 1) {
						progressText = progressText + " " + minutesToEnd + " minute left";
					} else if (secondsToEnd == 1) {
						progressText = progressText + " " + secondsToEnd + " second left";
					} else if (secondsToEnd > 0 && secondsToEnd < 200000 * 6) {
						progressText = progressText + " " + secondsToEnd + " seconds left";
					}

					mProgressText.setText(progressText);

				}
			} else {

				Util.log("STATUS:REASON " + status + ":" + mCursor.getInt(mReasonColumnIndex));

				switch (status) {

				// the download has failed (and will not be retried)
				case DownloadManager.STATUS_FAILED:

					manageError(mCursor.getInt(mReasonColumnIndex));

					break;
				// the download is waiting to retry or resume.
				case DownloadManager.STATUS_PAUSED:

					manageError(mCursor.getInt(mReasonColumnIndex));

					break;
				// the download is waiting to start
				case DownloadManager.STATUS_PENDING:
					break;
				// the download has successfully completed.
				case DownloadManager.STATUS_SUCCESSFUL:
					// redundant because managed by
					// DownloadCompletedReceiver
					break;

				default:
					break;

				}

			}

		} else {
			Util.log("Download cursor not available - ERROR");
		}
	}

	@Override
	protected void onStop() {

		Util.log("on Stop");
		
		
		if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
			try {
				if (mCursor != null) {
					mCursor.unregisterContentObserver(mDownloadContentObserver);
					mCursor.close();
				}
			} catch (Exception e) {
				Util.log(e.getMessage());
			}

			stopWatchdog();
		}



		super.onStop();

	}

	@Override
	protected void onRestart() {
		Util.log("onRestart RequestID " + mRequestID);
		long savedId = mShared.getLong(Constants.SHARED_DOWNLOAD_ID, -1);
		if(savedId==mRequestID && mRequestID==mPreviusID){
			requestNewCursor();

			startWatchdog();			
		}

		super.onRestart();
	}

	@Override
	protected void onStart() {

		mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_CLIENT_DOWNLOADER).commit();

		super.onStart();
	}



	@Override
	protected void onPause() {
		super.onPause();
		mPreviusID = mRequestID;
		TrackingHelper.stopActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		TrackingHelper.startActivity(this);
	}

	private void manageError(int reason) {

		String titleAlert = "";
		String messageAlert = "";

		switch (reason) {
		case DownloadManager.ERROR_UNKNOWN:
			Util.log("reason ERROR_UNKNOWN");
			// to verify - unrecoverable?
			titleAlert = getString(R.string.label_download_unknown);
			messageAlert = getString(R.string.label_text_download_unknown);
			break;
		// requested destination file already exists (the download manager will
		// not overwrite an existing file)
		case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
			Util.log("reason ERROR_FILE_ALREADY_EXISTS");
			// to verify - unrecoverable?
			titleAlert = getString(R.string.label_download_file_exist);
			messageAlert = getString(R.string.label_text_download_file_exist);

			break;
		// no external storage device was found. Typically, this is because the
		// SD card is not mounted.
		case DownloadManager.ERROR_DEVICE_NOT_FOUND:
			Util.log("reason ERROR_DEVICE_NOT_FOUND");
			titleAlert = getString(R.string.label_download_no_found);
			messageAlert = getString(R.string.label_text_download_no_found);

			break;
		// there was insufficient storage space. Typically, this is because the
		// SD card is full.
		case DownloadManager.ERROR_INSUFFICIENT_SPACE:
			Util.log("reason ERROR_INSUFFICIENT_SPACE");
			// to verify - unrecoverable?
			titleAlert = getString(R.string.label_download_no_space);
			messageAlert = getString(R.string.label_text_download_no_space);

			Util.log("action CLEANING_CACHE");
			Util.trimCache(Environment.getDownloadCacheDirectory());
			break;
		// an error receiving or processing data occurred at the HTTP level.
		case DownloadManager.ERROR_HTTP_DATA_ERROR:
			Util.log("reason ERROR_HTTP_DATA_ERROR");
			titleAlert = getString(R.string.label_download_stop);
			messageAlert = getString(R.string.label_text_download_stop);
			break;
		// an HTTP code was received that download manager can't handle.
		case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
			Util.log("reason ERROR_UNHANDLED_HTTP_CODE");
			titleAlert = getString(R.string.label_download_stop);
			messageAlert = getString(R.string.label_text_download_stop);
			break;
		// there were too many redirects
		case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
			Util.log("reason ERROR_TOO_MANY_REDIRECTS");
			titleAlert = getString(R.string.label_download_stop);
			messageAlert = getString(R.string.label_text_download_stop);
			break;
		// the download is waiting for network connectivity to proceed.
		case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
			Util.log("reason PAUSED_WAITING_FOR_NETWORK");
			// verified - recoverable - achieved i.e. setting airplane mode or
			// rebooting during download
			// manageRecoverableError();
			titleAlert = getString(R.string.label_download_pause);
			messageAlert = getString(R.string.label_text_download_pause);
			break;
		// the download exceeds a size limit for downloads over the mobile
		// network and the download manager is waiting for a Wi-Fi connection to
		// proceed.
		case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
			Util.log("reason PAUSED_QUEUED_FOR_WIFI");
			titleAlert = getString(R.string.label_download_pause);
			messageAlert = getString(R.string.label_text_download_pause);
			break;
		// the download is paused because some network error occurred and the
		// download manager is waiting before retrying the request
		case DownloadManager.PAUSED_WAITING_TO_RETRY:
			Util.log("reason PAUSED_WAITING_TO_RETRY");
			// verified - recoverable - achieved with a connection lost i.e.
			// turning off hotspot
			titleAlert = getString(R.string.label_download_stop);
			messageAlert = getString(R.string.label_text_download_stop);
			break;
		// the download is paused for some other reason
		case DownloadManager.PAUSED_UNKNOWN:
			Util.log("reason PAUSED_UNKNOWN");
			titleAlert = getString(R.string.label_download_pause);
			messageAlert = getString(R.string.label_text_download_pause);
			break;
		// some possibly transient error occurred but we can't resume the
		// download
		case DownloadManager.ERROR_CANNOT_RESUME:
			Util.log("reason ERROR_CANNOT_RESUME");
			// to verify - unrecoverable?
			titleAlert = getString(R.string.label_download_error_resume);
			messageAlert = getString(R.string.label_text_download_error_resume);
			break;
		case 404:
			// verified - unrecoverable
			Util.log("reason ERROR 404 NOT FOUND");
			titleAlert = getString(R.string.label_download_404);
			messageAlert = getString(R.string.label_text_download_404);
			break;
		default:
			Util.log("Unknown reason code=" + reason);
			titleAlert = getString(R.string.label_download_unknown);
			messageAlert = getString(R.string.label_text_download_unknown);
			break;
		}

		TrackingHelper.trackUpdateErrors(getApplicationContext(), TrackingHelper.TRACKING_UPDATE_DOWNLOAD_ERROR_PAGE,
				messageAlert);

		Util.log("SHOW ERROR page _ DOWNLOAD MANAGER - just if there isan update avilable");

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo infoNetwork = cm.getActiveNetworkInfo();
		if (!isFromHome() || (infoNetwork != null && infoNetwork.isConnectedOrConnecting())) {

			mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();

			setupFailDownloadUi();
			
			long savedId = mShared.getLong(Constants.SHARED_DOWNLOAD_ID, -1);
			if(savedId!=-1){
				mDownloadManager.remove(savedId); 
				mShared.edit().remove(Constants.SHARED_DOWNLOAD_ID).commit();				
			}

			mRequestID=0;
			//TIP-1241
			
			stopWatchdog();
		} else {
			Util.log("Unexpected ConnectionManager status!");
		}

	}

	private void lockFile(String filepath) {
		File file = new File(filepath);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			long totalBytes = mCursor.getLong(mTotalBytesColumnIndex);
			fis.getChannel().lock(0, totalBytes, true);
		} catch (FileNotFoundException e) {

			Util.log(e);
			// e.printStackTrace();
		} catch (IOException e) {

			Util.log(e);
		}
	}

	private void handleAction(Intent intent, boolean isNewIntent) {

		String action = intent.getAction();

		Util.log("handle ACTION - " + action);

		long savedId = mShared.getLong(Constants.SHARED_DOWNLOAD_ID, -1);

		if (!isNewIntent) {
			mFromHome = mShared.getBoolean(Constants.EXTRA_UPDATE_FROM_HOME, false);
			
			mUpdateInfo = new UpdateInfo(mShared.getString(Constants.SHARED_INFO_UPDATE, null));

		}

        if (mUpdateInfo != null && !mUpdateInfo.isMandatory()) {
        	mButtonsContainer.setVisibility(View.VISIBLE);
        } else {
        	mButtonsContainer.setVisibility(View.GONE);
        } 

		if(action==null){
			Util.log("Saved ID " + savedId + "\n requestId " + mRequestID);

			Util.log("Update info for retry " + mUpdateInfo.writeToString());

			mUpdateInfo = new UpdateInfo(mShared.getString(Constants.SHARED_INFO_UPDATE, null));

			if (savedId != -1) {
				mDownloadManager.remove(savedId);
			}
			if(mRequestID != -1){
				mDownloadManager.remove(mRequestID);
			}
			mShared.edit().putLong(Constants.SHARED_DOWNLOAD_ID,-1).commit();
			mRequestID = 0;
			downloadFile();
		}
		else if (action.equals(getString(R.string.intent_download_complete))) {

			Util.log("DOWNLOAD complete+ intentRequestID =" + mRequestID + "saveRequestId = " + savedId);
			if (savedId == mRequestID) {

				stopWatchdog();

				requestNewCursor();

				if (mCursor.moveToFirst()) {
					int status = mCursor.getInt(mStatusColumnIndex);

					Util.log("STATUS " + status);
					switch (status) {

					case DownloadManager.STATUS_SUCCESSFUL:
						Util.log("STATUS is successfull");
						mUpdateLocalFile = mCursor.getString(mFilenameColumnIndex);
						// Lock file
						lockFile(mUpdateLocalFile);

						mShared.edit().putString(Constants.SHARED_LOCATION_UPDATE, mUpdateLocalFile).commit();
						
						Intent i = new Intent(this, VerifyOtaActivity.class);
						i.putExtra(Constants.EXTRA_UPDATE_FILE_PATH, mUpdateLocalFile);
						i.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, mFromHome);
						i.putExtra(Constants.EXTRA_UPDATE_INFO, mUpdateInfo);
						i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivity(i);
						overridePendingTransition(0, 0);
						finish();
						overridePendingTransition(0, 0);

						break;
					case DownloadManager.STATUS_FAILED:
						Util.log("STATUS is FAILED");
						downloadFile();
						break;
					case DownloadManager.STATUS_PAUSED:
						Util.log("STATUS is PAUSED");
						downloadFile();
						break;
					case DownloadManager.STATUS_PENDING:
						Util.log("STATUS is PENDING");
						downloadFile();
						break;
					case DownloadManager.STATUS_RUNNING:
						Util.log("STATUS is RUNNING");
						// downloadFile();
						break;
					default:
						Util.log("STATUS is not successfull");
						break;
					}
				}
			}

		} else if (action.equals(getString(R.string.intent_download_click))) {

			if (savedId == mRequestID) {

				requestNewCursor();
				int status;

				try {
					status = mCursor.getInt(mStatusColumnIndex);
					Util.log("STATUS " + status);

					switch (status) {

					case DownloadManager.STATUS_SUCCESSFUL:
						Util.log("STATUS is successfull");
						mUpdateLocalFile = mCursor.getString(mFilenameColumnIndex);

						mShared.edit().putString(Constants.SHARED_LOCATION_UPDATE, mUpdateLocalFile).commit();

						// Lock file
						lockFile(mUpdateLocalFile);

						Intent i = new Intent(this, VerifyOtaActivity.class);
						i.putExtra(Constants.EXTRA_UPDATE_FILE_PATH, mUpdateLocalFile);
						i.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, mFromHome);
						i.putExtra(Constants.EXTRA_UPDATE_INFO, mUpdateInfo);
						i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivity(i);
						overridePendingTransition(0, 0);
						break;
					case DownloadManager.STATUS_PAUSED:
						startWatchdog();
						break;
					}
				} catch (android.database.CursorIndexOutOfBoundsException e) {

					// The item looks screwed up
					Util.log(e);

					// retry to download again
					// downloadFile();
					// Cleanup the status
					mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();

					goToStatusUpdateActivity();

				} catch (NullPointerException e) {

					if (mCursor == null) {
						// Cleanup the status
						mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE).commit();

						goToStatusUpdateActivity();
					}

				}

			}

		} else if (action.equals(getString(R.string.intent_download_retry))) {

			Util.log("Saved ID " + savedId + "\n requestId " + mRequestID);

			Util.log("Update info for retry " + mUpdateInfo.writeToString());

			if (savedId == mRequestID) {

				if (savedId != -1) {
					mDownloadManager.remove(savedId);
				}
				mRequestID = 0;
				downloadFile();

			}

		}

	}

	private void showInfoUpdate() {

		Intent intent = new Intent(this, InfoUpdateActivity.class);

		intent.putExtra(Constants.EXTRA_UPDATE_INFO, mUpdateInfo);
		intent.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, isFromHome());

		Util.log(" IS FROM HOME " + isFromHome());

		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

		startActivity(intent);
		overridePendingTransition(0, 0);

		finish();
		overridePendingTransition(0, 0);

		if (!isFromHome()) {
			showNotificationDownloadAvailable();
		}
	}

	// GDX:watchdog
	private void startWatchdog() {
		// if (mFromHome && mWatchdogEnabled) {
		// MPV - TIP-1097
		if (mWatchdogEnabled) {

			Util.log("Starting Watchdog");

			mTimer = new Timer();

			mWatchdogTimerTask = new TimerTask() {
				private long mLastKnownDataTransfer = 0;
				private long mLastKnownDataTransferMillis = 0;

				@Override
				public void run() {
					Util.log("Watchdog task run");

					if (mLastKnownDataTransfer != mLastObserverdDataTransfer) {
						// sono stati trasferiti dati
						mLastKnownDataTransfer = mLastObserverdDataTransfer;
						mLastKnownDataTransferMillis = mLastObservedTimeMillis;

						Util.log("Watchdog task: data transferred");
					} else if (System.currentTimeMillis() > mLastKnownDataTransferMillis + Constants.WATCHDOG_THRESHOLD) {
						Util.log("Watchdog task: no data transferred in 20 seconds");

						runOnUiThread(new Runnable() {
							public void run() {
								manageError(DownloadManager.ERROR_UNKNOWN);
							};
						});

						try {
							cancel();
						} catch (Exception e) {
							Util.log(e);
						}
					}
				}
			};

			mTimer.scheduleAtFixedRate(mWatchdogTimerTask, 5000, 20000);
		}
	}

	private void stopWatchdog() {
		// if (mFromHome && mWatchdogEnabled && mTimer != null) {
		if (mWatchdogEnabled && mTimer != null) {
			Util.log("Stopping Watchdog");
			try {
				mTimer.cancel();
			} catch (Exception e) {
				Util.log(e);
			} finally {
				mTimer = null;
			}
		}
	}

	private void goToStatusUpdateActivity() {

		finish();
		Intent intent = new Intent(this, StatusUpdateActivity.class);
		startActivity(intent);

	}
}
