
package com.rockchip.settings.wifi;

import com.rockchip.settings.R;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.util.Log;

import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
//import com.android.settings.RadioPreference;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Handler;
//import com.android.settings.wifi.WifiApDialog;
import android.util.Log;

/*
 * Displays preferences for Tethering.
 */
public class Wifi_ApSettings  implements DialogInterface.OnClickListener 
{

	private static final String WIFI_AP_SSID_AND_SECURITY = "wifi_ap_ssid_and_security";
	private static final String ENABLE_WIFI_AP = "enable_wifi_ap";
	private static final int CONFIG_SUBTEXT = R.string.wifi_tether_configure_subtext;

	private static final int OPEN_INDEX = 0;
	private static final int WPA_INDEX = 1;

	private static final int DIALOG_AP_SETTINGS = 1;

	private String[] mSecurityType;
	//private Preference mCreateNetwork;
	private CheckBoxPreference mEnableWifiAp;

	private Wifi_ApDialog mDialog;
	private WifiManager mWifiManager;
	//private WifiApEnabler mWifiApEnabler;
	private WifiConfiguration mWifiConfig = null;

	/////////////////add by hh////////////////////////
	private static final String PPPOE = "pppoe_select";
	private static final String DONGLE = "dongle_select";
	private static final String ETHERNET= "ethernet_select";
	private static final String AUTO = "auto_select";
	private static final String UPLINK_SELECTOR = "UPLINK_SELECTOR";
	//private RadioPreference mPPPoeRaido = null;
	//private RadioPreference mDongleRaido = null;
	//private RadioPreference mEthernetRaido = null;
	//private RadioPreference mAutoRaido = null;
	//private Preference lastpreference;
	private static String mKey;
	///////////////////////
	Context mContext;
	Handler mHandler;
	Wifi_ApEnabler mWifi_ApEnabler;
	
	public  Wifi_ApSettings(Context context, Handler handler, Wifi_ApEnabler wifiapensbler) 
	{
		//super.onCreate(savedInstanceState);
		mContext = context;
		mHandler = handler;
		mWifi_ApEnabler = wifiapensbler;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		mWifiConfig = mWifiManager.getWifiApConfiguration();
		//mSecurityType = getResources().getStringArray(R.array.wifi_ap_security);

		//addPreferencesFromResource(R.xml.wifi_ap_settings);

		//mCreateNetwork = findPreference(WIFI_AP_SSID_AND_SECURITY);
		//mEnableWifiAp = (CheckBoxPreference) findPreference(ENABLE_WIFI_AP);

		//mWifiApEnabler = new WifiApEnabler(this, mEnableWifiAp);

		///////////////////////add by ke//////////////////////////////
		CreateDialog();
/*
		if(mWifiConfig == null) {
		    String s = getString(com.android.internal.R.string.wifi_tether_configure_ssid_default);
		    mCreateNetwork.setSummary(String.format(getString(CONFIG_SUBTEXT),
		                                            s, mSecurityType[OPEN_INDEX]));
		} else {
		    mCreateNetwork.setSummary(String.format(getString(CONFIG_SUBTEXT),
		                              mWifiConfig.SSID,
		                              mWifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK) ?
		                              mSecurityType[WPA_INDEX] : mSecurityType[OPEN_INDEX]));
		}

*/
	/*////////////////add by hh////////////////////////
	mPPPoeRaido = (RadioPreference)findPreference(PPPOE);
	mDongleRaido = (RadioPreference)findPreference(DONGLE);
	mEthernetRaido = (RadioPreference)findPreference(ETHERNET);
	mAutoRaido = (RadioPreference)findPreference(AUTO);
		
	// 从数据库读取，默认值为Auto
	ContentResolver resolver = getContentResolver();
	String key = Settings.System.getString(resolver, UPLINK_SELECTOR);
	if(key != null)
	{
		setRaidoButton(key);
	}
	else
	{
		key = AUTO;
		setRaidoButton(key);
		mAutoRaido.setSummary(R.string.wifi_ap_connectting);
	}
	mKey = key;
	//////////////////////////////////////////////*/
	}
	
	private void LOGD(String msg)
	{
		if(true)
			Log.d("Wifi Settings",msg);
	}
	
	private Dialog CreateDialog() {			//int id
	    //if (id == DIALOG_AP_SETTINGS) {
	        mDialog = new Wifi_ApDialog(mContext, this, mWifiConfig);
	        return mDialog;
	    //}
	    //return null;
	}
	public void showDialog(){//int id
		LOGD("0~~~~~~~~~~~");
		//Dialog dia = CreateDialog( id);
		mDialog.show();
		LOGD("5~~~~~~~~~~~");
		LOGD("6~~~~~~~~~~~");
		
		return;
    	}
	public void onResume() {
	    //super.onResume();
	    //mWifiApEnabler.resume();
		IntentFilter filter = new IntentFilter();
		//filter.addAction(ConnectivityManager.TETHER_INTERFACE_SUCCESS);
		//filter.addAction(ConnectivityManager.TETHER_INTERFACE_FAIL);
		mContext.registerReceiver(mWifiApSettingReceiver,filter);
	}

	public void onPause() {
	    //super.onPause();
	    //mWifiApEnabler.pause();
	    mContext.unregisterReceiver(mWifiApSettingReceiver);
	}
		
	private BroadcastReceiver mWifiApSettingReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Log.d("WifiApSettings","mWifiApSettingReceiver onReceive***************************");
		//if(intent.getAction().equals(ConnectivityManager.TETHER_INTERFACE_SUCCESS))
		{
				String face = (String)intent.getExtra("TETHING_INTERFACE_MESSAGE","");
				Log.d("WifiApSettings","set success,face = "+face);
				//setSummary(mKey,true);
		}
		//else if(intent.getAction().equals(ConnectivityManager.TETHER_INTERFACE_FAIL))
		{
				Log.d("WifiApSettings","wifiApSetting set fail");
				//setSummary(mKey,false);
		}
	}
	};
/*		
	private void setSummary(String key,boolean success)
	{
			if((mPPPoeRaido == null) || (mDongleRaido == null) || (mEthernetRaido == null) 
						|| (mAutoRaido == null) || (key == null))
				return ;
	
			mPPPoeRaido.setSummary(R.string.wifi_ap_not_select);
			mDongleRaido.setSummary(R.string.wifi_ap_not_select);
			mEthernetRaido.setSummary(R.string.wifi_ap_not_select);
			mAutoRaido.setSummary(R.string.wifi_ap_not_select);
			
			if(PPPOE.equals(key))
			{
				if(success)
					mPPPoeRaido.setSummary(R.string.wifi_ap_connectted_succ);
				else
					mPPPoeRaido.setSummary(R.string.wifi_ap_connectted_fail);
			}
			else if(DONGLE.equals(key))
			{
				if(success)
					mDongleRaido.setSummary(R.string.wifi_ap_connectted_succ);
				else
					mDongleRaido.setSummary(R.string.wifi_ap_connectted_fail);
			}
			else if(ETHERNET.equals(key))
			{
				if(success)
					mEthernetRaido.setSummary(R.string.wifi_ap_connectted_succ);
				else
					mEthernetRaido.setSummary(R.string.wifi_ap_connectted_fail);
			}
			else if(AUTO.equals(key))
			{
				if(success)
					mAutoRaido.setSummary(R.string.wifi_ap_connectted_succ);
				else
					mAutoRaido.setSummary(R.string.wifi_ap_connectted_fail);
			}
			
			
					
	}
	private void setRaidoButton(String key)
	{
		if((mPPPoeRaido == null) || (mDongleRaido == null) || (mEthernetRaido == null) 
			|| (mAutoRaido == null) || (key == null))
			return ;
		
		mPPPoeRaido.setChecked(false);
		mDongleRaido.setChecked(false);
		mEthernetRaido.setChecked(false);
		mAutoRaido.setChecked(false);
		
		if(PPPOE.equals(key))
		{
			mPPPoeRaido.setChecked(true);
			mPPPoeRaido.setSummary(R.string.wifi_ap_connectting);
		}
		else if(DONGLE.equals(key))
		{
			mDongleRaido.setChecked(true);
			mDongleRaido.setSummary(R.string.wifi_ap_connectting);
		}
		else if(ETHERNET.equals(key))
		{
			mEthernetRaido.setChecked(true);
			mEthernetRaido.setSummary(R.string.wifi_ap_connectting);
		}
		else if(AUTO.equals(key))
		{
			mAutoRaido.setChecked(true);
			mAutoRaido.setSummary(R.string.wifi_ap_connectting);
		}
		
	}

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
    	String key = mKey;
	
        if (preference == mCreateNetwork) {
            showDialog(DIALOG_AP_SETTINGS);
        }
	else if(preference == mPPPoeRaido)
	{
		Log.d("WifiApSettings","onPreferenceTreeClick, preference == mPPPoeRaido");
		setRaidoButton(PPPOE);
		key = PPPOE;
	}
	else if(preference == mDongleRaido)
	{
		Log.d("WifiApSettings","onPreferenceTreeClick, preference == mDongleRaido");
		setRaidoButton(DONGLE);
		key = DONGLE;
	}
	else if(preference == mEthernetRaido)
	{
		Log.d("WifiApSettings","onPreferenceTreeClick, preference == mEthernetRaido");
		setRaidoButton(ETHERNET);
		key = ETHERNET;
	}
	else if(preference == mAutoRaido)
	{
		Log.d("WifiApSettings","onPreferenceTreeClick, preference == mAutoRaido");
		setRaidoButton(AUTO);
		key = AUTO;
	}

	if( lastpreference != preference && key != null ) {
		lastpreference = preference;
		mKey = key;
		Intent intent = new Intent(ConnectivityManager.TETHER_INFERFACE);
		intent.putExtra("TETHING_INTERFACE", key);
		sendBroadcast(intent);
       	
		// 保存到数据库
		Settings.System.putString(getContentResolver(),UPLINK_SELECTOR,key);
	}	
        return true;
    }
*/
    public void onClick(DialogInterface dialogInterface, int button) {

        if (button == DialogInterface.BUTTON_POSITIVE) {
            mWifiConfig = mDialog.getConfig();
            if (mWifiConfig != null) {
                /**
                 * if soft AP is running, bring up with new config
                 * else update the configuration alone
                 */
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                    mWifiManager.setWifiApEnabled(mWifiConfig, true);
                    /**
                     * There is no tether notification on changing AP
                     * configuration. Update status with new config.
                     */
                    mWifi_ApEnabler.updateConfigSummary(mWifiConfig);
			mWifi_ApEnabler.updatamessage();
                } else {
                    mWifiManager.setWifiApConfiguration(mWifiConfig);
                }
		/*
                mCreateNetwork.setSummary(String.format(getString(CONFIG_SUBTEXT),
                            mWifiConfig.SSID,
                            mWifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK) ?
                            mSecurityType[WPA_INDEX] : mSecurityType[OPEN_INDEX]));
                            */
            }
        }
    }
}

