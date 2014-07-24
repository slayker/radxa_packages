package com.rockchip.settings.wifi;

import com.rockchip.settings.R;

import android.content.Context;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.TextView;



public class WifiScanListViewAdapter extends BaseAdapter
{
	private Context mContext = null;
	private ArrayList<Access_Point> mArrayList = null;
	private LayoutInflater flater = null;
	
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

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if(position == 0)
		{
//			TextView View = new TextView(mContext);
//			Access_Point point = mArrayList.get(position);
//			View.setText(point.ssid);
			LinearLayout layout = (LinearLayout)flater.inflate(R.layout.wifi_ap_layout,null);
			TextView view = (TextView)layout.findViewById(R.id.add_wifi_network);
			Access_Point point = mArrayList.get(position);
			view.setText(point.ssid);
			ImageView image = (ImageView)layout.findViewById(R.id.devider);
			TextView hint = (TextView)layout.findViewById(R.id.no_wifi_founded);
			if(mArrayList.size() > 1)
			{
				image.setVisibility(View.GONE);
				hint.setVisibility(View.GONE);
			}
			return layout;
		}
		else
		{
			RelativeLayout layout = (RelativeLayout)flater.inflate(R.layout.wifi_item,null);
			TextView name = (TextView)layout.findViewById(R.id.wifi_name);
			TextView summary = (TextView)layout.findViewById(R.id.wifi_summary);
			Access_Point point = mArrayList.get(position);
			point.onBindView(layout);
			name.setText(point.ssid);
			summary.setText(point.getSummary());
			return layout;
		}
	}
}

