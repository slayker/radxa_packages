package hudl.ota;

import hudl.ota.util.Util;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class FirstJourneyOTAClient extends OTAActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		hideSystemBar();

		checkUpdated(this);
	}

	// FLOW Version 3.3

	private void checkUpdated(Context context) {

		String currentVersion = Build.VERSION.INCREMENTAL;

		SharedPreferences prefs = context.getSharedPreferences(Constants.TAG, Context.MODE_MULTI_PROCESS);

		String lastKnownVersion = prefs.getString(Constants.SHARED_LAST_KNOWN_VERSION_NUMBER, null);

		if (lastKnownVersion != null && !lastKnownVersion.equalsIgnoreCase(currentVersion)) {

			// MPV - TIP-1082
			prefs.edit().putLong(Constants.SHARED_LAST_UPDATED_TIME, System.currentTimeMillis()).commit();
			// Skip OTA flow
			skip();

		} else {

			// Go forward OTA flow

			Intent intent = new Intent(this, OTAClientChecker.class);
			intent.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, true);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
			overridePendingTransition(0, 0);

		}
		prefs.edit().putString(Constants.SHARED_LAST_KNOWN_VERSION_NUMBER, currentVersion).commit();

	}

	@Override
	protected void skip() {

		PackageManager pm = getPackageManager();
		ComponentName name = new ComponentName(this, FirstJourneyOTAClient.class);
		pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);

		Intent i = new Intent(Intent.ACTION_MAIN, null);
		i.addCategory(Intent.CATEGORY_HOME);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(i);
		overridePendingTransition(0, 0);

		Util.log("Status bar manager disable");
		finish();
		overridePendingTransition(0, 0);

	}
}
