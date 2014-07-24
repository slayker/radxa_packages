package com.rockchip.settings.wifi;

import com.rockchip.settings.R;
import com.rockchip.settings.wifi.Summary;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;


public class Access_Point implements Comparable<Access_Point> {
    private static final int[] STATE_SECURED = {R.attr.state_encrypted};
    private static final int[] STATE_NONE = {};

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    public final String ssid;
    public final String bssid;
    public final int security;
    public final int networkId;
	enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }
	PskType pskType = PskType.UNKNOWN;
    private WifiConfiguration mConfig;
    private int mRssi;
    public boolean mWPS_enabled;
    private WifiInfo mInfo;
    private DetailedState mState;
    private ImageView mSignal;
    private ImageView mWPS;
	private Context mContext = null;

	boolean wpsAvailable = false;

    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

	public Access_Point(Context context,String title)
	{
		mContext = context;
		ssid = title;
		bssid = null;
		security = 0;
		networkId = -1;
		mRssi = Integer.MAX_VALUE;
		mWPS_enabled = false;
	}
	
    public Access_Point(Context context, WifiConfiguration config) {
        //super(context);
        //setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        mContext = context;
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        security = getSecurity(config);
        networkId = config.networkId;
        mConfig = config;
        mRssi = Integer.MAX_VALUE;
		mWPS_enabled = false;
        bssid = null;

    }

    public Access_Point(Context context, ScanResult result) {
        //super(context);
        //setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        mContext = context;
        ssid = result.SSID;
        security = getSecurity(result);
        networkId = -1;
        mRssi = result.level;
        if (result.capabilities.contains("WPS"))
	    mWPS_enabled = true;
		else
	    	mWPS_enabled = false;
	//Log.d(TAG, "New AP from result: SSID: " + ssid + "  LOCALE: " + locale);
        bssid = result.BSSID;
		wpsAvailable = security != SECURITY_EAP && result.capabilities.contains("WPS");
    }
	
   // @Override
   public String getSecurityString(boolean concise) {
        Context context = mContext;
        switch(security) {
            case SECURITY_EAP:
                return concise ? context.getString(R.string.wifi_security_short_eap) :
                    context.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch (pskType) {
                    case WPA:
                        return concise ? context.getString(R.string.wifi_security_short_wpa) :
                            context.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa2) :
                            context.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa_wpa2) :
                            context.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? context.getString(R.string.wifi_security_short_psk_generic)
                                : context.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return concise ? context.getString(R.string.wifi_security_short_wep) :
                    context.getString(R.string.wifi_security_wep);
            case SECURITY_NONE:
            default:
                return concise ? "" : context.getString(R.string.wifi_security_none);
        }
    }
    public void onBindView(View view) {
        //setTitle(ssid);
        mSignal = (ImageView) view.findViewById(R.id.signal);
        mWPS = (ImageView) view.findViewById(R.id.wps_enabled);
	if (!mWPS_enabled)
	    mWPS.setImageDrawable(null);

        if (mRssi == Integer.MAX_VALUE) {
            mSignal.setImageDrawable(null);
        } else {
            mSignal.setImageResource(R.drawable.wifi_signal);
            mSignal.setImageState((security != SECURITY_NONE) ?
                    STATE_SECURED : STATE_NONE, true);
        }
		mSignal.setImageLevel(getLevel());
        //refresh();
  //      super.onBindView(view);
    }
/*    private ImageView getSignal(){
		mSignal = (ImageView) findViewById(R.id.signal);
		//mWPS = (ImageView) view.findViewById(R.id.wps_enabled);
		//if (!mWPS_enabled)
			//mWPS.setImageDrawable(null);

		if (mRssi == Integer.MAX_VALUE) {
			mSignal.setImageDrawable(null);
		} else {
			mSignal.setImageResource(R.drawable.wifi_signal);
			mSignal.setImageState((security != SECURITY_NONE) ?
			STATE_SECURED : STATE_NONE, true);
		}
		return mSignal;
    	}*/
    //@Override
    public int compareTo(Access_Point preference) {
        if (!(preference instanceof Access_Point)) {
            return 1;
        }
		
        Access_Point other = (Access_Point) preference;
        // Active one goes first.
        if (mInfo != other.mInfo) {
            return (mInfo != null) ? -1 : 1;
        }
        // Reachable one goes before unreachable one.
        if ((mRssi ^ other.mRssi) < 0) {
            return (mRssi != Integer.MAX_VALUE) ? -1 : 1;
        }
        // Configured one goes before unconfigured one.
        if ((networkId ^ other.networkId) < 0) {
            return (networkId != -1) ? -1 : 1;
        }
        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(other.ssid);
    }

    public boolean update(ScanResult result) {
        // We do not call refresh() since this is called before onBindView().
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                mRssi = result.level;
            }
            if (result.capabilities.contains("WPS"))
		        mWPS_enabled = true;
	    	else
	        	mWPS_enabled = false;

			refresh();
            return true;
        }
        return false;
    }

    public void update(WifiInfo info, DetailedState state) {
        boolean reorder = false;
        if (info != null && networkId != -1 && networkId == info.getNetworkId()) {
            reorder = (mInfo == null);
            mRssi = info.getRssi();
            mInfo = info;
            mState = state;
            refresh();
        } else if (mInfo != null) {
            reorder = true;
            mInfo = null;
            mState = null;
            refresh();
        }
        if (reorder) {
            //notifyHierarchyChanged();
        }
    }

    public int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 4);
    }

    boolean isWPSEnabled() {
        return mWPS_enabled;
    }
    public WifiConfiguration getConfig() {
        return mConfig;
    }

    public WifiInfo getInfo() {
        return mInfo;
    }

    public DetailedState getState() {
        return mState;
    }

    static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }



	private void refresh()
	{
		if(mCallBack != null)
			mCallBack.onCallBack();
	}
	
	public String getSummary()
	{
		if (mState != null) 
		{
			return Summary.get(mContext, mState);
		} 
		else if (mRssi == Integer.MAX_VALUE)
		{
			return mContext.getString(R.string.wifi_not_in_range);
		}
		else if (mConfig != null && mConfig.status == WifiConfiguration.Status.DISABLED)
		{     
			String status = null;
			switch (mConfig.disableReason) 
			{                
				case WifiConfiguration.DISABLED_AUTH_FAILURE:                   
					status = mContext.getString(R.string.wifi_disabled_password_failure);                   
					break;                
				case WifiConfiguration.DISABLED_DHCP_FAILURE:                
				case WifiConfiguration.DISABLED_DNS_FAILURE:                    
					status = mContext.getString(R.string.wifi_disabled_network_failure);                    
					break;                
				case WifiConfiguration.DISABLED_UNKNOWN_REASON:                    
					status = mContext.getString(R.string.wifi_disabled_generic);
					break;
			}  
			return status;
		}
		else // In range, not disabled.
		{ 
			StringBuilder summary = new StringBuilder();
			if (mConfig != null) { // Is saved network
				summary.append(mContext.getString(R.string.wifi_remembered));
			}

			if (security != SECURITY_NONE) {
				String securityStrFormat;
				if (summary.length() == 0) {
					securityStrFormat = mContext.getString(R.string.wifi_secured_first_item);
				} else {
					securityStrFormat = mContext.getString(R.string.wifi_secured_second_item);
				}
				summary.append(String.format(securityStrFormat, getSecurityString(true)));
			}
			
			if (mConfig == null && wpsAvailable) // Only list WPS available for unsaved networks
			{
				if (summary.length() == 0) 
				{
					summary.append(mContext.getString(R.string.wifi_wps_available_first_item));
				} 
				else 
				{
					summary.append(mContext.getString(R.string.wifi_wps_available_second_item));
				}
			}

			return summary.toString();
		}
	}

	private CallBack mCallBack;
	public void setCallBack(CallBack callback)
	{
		mCallBack = callback;
	}
}

