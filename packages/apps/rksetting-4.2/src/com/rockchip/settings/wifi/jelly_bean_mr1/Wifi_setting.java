package com.rockchip.settings.wifi.jelly_bean_mr1;

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
import android.content.pm.PackageManager;
import java.util.concurrent.atomic.AtomicBoolean;
import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
/////////////////////add///////////////////////////
import android.app.ProgressDialog;
import android.content.DialogInterface;
/////////////////////add///////////////////////////
public  class  Wifi_setting extends Activity implements DialogInterface.OnClickListener //AlertActivity
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

	private boolean mP2pSupported;
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

    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;
//    private boolean mP2pSupported;
	private WifiUICallBack mCallBack;

	// should activity finish once we have a connection?
    private boolean mAutoFinishOnConnection;
	
	private AtomicBoolean mConnected = new AtomicBoolean(false);
	
	// this boolean extra specifies whether to auto finish when connection is established
    private static final String EXTRA_AUTO_FINISH_ON_CONNECT = "wifi_auto_finish_on_connect";
	
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
		
		mReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        handleEvent( intent);
				LOGD("wifi setting handleevent");
		    }
		};
		
		mScanner = new Scanner();
	}

	
	private void createListView()
	{
		flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = (ListView)findViewById(R.id.listview);
		mAdapter = new WifiScanListViewAdapter(this,accessPoints);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mListItemClickListener);
		mListView.requestFocus();
	}

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_layout);
		
		mP2pSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mConnectListener = new WifiManager.ActionListener() 
		{
			public void onSuccess() 
			{
			}
			public void onFailure(int reason) 
			{
				Toast.makeText(Wifi_setting.this,R.string.wifi_failed_connect_message,Toast.LENGTH_SHORT).show();
			}
		};

        mSaveListener = new WifiManager.ActionListener() 
		{
			public void onSuccess() 
			{
			}
			public void onFailure(int reason) 
			{
				Toast.makeText(Wifi_setting.this,R.string.wifi_failed_save_message,Toast.LENGTH_SHORT).show();
			}
		};

        mForgetListener = new WifiManager.ActionListener() 
		{
			public void onSuccess() 
			{
			}
			public void onFailure(int reason) 
			{
				Toast.makeText(Wifi_setting.this,R.string.wifi_failed_forget_message,Toast.LENGTH_SHORT).show();
			}
		};

/*
		if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
            mDlgEdit = savedInstanceState.getBoolean(SAVE_DIALOG_EDIT_MODE);
            mAccessPointSavedState = savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
        }
*/
		// first if we're supposed to finish once we have a connection
//        mAutoFinishOnConnection = intent.getBooleanExtra(EXTRA_AUTO_FINISH_ON_CONNECT, false);
		
		mCallBack = new WifiUICallBack();
		createListView();
	//	mAlert.setView(mListView,-5,-5,-5,-5);
		
	//	this.setupAlert();	    
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
		else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) 
		{
			updateAccessPoints();
		} 
		else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) 
		{
            //Ignore supplicant state changes when network is connected
            //TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            //introduce a broadcast that combines the supplicant and network
            //network state change events so the apps dont have to worry about
            //ignoring supplicant state change when network is connected
            //to get more fine grained information.
            SupplicantState state = (SupplicantState) intent.getParcelableExtra(
                    WifiManager.EXTRA_NEW_STATE);
            if (!mConnected.get() && SupplicantState.isHandshakeState(state)) {
                updateConnectionState(WifiInfo.getDetailedStateOf(state));
            }
        } 
		else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) 
		{
			NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
      //      changeNextButtonState(info.isConnected());
            updateAccessPoints();
            updateConnectionState(info.getDetailedState());
            if (mAutoFinishOnConnection && info.isConnected()) {
             //   if (activity != null) 
				{
                    Wifi_setting.this.setResult(Activity.RESULT_OK);
                    Wifi_setting.this.finish();
                }
                return;
            }
        }
		else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        } 
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
		
		for (int i = accessPoints.size() - 1; i >= 0; --i) {
			accessPoints.get(i).update(mLastInfo, mLastState);
		}
		
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

	void submit()
	{
		final WifiConfiguration config = mDialog.getConfig();

		if (config == null) 
		{
			if (mSelected != null
					&& !requireKeyStore(mSelected.getConfig())
					&& mSelected.networkId != INVALID_NETWORK_ID) 
			{
				mWifiManager.connect(mSelected.networkId,mConnectListener);
			}
		} 
		else if (config.networkId != INVALID_NETWORK_ID) 
		{
			if (mSelected != null) 
			{
				mWifiManager.save(config, mSaveListener);
			}
		} 
		else 
		{
			if (mDialog.isEdit() || requireKeyStore(config)) 
			{
				mWifiManager.save(config, mSaveListener);
			} 
			else 
			{
				mWifiManager.connect(config, mConnectListener);
			}
		}

		if (mWifiManager.isWifiEnabled()) 
		{
			mScanner.resume();
		}
		updateAccessPoints();
	}
	
	 void forget() 
	 {
	 	if (mSelected.networkId == INVALID_NETWORK_ID) {
            // Should not happen, but a monkey seems to triger it
            Log.e(TAG, "Failed to forget invalid network " + mSelected.getConfig());
            return;
        }

        mWifiManager.forget(mSelected.networkId, mForgetListener);

        if (mWifiManager.isWifiEnabled()) {
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

	private void saveNetworks() 
	{
	    // Always save the configuration with all networks enabled.
	    enableNetworks();
	    mWifiManager.saveConfiguration();
	    updateAccessPoints();
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
/*
	public void onClick(DialogInterface dialogInterface, int button) {
        if (button == Wifi_Dialog.BUTTON_FORGET && mSelected != null) {
            if (mSelected.networkId != -1)
            forget(mSelected.networkId);
            else { // Start PIN -- Yongle Lai
                if (mSelected.bssid != null)
                {
                }
            }
        } else if (button == Wifi_Dialog.BUTTON_SUBMIT && mDialog != null) {
            WifiConfiguration config = mDialog.getConfig();

            if (config == null) {
                if (mSelected != null && !requireKeyStore(mSelected.getConfig())) {
                    //mWifiManager.enableNetwork(mSelected.networkId, true);
                    connect(mSelected.networkId);
                }
            } else if (config.networkId != -1) {
                if (mSelected != null) {
                    mWifiManager.updateNetwork(config);
                    saveNetworks();
		    connect(mSelected.networkId);
                }
            } else {
                int networkId = mWifiManager.addNetwork(config);
                if (networkId != -1) {
                    mWifiManager.enableNetwork(networkId, false);
                    config.networkId = networkId;
                    if (mDialog.edit || requireKeyStore(config)) {
                        saveNetworks();
                    } else {
                        connect(networkId);
                    }
                }
            }
        }
    }*/


	public void onClick(DialogInterface dialogInterface, int button) {
        if (button == Wifi_Dialog.BUTTON_FORGET && mSelected != null) {
            forget();
        } else if (button == Wifi_Dialog.BUTTON_SUBMIT) {
            submit();
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
		if (mKeyStoreNetworkId != INVALID_NETWORK_ID && KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) { // KeyStore.getInstance().test() == KeyStore.NO_ERROR
			mWifiManager.connect(mKeyStoreNetworkId, mConnectListener);
		}
		mKeyStoreNetworkId = INVALID_NETWORK_ID;

		updateAccessPoints();
    }
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(mReceiver);
		mScanner.pause();
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		

		if (mResetNetworks) {
			enableNetworks();
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
}


