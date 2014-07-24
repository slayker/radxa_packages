package com.rk_itvui.allapp;

import android.app.Activity;
import java.util.ArrayList;
import java.util.LinkedList;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.view.LayoutInflater;

import android.util.DisplayMetrics;
import android.view.WindowManager;

public class AppGridViewAdpter extends BaseAdapter
{
       private static final String TAG = "AppGridViewAdpter:BaseAdapter";

	private Activity mActivity = null;
	private ArrayList<PackageInformation> mList = null;
	private final int EDGE_PADDING = 2;
	private int mTextColor = Color.WHITE;
	private LayoutInflater flater = null;
      
	public AppGridViewAdpter(Activity activity, ArrayList<PackageInformation> list)
	{
		mActivity = activity;
		mList = list;
		flater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public AppGridViewAdpter(Activity activity, LinkedList<PackageInformation> list)
	{
		mActivity = activity;
		mList = new ArrayList<PackageInformation>();
		for(int i = 0; i < list.size(); i++){
			mList.add(list.get(i));
		}
		flater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	void setTextColor(int color)
	{
		mTextColor = color;
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		if(mList != null)
			return mList.size();
		
		return 0;
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(mList != null)
			return mList.get(position);

		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if((mList != null) && (position >= mList.size()))
			return null;

		LinearLayout layout = null;
		if(convertView != null)
		{
			layout = (LinearLayout)convertView;
		}

		layout = (LinearLayout)flater.inflate(R.layout.grid_view_item,null);
		layout.setPadding(EDGE_PADDING, EDGE_PADDING, EDGE_PADDING, EDGE_PADDING);
		layout.setGravity(Gravity.CENTER);

		ImageView image = (ImageView)layout.findViewById(R.id.item_imageview);
		image.setImageDrawable(mList.get(position).getIcon());
			
		float width   = (ScreenInfo.WIDTH/12.0f);
              float height  = (ScreenInfo.HEIGHT/6.0f);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)width, (int)width);
        	image.setScaleType(ScaleType.FIT_XY);
		image.setLayoutParams(params);
		image.setFocusable(false);
		image.setClickable(false);
		
		TextView text = (TextView)layout.findViewById(R.id.item_textview);
		text.setTextColor(Color.WHITE);
		text.setGravity(Gravity.CENTER|Gravity.TOP);
		text.setText(mList.get(position).getAppName());
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams((int)width*2, (int)height);
		text.setLayoutParams(textParams);
		text.setSingleLine();
		text.setEllipsize(TruncateAt.MARQUEE);
		text.setFocusable(false);
		text.setClickable(false);
              text.setTextSize(height/3);

 		float itemwidth   = (ScreenInfo.WIDTH/6.0f);
              float itemheight  = (ScreenInfo.HEIGHT/3.0f);
              AbsListView.LayoutParams itemParams = new AbsListView.LayoutParams((int)itemwidth, (int)itemheight);
		layout.setLayoutParams(itemParams);			
		return layout;
	}
}


