package com.rockchip.settings.bluetooth;

import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.SettingItemAddManager;
import com.rockchip.settings.SettingItem;
import com.rockchip.settings.SettingItemClick;
import java.util.WeakHashMap;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.widget.TextView;
import java.util.Collection;
import com.rockchip.settings.RKSettings;
import android.view.View;
import android.text.TextWatcher;
import android.app.AlertDialog;
import android.text.Editable;
import android.widget.EditText;
import android.app.Dialog;
import android.widget.Button;
import android.content.DialogInterface;
import com.rockchip.settings.bluetooth.RenameEditTextPreference;
import com.rockchip.settings.ScreenInformation;
import android.util.Log;



public class BluetoothSettings implements BluetoothCallback
{
	private Context mContext = null;
	private Handler mHandler = null;
    private final IntentFilter mIntentFilter;
	
	private BluetoothEnabler mBluetoothEnabler = null;

	BluetoothDevice mSelectedDevice;

    LocalBluetoothAdapter mLocalAdapter;
    LocalBluetoothManager mLocalManager;

    private BluetoothDiscoverableEnabler mDiscoverableEnabler;

	private BluetoothDeviceFilter.Filter mFilter = BluetoothDeviceFilter.ALL_FILTER;

	final WeakHashMap<CachedBluetoothDevice, RKBluetoothDevice> mDeviceHashMap =
            new WeakHashMap<CachedBluetoothDevice, RKBluetoothDevice>();

	private ArrayList<RKBluetoothDevice> mRKBluetoothDevice = new ArrayList<RKBluetoothDevice>();
	private SettingItem  mPairedDevicesCategory = null;
	private TextView mScanCategorySummary = null;
	private int mParentId = R.string.bluetooth_preference_found_devices;
	private int mCount = 0;

	private EditText mEditText = null;
	final void setFilter(BluetoothDeviceFilter.Filter filter) {
        mFilter = filter;
    }

    final void setFilter(int filterType) {
        mFilter = BluetoothDeviceFilter.getFilter(filterType);
    }
	
	public boolean BluetoothExist()
	{
		  return (mLocalManager != null);
	}
	
	public BluetoothSettings(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
		mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
		
        mLocalManager = LocalBluetoothManager.getInstance(context);
        if(mLocalManager == null)
        	return ;
		mLocalAdapter = mLocalManager.getBluetoothAdapter();
        mBluetoothEnabler = new BluetoothEnabler(context, handler);
		mDiscoverableEnabler = new BluetoothDiscoverableEnabler(context,handler,mLocalAdapter);
		
		getBluetoothAvaibleCategory();
		if(mLocalAdapter != null)
			((RKSettings)mContext).updateSettingItem(R.string.bluetooth_device_advanced_rename_device,mLocalAdapter.getName(),null,null);
	}

	private final BroadcastReceiver mBluetoothSettingsReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) 
			{
				updateDeviceName();
			}
		}

		private void updateDeviceName() 
		{
			if (mLocalAdapter.isEnabled())
			{
				LOG("updateDeviceName(), mLocalAdapter.getName() = "+mLocalAdapter.getName());
				//mMyDevicePreference.setTitle(mLocalAdapter.getName());
			}
		}
	};

	
	public void Resume() 
	{
        if (mLocalManager == null) 
			return;
		
		mContext.registerReceiver(mBluetoothSettingsReceiver, mIntentFilter);
		if (mBluetoothEnabler != null) 
		{
			mBluetoothEnabler.Resume();
		}

		if (mDiscoverableEnabler != null) 
		{
			mDiscoverableEnabler.resume();
		}
        mLocalManager.getEventManager().registerCallback(this);
		updateContent(mLocalAdapter.getBluetoothState(),true);
//		addCachedDevices();
//		startScan();
	}

	private void startScan()
	{
		if (mLocalManager == null) 
			return;
		 
		int state = mLocalAdapter.getBluetoothState();
		if(state == BluetoothAdapter.STATE_ON)
		{
			LOG("startScan()******************************");
			mLocalAdapter.startScanning(true);
		}
	}

	private void stopScan()
	{
		if (mLocalManager == null) 
			return;

		mLocalAdapter.startScanning(false);
	}
	
	public void Pause() 
	{
        if (mLocalManager == null) 
			return;
		
		mContext.unregisterReceiver(mBluetoothSettingsReceiver);
		if (mBluetoothEnabler != null) 
		{
			mBluetoothEnabler.Pause();
		}

		if (mDiscoverableEnabler != null) 
		{
			mDiscoverableEnabler.pause();
		}
		removeAllDevices();
        mLocalManager.getEventManager().unregisterCallback(this);
	}

	public void handleClick(int id)
	{
		if (mLocalManager == null) 
			return;
			
		switch(id)
		{
			case R.string.bluetooth_settings:
				if (mBluetoothEnabler != null) 
				{
					mBluetoothEnabler.onCheckedChanged();
					if(mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON)
					{
						startScan();
					}
				}
				break;
			case R.string.bluetooth_visibility:
				if(mDiscoverableEnabler != null)
					mDiscoverableEnabler.onClick();
				break;
			case R.string.bluetooth_device_advanced_rename_device:
				if(mLocalAdapter == null)
					return ;
				RenameEditTextPreference mRenameDeviceName = new RenameEditTextPreference(); 
				{
					mEditText = new  EditText(mContext);
					mEditText.setText(mLocalAdapter.getName());
					mEditText.addTextChangedListener(mRenameDeviceName);
					AlertDialog mReNameDevice = new AlertDialog.Builder(mContext)
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
								mLocalAdapter.setName(mEditText.getText().toString());
								if(mLocalAdapter != null)
									((RKSettings)mContext).updateSettingItem(R.string.bluetooth_device_advanced_rename_device,mLocalAdapter.getName(),null,null);
								mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
							}
						})
		                .setNegativeButton(R.string.dlg_cancel, null)
		                .show();
					mRenameDeviceName.setDialog(mReNameDevice);
					if((mEditText.getText() != null) && (mEditText.getText().length() > 0) && !mEditText.getText().toString().equals(mLocalAdapter.getName()))
					{
						Button b = mReNameDevice.getButton(AlertDialog.BUTTON_POSITIVE);
						b.setEnabled(true);
					}
					else
					{
						Button b = mReNameDevice.getButton(AlertDialog.BUTTON_POSITIVE);
						b.setEnabled(false);
					}
				}
		}
	}

	private void updateContent(int bluetoothState,boolean scanState)
	{
		// 创建连接的目录
		if(bluetoothState == BluetoothAdapter.STATE_ON)
		{
			mCount = 0;
			int id = 0;
			if(mPairedDevicesCategory == null)
			{
				id = SettingItemAddManager.getInstance().findID();
				mPairedDevicesCategory = new SettingItem(1,R.string.bluetooth_settings_title,id,"",true);

				LayoutInflater flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = (View)flater.inflate(R.layout.bluetooth_category,null);
				if(view != null)
				{
					TextView textView = (TextView)view.findViewById(R.id.bluetooth_category_name);
					textView.setText(R.string.bluetooth_preference_paired_devices);
					textView.setTextSize(ScreenInformation.mScreenWidth/50f*ScreenInformation.mDpiRatio);
				}
				if((mPairedDevicesCategory != null) && (view != null))
				{
					mPairedDevicesCategory.setView(view);
				}
				
				SettingItemAddManager.getInstance().addSettingItem("network",mPairedDevicesCategory,R.string.bluetooth_preference_found_devices,false);
			}

			setFilter(BluetoothDeviceFilter.BONDED_DEVICE_FILTER);
			mParentId = id;
			addCachedDevices();
			if(mCount == 0) // pair的个数为0,删除mPairedDevicesCategory
			{
				if(mPairedDevicesCategory != null)
				{
					SettingItemAddManager.getInstance().deleteSettingItem("network",id);
				}
			}
			
			// 添加扫描到的设备
			mCount = 0;
			setFilter(BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER);
			mParentId = R.string.bluetooth_preference_found_devices;
			addCachedDevices();

			if (mCount == 0) 
			{
				if (scanState) 
				{
					startScan();
				} 
			}

			return ;
		}

		removeAllDevices();
	}

	public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) 
	{
		LOG("onDeviceDeleted()***********************");
		RKBluetoothDevice device = mDeviceHashMap.remove(cachedDevice);
		if(device != null)
		{
			device.removeView();
		}
	}

	public void onDeviceAdded(CachedBluetoothDevice cachedDevice) 
	{
		LOG("onDeviceAdded()********************");
		
		if (mDeviceHashMap.get(cachedDevice) != null) 
		{
			return;
		}
		// Prevent updates while the list shows one of the state messages
		if (mLocalAdapter.getBluetoothState() != BluetoothAdapter.STATE_ON) return;

		if (mFilter.matches(cachedDevice.getDevice()))
		{
			createDevicePreference(cachedDevice);
		}
	}

	private void getBluetoothAvaibleCategory()
	{
		SettingItem item = ((RKSettings)mContext).findSettingItem("network",R.string.bluetooth_preference_found_devices);
		if(item != null)
		{
			item.setOnSettingItemClick(mItemClickListener);
			View view = item.getView();
			if(view != null)
			{
				mScanCategorySummary = (TextView)view.findViewById(R.id.bluetooth_scan_category_summary);
				mScanCategorySummary.setTextSize(ScreenInformation.mScreenWidth/50f*ScreenInformation.mDpiRatio);
			}
		}
			
	}

	private SettingItemClick mItemClickListener = new SettingItemClick()
	{
		public void onItemClick(SettingItem item,int id)
		{
			if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON)
				mLocalAdapter.startScanning(true);
			else if(mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_OFF)
				mLocalAdapter.stopScanning();
		}

		public void onItemLongClick(SettingItem item,int id)
		{
			LOG("onItemLongClick,onItemClick(), id = "+id);
		}
	};
	
	private void updateScanCategoryUi(String summary)
	{
		if(mScanCategorySummary != null)
		{
			if(summary != null)
			{
				mScanCategorySummary.setText(summary);
			}
		}
			
	}

	private void updateScanCategoryUi(int id)
	{
		if(mScanCategorySummary != null)
		{
			if(id != 0)
			{
				mScanCategorySummary.setText(id);
			}
		}
			
	}
	
	public void onScanningStateChanged(boolean started) 
	{
		LOG("onScanningStateChanged(), *******************started = "+started);
		if(started)
			updateScanCategoryUi(R.string.progress_scanning);
		else if(mRKBluetoothDevice.size() == 0)
			updateScanCategoryUi(R.string.bluetooth_no_devices_found);
		else 
			updateScanCategoryUi("");
    }

	public void onBluetoothStateChanged(int bluetoothState) 
	{
		LOG("onBluetoothStateChanged(),*********************** bluetoothState = "+bluetoothState);
		if (bluetoothState == BluetoothAdapter.STATE_OFF) 
		{
			updateScanCategoryUi("");
		}

		updateContent(bluetoothState, true);
	}

	public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState)
	{
		LOG("onDeviceBondStateChanged(), bondState = "+bondState);
		removeAllDevices();
		updateContent(mLocalAdapter.getBluetoothState(), false);
	}

	void createDevicePreference(CachedBluetoothDevice cachedDevice) 
	{
		mCount ++;
		RKBluetoothDevice bluetoothDevice = new RKBluetoothDevice(mContext,mHandler,cachedDevice,mParentId);
		mRKBluetoothDevice.add(bluetoothDevice);
		mDeviceHashMap.put(cachedDevice, bluetoothDevice);
    }

	void addCachedDevices() 
	{
        Collection<CachedBluetoothDevice> cachedDevices =
                mLocalManager.getCachedDeviceManager().getCachedDevicesCopy();
        for (CachedBluetoothDevice cachedDevice : cachedDevices) {
            onDeviceAdded(cachedDevice);
        }
    }
	
	private void removeAllDevices()
	{
		LOG("removeAllDevices()*******************");
		mLocalAdapter.stopScanning();
		
		for(int i = 0; i < mRKBluetoothDevice.size(); i++)
		{
			RKBluetoothDevice device = mRKBluetoothDevice.get(i);
			if(device != null)
			{
				int id = device.getDeviceId();
				SettingItemAddManager.getInstance().deleteSettingItem("network",id);
			}
		}

		if(mPairedDevicesCategory != null)
		{
			SettingItemAddManager.getInstance().deleteSettingItem("network",mPairedDevicesCategory.getId());
			mPairedDevicesCategory = null;
		}
		
		mRKBluetoothDevice.clear();
        mDeviceHashMap.clear();
		mCount = 0;
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}
	
	private void LOG(String msg)
	{
		if(true)
			Log.d("BluetoothSettings",msg);
	}
}
