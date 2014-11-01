package hudl.ota.update;

import hudl.ota.Constants;
import hudl.ota.OTAClientDownloader;
import hudl.ota.R;
import hudl.ota.util.Util;

import java.util.List;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.UserHandle;

public class DownloadUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		long savedId = context.getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS).getLong(
				Constants.SHARED_DOWNLOAD_ID, -1);

		Util.log("ACTION " + action + " savedID " + savedId);

		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

			Util.log("Download activity " + downloadId + " savedID " + savedId);

			if ((downloadId == savedId) && isStateSuccessfull(context, downloadId)) {

				// Check the status of the request...

				ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				// get the info from the currently running task
				List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

				Util.log("CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());

				ComponentName componentInfo = taskInfo.get(0).topActivity;
				String classname = componentInfo.getClassName();

				Intent intentDownloadComplete = new Intent(context, OTAClientDownloader.class);
				intentDownloadComplete.setAction(context.getString(R.string.intent_download_complete));
				intentDownloadComplete.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
				intentDownloadComplete.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				if (classname.equalsIgnoreCase("hudl.ota.OTAClientDownloader")) {
					context.startActivity(intentDownloadComplete);
				} else {
					// SHOW NOTIFICATION
					NotificationManager nm = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					Notification.Builder mBuilder = new Notification.Builder(context);
					mBuilder.setSmallIcon(R.drawable.hudl_notification_small);
					mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
							R.drawable.hudl_notification_large));
					mBuilder.setContentTitle(context.getString(R.string.hudl_update_title));
					mBuilder.setContentText(context.getString(R.string.hudl_update_download_complete));
					mBuilder.setOngoing(true);
					// mBuilder.setAutoCancel(true);
					mBuilder.setPriority(Notification.PRIORITY_HIGH);
					PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, intentDownloadComplete, 0,null,new UserHandle(UserHandle.USER_CURRENT));

					mBuilder.setContentIntent(pendingIntent);

					nm.notifyAsUser(null, Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID, mBuilder.build(),
							UserHandle.ALL);
					//FIX TIP-1162
					nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, UserHandle.ALL);
					nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_READY_TO_INSTALL, UserHandle.ALL);
					nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE, UserHandle.ALL);
					// nm.notify(Constants.NOTIFICATION_DOWNLOAD_COMPLETE_ID,
					// mBuilder.build());
					// nm.cancel(Constants.NOTIFICATION_DOWNLOAD_RUNNING);

				}
			}

		} else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {

			long[] arrayDownloadID = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
			boolean isFound = false;

			for (int i = 0; i < arrayDownloadID.length && !isFound; i++) {

				Util.log("Download id " + arrayDownloadID[i]);

				isFound = (arrayDownloadID[i] == savedId);
			}

			if (isFound) {
				// OPEN the right activity
				Intent intentNewActivity = new Intent(context, OTAClientDownloader.class);
				intentNewActivity.setAction(context.getString(R.string.intent_download_click));
				intentNewActivity.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, savedId);
				intentNewActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentNewActivity);
			} else {

				Util.log("Download not found ");
			}
		}

	}

	private boolean isStateSuccessfull(Context context, long id) {

		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));

		boolean isSuccessFull = false;

		if (cursor != null && !cursor.isClosed() && cursor.getCount() == 1) {
			cursor.moveToFirst();

			int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));

			isSuccessFull = (status == DownloadManager.STATUS_SUCCESSFUL);

		}

		return isSuccessFull;

	}

}
