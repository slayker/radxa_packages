package hudl.ota;

import hudl.ota.util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.app.StatusBarManager;

@SuppressLint(value = { "all" })
public abstract class OTAActivity extends Activity {
	protected static final String TAG = "PharaohOTAAgent";

	protected static final String KEY_REQUEST_ID = "com.tesco.ota.key";

	public static final int STATUS_DISABLE_NONE = 0x00000000;

	protected static final int STATUS_BAR_DISABLE_BACK = 0x00400000;
	protected static final int STATUS_BAR_DISABLE_EXPAND = 0x00010000;
	protected static final int STATUS_BAR_DISABLE_NOTIFICATION_ICONS = 0x00020000;
	protected static final int STATUS_BAR_DISABLE_NOTIFICATION_ALERTS = 0x00040000;
	protected static final int STATUS_BAR_DISABLE_SYSTEM_INFO = 0x00100000;
	protected static final int STATUS_BAR_DISABLE_HOME = 0x00200000;
	protected static final int STATUS_BAR_DISABLE_RECENT = 0x01000000;
	protected static final int STATUS_BAR_DISABLE_SEARCH = 0x02000000;

	private PowerManager.WakeLock wl = null;

	protected SharedPreferences mShared;

	protected WifiBroadcastReceiver mWifiReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		super.onCreate(savedInstanceState);
		
		if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
			Toast.makeText(this, R.string.toast_notification_only_owner, Toast.LENGTH_LONG).show();
			overridePendingTransition(0, 0);
			finish();
			overridePendingTransition(0, 0);
			return;
		}
		
		mShared = getSharedPreferences(Constants.TAG, MODE_MULTI_PROCESS);

		Util.log("on Create");

	}

	@Override
	protected void onStart() {

		Util.log("On Start ");
		super.onStart();

		// If we come from home,
		// we have to hide the status bar manager

		if (isFromHome()) {

			// disableSystemBar();

			mWifiReceiver = new WifiBroadcastReceiver();
			IntentFilter filter = new IntentFilter();
			// filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

			registerReceiver(mWifiReceiver, filter);

			Util.log(" wifi receviver registered");
		}

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
		wl.acquire();
	}

	@Override
	protected void onStop() {

		// enableSystemBar();
		if(	UserHandle.myUserId() == UserHandle.USER_OWNER){

			if (isFromHome() && mWifiReceiver != null) {

				unregisterReceiver(mWifiReceiver);
				mWifiReceiver = null;

				Util.log(" wifi receviver unregistered");

			}

			wl.release();
		}
		Util.log("on Stop");

		super.onStop();

	}

	protected boolean isFromHome() {
		return getIntent().getBooleanExtra(Constants.EXTRA_UPDATE_FROM_HOME, false);
	}

	protected void skipAndDisable() {
		PackageManager pm = getPackageManager();
		ComponentName name = new ComponentName(this, FirstJourneyOTAClient.class);
		pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

		Intent i = new Intent(Intent.ACTION_MAIN, null);
		i.addCategory(Intent.CATEGORY_HOME);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(i);
		overridePendingTransition(0, 0);

		// enableSystemBar();

		Util.log("Status bar manager disable");
		finish();
		overridePendingTransition(0, 0);

	}

	protected void skip() {
		if (isFromHome()) {

			PackageManager pm = getPackageManager();
			ComponentName name = new ComponentName(this, FirstJourneyOTAClient.class);
			pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);

			Intent i = new Intent(Intent.ACTION_MAIN, null);
			i.addCategory(Intent.CATEGORY_HOME);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(i);
			overridePendingTransition(0, 0);

			// enableSystemBar();

			Util.log("Status bar manager disable");
		}

		finish();
		overridePendingTransition(0, 0);

	}

	protected Bitmap getBitmap(int id) {
		return BitmapFactory.decodeResource(this.getResources(), id);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		LinearLayout root = (LinearLayout) findViewById(R.id.root);
		root.setPadding(getResources().getDimensionPixelSize(R.dimen.activity_margin_left), getResources()
				.getDimensionPixelSize(R.dimen.activity_margin_top),
				getResources().getDimensionPixelSize(R.dimen.activity_margin_right), getResources()
						.getDimensionPixelSize(R.dimen.activity_margin_bottom));

		for (int i = 1; i <= 4; i++) {
			Button button = (Button) root.findViewWithTag("button_" + i);
			if (button != null) {
				if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					button.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
							R.dimen.button_width), getResources().getDimensionPixelSize(R.dimen.button_height)));
				} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
					button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
							getResources().getDimensionPixelSize(R.dimen.button_height)));
				}
			}
		}

	}

	protected void hideSystemBar() {
		Intent intent = new Intent("hide");
		intent.setComponent(new ComponentName("hudl.setup", "hudl.setup.HudlSetupService"));
		startService(intent);

	}

}
