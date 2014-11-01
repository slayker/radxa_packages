package hudl.ota.update;

import hudl.ota.Constants;
import hudl.ota.util.Util;

import java.util.Date;


import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		long lastCheck = context.getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS)
				.getLong(Constants.SHARED_LAST_CHECK_TIMESTAMP, 0);

		long now = System.currentTimeMillis();

		if (now > lastCheck + Constants.CHECK_INTERVAL + getRandomOffset()){
			
			Util.log("OTA " + "ALARM! " + new Date());

			Intent i = new Intent(context, CheckingUpdateService.class);
			context.startService(i);
		}
	}
	
	private long getRandomOffset() {
		return ((int) (Math.random() * 5)) * AlarmManager.INTERVAL_HALF_HOUR;
	}

}
