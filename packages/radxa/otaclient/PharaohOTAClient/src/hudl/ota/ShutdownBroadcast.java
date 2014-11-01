package hudl.ota;

import hudl.ota.util.Util;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;

public class ShutdownBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {

		SharedPreferences shared = context.getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS);

		DownloadManager managerDownload = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

		// Removed file downloaded
		long idDownload = shared.getLong(Constants.SHARED_DOWNLOAD_ID, -1);
		if (idDownload != -1) {

			Util.log(" Remove download before shutting down the device");
			managerDownload.remove(idDownload);
			shared.edit().remove(Constants.SHARED_DOWNLOAD_ID).commit();
			NotificationManager nm = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

			nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_RUNNING, UserHandle.ALL);

		}

	}

}
