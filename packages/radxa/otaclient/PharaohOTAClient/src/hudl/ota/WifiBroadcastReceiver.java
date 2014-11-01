package hudl.ota;

import hudl.ota.util.Util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

/*
 * Broadcast receiver which check the wifi connection.
 * If the wifi connection is lost, Setting wifi activity will be on the screen.
 * 
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {

	private boolean isFirstNotification = true;

	@Override
	public void onReceive(Context context, Intent intent) {

		Util.log("Wifi connection changed ");

		// If the connection is lost, we need to show the wifi
		// setting menu

		String extraInfo = intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO);
		String extraREason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
		String extraFailor = intent.getStringExtra(ConnectivityManager.EXTRA_IS_FAILOVER);

		Util.log(" EXTRA INFO " + extraInfo);
		Util.log(" EXTRA REASON " + extraREason);
		Util.log(" EXTRA FAILOVER" + extraFailor);

		if (!isFirstNotification && intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {

			Util.log("Enable WIFI");

			Intent wifiIntent = new Intent(context, LossWifiIntoActivity.class);
			wifiIntent.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, true);
			wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
			context.startActivity(wifiIntent);

		} else {

			isFirstNotification = false;
		}
	}

}
