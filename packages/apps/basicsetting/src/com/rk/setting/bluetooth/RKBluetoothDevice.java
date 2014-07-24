package com.rk.setting.bluetooth;


import android.content.Context;
import android.os.Handler;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import com.rk.setting.bluetooth.CachedBluetoothDevice;
import android.util.TypedValue;
import java.util.List;
import com.rk.setting.R;
//import com.rockchip.settings.SettingItemAddManager;
//import com.rockchip.settings.SettingItem;

import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
//import com.rockchip.settings.SettingMacroDefine;
//import com.rockchip.settings.SettingItemClick;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.app.Dialog;
import android.widget.Button;
import com.rk.setting.bluetooth.RenameEditTextPreference;
import com.rk.setting.bluetooth.BluetoothSetting;
import com.rk.setting.bluetooth.BluetoothSetting;
import android.util.Log;


public class RKBluetoothDevice implements CachedBluetoothDevice.Callback
{
	private static int sDimAlpha = Integer.MIN_VALUE;
    private CachedBluetoothDevice mCachedDevice;
	private Context mContext;
	private Handler mHandler;

	public String mTitle = null;
	public int mSummaryId = -1;
	public int mIcon = -1;
	public boolean mEnable = false;

	LayoutInflater mFlater = null;
	private LinearLayout mView = null;

    private AlertDialog mDisconnectDialog;

	private EditText mEditText = null;
	public RKBluetoothDevice(Context context,   Handler handler, CachedBluetoothDevice cachedDevice) 
	{
		mContext = context;
		mHandler = handler;
        if (sDimAlpha == Integer.MIN_VALUE) 
		{
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, outValue, true);
            sDimAlpha = (int) (outValue.getFloat() * 255);
        }

        mCachedDevice = cachedDevice;
        cachedDevice.registerCallback(this);

		mFlater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		onDeviceAttributesChanged();
    }
	
	public CachedBluetoothDevice getCachedDevice() 
	{
        return mCachedDevice;
    }
	
	public void onDeviceAttributesChanged()
	{
		mTitle = mCachedDevice.getName();
		mSummaryId = getConnectionSummary();
		mIcon = getBtClassDrawable();
		mEnable = !mCachedDevice.isBusy();
		mHandler.sendEmptyMessage(BluetoothSetting.UpdateListView);
	}

	public void onClick()
	{
		int bondState = mCachedDevice.getBondState();
        if (mCachedDevice.isConnected()) 
		{
            askDisconnect();
        } 
		else if (bondState == BluetoothDevice.BOND_BONDED) 
		{
            mCachedDevice.connect(true);
        } 
		else if (bondState == BluetoothDevice.BOND_NONE) 
		{
			Log.d("RKBluetoothDevice","pair()*********************");
            pair();
        }
	}

	public void onLongClick()
	{
	//	if(mCachedDevice.getBondState() == BluetoothDevice.BOND_BONDED)
		{
			mEditText = new  EditText(mContext);
			mEditText.setText(mCachedDevice.getName());
			RenameEditTextPreference reName = new RenameEditTextPreference();
			mEditText.addTextChangedListener(reName);
			AlertDialog mReNameDialog = new AlertDialog.Builder(mContext)
		/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
			   	.setView(mEditText)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.bluetooth_device_advanced_rename_device)
                .setPositiveButton(R.string.bluetooth_device_advanced_rename_device, new DialogInterface.OnClickListener() 
                {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mCachedDevice.setName(mEditText.getText().toString());
		//				if(mLocalAdapter != null)
		//					((RKSettings)mContext).updateSettingItem(R.string.bluetooth_device_advanced_rename_device,mLocalAdapter.getName(),null,null);
		//				mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
					}
				})
                .setNegativeButton(R.string.dlg_cancel, null)
                .show(); 
			reName.setDialog(mReNameDialog);
			if((mEditText.getText() != null) && (mEditText.getText().length() > 0) && !mEditText.getText().toString().equals(mCachedDevice.getName()))
			{
				Button b = mReNameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setEnabled(true);
			}
			else
			{
				Button b = mReNameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setEnabled(false);
			}
		}
	}
/*
	private SettingItemClick mItemClickListener = new SettingItemClick()
	{
		public void onItemClick(SettingItem item,int id)
		{
            Log.d("RKBluetoothDevice","mItemClickListener,onItemClick(), id = "+id);
			int bondState = mCachedDevice.getBondState();
	        if (mCachedDevice.isConnected()) 
			{
	            askDisconnect();
	        } 
			else if (bondState == BluetoothDevice.BOND_BONDED) 
			{
	            mCachedDevice.connect(true);
	        } 
			else if (bondState == BluetoothDevice.BOND_NONE) 
			{
	            pair();
	        }
		}

		public void onItemLongClick(SettingItem item,int id)
		{
			Log.d("RKBluetoothDevice","onItemLongClick,onItemClick(), id = "+id);

			if(mCachedDevice.getBondState() == BluetoothDevice.BOND_BONDED)
			{
				mEditText = new  EditText(mContext);
				mEditText.setText(mCachedDevice.getName());
				RenameEditTextPreference reName = new RenameEditTextPreference();
				mEditText.addTextChangedListener(reName);
				AlertDialog mReNameDialog = new AlertDialog.Builder(mContext)
					.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
				   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
				   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)
				   	.setView(mEditText)
	                .setIcon(android.R.drawable.ic_dialog_alert)
	                .setTitle(R.string.bluetooth_device_advanced_rename_device)
	                .setPositiveButton(R.string.bluetooth_device_advanced_rename_device, new DialogInterface.OnClickListener() 
	                {
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mCachedDevice.setName(mEditText.getText().toString());
			//				if(mLocalAdapter != null)
			//					((RKSettings)mContext).updateSettingItem(R.string.bluetooth_device_advanced_rename_device,mLocalAdapter.getName(),null,null);
			//				mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
						}
					})
	                .setNegativeButton(R.string.dlg_cancel, null)
	                .show(); 
				reName.setDialog(mReNameDialog);
				if((mEditText.getText() != null) && (mEditText.getText().length() > 0) && !mEditText.getText().toString().equals(mCachedDevice.getName()))
				{
					Button b = mReNameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
					b.setEnabled(true);
				}
				else
				{
					Button b = mReNameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
					b.setEnabled(false);
				}
			}
		}
	};*/


	private void pair() 
	{
        if (!mCachedDevice.startPairing()) 
		{
            Utils.showError(mContext, mCachedDevice.getName(),
                    R.string.bluetooth_pairing_error_message);
        }
    }

	private void askDisconnect() 
	{
        Context context = mContext;
        String name = mCachedDevice.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(R.string.bluetooth_device);
        }
        String message = context.getString(R.string.bluetooth_disconnect_all_profiles, name);
        String title = context.getString(R.string.bluetooth_disconnect_title);

        DialogInterface.OnClickListener disconnectListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCachedDevice.disconnect();
            }
        };

        mDisconnectDialog = Utils.showDisconnectDialog(context,
                mDisconnectDialog, disconnectListener, title, Html.fromHtml(message));
    }
	
	private int getConnectionSummary() 
	{
        final CachedBluetoothDevice cachedDevice = mCachedDevice;

        boolean profileConnected = false;       // at least one profile is connected
        boolean a2dpNotConnected = false;       // A2DP is preferred but not connected
        boolean headsetNotConnected = false;    // Headset is preferred but not connected

        for (LocalBluetoothProfile profile : cachedDevice.getProfiles()) 
		{
            int connectionStatus = cachedDevice.getProfileConnectionState(profile);

            switch (connectionStatus) 
			{
                case BluetoothProfile.STATE_CONNECTING:
                case BluetoothProfile.STATE_DISCONNECTING:
                    return Utils.getConnectionStateSummary(connectionStatus);

                case BluetoothProfile.STATE_CONNECTED:
                    profileConnected = true;
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    if (profile.isProfileReady() && profile.isPreferred(cachedDevice.getDevice())) 
					{
                        if (profile instanceof A2dpProfile) {
                            a2dpNotConnected = true;
                        } else if (profile instanceof HeadsetProfile) {
                            headsetNotConnected = true;
                        }
                    }
                    break;
            }
        }

        if (profileConnected) 
		{
            if (a2dpNotConnected && headsetNotConnected) 
			{
                return R.string.bluetooth_connected_no_headset_no_a2dp;
            } else if (a2dpNotConnected) {
                return R.string.bluetooth_connected_no_a2dp;
            } else if (headsetNotConnected) {
                return R.string.bluetooth_connected_no_headset;
            } else {
                return R.string.bluetooth_connected;
            }
        }

        switch (cachedDevice.getBondState()) {
            case BluetoothDevice.BOND_BONDING:
                return R.string.bluetooth_pairing;

            case BluetoothDevice.BOND_BONDED:
            case BluetoothDevice.BOND_NONE:
            default:
                return 0;
        }
    }

	private int getBtClassDrawable() {
        BluetoothClass btClass = mCachedDevice.getBtClass();
        if (btClass != null) {
            switch (btClass.getMajorDeviceClass()) {
                case BluetoothClass.Device.Major.COMPUTER:
                    return R.drawable.ic_bt_laptop;

                case BluetoothClass.Device.Major.PHONE:
                    return R.drawable.ic_bt_cellphone;

                case BluetoothClass.Device.Major.PERIPHERAL:
                    return HidProfile.getHidClassDrawable(btClass);

                case BluetoothClass.Device.Major.IMAGING:
                    return R.drawable.ic_bt_imaging;

                default:
                    // unrecognized device class; continue
            }
        } else {
            Log.w("RKBluetoothDevice", "mBtClass is null");
        }

        List<LocalBluetoothProfile> profiles = mCachedDevice.getProfiles();
        for (LocalBluetoothProfile profile : profiles) {
            int resId = profile.getDrawableResource(btClass);
            if (resId != 0) {
                return resId;
            }
        }
        if (btClass != null) {
            if (btClass.doesClassMatch(BluetoothClass.PROFILE_A2DP)) {
                return R.drawable.ic_bt_headphones_a2dp;

            }
            if (btClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET)) {
                return R.drawable.ic_bt_headset_hfp;
            }
        }
        return 0;
    }

	public boolean equals(RKBluetoothDevice device)
	{
		if(device != null)
			return device.mCachedDevice.equals(this.mCachedDevice);
		return false;
	}
}
