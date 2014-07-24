package com.rockchip.settings.pppoe;

import android.content.Context;
import android.os.Handler;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;

import android.net.pppoe.PppoeManager;
import android.net.ethernet.EthernetManager;
import android.net.EthernetDataTracker;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;

import android.database.Cursor;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.widget.ListView;
import android.view.LayoutInflater;
import com.rockchip.settings.AlterDialogListViewAdapter;
import android.widget.AdapterView;
import android.view.View;
import android.content.pm.PackageManager;


public class PppoeSettings
{
	private Context mContext = null;
	private Handler mHandler = null;

	private static final String REFRESH_PPPOE_CHECKBOX_ACTION = "android.settings.pppoe.REFRESH_PPPOE_CHECKBOX_ACTION";
    private static final String KEY_PPPOE_ENABLER = "pppoe_connect";
    private static final String KEY_PPPOE_PHY_IFACE = "physical_interface";
    private static final String KEY_PPPOE_ADD_ACCOUNT = "pppoe_add_account";
    private static final String DEFAULT_PHY_IFACE = "ethernet";
	
	private PppoeManager mPppoeMgr;
	private EthernetManager mEthMgr;
    private int mPppoeState;
    private String mIface = DEFAULT_PHY_IFACE;
	private int mIfaceSelection = 0;

	private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int USER_INDEX = 2;
    private static final int DNS1_INDEX = 3;
    private static final int DNS2_INDEX = 4;
    private static final int PASSWORD_INDEX = 5;
    private static final Uri PREFERRED_PPPOE_URI = Uri.parse("content://pppoe/accounts/preferaccount");
    private static final String DEFAULT_SORT_ORDER = "name ASC";

	private boolean mPppoeConnect = false;
	private ArrayList<String> mArrayListIface = new ArrayList<String>();

	private AlertDialog mIfaceDialog;
	
	public PppoeSettings(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;

        mPppoeMgr = (PppoeManager) context.getSystemService(Context.PPPOE_SERVICE);
		if (mPppoeMgr == null) 
		{
            LOG("get pppoe manager failed");
            return;
        }
		
        mEthMgr = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);		
        mPppoeState = mPppoeMgr.getPppoeState();

		String[] face = mContext.getResources().getStringArray(R.array.entries_pppoe_phy_iface);
		if(face != null)
		{
			for(int i = 0; i < face.length; i++)
			{
				if(mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI) && face[i].equals("WiFi"))
					mArrayListIface.add(face[i]);
				if(mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_ETHERNET) && face[i].equals("Ethernet"))
					mArrayListIface.add(face[i]);
			}
		}

		if(mArrayListIface.size() == 1)
		{
			String face1 = mArrayListIface.get(0);
			if((face1 != null) && (face1.equals("WiFi")))
				mIface = "wifi";
			else if((face1 != null) && (face1.equals("Ethernet")))
				mIface = "ethernet";
		}
		else if(mArrayListIface.size() == 0)
		{
			mIface = "";
		}
		
		((RKSettings)mContext).updateSettingItem(R.string.pppoe_phy_iface,mIface,
							null,null);
		
       	refreshEnablePppoeCheckBox();
	}

	public void Resume()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(PppoeManager.PPPOE_STATE_CHANGED_ACTION);
		filter.addAction(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
		filter.addAction(REFRESH_PPPOE_CHECKBOX_ACTION);
		mContext.registerReceiver(mPpppoeReceiver, filter);
	}

	public void Pause()
	{
		mContext.unregisterReceiver(mPpppoeReceiver);
	}
	
	private void refreshEnablePppoeCheckBox()
	{
		String summary = null;

		LOG("mPppoeState:"+mPppoeState);

		if (PppoeManager.PPPOE_STATE_DISCONNECTED == mPppoeState ) 
		{
			mPppoeConnect = false;
			((RKSettings)mContext).setSettingItemClickable(R.string.pppoe_connect,true);
			((RKSettings)mContext).updateSettingItem(R.string.pppoe_connect,R.string.disconncect,
							R.string.pppoe_quick_toggle_summary,-1);
		} 
		else if ( PppoeManager.PPPOE_STATE_DISCONNECTING == mPppoeState ) 
		{
			mPppoeConnect = false;
			((RKSettings)mContext).setSettingItemClickable(R.string.pppoe_connect,false);
			((RKSettings)mContext).updateSettingItem(R.string.pppoe_connect,R.string.disconncecting,
							R.string.pppoe_disconnecting,-1);
		} 
		else if ( PppoeManager.PPPOE_STATE_CONNECTING == mPppoeState ) 
		{
			mPppoeConnect = false;
			((RKSettings)mContext).setSettingItemClickable(R.string.pppoe_connect,false);
			((RKSettings)mContext).updateSettingItem(R.string.pppoe_connect,R.string.connecting,
							R.string.pppoe_connecting,-1);
		} 
		else if ( PppoeManager.PPPOE_STATE_CONNECTED == mPppoeState ) 
		{
			mPppoeConnect = true;
			((RKSettings)mContext).setSettingItemClickable(R.string.pppoe_connect,true);
			((RKSettings)mContext).updateSettingItem(R.string.pppoe_connect,R.string.connected,
							R.string.pppoe_connected,-1);
		}

		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}

	private void sendRefreshCheckboxBroadcast() 
	{
		Intent intent = new Intent(REFRESH_PPPOE_CHECKBOX_ACTION);
		mContext.sendBroadcast(intent);
	}

    private void setPppoeEnabled(final boolean enable) 
	{
        LOG("setPppoeEnabled:"+enable);

        if (enable) {
            String[] projection = new String[] { "_id",
                                                 "name",
                                                 "user",
                                                 "dns1",
                                                 "dns2",
                                                 "password", };

            String user = null;
            String dns1 = null;
            String dns2 = null;
            String password = null;

            Cursor c = ((RKSettings)mContext).managedQuery(PREFERRED_PPPOE_URI,
            projection, null, null,
            DEFAULT_SORT_ORDER);

            LOG("c.getCount="+c.getCount());

            if(c.getCount() == 0) {
                Toast.makeText(mContext,
                                R.string.pppoe_invalid_account,
                                Toast.LENGTH_SHORT).show();
                sendRefreshCheckboxBroadcast();
                return;
            }

            c.moveToFirst();
            user = c.getString(USER_INDEX);
            dns1= c.getString(DNS1_INDEX);
            dns2= c.getString(DNS2_INDEX);
            password = c.getString(PASSWORD_INDEX);
            c.close();

            LOG("user="+user);
            if(user.equals("") || password.equals("")) {
                Toast.makeText(mContext,
                                R.string.pppoe_invalid_account,
                                Toast.LENGTH_SHORT).show();
                sendRefreshCheckboxBroadcast();
                return;
            }

            if (mPppoeMgr == null) {
                LOG("mPppoeMgr == null");
                sendRefreshCheckboxBroadcast();
                return;
            }

            String iface;
            if (mIface.equals("wifi")) {
                iface = "wlan0";
                ConnectivityManager connectivity =
                         (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity == null) {
                    LOG("connectivity manager is null");
                    sendRefreshCheckboxBroadcast();
                    return;
                }

                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info == null || !info.isConnected() ||
                    info.getType() != ConnectivityManager.TYPE_WIFI) {
                    Toast.makeText(mContext,
                                    R.string.pppoe_connect_wifi_first,
                                    Toast.LENGTH_SHORT).show();
                    sendRefreshCheckboxBroadcast();
                    return;
                }
            } else if (mIface.equals("ethernet")){
                iface = "eth0";
                if((mEthMgr.getEthernetIfaceState() != EthernetDataTracker.ETHER_IFACE_STATE_UP) ||
                (mEthMgr.getEthernetCarrierState() != 1)) {
                    Toast.makeText(mContext,
                                    R.string.pppoe_connect_eth_first,
                                    Toast.LENGTH_SHORT).show();
                    sendRefreshCheckboxBroadcast();
                    return;
                }
            } else {
                LOG("physical interface abnormal:"+mIface);
                sendRefreshCheckboxBroadcast();
                return;
            }

            mPppoeMgr.setupPppoe(user, iface, dns1, dns2, password);
            mPppoeMgr.startPppoe();
        } else {
            if(mPppoeState == PppoeManager.PPPOE_STATE_CONNECTED) {
                mPppoeMgr.stopPppoe();
            }
        }
    }

	public void onClick(int id)
	{
		switch(id)
		{
			case R.string.pppoe_connect:
				((RKSettings)mContext).setSettingItemClickable(R.string.pppoe_connect,false);
           		setPppoeEnabled(!mPppoeConnect);
				break;
				
			case R.string.pppoe_phy_iface:
				if(mArrayListIface.size() > 1)
					createIfaceDialog();
				else if(mArrayListIface.size() == 1)
				{
					String face = mArrayListIface.get(0);
					if((face != null) && (face.equals("WiFi")))
						mIface = "wifi";
					else if((face != null) && (face.equals("Ethernet")))
						mIface = "ethernet";
				}
				break;
				
			case R.string.pppoe_add_account:
				Intent intent = new Intent(mContext, PppoeAccountsSetting.class); 		
				mContext.startActivity(intent);
				break;
		}
	}

	private AlterDialogListViewAdapter mAdapter;
	private void createIfaceDialog()
	{
		LayoutInflater flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ListView view = (ListView)flater.inflate(R.layout.listview,null);
		view.setBackgroundDrawable(null);
		mAdapter = new AlterDialogListViewAdapter(mContext,mArrayListIface);
		mAdapter.setSelection(mIfaceSelection);
		view.setAdapter(mAdapter);
		view.setOnItemClickListener(mListItemClister);
		
		mIfaceDialog = new AlertDialog.Builder(mContext)
				/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
						.setTitle(R.string.pppoe_phy_iface_dialog_title)
						.setView(view)
						.setPositiveButton(R.string.dlg_ok,new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which)
								{
									if(mIfaceSelection == 0)
										mIface = "ethernet";
									else if(mIfaceSelection == 1)
										mIface = "wifi";
									mIfaceDialog.dismiss();
									((RKSettings)mContext).updateSettingItem(R.string.pppoe_phy_iface,mIface,null,null);
									mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
								}
							})
						.show();
	}

	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			if(mIfaceSelection != position)
			{
				mIfaceSelection = position;
				mAdapter.setSelection(mIfaceSelection);
				mAdapter.notifyDataSetChanged();
			}
		}
	};
	
	private final BroadcastReceiver mPpppoeReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();

			if (action.equals(PppoeManager.PPPOE_STATE_CHANGED_ACTION) )
			{
				mPppoeState = intent.getIntExtra(PppoeManager.EXTRA_PPPOE_STATE, PppoeManager.PPPOE_STATE_DISCONNECTED);
				LOG("mPppoeState = " + mPppoeState);
			} 
			else if (action.equals(REFRESH_PPPOE_CHECKBOX_ACTION)) 
			{
				refreshEnablePppoeCheckBox();
			}
			refreshEnablePppoeCheckBox();
		}
	};


	private void LOG(String msg)
	{
		if(true)
			Log.d("PppoeSettings",msg);
	}
}
