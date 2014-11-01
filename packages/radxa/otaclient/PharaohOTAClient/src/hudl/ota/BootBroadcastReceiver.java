package hudl.ota;

import hudl.ota.model.UpdateCheck;
import hudl.ota.model.UpdateInfo;
import hudl.ota.network.NetworkService;
import hudl.ota.update.AlarmReceiver;
import hudl.ota.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

	private boolean mUpdateDetectedBoot = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			
			if (UserHandle.myUserId() == UserHandle.USER_OWNER){
				unlockFile(Constants.CACHE_TARGET);

				checkUpdated(context);

				setupAlarm(context, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR);

				cleanupOTAshared(context);
			}
			else{
				checkUpdated(context);

				setupAlarm(context, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR);
			}
		}
	}

	private void unlockFile(String filepath) {
		File file = new File(filepath);
		if (file.exists()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				fis.getChannel().lock(0, file.length(), true).release();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void cleanupOTAshared(Context context) {

		SharedPreferences shared;
		SharedPreferences.Editor editor;
		DownloadManager managerDownload;

		// When the shutdown is running, clean all OTA data stored

		shared = context.getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS);
		managerDownload = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

		// Removed file downloaded
		long idDownload = shared.getLong(Constants.SHARED_DOWNLOAD_ID, -1);
		if (idDownload != -1) {

			Util.log(" Remove download before shutting down the device");
			managerDownload.remove(idDownload);

		}

		Util.log("CLEAN up the whole variables");
		// Cleanup of the whole variable
		editor = shared.edit();
		editor.putLong(Constants.SHARED_DOWNLOAD_ID, -1);
		editor.putString(Constants.SHARED_LOCATION_UPDATE, null);
		editor.putBoolean(Constants.SHARED_ACTIVE_NEW_DOWNLOAD_NOTIFICATION, true).commit();

		// MPV - TIP-1087 (If there is any update available, detected on boot,
		// we push the notification and don't clear the related shpref)
		if (!mUpdateDetectedBoot) {
			editor.putString(Constants.SHARED_INFO_UPDATE, null);

			editor.putString(Constants.SHARED_STATUS_OTA_UPDATE, null);

			editor.putInt(Constants.SHARED_STATUS_OTA_UPDATE, 1);
			editor.putBoolean(Constants.SHARED_ACTIVE_NEW_DOWNLOAD_NOTIFICATION, false).commit();
		}

		editor.commit();

		mUpdateDetectedBoot = false;

	}

	private void setupAlarm(Context context, long when) {
		Intent intentTolaunch = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 234324243, intentTolaunch, 0);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC, when, Constants.ALARM_INTERVAL, pendingIntent);
	}

	private void checkUpdated(final Context context) {
		String currentVersion = Build.VERSION.INCREMENTAL;

		SharedPreferences prefs = context.getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS);

		String lastKnownVersion = prefs.getString(Constants.SHARED_LAST_KNOWN_VERSION_NUMBER, null);

		if (lastKnownVersion != null && !lastKnownVersion.equalsIgnoreCase(currentVersion)) {

			// show notification...

			// MPV - TIP-1082
			prefs.edit().putLong(Constants.SHARED_LAST_UPDATED_TIME, System.currentTimeMillis()).commit();

		}

		prefs.edit().putString(Constants.SHARED_LAST_KNOWN_VERSION_NUMBER, currentVersion).commit();
	}

}
