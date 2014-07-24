package com.rockchip.settings.bluetooth;

import android.content.Context;
import android.os.Handler;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.bluetooth.BluetoothAdapter;
//import com.rockchip.settings.WirelessSettings;
import android.provider.Settings;
import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;
import android.util.Log;



public class BluetoothEnabler
{
	private Context mContext = null;
	private Handler mHandler = null;
	private final LocalBluetoothAdapter mLocalAdapter;
    private final IntentFilter mIntentFilter;
	
	public BluetoothEnabler(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;

		LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context);
		if (manager == null) 
		{
			// Bluetooth is not supported
			mLocalAdapter = null;
		} 
		else
		{
			mLocalAdapter = manager.getBluetoothAdapter();
		}
		mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

		if(mLocalAdapter != null)
			handleStateChanged(mLocalAdapter.getBluetoothState());
	}

	private final BroadcastReceiver mBluetoothEnablerReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			handleStateChanged(state);
		}
    };

	public void Resume() 
	{
		if (mLocalAdapter == null)
		{
	//      mSwitch.setEnabled(false);
			return;
		}

		// Bluetooth state is not sticky, so set it manually
		handleStateChanged(mLocalAdapter.getBluetoothState());

		mContext.registerReceiver(mBluetoothEnablerReceiver, mIntentFilter);
		//     mSwitch.setOnCheckedChangeListener(this);
	}

    public void Pause() 
	{
		if (mLocalAdapter == null) 
		{
			return;
		}

		mContext.unregisterReceiver(mBluetoothEnablerReceiver);
//		mSwitch.setOnCheckedChangeListener(null);
	}

	public void onCheckedChanged()
	{
        // Show toast message if Bluetooth is not allowed in airplane mode
    /*    if (isChecked &&
                !WirelessSettings.isRadioAllowed(mContext, Settings.System.RADIO_BLUETOOTH)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off
            buttonView.setChecked(false);
        }*/

		
		LOG("onCheckedChanged");
        if (mLocalAdapter != null) {
			int state = mLocalAdapter.getBluetoothState();
			boolean on = (state == BluetoothAdapter.STATE_ON);
            mLocalAdapter.setBluetoothEnabled(!on);
        }
		((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,false);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
 //       mSwitch.setEnabled(false);
    }
	
	void handleStateChanged(int state) 
	{
		LOG("handleStateChanged(),state = "+state);
        switch (state) 
		{
            case BluetoothAdapter.STATE_TURNING_ON:
				LOG("BluetoothAdapter.STATE_TURNING_ON");
				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turning_on,-1,-1);
				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
     //           mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turn_on,-1,-1);
				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
				LOG("BluetoothAdapter.STATE_ON");
     //           mSwitch.setChecked(true);
     //           mSwitch.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
				LOG("BluetoothAdapter.STATE_TURNING_OFF");
				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turning_off,-1,-1);
				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
     //           mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
				LOG("BluetoothAdapter.STATE_OFF");
				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turn_off,-1,-1);
				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
     //           mSwitch.setChecked(false);
     //           mSwitch.setEnabled(true);
                break;
     //       default:
     //           mSwitch.setChecked(false);
     //            mSwitch.setEnabled(true);
        }

		
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
    }

	private void LOG(String msg)
	{
		if(true)
			Log.d("BluetoothEnabler",msg);
	}
}

