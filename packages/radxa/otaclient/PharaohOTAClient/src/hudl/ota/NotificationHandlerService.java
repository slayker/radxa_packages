package hudl.ota;

import hudl.ota.util.Util;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;

/**
 * Under development
 * 
 * @author matteobonifazi
 * 
 */
public class NotificationHandlerService extends Service {

	private DownloadManager mDownloadManager;

	private long mRequestID;
	private Cursor mCursor;
	private DownloadContentObserver mDownloadContentObserver;
	private int mStatusColumnIndex = 0;
	private int mReasonColumnIndex = 0;
	private int mBytesSoFarColumnIndex = 0;
	private int mTotalBytesColumnIndex = 0;
	private int mFilenameColumnIndex = 0;
	private int mLastTimeStampIndex = 0;

	private class DownloadContentObserver extends ContentObserver {
		public DownloadContentObserver() {
			super(new Handler());
			return;
		}

		public void onChange(boolean selfChange) {
			updateNotification();
		}

	}

	private void updateNotification() {

		requestNewCursor();

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder mNotifyBuilder = new Notification.Builder(this);
		mNotifyBuilder.setContentTitle(getString(R.string.label_notification_download_running_title));
		mNotifyBuilder.setSmallIcon(R.drawable.hudl_notification_small);
		mNotifyBuilder.setOngoing(true);

		int status = mCursor.getInt(mStatusColumnIndex);
		long totalBytes = mCursor.getLong(mTotalBytesColumnIndex);
		long reason = mCursor.getLong(mBytesSoFarColumnIndex);
		long bytesSoFar = mCursor.getLong(mBytesSoFarColumnIndex);
		String fileName = mCursor.getString(mFilenameColumnIndex);
		long timestamp = mCursor.getLong(mLastTimeStampIndex);

		Util.log("TIMESTAMP " + timestamp + " TOTAL BYTES " + totalBytes + " STATUS " + status);

		Util.log("SERVICE - Column STATUS " + mStatusColumnIndex + " value " + status);
		Util.log("SERVICE - Column Reason " + mReasonColumnIndex + " value " + reason);
		Util.log("SERVICE - Column ByteSoFar " + mBytesSoFarColumnIndex + " value " + bytesSoFar);
		Util.log("SERVICE - Column TotalByte " + mTotalBytesColumnIndex + " value " + totalBytes);
		Util.log("SERVICE - Column Filename  " + mFilenameColumnIndex + " value " + fileName);
		Util.log("SERVICE - Column LastTime " + mLastTimeStampIndex + " value " + timestamp);

		if (status == DownloadManager.STATUS_RUNNING) {

			if (totalBytes <= 0 || bytesSoFar < 0) {

				Util.log("Notification Not update from service - Infinity ");

				mNotifyBuilder.setProgress((int) totalBytes, 0, true);

				mNotifyBuilder.setContentText(getString(R.string.label_notification_download_starting));

			} else {

				Util.log("Notification update from service ");

				mNotifyBuilder.setProgress((int) totalBytes, (int) bytesSoFar, false);

				int mb = (int) (totalBytes / 1024 / 1024);
				int perc = (int) (100 * (float) bytesSoFar / (float) totalBytes);

				long currentTimestamp = System.currentTimeMillis();

				long timeSoFar = currentTimestamp - timestamp;

				long timeToEnd = (long) ((100 * (float) timeSoFar) / (float) perc);

				long remaningTime = timeToEnd - timeSoFar;
				long secondsToEnd = (int) (remaningTime / (1000));

				Util.log("Time so far " + timeSoFar);
				Util.log("remainging time " + remaningTime);
				Util.log("MB " + mb);
				Util.log("Percentae " + perc);

				String progressText = "Downloading " + mb + "MB (" + perc + "%) \n";

				if (secondsToEnd > 0) {
					progressText = progressText + "Remaing time " + secondsToEnd + " seconds";
				}

				mNotifyBuilder.setContentText(progressText);

			}
		}

		Util.log(" SHOW NOTIFICATION from service");

		// mNotificationManager.notify(Constants.NOTIFICATION_DOWNLOAD_RUNNING,
		// mNotifyBuilder.build());
		mNotificationManager.notifyAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, mNotifyBuilder.build(),
				UserHandle.ALL);
		
		mNotificationManager.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID, UserHandle.ALL);
		mNotificationManager.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, UserHandle.ALL);
		mNotificationManager.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE, UserHandle.ALL);
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public void onCreate() {

		mDownloadContentObserver = new DownloadContentObserver();
		mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		mRequestID = intent.getLongExtra(Constants.EXTRA_DOWNLOAD_ID, -1);

		Util.log(" REQUESTID " + mRequestID);

		requestNewCursor();

		return super.onStartCommand(intent, flags, startId);
	}

	private void requestNewCursor() {
		if (mCursor != null) {
			mCursor.unregisterContentObserver(mDownloadContentObserver);
			mCursor.close();

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
	public void onDestroy() {

		if (mCursor != null) {
			mCursor.unregisterContentObserver(mDownloadContentObserver);
			mCursor.close();

			mCursor = null;

		}
		super.onDestroy();
	}

}
