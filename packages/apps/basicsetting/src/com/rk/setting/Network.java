package com.rk.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.net.EthernetDataTracker;
import com.rk.setting.ethernet.EthernetSettings;
import com.rk.setting.wifi.WifiAdvanced;
import com.rk.setting.wifi.Wifi_Enabler;
import com.rk.setting.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;
import android.util.Log;


public class Network extends Activity
{
	TextView mWifiStatus = null;
	TextView mWifiMac = null;
	TextView mWifiIp = null;
	TextView mWifiGateway = null;
	TextView mWifiNetMask = null;
	TextView mWifiDNS1 = null;
	TextView mWifiDNS2 = null;

	TextView mEthernetStatus = null;
	TextView mEthernetMac = null;
	TextView mEthernetIp = null;
	TextView mEthernetGateWay = null;
	TextView mEthernetNetMask = null;
	TextView mEthernetDNS1 = null;
	TextView mEthernetDNS2 = null;

	private EthernetSettings mEthernet = null;
	private WifiAdvanced mWifiAdvance = null;
	private Wifi_Enabler mWifiEnabler = null;

	private boolean mEthernetEnable = false;
	private boolean mWifiEnable = false;
	
	private void createTitle()
	{
		ImageView image = (ImageView)findViewById(R.id.title_image);
		Bitmap resize = bitMapScale(R.drawable.network);
		image.setScaleType(ImageView.ScaleType.CENTER);
		image.setImageBitmap(resize);

		TextView title = (TextView)findViewById(R.id.title_text);
		title.setTextSize(ScreenInformation.mScreenWidth/25f*ScreenInformation.mDpiRatio);
	}

	private Bitmap bitMapScale(int id)
	{
		Bitmap map = BitmapFactory.decodeResource(this.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*ScreenInformation.mDpiRatio;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}

	private void setWifiViewSize()
	{
		float size = 0f;
		float off = 0f;

		if(mEthernetEnable)
		{
			size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio-3f;
			off = 5f;
		}
		else
		{
			size = ScreenInformation.mScreenWidth/30f*ScreenInformation.mDpiRatio;
			off = 5f;
		}
		
		// wifi
		TextView view = (TextView)findViewById(R.id.wifi_status_title);
		view.setTextSize(size+off);
		//wifi mac
		view = (TextView)findViewById(R.id.wifi_mac_title);
		view.setTextSize(size);
		//wifi IP
		view = (TextView)findViewById(R.id.wifi_ip_title);
		view.setTextSize(size);
		// wifi gateway
		view = (TextView)findViewById(R.id.wifi_gateway_title);
		view.setTextSize(size);
		// wifi netMask
		view = (TextView)findViewById(R.id.wifi_netmask_title);
		view.setTextSize(size);
		// wifi DNS1
		view = (TextView)findViewById(R.id.wifi_dns1_title);
		view.setTextSize(size);
		// wifi DNS2
		view = (TextView)findViewById(R.id.wifi_dns2_title);
		view.setTextSize(size);
	}

	private void setEthernetViewSize()
	{
		float size = 0f;
		float off = 0f;

		if(mWifiEnable)
		{
			size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio-3f;
			off = 5f;
		}
		else
		{
			size = ScreenInformation.mScreenWidth/30f*ScreenInformation.mDpiRatio;
			off = 5f;
		}

		// ethernet
		TextView view = (TextView)findViewById(R.id.ethernet_status_title);
		view.setTextSize(size+off);
		//ethernet mac
		view = (TextView)findViewById(R.id.ethernet_mac_title);
		view.setTextSize(size);
		//ethernet IP
		view = (TextView)findViewById(R.id.ethernet_ip_title);
		view.setTextSize(size);
		// ethernet gateway
		view = (TextView)findViewById(R.id.ethernet_gateway_title);
		view.setTextSize(size);
		// ethernet netMask
		view = (TextView)findViewById(R.id.ethernet_netmask_title);
		view.setTextSize(size);
		// ethernet DNS1
		view = (TextView)findViewById(R.id.ethernet_dns1_title);
		view.setTextSize(size);
		// ethernet DNS2
		view = (TextView)findViewById(R.id.ethernet_dns2_title);
		view.setTextSize(size);
	}
	
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_CLEARABLE_FLAGS);

		mEthernetEnable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_ETHERNET);
		mWifiEnable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
		
		if(!mEthernetEnable && mWifiEnable)
			setContentView(R.layout.network_no_ethernet);
		else if(mEthernetEnable && !mWifiEnable)
			setContentView(R.layout.network_no_wifi);
		else
        	setContentView(R.layout.network);
		
		createTitle();
		
		if(mEthernetEnable)
		{
			setEthernetViewSize();
			mEthernet = new EthernetSettings(this);
			if(mEthernet != null)
				mEthernet.setCallBack(new EthernetCallBack());
			
			createEthernetInformation();
		}

		if(mWifiEnable)
		{
			setWifiViewSize();
			mWifiAdvance = new WifiAdvanced(this);
			mWifiEnabler = new Wifi_Enabler(this);
			createWifiInformation();
		}
   }

	public void onResume()
	{
		super.onResume();
		if(mEthernetEnable && (mEthernet != null))
		{
			mEthernet.Resume();
			updateWifiStatus();
		}
		if(mWifiEnable && mWifiEnabler != null)
		{
			mWifiEnabler.resume();
			mWifiEnabler.setCallBack(new WifiCallBack());
			updateEthernetStatus();
	//		boolean on = mWifiEnabler.getWifiStatus();
	//		String status = on?"Open":"Close";
		}
	}
	
	public void onPause()
	{
		super.onPause();
		if(mEthernetEnable && (mEthernet != null))
		{
			mEthernet.Pause();
		}
		if(mWifiEnable && mWifiEnabler != null)
		{
			mWifiEnabler.pause();
		}
	}
	
	private void createWifiInformation()
	{
		float size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio-3f;
		if(!mEthernetEnable)
		{
			size = ScreenInformation.mScreenWidth/30f*ScreenInformation.mDpiRatio-5f;
		}
		mWifiStatus = (TextView)findViewById(R.id.wifi_status);
		mWifiMac = (TextView)findViewById(R.id.wifi_mac_address);
		mWifiIp = (TextView)findViewById(R.id.wifi_ip_address);
		mWifiGateway = (TextView)findViewById(R.id.wifi_gateway);
		mWifiNetMask = (TextView)findViewById(R.id.wifi_netmask);
		mWifiDNS1 = (TextView)findViewById(R.id.wifi_dns1);
		mWifiDNS2 = (TextView)findViewById(R.id.wifi_dns2);

		mWifiStatus.setTextSize(size);
		mWifiMac.setTextSize(size);
		mWifiIp.setTextSize(size);
		mWifiGateway.setTextSize(size);
		mWifiNetMask.setTextSize(size);
		mWifiDNS1.setTextSize(size);
		mWifiDNS2.setTextSize(size);
		
		updateWifiStatus();
	}

	private void createEthernetInformation()
	{
		float size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio-3f;
		if(!mWifiEnable)
		{
			size = ScreenInformation.mScreenWidth/30f*ScreenInformation.mDpiRatio-5f;
		}
		
		mEthernetStatus = (TextView)findViewById(R.id.ethernet_status);
		mEthernetMac = (TextView)findViewById(R.id.ethernet_mac_address);
		mEthernetIp = (TextView)findViewById(R.id.ethernet_ip_address);
		mEthernetGateWay = (TextView)findViewById(R.id.ethernet_gateway);
		mEthernetNetMask = (TextView)findViewById(R.id.ethernet_netmask);
		mEthernetDNS1 = (TextView)findViewById(R.id.ethernet_dns1);
		mEthernetDNS2 = (TextView)findViewById(R.id.ethernet_dns2);

		mEthernetStatus.setTextSize(size);
		mEthernetMac.setTextSize(size);
		mEthernetIp.setTextSize(size);
		mEthernetGateWay.setTextSize(size);
		mEthernetNetMask.setTextSize(size);
		mEthernetDNS1.setTextSize(size);
		mEthernetDNS2.setTextSize(size);

		updateEthernetStatus();
	}

	public class WifiCallBack implements CallBackListenner
	{
		public void onCallBack()
		{
			updateWifiStatus();
		}
	};
	
	public class EthernetCallBack implements CallBackListenner
	{
		public void onCallBack()
		{
			if(mEthernet != null)
			{
				updateEthernetStatus();
			}
		}
	}


	private void updateWifiStatus()
	{
		if(mWifiEnabler.getWifiStatus())
		{
			mWifiStatus.setText(R.string.connected);
			mWifiMac.setText(mWifiAdvance.getWifiMACAddress());
			mWifiIp.setText(mWifiAdvance.getIPAddress());
			mWifiGateway.setText(mWifiAdvance.getGateWay());
			mWifiNetMask.setText(mWifiAdvance.getNetMask());
			mWifiDNS1.setText(mWifiAdvance.getDNS1());
			mWifiDNS2.setText(mWifiAdvance.getDNS2());
		}
		else
		{
			mWifiStatus.setText(R.string.disconnected);
			mWifiMac.setText(R.string.status_unavailable);
			mWifiIp.setText(R.string.status_unavailable);
			mWifiGateway.setText(R.string.status_unavailable);
			mWifiNetMask.setText(R.string.status_unavailable);
			mWifiDNS1.setText(R.string.status_unavailable);
			mWifiDNS2.setText(R.string.status_unavailable);
		}
	}

	private void updateEthernetStatus()
	{
		mEthernetStatus.setText(mEthernet.getEthernetStatus());
		mEthernetMac.setText(mEthernet.getMacAddress());
		mEthernetIp.setText(mEthernet.getIPAddress());
		mEthernetGateWay.setText(mEthernet.getGateWay());
		mEthernetNetMask.setText(mEthernet.getNetMask());
		mEthernetDNS1.setText(mEthernet.getDNS1());
		mEthernetDNS2.setText(mEthernet.getDNS2());
	}
}

