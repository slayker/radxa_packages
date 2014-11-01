package hudl.ota.update;

import hudl.ota.Constants;
import hudl.ota.InfoUpdateActivity;
import hudl.ota.R;
import hudl.ota.model.UpdateCheck;
import hudl.ota.model.UpdateInfo;
import hudl.ota.network.NetworkService;
import hudl.ota.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;

public class CheckingUpdateService extends IntentService {

	public CheckingUpdateService() {
		super("hudl.ota.CheckingUpdateService");
	}

	public CheckingUpdateService(String name) {
		super("hudl.ota.CheckingUpdateService");

	}

	// Check if there is already a OTA running
	private boolean isOtaRunningByUser() {

		ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);
		boolean isServiceFound = false;
		for (int i = 0; i < services.size(); i++) {
			if (services.get(i).topActivity.getPackageName().equalsIgnoreCase("hudl.ota")) {
				Util.log("Service Running - " + services.get(i).topActivity.getPackageName());
				isServiceFound = true;
			}
		}
		return isServiceFound;

	}

	private boolean isDownloadRunningByUser() {

		SharedPreferences shared = getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS);
		Util.log("User running one download: "+shared.getLong(Constants.SHARED_DOWNLOAD_ID, -1));

		return (shared.getLong(Constants.SHARED_DOWNLOAD_ID, -1) != -1);

	}

	private boolean isStatusOTAasIdle() {
		SharedPreferences shared = getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS);
		return (shared.getInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_IDLE) == Constants.STATUS.STATUS_IDLE);
	}
	
	private boolean isOwnerUser(){
		Util.log("User Owner?"+(UserHandle.myUserId() == UserHandle.USER_OWNER));
		return UserHandle.myUserId() == UserHandle.USER_OWNER;
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		if(!isOwnerUser()){
			Util.log("DON'T SHOW NOTIFICATION");
			return;
		}
		
		String version;
		Util.log("BUILD VERSION INCREMENTAL " + Build.VERSION.INCREMENTAL);

		version = Build.VERSION.INCREMENTAL;

		Util.log("Version to check " + version);
		UpdateCheck updateCheck;
		
		
		if (Constants.DEBUG) {
			updateCheck = NetworkService.getInstance().getUpdateInformation(version, Constants.URL_AMAZON_STAGING,
					CheckingUpdateService.this, false);
		} else {
			updateCheck = NetworkService.getInstance().getUpdateInformation(version, Constants.URL_AMAZON,
					CheckingUpdateService.this, false);
		}

		if (updateCheck != null) {

			Util.log("UPDATE STATUS "+updateCheck.getStatus());
			if (updateCheck.getStatus().equalsIgnoreCase("OK")) {
				// there is an update

				UpdateInfo updateInfo = updateCheck.getUpdateInfo();

				if (updateInfo != null && updateInfo.isValid()) {

					Util.log("Update Info - new One	");

					Intent i = new Intent(CheckingUpdateService.this, InfoUpdateActivity.class);

					i.putExtra(Constants.EXTRA_UPDATE_INFO, updateInfo);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					if (/* isOtaRunningByUser() || !isDownloadRunningByUser() || */isStatusOTAasIdle()) {

						getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS).edit().putString(Constants.SHARED_INFO_UPDATE, updateInfo.writeToString()).commit();
						getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS).edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE,Constants.STATUS.STATUS_INFO_UPDATE_ACTIVITY).commit();
						showNotification(updateInfo);

						Util.log("OTA is not running - show notification");
					} else {

						Util.log("OTA is already running");

					}

				}
			}

		}
		else {
			Util.log("updateCheck NULL");
		}

	}

	private void showNotification(UpdateInfo updateInfo) {

		// SHOW NOTIFICATION
		NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder mBuilder = new Notification.Builder(this);
		mBuilder.setSmallIcon(R.drawable.hudl_notification_small);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.hudl_notification_large));
		mBuilder.setContentTitle(this.getString(R.string.hudl_update_title));
		mBuilder.setContentText(this.getString(R.string.hudl_update_new_available));
		mBuilder.setOngoing(true);
		Intent intentOTAavailable = new Intent();
		intentOTAavailable.setClass(this, InfoUpdateActivity.class);
		intentOTAavailable.putExtra(Constants.EXTRA_UPDATE_INFO, updateInfo);
		PendingIntent pendingIntent = PendingIntent.getActivityAsUser(this, 0, intentOTAavailable,
				PendingIntent.FLAG_CANCEL_CURRENT, null, new UserHandle(UserHandle.USER_CURRENT));

		mBuilder.setContentIntent(pendingIntent);

		nm.notifyAsUser(null, Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE, mBuilder.build(), UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID, UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, UserHandle.ALL);
		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, UserHandle.ALL);
		// nm.notify(Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE,
		// mBuilder.build());

	}
}
