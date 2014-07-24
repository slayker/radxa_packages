package com.rk.setting.wifi;

import com.rk.setting.R;

import android.content.Context;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.MyGallery;
import com.rk.setting.ScreenInformation;
import android.widget.LinearLayout;
import android.util.Log;


public class WifiScanListViewAdapter extends BaseAdapter
{
	private Context mContext = null;
	private ArrayList<Access_Point> mArrayList = null;
	private LayoutInflater flater = null;
	private boolean mFocus = false;
	
	public WifiScanListViewAdapter(Context context,ArrayList<Access_Point> array)
	{
		mContext = context;
		mArrayList = array;
		flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	public void setFocus(boolean focus)
	{
		mFocus = focus;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LinearLayout layout = (LinearLayout)flater.inflate(R.layout.wifi_item,null);
		int width = (int)(((float)(ScreenInformation.mScreenWidth-3*50f-40f*ScreenInformation.mDensityDpi/ScreenInformation.mDefaultDpi))/4f);
		int height = (int)(((float)ScreenInformation.mScreenHeight)*3f/5f-50f);
		MyGallery.LayoutParams layoutParams = new MyGallery.LayoutParams(width-20, height);//256
		layout.setLayoutParams(layoutParams);
	//	Log.d("WifiScanList","width = "+width+",height = "+height);
		float textSize = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio+5f;
		
		TextView name = (TextView)layout.findViewById(R.id.device_name);
		TextView summary = (TextView)layout.findViewById(R.id.device_security);
		TextView status = (TextView)layout.findViewById(R.id.device_status);
		
		name.setTextSize(textSize);
		summary.setTextSize(textSize-10f);
		status.setTextSize(textSize-10f);
		
		Access_Point point = mArrayList.get(position);
		point.onBindView(layout);
		
		name.setText(point.ssid);
		summary.setText(point.getSecurity());
		status.setText(point.getStatus());
		Log.d("WifiScanListViewAdapter","getView mFocus = "+mFocus);
		if(mFocus)
		{
			layout.requestFocus();
		}
		
		return layout;
	}
}

