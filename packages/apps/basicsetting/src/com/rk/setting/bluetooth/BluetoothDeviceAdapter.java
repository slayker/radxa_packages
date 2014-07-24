package com.rk.setting.bluetooth;

import com.rk.setting.R;
import android.widget.BaseAdapter;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;
import java.util.ArrayList;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MyGallery;
import com.rk.setting.ScreenInformation;

import com.rk.setting.bluetooth.RKBluetoothDevice;


public class BluetoothDeviceAdapter extends BaseAdapter
{
	private Context mContext = null;
	private ArrayList<RKBluetoothDevice> mArrayList = null;
	private LayoutInflater mFlater = null;
	
	public BluetoothDeviceAdapter(Context context,ArrayList<RKBluetoothDevice> array)
	{
		mContext = context;
		mArrayList = array;
		mFlater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() 
	{
		if(mArrayList != null)
			return mArrayList.size();

		return 0;
	}

	public Object getItem(int position) 
	{
		if(mArrayList != null)
			return mArrayList.get(position);

		return null;
	}

	public long getItemId(int position) 
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View mView = (LinearLayout)mFlater.inflate(R.layout.bluetooth_device,null);
		
		int width = (int)(((float)(ScreenInformation.mScreenWidth-3*50f-40f*ScreenInformation.mDensityDpi/ScreenInformation.mDefaultDpi))/4f);
		int height = (int)(((float)ScreenInformation.mScreenHeight)*3f/5f-50f);
		MyGallery.LayoutParams layoutParams = new MyGallery.LayoutParams(width-20, height);//256
		mView.setLayoutParams(layoutParams);
		
		if(mView != null)
		{	
			RKBluetoothDevice device = mArrayList.get(position);
			ImageView image = (ImageView)mView.findViewById(R.id.device_img);

			width = (int)(ScreenInformation.mScreenWidth/10f);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,width);
			image.setLayoutParams(params);
			
			if(image != null)
			{
				if(device.mIcon != 0)
					image.setImageDrawable(mContext.getResources().getDrawable(device.mIcon));
				else image.setImageDrawable(null);
			}

			TextView title = (TextView)mView.findViewById(R.id.device_name);
			float size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio;
			title.setTextSize(size);
			if(title != null)
			{
				title.setText(device.mTitle);
			}

			TextView summary = (TextView)mView.findViewById(R.id.device_summary);
			summary.setTextSize(size);
			if(summary != null)
			{
				if(device.mSummaryId != 0)
				{
					summary.setText(mContext.getResources().getString(device.mSummaryId));
				}
			}
/*
			ImageView profile = (ImageView)mView.findViewById(R.id.device_profile);
			if(profile != null)
			{
				if (mCachedDevice.getBondState() == BluetoothDevice.BOND_BONDED)
				{
					profile.setVisibility(View.VISIBLE);
				}
			}*/
		}

		return mView;

	}
}
