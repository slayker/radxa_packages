package com.rockchip.settings.wifi;

import com.rockchip.settings.R;
import com.rockchip.settings.ListViewAdapter;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;

//import com.rockchip.settings.WirelessSettings;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;
import android.util.Log;

public class Wifi_ApEnabler {
	private final Context mContext;
	private ListViewAdapter mListViewAdapter = null;
//	private String mCurrentState = null;
	private Handler mHandler;
	private WifiManager mWifiManager;
	private final IntentFilter mIntentFilter;
	private boolean mOpen = false;

	ConnectivityManager mCm;
	private String[] mWifiRegexs;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		    	String action = intent.getAction();
		    	if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
			       handleWifiApStateChanged(intent.getIntExtra(
			              WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
		    	} else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) {
		       	ArrayList<String> available = intent.getStringArrayListExtra(
		                	ConnectivityManager.EXTRA_AVAILABLE_TETHER);
		        	ArrayList<String> active = intent.getStringArrayListExtra(
		                	ConnectivityManager.EXTRA_ACTIVE_TETHER);
		        	ArrayList<String> errored = intent.getStringArrayListExtra(
		                	ConnectivityManager.EXTRA_ERRORED_TETHER);
		        	updateTetherState(available.toArray(), active.toArray(), errored.toArray());
		    	}
			updatamessage();
		}
	};
	private void LOGD(String msg)
	{
		if(true)
			Log.d("Wifi Enabler",msg);
	}

    public Wifi_ApEnabler(Context context, Handler handler,ListViewAdapter adapter) {
        mContext = context;
		mHandler = handler;
        mListViewAdapter = adapter;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiRegexs = mCm.getTetherableWifiRegexs();

		((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.accessibility_service_state_off,-1,-1);
		mOpen = false;
		
        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
        enableWifiCheckBox();
        //mCheckBox.setOnPreferenceChangeListener(this);
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        //mCheckBox.setOnPreferenceChangeListener(null);
    }

    private void enableWifiCheckBox() {
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if(!isAirplaneMode) {
	//		mListViewAdapter.updateStatus(R.string.wifi_tether_checkbox_text,R.string.open,-1);
            //mCheckBox.setEnabled(true);
        } else {
	//		mListViewAdapter.updateStatus(R.string.wifi_tether_checkbox_text,R.string.off,-1);
            //mCheckBox.setEnabled(false);
        }
    }

	public boolean onPreferenceChange() {

		final ContentResolver cr = mContext.getContentResolver();
		boolean enable = !mOpen;

		/**
		 * Disable Wifi if enabling tethering
		 */
		int wifiState = mWifiManager.getWifiState();
		if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
			(wifiState == WifiManager.WIFI_STATE_ENABLED))) {
			mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
		}

		if (mWifiManager.setWifiApEnabled(null, enable)) {
            		/* Disable here, enabled on receiving success broadcast */
            	((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.accessibility_service_state_off,-1,-1);
        	} else {
            	((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.wifi_error,-1,-1);
				updatamessage();
        	}
			

        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }

        return false;
    }

    void updateConfigSummary(WifiConfiguration wifiConfig) {
        String s = mContext.getString(
                com.android.internal.R.string.wifi_tether_configure_ssid_default);
		String summary = String.format(
                    mContext.getString(R.string.wifi_tether_enabled_subtext),
                    (wifiConfig == null) ? s : wifiConfig.SSID);
		((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,summary,null,null);
		updatamessage();
    }

    private void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        boolean wifiTethered = false;
        boolean wifiErrored = false;
		LOGD("updateTetherState~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiTethered = true;
            }
        }
        for (Object o: errored) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiErrored = true;
            }
        }

        if (wifiTethered) {
            WifiConfiguration wifiConfig = mWifiManager.getWifiApConfiguration();
            updateConfigSummary(wifiConfig);
        } else if (wifiErrored) {
			((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.wifi_error,-1,-1);
			updatamessage();
        }
    }

    private void handleWifiApStateChanged(int state) {
		mOpen = false;
        switch (state) {
            case WifiManager.WIFI_AP_STATE_ENABLING:
				((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.wifi_starting,-1,-1);
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
				((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.accessibility_service_state_on,-1,-1);
				mOpen = true;
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                ((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.wifi_stopping,-1,-1);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLED:
                ((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.accessibility_service_state_off,-1,-1);
                enableWifiCheckBox();
                break;
            default:
			((RKSettings)mContext).updateSettingItem(R.string.wifi_tether_checkbox_text,R.string.wifi_error,-1,-1);
                enableWifiCheckBox();
        }
		updatamessage();
    }
	
	public void updatamessage()
	{
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
		LOGD("updatamessage,send msg to update UI");
	}
}

