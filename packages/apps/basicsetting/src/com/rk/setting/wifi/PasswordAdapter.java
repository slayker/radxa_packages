package com.rk.setting.wifi;

import com.rk.setting.R;
import java.util.ArrayList;
import android.content.Context;
import java.util.Map;
import java.util.HashMap;
import android.widget.BaseAdapter;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import com.rk.setting.ScreenInformation;

public class PasswordAdapter extends BaseAdapter
{
	private Context mContext = null;
	private Map<String, ArrayList<CharacterNumber>> mMap = null;
	private int selection = 0;
	private int mIndicator = 0;
	private LayoutInflater flater = null;
	
	public PasswordAdapter(Context context, Map<String, ArrayList<CharacterNumber>> map)
	{
		mContext = context;
		mMap = map;
		flater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public void setTabHostIndicator(int i)
	{
		mIndicator = i;
	}

	public int getTabHostIndicator()
	{
		return mIndicator;
	}
	
	public ArrayList<CharacterNumber> getContent()
	{
		ArrayList<CharacterNumber> array = null;
		if(mMap != null)
		{
			switch(mIndicator)
			{
				case 0:array = mMap.get("lower");break;
				case 1:array = mMap.get("upper");break;
				case 2:array = mMap.get("token");break;
				default:break;
			}
		}

		return array;
	}
	
	public int getCount() 
	{
		ArrayList<CharacterNumber> array = null;
		if(mMap != null)
		{
			switch(mIndicator)
			{
				case 0:
					array = mMap.get("lower");
					if(array != null)
						return array.size();
					return 0;
					
				case 1:
					array = mMap.get("upper");
					if(array != null)
						return array.size();
					return 0;
				case 2:
					array = mMap.get("token");
					if(array != null)
						return array.size();
					return 0;
			}
		}
		return 0;
	}

	public Object getItem(int position)
	{
		if(mMap != null)
		{
			return mMap.get("lower");
		}

		return null;
	}

	public long getItemId(int position) 
	{
		return position;
	}

	public void invalidate()
	{
		notifyDataSetChanged();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		TextView view = (TextView)flater.inflate(R.layout.password, null);
		ArrayList<CharacterNumber> array = getContent();
		if(array != null)
		{
			CharacterNumber charcter = array.get(position);
			view.setText(charcter.getString());
			view.setTag(charcter.getNumber());

			float textSize = ScreenInformation.mScreenWidth/50f*ScreenInformation.mDpiRatio;
			view.setTextSize(textSize);
		}
		return view;
	}
}
