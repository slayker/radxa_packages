package com.rockchip.settings.bluetooth;


import android.content.Context;
import android.os.Handler;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import com.rockchip.settings.bluetooth.CachedBluetoothDevice;
import android.util.TypedValue;
import java.util.List;
import com.rockchip.settings.R;
import com.rockchip.settings.SettingItemAddManager;
import com.rockchip.settings.SettingItem;

import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.SettingItemClick;
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
import com.rockchip.settings.bluetooth.RenameEditTextPreference;
import com.rockchip.settings.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;


public class RKBluetoothDevice implements CachedBluetoothDevice.Callback
{
	private static int sDimAlpha = Integer.MIN_VALUE;
    private CachedBluetoothDevice mCachedDevice;
	private Context mContext;
	private Handler mHandler;
	private int mId = 0;

	public String mTitle = null;
	public int mSummaryId = -1;
	public int mIcon = -1;
	public boolean mEnable = false;

	LayoutInflater mFlater = null;
	private LinearLayout mView = null;
	private SettingItem mItem = null;

	private OnClickListener mOnSettingsClickListener;

    private AlertDialog mDisconnectDialog;

	private EditText mEditText = null;
	public RKBluetoothDevice(Context context,   Handler handler, CachedBluetoothDevice cachedDevice,int parentId) 
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

		mId = SettingItemAddManager.getInstance().findID();
		Log.d("RKBluetoothDevice","RKBluetoothDevice() ,id = "+mId);
		mFlater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		mItem = new SettingItem(1,R.string.bluetooth_settings_title,mId,"",true);
		mItem.setOnSettingItemClick(mItemClickListener);
		SettingItemAddManager.getInstance().addSettingItem("network",mItem,parentId,true);
		onDeviceAttributesChanged();
    }
	
	public CachedBluetoothDevice getCachedDevice() 
	{
        return mCachedDevice;
    }


	public int getDeviceId()
	{
		return mId;
	}
	
	public void onDeviceAttributesChanged()
	{
		mTitle = mCachedDevice.getName();
		mSummaryId = getConnectionSummary();
		mIcon = getBtClassDrawable();
		mEnable = !mCachedDevice.isBusy();
		getView();
	}

	private Bitmap bitMapScale(int id,float scaleParameter)
	{
		Bitmap map = BitmapFactory.decodeResource(mContext.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*scaleParameter;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}
	
	private void getView()
	{
		if(mId == -1)
			return;
		
		mView = (LinearLayout)mFlater.inflate(R.layout.bluetooth_device,null);
		if(mView != null)
		{
			ImageView image = (ImageView)mView.findViewById(R.id.device_img);
			if(image != null)
			{
				if(mIcon != 0)
				{
					Bitmap map = bitMapScale(mIcon,1f);
					image.setImageBitmap(map);
				}
				else image.setImageDrawable(null);
			}

			TextView title = (TextView)mView.findViewById(R.id.device_name);
			if(title != null)
			{
				title.setText(mTitle);
				title.setTextSize(ScreenInformation.mScreenWidth/50f*ScreenInformation.mDpiRatio);
			}

			TextView summary = (TextView)mView.findViewById(R.id.device_summary);
			if(summary != null)
			{
				if(mSummaryId != 0)
				{
					summary.setText(mContext.getResources().getString(mSummaryId));
					summary.setTextSize(ScreenInformation.mScreenWidth/60f*ScreenInformation.mDpiRatio);
				}
			}

			ImageView profile = (ImageView)mView.findViewById(R.id.device_profile);
			if(profile != null)
			{
				if (mCachedDevice.getBondState() == BluetoothDevice.BOND_BONDED)
				{
					profile.setVisibility(View.VISIBLE);
		//			profile.setClickable(true);
					profile.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							Log.d("RKBluetoothDevice","**********************profile is click()");
			                Bundle args = new Bundle(1);
			                args.putParcelable(DeviceProfilesSettings.EXTRA_DEVICE, mCachedDevice.getDevice());

			        //        ((PreferenceActivity)mContext).startPreferencePanel(DeviceProfilesSettings.class.getName(), args,
			       //                 R.string.bluetooth_device_advanced_title, null, null, 0);
				   /*
							Intent intent = new Intent(Intent.ACTION_MAIN);
				  	        intent.setClass(mContext, DeviceProfilesSettings.class.getClass());
					        intent.putExtra(":android:show_fragment", DeviceProfilesSettings.class.getName());
					        intent.putExtra(":android:show_fragment_args", args);
					        intent.putExtra(":android:show_fragment_title", R.string.bluetooth_device_advanced_title);
					        intent.putExtra(":android:show_fragment_short_title", 0);
					        intent.putExtra(":android:no_headers", true);

							mContext.startActivity(intent);*/
						}
					}
					);
				}
			}
			mView.setTag(mId);
		}
		mItem.setView(mView);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}

	public void removeView()
	{
		SettingItemAddManager.getInstance().deleteSettingItem("network",mId);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}


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
	};


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

	public void setId(int id)
	{
		mId = id;
	}

	public int getId()
	{
		return mId;
	}
}
