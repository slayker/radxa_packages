package com.rockchip.settings.pppoe;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.graphics.Color;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.AdapterView;
import android.view.ViewGroup;
import com.rockchip.settings.R;
import java.util.ArrayList;
import com.rockchip.settings.pppoe.PppoeAccountsSetting.PppoeAccounts;
import com.rockchip.settings.ScreenInformation;

public class PppoeAccountAdapter extends BaseAdapter
{
	private Context mContext = null;
	private ArrayList<PppoeAccounts> mAccountArrayList = null;
	private LayoutInflater flater = null;
	private int mSelection = -1;

	public PppoeAccountAdapter(Context context, ArrayList<PppoeAccounts> List)
	{
		mContext = context;
		mAccountArrayList = List;
		flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setSelection(int i)
	{
//		if((i >= 0) && (i < mAccountArrayList.size()))
		{
			mSelection = i;
		}
	}
	
	public int getCount() 
	{
		if(mAccountArrayList != null)
			return mAccountArrayList.size();

		return 0;
	}

	public Object getItem(int position) 
	{
		if(mAccountArrayList != null)
		{
			return mAccountArrayList.get(position);
		}

		return null;
	}
	
	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		RelativeLayout layout = (RelativeLayout)flater.inflate(R.layout.alterdialog_listview_item,null);

		TextView textview = (TextView)layout.findViewById(R.id.list_content);
		ImageView image = (ImageView)layout.findViewById(R.id.list_img);
		TextView summary = (TextView)layout.findViewById(R.id.summary);
		
		PppoeAccounts account = mAccountArrayList.get(position);
		summary.setVisibility(View.VISIBLE);
		summary.setText(account.user);
		summary.setTextSize(ScreenInformation.mScreenWidth/50f*ScreenInformation.mDpiRatio);
		textview.setText(account.name);
		textview.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
		if(mSelection == position)
		{
			image.setVisibility(View.VISIBLE);
			image.setImageResource(R.drawable.selected);
		}
		else
		{
			image.setVisibility(View.GONE);
			image.setImageBitmap(null);
		}

		return layout;
	}
}
