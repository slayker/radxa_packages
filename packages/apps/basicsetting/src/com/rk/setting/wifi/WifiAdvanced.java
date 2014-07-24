package com.rk.setting.wifi;

import android.provider.Settings;
import android.provider.Settings.Secure;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import java.net.InetAddress;
import android.net.NetworkUtils;
import android.net.DhcpInfo;
import java.util.Iterator;
import com.rk.setting.R;

public class WifiAdvanced
{
	private WifiManager mWifiManager;
	private DhcpInfo mDhcpInfo;
	private Context mCotenxt = null;

	public WifiAdvanced(Context context)
	{
		mCotenxt = context;
		mWifiManager = (WifiManager)mCotenxt.getSystemService(Context.WIFI_SERVICE);

		if(mWifiManager != null)
		{
			mDhcpInfo = mWifiManager.getDhcpInfo(); 
		}
	}

	public String getWifiMACAddress()
	{
		String macAddress = null;
		if(mWifiManager != null)
		{
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
		}

		return macAddress;
	}

	public String getIPAddress()
	{
		return getWifiIpAddresses(mCotenxt);
	}

	/**
     * Returns the WIFI IP Addresses, if any, taking into account IPv4 and IPv6 style addresses.
     * @param context the application context
     * @return the formatted and comma-separated IP addresses, or null if none.
     */
    public static String getWifiIpAddresses(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        LinkProperties prop = cm.getLinkProperties(ConnectivityManager.TYPE_WIFI);
        String ip = formatIpAddresses(prop);
		if((ip == null) || ip.equals(""))
			ip = context.getString(R.string.status_unavailable);

		return ip;
    }

	private static String formatIpAddresses(LinkProperties prop) {
        if (prop == null) return null;
        Iterator<InetAddress> iter = prop.getAddresses().iterator();
        // If there are no entries, return null
        if (!iter.hasNext()) return null;
        // Concatenate all available addresses, comma separated
        String addresses = "";
        while (iter.hasNext()) {
            addresses += iter.next().getHostAddress();
            if (iter.hasNext()) addresses += ", ";
        }
        return addresses;
    }

	
	public String getGateWay()
	{ 
		return putAddress(mDhcpInfo.gateway);	  
	} 

	public String getDNS1()
	{ 
		return putAddress(mDhcpInfo.dns1);	  
	}

	public String getDNS2()
	{ 
		return putAddress(mDhcpInfo.dns2);	  
	} 

	public String getNetMask()
	{ 
		return putAddress(mDhcpInfo.netmask);	  
	} 

	private static String putAddress(int addr) {
        return NetworkUtils.intToInetAddress(addr).getHostAddress();
    }
}
