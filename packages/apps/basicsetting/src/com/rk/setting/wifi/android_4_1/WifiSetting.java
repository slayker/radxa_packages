package com.rk.setting.wifi.android_4_1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MyGallery;
import com.rk.setting.wifi.Wifi_Enabler;
import com.rk.setting.CallBackListenner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.Toast;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import com.rk.setting.R;
import android.os.Handler;
import android.os.Message;
import java.util.List;
import android.net.wifi.WifiInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration.Status;
import android.security.Credentials;
import android.security.KeyStore;
import android.content.DialogInterface;
import java.util.concurrent.atomic.AtomicBoolean;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.content.Context;
import android.widget.TabWidget;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import android.widget.TextView;
import android.widget.GridView;
import android.view.KeyEvent;
import android.widget.EditText;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.security.Credentials;
import android.security.KeyStore;
import android.net.NetworkInfo.DetailedState;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import com.rk.setting.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;
import android.app.ProgressDialog;
import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
import com.rk.setting.wifi.*;
import android.util.Log;


public class WifiSetting extends Activity implements DialogInterface.OnClickListener
{
	private final int NO_DEVICE = 0;
	
	private Button mSwitch = null;
	private MyGallery mGallery = null;
	private TextView mTextView = null;
	private Wifi_Enabler mWifiEnabler = null;
	private WifiManager mWifiManager;
	private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;

    // should Next button only be enabled when we have a connection?
    private boolean mEnableNextOnConnection;
//    private boolean mInXlSetupWizard;
	
	private final Scanner mScanner;
	private ArrayList<Access_Point> mAccessPoints = new ArrayList<Access_Point>();
	private WifiScanListViewAdapter mAdapter = null;
	private AtomicBoolean mConnected = new AtomicBoolean(false);

	private DetailedState mLastState;
	private WifiInfo mLastInfo;
	private int mLastPriority;
	private boolean mResetNetworks = false;
	
	private final int INVALID_NETWORK_ID = -1;
	private int mKeyStoreNetworkId = INVALID_NETWORK_ID;

	private Wifi_Dialog mDialog;
	private Access_Point mSelected;
	private int mGallerySelect = 0;

	private View mParentDevice = null;
	private ViewGroup mParentInput = null;

	private TabWidget mTabHost = null;
	private TabWidget mTabOperation = null;
	private Map<String, ArrayList<CharacterNumber>> mMap = new HashMap<String, ArrayList<CharacterNumber>>();
	private GridView mGridView = null;
	private PasswordAdapter mPasswordAdapter = null;
	private TextView mWifiNameView = null;
	private TextView  mSecurityView = null;
	private EditText mPasswordEdit = null;
	private Button mSummitButton = null;

	private String mPassWord = null;
	private int mSecurity;

	private static final String KEYSTORE_SPACE = "keystore://";
	private boolean mEdit = false;
	private View mFocusView = null;
	
	private Button space = null;
	private	Button delete = null;
	private	Button clear = null;

	ProgressDialog mProgressDialog = null;

	private WifiManager.Channel mChannel;
    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;
	
	private void createTitle()
	{
		ImageView image = (ImageView)findViewById(R.id.title_image);
		Bitmap resize = bitMapScale(R.drawable.wifi,ScreenInformation.mDpiRatio);
		image.setScaleType(ImageView.ScaleType.CENTER);
		image.setImageBitmap(resize);

		TextView title = (TextView)findViewById(R.id.title_text);
		title.setTextSize(ScreenInformation.mScreenWidth/25f*ScreenInformation.mDpiRatio);
	}

	private Bitmap bitMapScale(int id,float scaleParameter)
	{
		Bitmap map = BitmapFactory.decodeResource(this.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*scaleParameter;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}
	
	public  WifiSetting() 
	{
		mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mReceiver = new BroadcastReceiver() 
		{
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        handleEvent(context,intent);
		    }
		};
		
		mScanner = new Scanner();
	}
	
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_CLEARABLE_FLAGS);
        setContentView(R.layout.wifi_setting);

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mChannel = mWifiManager.initialize(this, this.getMainLooper(), null);

        mConnectListener = new WifiManager.ActionListener() 
		{
			public void onSuccess() 
			{
			}
			public void onFailure(int reason) 
			{
				Toast.makeText(WifiSetting.this,R.string.wifi_failed_connect_message,Toast.LENGTH_SHORT).show();
			}
		};

        mSaveListener = new WifiManager.ActionListener() 
		{
			public void onSuccess() 
			{
			}
			public void onFailure(int reason) 
			{
				Toast.makeText(WifiSetting.this,R.string.wifi_failed_save_message,Toast.LENGTH_SHORT).show();
			}
		};

        mForgetListener = new WifiManager.ActionListener() 
		{
			public void onSuccess() 
			{
			}
			public void onFailure(int reason) 
			{
				Toast.makeText(WifiSetting.this,R.string.wifi_failed_forget_message,Toast.LENGTH_SHORT).show();
			}
		};
		
		createTitle();
		// init 1
		mParentDevice = (ViewGroup)findViewById(R.id.container_device);

		TextView view = (TextView)findViewById(R.id.switch_text);
		view.setTextSize(ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio);
		
		mWifiEnabler = new Wifi_Enabler(this);
		createSwitch();
		mGallery = (MyGallery)findViewById(R.id.wifi_device);
		mAdapter = new WifiScanListViewAdapter(this,mAccessPoints);
		mGallery.setAdapter(mAdapter);
		mGallery.setOnItemClickListener(mListItemClickListener);
		mGallery.setSpacing(50);
		mTextView = (TextView)findViewById(R.id.device_not_found);

		// init 2
		mParentInput = (ViewGroup)findViewById(R.id.container_input);
		loadResource();
		createGridView();
		createTabHost();
		createButton();
		createWifiInformation();

		mSwitch.requestFocus();
    }

	private AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			Access_Point access = mAccessPoints.get(position);
			mSelected = access;
//			Log.d("wifiSetting","access.networkId = "+access.networkId);//access.networkId != -1
//			Log.d("wifiSetting","access.getState() != null ? Yes or No : "+(access.getState()));
//			Log.d("wifiSetting","access.getLevel() = "+access.getLevel());
			if(access.networkId != -1)
			{
				showDialog(access,false);
			}
			else if(Access_Point.SECURITY_NONE == mSelected.security)
			{
				onClickCallback();
			}
			else
			{
				mParentInput = (ViewGroup)findViewById(R.id.container_input);
				mPasswordEdit.setText(null);
				mSummitButton.setEnabled(false);
				mPassWord = null;
				if(mWifiNameView != null)
					mWifiNameView.setText(mSelected.ssid);
				if(mSecurityView != null)
					mSecurityView.setText(mSelected.getSecurity());
				mSelected.onBindView(mParentInput);
				mParentDevice.setVisibility(View.GONE);
				mParentInput.setVisibility(View.VISIBLE);
				mSecurity = (mSelected == null) ? Access_Point.SECURITY_NONE : mSelected.security;
				
				setTabHostFocus(0);
				mGridView.requestFocus();
				mGridView.setSelection(0);
			}
		}
	};

	private void createSwitch()
	{
		mSwitch = (Button)findViewById(R.id.wifi_switch);
		Bitmap map = bitMapScale(R.drawable.switch_on,ScreenInformation.mDpiRatio);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(map.getWidth(),map.getHeight());
		mSwitch.setLayoutParams(params);
		
		boolean on = mWifiEnabler.getWifiStatus();
		int status = on?R.drawable.switch_on:R.drawable.switch_off;
		if(mSwitch != null)
		{
			mSwitch.setBackgroundResource(status);
		}
		
		mSwitch.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(hasFocus)
				{
					boolean on = mWifiEnabler.getWifiStatus();
					int status = on?R.drawable.switch_on_selected:R.drawable.switch_off_selected;
					if(mSwitch != null)
						mSwitch.setBackgroundResource(status);
				}
				else
				{
					boolean on = mWifiEnabler.getWifiStatus();
					int status = on?R.drawable.switch_on:R.drawable.switch_off;
					if(mSwitch != null)
						mSwitch.setBackgroundResource(status);
				}
			}
		}
		);
		
	}
	
	private void showDialog(Access_Point accessPoint, boolean edit) {
		if (mDialog != null) {
		    mDialog.dismiss();
		}
		mDialog = new Wifi_Dialog(this, this, accessPoint, edit);
		mDialog.show();
	}
	
	public void onResume()
	{
		super.onResume();
		if(mWifiEnabler != null)
		{
			mWifiEnabler.resume();
			mWifiEnabler.setCallBack(new CallBack());
			boolean on = mWifiEnabler.getWifiStatus();
			int status = on?R.drawable.switch_on_selected:R.drawable.switch_off_selected;
			if(mSwitch != null)
				mSwitch.setBackgroundResource(status);
			if((on) && (mAccessPoints.size() > 0) && mGallery != null)
			{
				mGallery.requestFocus();
				mGallery.setSelection(0);
				status = on?R.drawable.switch_on:R.drawable.switch_off;
				if(mSwitch != null)
					mSwitch.setBackgroundResource(status);
			}
		}

		this.registerReceiver(mReceiver, mFilter);
		if (mKeyStoreNetworkId != INVALID_NETWORK_ID && KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) { // KeyStore.getInstance().test() == KeyStore.NO_ERROR
			mWifiManager.connect(mChannel, mKeyStoreNetworkId, mConnectListener);
		}
		mKeyStoreNetworkId = INVALID_NETWORK_ID;

		updateAccessPoints();
	}

	public void onPause()
	{
		super.onPause();
		if(mWifiEnabler != null)
		{
			mWifiEnabler.pause();
		}
		this.unregisterReceiver(mReceiver);
        mScanner.pause();
	}
	
	public void onButtonClick(View v) 
	{
		Log.d("WifiSetting","onButtonClick()*********************");
		ScaleAnimation animation = new ScaleAnimation(1f,0.8f,1f,0.8f,
							Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		animation.setDuration(50);
		v.startAnimation(animation);
		
		if(mWifiEnabler != null)
		{
			if((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) || 
								(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING))
			{
				createProgressDialog();
			}
			mWifiEnabler.onWiFiClick();
		}
	}

	void createProgressDialog()
	{
		mProgressDialog = new ProgressDialog(this);
/*		mProgressDialog.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium);*/
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage(this.getResources().getString(R.string.bluetooth_preference_scan_title));
		mProgressDialog.setIcon(0);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false); 
		mProgressDialog.show();
	}
    private void updateAccessPoints() {
        final int wifiState = mWifiManager.getWifiState();
		
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                // AccessPoints are automatically sorted with TreeSet.
                if(mProgressDialog != null)
            	{
            		mProgressDialog.dismiss();
					mProgressDialog = null;
            	}
                View view = null;
				if(mParentInput.getVisibility() == View.VISIBLE)
				{
					view = mParentInput.findFocus();
				}

				if(mParentDevice.getVisibility() == View.VISIBLE)
				{
					if(!mGallery.isMoving())
					{
						constructAccessPoints();
						if(mAccessPoints.size() != 0)
						{
							mHandler.removeMessages(NO_DEVICE);
							mGallery.setVisibility(View.VISIBLE);
							mTextView.setVisibility(View.GONE);
					//		if(!mGallery.isMoving())
							{
								mGallerySelect = mGallery.getCurrentSelection();
								if(mGallerySelect >= mAccessPoints.size())
									mGallerySelect = mAccessPoints.size()-1;
								if(mGallerySelect < 0)
									mGallerySelect = 0;
								Log.d("WifiSetting","updateAccessPoints(), mGallerySelect = "+mGallerySelect);
								mGallery.setSelection(mGallerySelect);
								mGallery.setStayPosition(true);
								mAdapter.notifyDataSetChanged();
							}
						}
						else
						{
					/*		mGallery.setVisibility(View.GONE);
							mTextView.setVisibility(View.VISIBLE);
							mTextView.setText(R.string.no_device);*/

							mHandler.sendEmptyMessageDelayed(NO_DEVICE,4000);
						}
					}
				}
				else if(mParentInput.getVisibility() == View.VISIBLE)
				{
					if(view != null)
						view.requestFocus();
				}
                break;

            case WifiManager.WIFI_STATE_ENABLING:
				mAccessPoints.clear();
				mGallerySelect = 0;
				mAdapter.notifyDataSetChanged();
				mGallery.setVisibility(View.INVISIBLE);
				mTextView.setVisibility(View.GONE);
      			// set Adapter to null;
                break;

            case WifiManager.WIFI_STATE_DISABLING:
     			mAccessPoints.clear();
	  			mGallerySelect = 0;
	 			mAdapter.notifyDataSetChanged();
				mGallery.setVisibility(View.INVISIBLE);
				mTextView.setVisibility(View.GONE);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
				if(mProgressDialog != null)
            	{
            		mProgressDialog.dismiss();
					mProgressDialog = null;
            	}
     			mAccessPoints.clear();
	 			mGallerySelect = 0;
	 			mAdapter.notifyDataSetChanged();
				mGallery.setVisibility(View.INVISIBLE);
				mTextView.setVisibility(View.GONE);
                break;
        }
    }
	
	private ArrayList<Access_Point> constructAccessPoints() 
	{
		mAccessPoints.clear();
		ArrayList<Access_Point> accessPoints = mAccessPoints;
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
				accessPoint.setCallBack(mUICallBack);
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
					Access_Point access = new Access_Point(this, result);
					access.setCallBack(mUICallBack);
					accessPoints.add(access);
			    }

			}
		}

		Collections.sort(accessPoints);
		return accessPoints;
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
		Log.d("WifiSetting","handleEvent(),action = "+action);
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            //Ignore supplicant state changes when network is connected
            //TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            //introduce a broadcast that combines the supplicant and network
            //network state change events so the apps dont have to worry about
            //ignoring supplicant state change when network is connected
            //to get more fine grained information.
            if (!mConnected.get()) {
                updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                        intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }

     /*       if (mInXlSetupWizard) {
                ((WifiSettingsForSetupWizardXL)getActivity()).onSupplicantStateChanged(intent);
            }*/
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
    //        changeNextButtonState(info.isConnected());
            updateAccessPoints();
            updateConnectionState(info.getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        } 
/*		else if (WifiManager.ERROR_ACTION.equals(action)) {
            int errorCode = intent.getIntExtra(WifiManager.EXTRA_ERROR_CODE, 0);
            switch (errorCode) {
                case WifiManager.WPS_OVERLAP_ERROR:
                    Toast.makeText(context, R.string.wifi_wps_overlap_error,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
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
	
	private void updateConnectionState(DetailedState state) 
	{
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

	void submit()
	{
		final WifiConfiguration config = mDialog.getConfig();

		if (config == null) 
		{
			if (mSelected != null
					&& !requireKeyStore(mSelected.getConfig())
					&& mSelected.networkId != INVALID_NETWORK_ID) 
			{
				mWifiManager.connect(mChannel, mSelected.networkId,mConnectListener);
			}
		} 
		else if (config.networkId != INVALID_NETWORK_ID) 
		{
			if (mSelected != null) 
			{
				mWifiManager.save(mChannel, config, mSaveListener);
			}
		} 
		else 
		{
			if (mDialog.isEdit() || requireKeyStore(config)) 
			{
				mWifiManager.save(mChannel, config, mSaveListener);
			} 
			else 
			{
				mWifiManager.connect(mChannel, config, mConnectListener);
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
		if (mSelected.networkId == INVALID_NETWORK_ID) 
		{
		    return;
		}

		mWifiManager.forget(mChannel, mSelected.networkId, mForgetListener);

		if (mWifiManager.isWifiEnabled()) 
		{
		    mScanner.resume();
		}
		updateAccessPoints();

		// We need to rename/replace "Next" button in wifi setup context.
		//        changeNextButtonState(false);
	}

	private void saveNetworks() 
	{
	    // Always save the configuration with all networks enabled.
	    enableNetworks();
	    mWifiManager.saveConfiguration();
	    updateAccessPoints();
	}
	
	private void enableNetworks() 
	{
        for (int i = mAccessPoints.size() - 1; i >= 0; --i) 
		{
            WifiConfiguration config = ((Access_Point) mAccessPoints.get(i)).getConfig();
            if (config != null && config.status != Status.ENABLED) {
                mWifiManager.enableNetwork(config.networkId, false);
            }
        }
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

	public void onClick(DialogInterface dialogInterface, int button) {
        if (button == Wifi_Dialog.BUTTON_FORGET && mSelected != null) {
            forget();
        } else if (button == Wifi_Dialog.BUTTON_SUBMIT) {
            submit();
        }
    }
	
	private void loadResource()
	{
		ArrayList<CharacterNumber> charater_lower = new ArrayList<CharacterNumber>();
		ArrayList<CharacterNumber> charater_upper = new ArrayList<CharacterNumber>();
		ArrayList<CharacterNumber> token = new ArrayList<CharacterNumber>();
		
		String[] ch = //mContext.getResources().getStringArray(R.array.character);
		{
			"a","b","c","d","e","f","g",
			"h","i","j","k","l","m","n",
			"o","p","q","r","s","t",
			"u","v","w","x","y","z"
		};
		
		for(int i = 0; i < ch.length; i++)
		{
			charater_lower.add(new CharacterNumber(i,ch[i]));
			charater_upper.add(new CharacterNumber(i,ch[i].toUpperCase()));
		}
		String[] number = //mContext.getResources().getStringArray(R.array.number);
		{
			"0","1","2","3","4","5","6","7","8","9"
		};
		int size = charater_lower.size();
		for(int i = 0; i < number.length; i++)
		{
			charater_lower.add(new CharacterNumber(i+size,number[i]));
			charater_upper.add(new CharacterNumber(i+size,number[i]));
		}
		String[] toke = //mContext.getResources().getStringArray(R.array.token);
		{
			"!","#","$","%","&",
			"~","*","\\","<","?","^",
			"_","'",";",":","|","=",
					"[","]","{","}",
					",","+","-",
					"(",")","'","\""
		};
		for(int i = 0; i < toke.length; i++)
		{
			token.add(new CharacterNumber(i,toke[i]));
		}
		String[] toke_normal = //mContext.getResources().getStringArray(R.array.toke_normal);
		{
			".","_","@",".com",".net",".cn"
		};
		size = charater_lower.size();
		int temp = token.size();
		for(int i = 0; i < toke_normal.length; i++)
		{
			charater_lower.add(new CharacterNumber(i+size,toke_normal[i]));
			charater_upper.add(new CharacterNumber(i+size,toke_normal[i]));
			token.add(new CharacterNumber(i+temp,toke_normal[i]));
		}
		
		mMap.put("lower", charater_lower);
		mMap.put("upper", charater_upper);
		mMap.put("token", token);
	}

	private void setTabHostFocus(int focus)
	{
		Log.d("WifiSetting","setTabHostFocus(), focus = "+focus);
		mTabHost.setCurrentTab(focus);
//		mGridView.requestFocus();
//		mGridView.setSelection(0);
		mPasswordAdapter.setTabHostIndicator(focus);
		mPasswordAdapter.invalidate();
		for (int i = 0; i < 3; i++) 
		{
			if (i != focus) 
			{
				View view = mTabHost.getChildAt(i);
				view.setBackgroundResource(R.drawable.background);
			} 
			else 
			{
				View view = mTabHost.getChildAt(i);
				view.setBackgroundResource(R.drawable.background_selected);
			}
		}
	}

	void createButton()
	{
		space = (Button)findViewById(R.id.space);
		delete = (Button)findViewById(R.id.delete);
		clear = (Button)findViewById(R.id.clear);

		float textSize = ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio;
		space.setTextSize(textSize);
		delete.setTextSize(textSize);
		clear.setTextSize(textSize);
		
		space.setOnClickListener(mButtonClickListener);
		delete.setOnClickListener(mButtonClickListener);
		clear.setOnClickListener(mButtonClickListener);

		clear.setNextFocusRightId(R.id.submit);
	}
	
	void createTabHost()
	{
		mTabHost = (TabWidget)findViewById(R.id.tabwidget);
		if (mTabHost == null)
			return;

		for (int i = mTabHost.getChildCount() - 1; i >= 0; i--)
		{
			TextView child = (TextView)mTabHost.getChildAt(i);
			child.setTag(i);
			child.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
			child.setOnFocusChangeListener(new View.OnFocusChangeListener() 
			{
				public void onFocusChange(View v, boolean hasFocus) 
				{
					if (hasFocus) 
					{
						for (int j = 0; j < mTabHost.getTabCount(); j++) 
						{
							if (mTabHost.getChildTabViewAt(j) == v) 
							{
								setTabHostFocus(j);
							}
						}
					}
			}});
        
			child.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v) 
				{
					int current = ((Integer) v.getTag()).intValue();
					setTabHostFocus(current);

				}
			});
		}
		setTabHostFocus(0);
	}
	
	private void createGridView()
	{
		mGridView = (GridView)findViewById(R.id.tabconent);//GridView
		mPasswordAdapter = new PasswordAdapter(this,mMap);
		mGridView.setAdapter(mPasswordAdapter);
		mGridView.setHorizontalSpacing((int)(10*ScreenInformation.mDpiRatio));
		mGridView.setVerticalSpacing((int)(10*ScreenInformation.mDpiRatio));
		mGridView.setNumColumns(7);
		mGridView.requestFocus();
		mGridView.setSelection(0);
		mGridView.setOnItemClickListener(mListItemClister);
		mGridView.setOnItemLongClickListener(mLongListItemClickListener);
	}

	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) 
		{
			ScaleAnimation animation = new ScaleAnimation(1f,0.8f,1f,0.8f,
							Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
			animation.setDuration(50);
			view.startAnimation(animation);
			Log.d("Wifi_Input","view's tag = " + view.getTag() + ",position = " + position
					+ ",arg3 = " + arg3);
			int id = ((Integer) view.getTag()).intValue();
			String key = findString(id);
			if(mPassWord == null)
				mPassWord = key;
			else
				mPassWord += key;
			
			mPasswordEdit.append(key);
			validate();
		}
	};

	private View.OnClickListener mButtonClickListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			ScaleAnimation animation = new ScaleAnimation(1f,0.8f,1f,0.8f,
							Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
			animation.setDuration(50);
			v.startAnimation(animation);
			
			int id = v.getId();
			switch(id)
			{
				case R.id.space:
					if(mPassWord == null)
						mPassWord = " ";
					else
						mPassWord += " ";
					
					mPasswordEdit.append(" ");
					validate();
					break;
				case R.id.delete:
					if(mPassWord != null)
					{
						int len = mPassWord.length();
						if(len > 1)
						{
							String temp = mPassWord.substring(0,mPassWord.length()-1);
							mPassWord = temp;
						}
						else mPassWord = null;
					}
					mPasswordEdit.setText(mPassWord);
					validate();
					break;
				case R.id.clear:
					mPassWord = null;
					mPasswordEdit.setText(mPassWord);
					validate();
					break;
			}
		}
	};
	
	private void validate() {
        // TODO: make sure this is complete.

		Log.d("Wifi_Dialog","mSecurity = "+mSecurity+",mPassword.length() = "+mPasswordEdit.length());
		Log.d("Wifi_Dialog","mAccessPoint.networkId = "+mSelected.networkId);
        if (/*(mSsid != null && mSsid.length() == 0) ||*/
                ((mSelected == null || mSelected.networkId == -1) &&
                ((mSecurity == Access_Point.SECURITY_WEP && mPasswordEdit.length() == 0) ||
                (mSecurity == Access_Point.SECURITY_PSK && mPasswordEdit.length() < 8)))) {
            Log.d("Wifi_Dialog","*******************setEnabled(false)");
            mSummitButton.setEnabled(false);
			mSummitButton.setTextColor(Color.GRAY);
        } else {
        	Log.d("Wifi_Dialog","*******************setEnabled(true)");
            mSummitButton.setEnabled(true);
			mSummitButton.setTextColor(Color.WHITE);
        }
    }
	
	private AdapterView.OnItemLongClickListener mLongListItemClickListener = new AdapterView.OnItemLongClickListener()
	{
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3)
		{
			return true;
	//		int id = ((Integer) view.getTag()).intValue();
	/*		if(mPassWord != null)
			{
				int len = mPassWord.length();
				if(len > 1)
				{
					String temp = mPassWord.substring(0,mPassWord.length()-1);
					mPassWord = temp;
				}
				else mPassWord = null;
			}

			mPassword.setText(mPassWord);
			validate();
			return true;*/
		}
	};


	public WifiUICallBack mUICallBack = new WifiUICallBack()
	{
		public void onCallBack()
		{
			if((mGallery != null) && (mAdapter != null) && (mGallery.getVisibility() == View.VISIBLE))
			{
				mAdapter.notifyDataSetChanged();
			}
		}
	};
	
	private void onClickCallback()
	{
		final WifiConfiguration config = getConfig();

		if (config == null) 
		{
			if (mSelected != null
					&& !requireKeyStore(mSelected.getConfig())
					&& mSelected.networkId != INVALID_NETWORK_ID) 
			{
				mWifiManager.connect(mChannel, mSelected.networkId,mConnectListener);
			}
		} 
		else if (config.networkId != INVALID_NETWORK_ID) 
		{
			if (mSelected != null) 
			{
				mWifiManager.save(mChannel, config, mSaveListener);
			}
		} 
		else 
		{
			if (mEdit || requireKeyStore(config)) 
			{
				mWifiManager.save(mChannel, config, mSaveListener);
			} 
			else 
			{
				mWifiManager.connect(mChannel, config, mConnectListener);
			}
		}

		if (mWifiManager.isWifiEnabled()) 
		{
			mScanner.resume();
		}
		updateAccessPoints();
	}
	
	private void createWifiInformation()
	{
		mWifiNameView = (TextView)findViewById(R.id.wifi_name);
		mSecurityView = (TextView)findViewById(R.id.wifi_security);
		TextView password = (TextView)findViewById(R.id.password_title);
		

		float textSize = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio;
		int height = (int)(ScreenInformation.mScreenWidth/20f*ScreenInformation.mDpiRatio);

		mWifiNameView.setTextSize(textSize+10);
		mSecurityView.setTextSize(textSize);
		password.setTextSize(textSize);

		mPasswordEdit = (EditText)findViewById(R.id.password_edit);
		mPasswordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordEdit.setTextSize(textSize);
		
		mSummitButton = (Button)findViewById(R.id.submit);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
												LinearLayout.LayoutParams.WRAP_CONTENT);
		mSummitButton.setLayoutParams(params);
		mSummitButton.setEnabled(false);
		mSummitButton.setTextSize(textSize);
		mSummitButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				ScaleAnimation animation = new ScaleAnimation(1f,0.8f,1f,0.8f,
							Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
				animation.setDuration(50);
				v.startAnimation(animation);
				
				onClickCallback();
				mParentDevice.setVisibility(View.VISIBLE);
				mParentInput.setVisibility(View.GONE);
			}
		});
		
		mSummitButton.setTextColor(Color.GRAY);
	}

	public WifiConfiguration getConfig() {
        if (mSelected != null && mSelected.networkId != -1 && !mEdit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();
/*
        if (mAccessPoint == null) {
            config.SSID = Access_Point.convertToQuotedString(
                    mSsid.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else */if (mSelected.networkId == -1) {
            config.SSID = Access_Point.convertToQuotedString(
                    mSelected.ssid);
        } else {
            config.networkId = mSelected.networkId;
        }

        switch (mSecurity) {
            case Access_Point.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case Access_Point.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPasswordEdit.length() != 0) {
                    int length = mPasswordEdit.length();
                    String password = mPasswordEdit.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                return config;

            case Access_Point.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPasswordEdit.length() != 0) {
                    String password = mPasswordEdit.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                return config;
/*
            case Access_Point.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                config.eap.setValue((String) mEapMethod.getSelectedItem());

                config.phase2.setValue((mPhase2.getSelectedItemPosition() == 0) ? "" :
                        "auth=" + mPhase2.getSelectedItem());
                config.ca_cert.setValue((mEapCaCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                        (String) mEapCaCert.getSelectedItem());
                config.client_cert.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                        (String) mEapUserCert.getSelectedItem());
                config.private_key.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                        (String) mEapUserCert.getSelectedItem());
                config.identity.setValue((mEapIdentity.length() == 0) ? "" :
                        mEapIdentity.getText().toString());
                config.anonymous_identity.setValue((mEapAnonymous.length() == 0) ? "" :
                        mEapAnonymous.getText().toString());
                if (mPasswordEdit.length() != 0) {
                    config.password.setValue(mPasswordEdit.getText().toString());
                }
                return config;*/
        }
        return null;
    }
	
	public String findString(int id)
	{
		ArrayList<CharacterNumber> array = mPasswordAdapter.getContent();
		for(int i = 0; i < array.size(); i ++)
		{
			CharacterNumber ch = array.get(i);
			if(id == ch.getNumber())
				return ch.getString();
		}

		return null;
	}
	
	public class CallBack implements CallBackListenner
	{
		public void onCallBack()
		{
			if(mParentDevice.getVisibility() == View.VISIBLE)
			{
				boolean on = mWifiEnabler.getWifiStatus();
				int status = on?R.drawable.switch_on_selected:R.drawable.switch_off_selected;
				if(mSwitch != null)
					mSwitch.setBackgroundResource(status);
				if((on) && (mAccessPoints.size() > 0) && mGallery != null)
				{
					mGallery.requestFocus();
					mGallery.setPosition(-1);
					mGallery.setSelection(0);
					status = on?R.drawable.switch_on:R.drawable.switch_off;
					if(mSwitch != null)
						mSwitch.setBackgroundResource(status);
				}
				updateAccessPoints();
			}
		}
	}


	private class Scanner extends Handler 
	{
		private int mRetry = 0;

		void resume() 
		{
			if (!hasMessages(0)) 
			{
				sendEmptyMessage(0);
			}
		}

		void pause() 
		{
			mRetry = 0;
			//mAccessPoints.setProgress(false);
			removeMessages(0);
		}

		public void handleMessage(Message message) 
		{
			if (mWifiManager.startScanActive()) 
			{
				mRetry = 0;
			}
			else if (++mRetry >= 3) 
			{
				mRetry = 0;
				Toast.makeText(WifiSetting.this, R.string.wifi_fail_to_scan,Toast.LENGTH_LONG).show();
				return;
			}
			sendEmptyMessageDelayed(0, 6000);
		}
	}


	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{
				case NO_DEVICE:
					mGallery.setVisibility(View.GONE);
					mTextView.setVisibility(View.VISIBLE);
					mTextView.setText(R.string.no_device);
					break;
			}
		}
	};
	
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		int keyCode = event.getKeyCode();
		if((keyCode == KeyEvent.KEYCODE_BACK) && (mParentInput != null) && (mParentInput.getVisibility() == View.VISIBLE))
		{
			mParentDevice.setVisibility(View.VISIBLE);
			mParentInput.setVisibility(View.GONE);
			mPassWord = null;
			mFocusView = null;
			if(mGallery != null)
				mGallery.requestFocus();
			return true;
		}

		return super.dispatchKeyEvent(event);
	}
}
