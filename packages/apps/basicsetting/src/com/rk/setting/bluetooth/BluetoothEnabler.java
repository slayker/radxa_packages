package com.rk.setting.bluetooth;

import android.content.Context;
import android.os.Handler;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.bluetooth.BluetoothAdapter;
import android.provider.Settings;
import android.os.Message;
import com.rk.setting.R;
import com.rk.setting.bluetooth.BluetoothSetting;
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
			LOG("mBluetoothEnablerReceiver(), state = "+state);
			handleStateChanged(state);
		}
    };

	public void Resume() 
	{
		if (mLocalAdapter == null)
		{
			return;
		}

		// Bluetooth state is not sticky, so set it manually
		handleStateChanged(mLocalAdapter.getBluetoothState());

		mContext.registerReceiver(mBluetoothEnablerReceiver, mIntentFilter);
	}

    public void Pause() 
	{
		if (mLocalAdapter == null) 
		{
			return;
		}

		mContext.unregisterReceiver(mBluetoothEnablerReceiver);
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

		
        if (mLocalAdapter != null) {
			int state = mLocalAdapter.getBluetoothState();
			boolean on = (state == BluetoothAdapter.STATE_ON);
            mLocalAdapter.setBluetoothEnabled(!on);
        }
		
    }
	
	void handleStateChanged(int state) 
	{
		LOG("handleStateChanged(),state = "+state);
		Message msg = new Message();
		msg.what = BluetoothSetting.Update_BluetoothState;
		msg.arg1 = state;
		mHandler.sendMessage(msg);
/*		
        switch (state) 
		{
            case BluetoothAdapter.STATE_TURNING_ON:
				LOG("BluetoothAdapter.STATE_TURNING_ON");
//				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turning_on,-1,-1);
//				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
                break;
            case BluetoothAdapter.STATE_ON:
				msg.arg1 = BluetoothSetting.BlueTooth_On;
//				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turn_on,-1,-1);
//				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
				LOG("BluetoothAdapter.STATE_ON");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
				LOG("BluetoothAdapter.STATE_TURNING_OFF");
//				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turning_off,-1,-1);
//				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
                break;
            case BluetoothAdapter.STATE_OFF:
				LOG("BluetoothAdapter.STATE_OFF");
//				((RKSettings)mContext).updateSettingItem(R.string.bluetooth_settings,R.string.turn_off,-1,-1);
//				((RKSettings)mContext).setSettingItemClickable(R.string.bluetooth_settings,true);
                break;
        }*/
		
   }

	private void LOG(String msg)
	{
		if(true)
			Log.d("BluetoothEnabler",msg);
	}
}

