package com.rockchip.settings.wifi.ice_cream;

//import com.android.settings.ProgressCategory;
import com.rockchip.settings.R;
import com.rockchip.settings.wifi.Summary;
import com.rockchip.settings.wifi.*;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
	
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.Activity;
import android.app.ListActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.preference.CheckBoxPreference;
//import android.preference.Preference;
//import android.preference.PreferenceActivity;
//import android.preference.PreferenceScreen;
import android.provider.Settings.Secure;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.TextUtils;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;

import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleAdapter;
import java.util.Collection;
import java.util.Collections;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.android.internal.util.AsyncChannel;
import android.app.AlertDialog;
import android.net.wifi.WpsResult;
import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
/////////////////////add///////////////////////////
import android.app.ProgressDialog;
import android.content.DialogInterface;
/////////////////////add///////////////////////////
public  class  Wifi_setting extends AlertActivity implements DialogInterface.OnClickListener 
{
	private static final String TAG = "WifiSettings";
	private static final int MENU_ID_SCAN = Menu.FIRST;
	private static final int MENU_ID_ADVANCED = Menu.FIRST + 1;
	private static final int MENU_ID_CONNECT = Menu.FIRST + 2;
	private static final int MENU_ID_FORGET = Menu.FIRST + 3;
	private static final int MENU_ID_MODIFY = Menu.FIRST + 4;

	private final IntentFilter mFilter;
	private final BroadcastReceiver mReceiver;
	private final Scanner mScanner;

	private WifiManager mWifiManager;
	private Wifi_Enabler mWifi_Enabler;

	private DetailedState mLastState;
	private WifiInfo mLastInfo;
	private int mLastPriority;

	private boolean mResetNetworks = false;
	private int mKeyStoreNetworkId = -1;
	private Access_Point mSelected;
	Runnable mTimeRunnable;
	private Wifi_Dialog mDialog;

	private ListView mListView = null;
	ArrayList<Access_Point> accessPoints = new ArrayList<Access_Point>();
	WifiScanListViewAdapter mAdapter = null;
	private LayoutInflater flater = null;
	private LinearLayout mView = null;
	private WifiUICallBack mCallBack;
	public  Wifi_setting() {
		mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mFilter.addAction(WifiManager.ERROR_ACTION);
		/*
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mFilter.addAction(WifiManager.WPS_SUCCESS_ACTION);
		*/
		mReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        handleEvent( intent);
			LOGD("wifi setting handleevent");
		    }
		};
		LOGD("wifi setting in~~~~~");
		
		mScanner = new Scanner();
		/*////////////////////add//////////////////////////
		
		mTimeRunnable = new Runnable() {
		        public void run() {
				mProgressView.incrementProgressBy(1);
				if(mProgressView.getProgress() < mProgressView.getMax()) {
					mScanner.postDelayed(this, 3000);   
				} else
				{
					mScanner.removeCallbacks(mTimeRunnable);
		        		mProgressView.setProgress(0);
					mProgressView.cancel();
				}
		    }
		};

	////////////////////add//////////////////////////*/
	}

	
	private void createListView()
	{
		flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = (ListView)flater.inflate(R.layout.listview,null);
		mAdapter = new WifiScanListViewAdapter(this,accessPoints);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mListItemClickListener);
		mListView.requestFocus();
	}

	
	public void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWifiManager.asyncConnect(this, new WifiServiceHandler());
		mCallBack = new WifiUICallBack();
		createListView();
		mAlert.setView(mListView,-5,-5,-5,-5);
		
		this.setupAlert();	    
	}
	/*
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
		if (info instanceof AdapterContextMenuInfo) {
			Preference preference = (Preference) getListView().getItemAtPosition(
				((AdapterContextMenuInfo) info).position);

		if (preference instanceof Access_Point) {
			mSelected = (Access_Point) preference;
			menu.setHeaderTitle(mSelected.ssid);
			if (mSelected.getLevel() != -1 && mSelected.getState() == null) {
				menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
			}
			if (mSelected.networkId != -1) {
				menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
				if (mSelected.security != AccessPoint.SECURITY_NONE) {
					menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
					}
				}
			}
		}
	}*/
	private void updateAccessPoints() 
	{
		LOGD("updateAccessPoints****************");
		if(accessPoints != null)
		{
			accessPoints.clear();
//			Access_Point access = new Access_Point(this,getResources().getString(R.string.wifi_add_network));
//			accessPoints.add(access);
		}
		//Map<String, Object> map = new HashMap<String, Object>();
		List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
		if (configs != null) {
			mLastPriority = 0;
			for (WifiConfiguration config : configs) {
				if (config.priority > mLastPriority) {
				    mLastPriority = config.priority;
				}

				// Shift the status to make enableNetworks() more efficient.
				if (config.status == Status.CURRENT) {
				    config.status = Status.ENABLED;
				} else if (mResetNetworks && config.status == Status.DISABLED) {
				    config.status = Status.CURRENT;
				}

				Access_Point accessPoint = new Access_Point(this, config);
				accessPoint.update(mLastInfo, mLastState);
				accessPoint.setCallBack(mCallBack);
				accessPoints.add(accessPoint);
			}
		}

		List<ScanResult> results = mWifiManager.getScanResults();
		if (results != null) {
			for (ScanResult result : results) {
			    // Ignore hidden and ad-hoc networks.
			    if (result.SSID == null || result.SSID.length() == 0 ||
			            result.capabilities.contains("[IBSS]")) {
			        continue;
			    }

			    boolean found = false;
			    for (Access_Point accessPoint : accessPoints) {
			        if (accessPoint.update(result)) {
			            found = true;
			        }
			    }
			    if (!found) {
					accessPoints.add(new Access_Point(this, result));
			    }
			}
		}

		Collections.sort(accessPoints);

		Access_Point access = new Access_Point(this,getResources().getString(R.string.wifi_add_network));
		if(accessPoints.size() > 0)
		{
			accessPoints.add(0,access);
		}
		else
		{
			accessPoints.add(access);
		}
			
		
/*		for (Access_Point accessPoint : accessPoints) {
			//mAccessPoints.addPreference(accessPoint);
			LOGD("Access Point ssid is :::::::::::::::::"+accessPoint.ssid+",level = "+accessPoint.getLevel());
		}*/
			
		mAdapter.notifyDataSetChanged();
    	}
	 private void LOGD(String msg)
	{
		if(true)
			Log.d("Wifi Settings",msg);
	}
	private void handleEvent(Intent intent) {
		LOGD("handleEvent(), action = "+intent.getAction());
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        }
	else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            updateAccessPoints();
        } 
	else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
            if (mSelected != null && mSelected.networkId != -1) {
                mSelected = null;
            }
            updateAccessPoints();
        } 
	else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
		/*
	    if (intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR))
	    {
		if (intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0) == 1)
		{
			int found = 0;
			Access_Point found_ap = null;
			Log.d(TAG, ">>>>intent.getExtra(WifiManager.EXTRA_SUPPLICANT_ERROR_BSSID, 0)=" + 
               		intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR_BSSID, 0));
			int networkId = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR_BSSID, 0);
			
			synchronized(this) {
                 		for (int i = accessPoints.size() - 1; i >= 0; --i) 
				{
					Access_Point ap = (Access_Point) accessPoints.get(i);
					WifiConfiguration config = ap.getConfig();
					if (config == null)
						continue;
 
					if (config.networkId == networkId)
					{
						found = 1;
						found_ap = ap;
						break;
					}
                 		}
			}
			if (found == 1)
				showDialog(found_ap, true);
		}
	    }	*/
            updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                    intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
        }
	else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(((NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
        }
	else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        } 
	/*
	else if (WifiManager.WPS_SUCCESS_ACTION.equals(action)) {
            Log.d(TAG, "We received the WPS_SUCCESS action.");
                mScanner.removeCallbacks(mTimeRunnable);
                //mProgressView.setProgress(0);
                //mProgressView.cancel();
        }*/
    }
	private void updateWifiState(int state) {
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            mScanner.resume();
            updateAccessPoints();
        } else {
            mScanner.pause();
            //mAccessPoints.removeAll();
        }
    }
	private void updateConnectionState(DetailedState state) {
		/* sticky broadcasts can call this when wifi is disabled */
		if (!mWifiManager.isWifiEnabled()) {
			mScanner.pause();
			return;
		}

		if (state == DetailedState.OBTAINING_IPADDR) {
			mScanner.pause();
		} else {
			mScanner.resume();
		}

		mLastInfo = mWifiManager.getConnectionInfo();
		if (state != null) {
			mLastState = state;
		}
		/*
		for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
			((AccessPoint) mAccessPoints.getPreference(i)).update(mLastInfo, mLastState);
		}
		*/
		if (mResetNetworks && (state == DetailedState.CONNECTED ||
		    state == DetailedState.DISCONNECTED || state == DetailedState.FAILED)) {
			updateAccessPoints();
			enableNetworks();
        }
	}

	
	private AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			if(position == 0)
			{
				LOGD("AdapterView.OnItemClickListener mListItemClickListener, position = 0, showDialog(null, true)");
				showDialog(null, true);
			}
			else
			{
				mSelected = accessPoints.get(position);
				showDialog(mSelected, false);
			}
		}
	};
	/*
	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
///////////////////add///////////////////////////
		if(preference == mTestWPS) {
			mScanner.removeCallbacks(mTimeRunnable);
		    	mWifiManager.startPBC(null);
			mProgressView.setProgress(0);
	                mProgressView.setTitle("WPS PBC"); 
	                mProgressView.setMessage("Press PBC button in 2 minutes."); 
			mProgressView.show();
			//mProgressView.getWindow().setLayout(400, 200);
			mScanner.post(mTimeRunnable);
		} else 
/////////////////////add//////////////////////////
        if (preference instanceof AccessPoint) {
            mSelected = (AccessPoint) preference;
            showDialog(mSelected, false);
        } else if (preference == mAddNetwork) {
            mSelected = null;
            showDialog(null, true);
        } else if (preference == mNotifyOpenNetworks) {
            Secure.putInt(getContentResolver(),
                    Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    mNotifyOpenNetworks.isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }
*/
	private void forget(int networkId) 
	{
		mWifiManager.forgetNetwork(networkId);
		if (mWifiManager.isWifiEnabled()) 
		{
            mScanner.resume();
        }
		updateAccessPoints();
	}
	
	private void enableNetworks() 
	{
	/*
        for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
            WifiConfiguration config = ((Access_Point) mAccessPoints.getPreference(i)).getConfig();
            if (config != null && config.status != Status.ENABLED) {
                mWifiManager.enableNetwork(config.networkId, false);
            }
        }*/
        mResetNetworks = false;
	}

	 private boolean requireKeyStore(WifiConfiguration config) {
        if (Wifi_Dialog.requireKeyStore(config) &&
                KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) { //KeyStore.getInstance().test() != KeyStore.NO_ERROR
            mKeyStoreNetworkId = config.networkId;
            Credentials.getInstance().unlock(this);
            return true;
        }
        return false;
    }
	public void onClick(DialogInterface dialogInterface, int button) 
	{
		if (button == Wifi_Dialog.BUTTON_FORGET && mSelected != null) 
		{
			if (mSelected.networkId != -1)
				forget(mSelected.networkId);
		}
		else if (button == Wifi_Dialog.BUTTON_SUBMIT && mDialog != null) 
		{
			WifiConfiguration config = mDialog.getConfig();

			if (config == null) 
			{
				if (mSelected != null && !requireKeyStore(mSelected.getConfig()) && 
						mSelected.networkId != INVALID_NETWORK_ID) 
				{
					mWifiManager.connectNetwork(mSelected.networkId);
				}
			} 
			else if (config.networkId != INVALID_NETWORK_ID) 
			{
				if (mSelected != null) 
				{
					mWifiManager.saveNetwork(config);
				}
			} 
			else 
			{
				if (mDialog.edit || requireKeyStore(config)) 
				{
				mWifiManager.saveNetwork(config);
				}
				else 
				{
					mWifiManager.connectNetwork(config);
				}
			}
		}
	}
	private void showDialog(Access_Point accessPoint, boolean edit) {
		if (mDialog != null) {
		    mDialog.dismiss();
		}
		mDialog = new Wifi_Dialog(this, this, accessPoint, edit);
		mDialog.show();
	}
	@Override
	protected void onResume() {
		super.onResume();
		//if (mWifiEnabler != null) {
			//mWifiEnabler.resume();
		//}
		registerReceiver(mReceiver, mFilter);
		if (mKeyStoreNetworkId != -1 && KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) { // KeyStore.getInstance().test() == KeyStore.NO_ERROR
			mWifiManager.connectNetwork(mKeyStoreNetworkId);
		}
		mKeyStoreNetworkId = -1;
    }
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(mReceiver);
		mScanner.pause();
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public class WifiUICallBack implements CallBack 
	{
		public void onCallBack()
		{
			if((mListView != null) && (mAdapter != null))
			{
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	private class Scanner extends Handler {
		private int mRetry = 0;

		void resume() {
		    if (!hasMessages(0)) {
		        sendEmptyMessage(0);
		    }
		}

		void pause() {
		    mRetry = 0;
		    //mAccessPoints.setProgress(false);
		    removeMessages(0);
		}

		@Override
		public void handleMessage(Message message) {
		    if (mWifiManager.startScanActive()) {
		        mRetry = 0;
		    } else if (++mRetry >= 3) {
		        mRetry = 0;
		        Toast.makeText(Wifi_setting.this, R.string.wifi_fail_to_scan,
		                Toast.LENGTH_LONG).show();
		        return;
		    }
		    //mAccessPoints.setProgress(mRetry != 0);
		    sendEmptyMessageDelayed(0, 6000);
		}
	}
	
	private class WifiServiceHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        //AsyncChannel in msg.obj
                    } else {
                        //AsyncChannel set up failure, ignore
                        Log.e(TAG, "Failed to establish AsyncChannel connection");
                    }
                    break;
                case WifiManager.CMD_WPS_COMPLETED:
                    WpsResult result = (WpsResult) msg.obj;
                    if (result == null) break;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Wifi_setting.this)
                        .setTitle(R.string.wifi_wps_setup_title)
                        .setPositiveButton(android.R.string.ok, null);
                    switch (result.status) {
                        case FAILURE:
                            dialog.setMessage(R.string.wifi_wps_failed);
                            dialog.show();
                            break;
                        case IN_PROGRESS:
                            dialog.setMessage(R.string.wifi_wps_in_progress);
                            dialog.show();
                            break;
                        default:
                            if (result.pin != null) {
                                dialog.setMessage(getResources().getString(
                                        R.string.wifi_wps_pin_output, result.pin));
                                dialog.show();
                            }
                            break;
                    }
                    break;
                //TODO: more connectivity feedback
                default:
                    //Ignore
                    break;
            }
        }
    }
}


