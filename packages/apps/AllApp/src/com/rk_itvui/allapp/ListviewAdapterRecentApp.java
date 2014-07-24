package com.rk_itvui.allapp;

import java.util.ArrayList;
import java.util.LinkedList;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListviewAdapterRecentApp extends BaseAdapter{

	private LinkedList<PackageInformation> mList = null;
       private ArrayList<PackageInformation> mApp = null;

	private final int EDGE_PADDING = 2;
	private LayoutInflater mFlater = null;
	
	public ListviewAdapterRecentApp(Activity activity,
                      LinkedList<PackageInformation> recent, ArrayList<PackageInformation> app){
              mApp = app;
		mList = recent;
		mFlater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(position>=mList.size()){
			return null;
		}
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		if(position>=mList.size()){
			return -1;
		}
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if((mList != null) && (position >= mList.size()))
			return null;

		LinearLayout layout = null;
		if(convertView != null)
		{
			layout = (LinearLayout)convertView;
		}
		
		layout = (LinearLayout)mFlater.inflate(R.layout.listview_item,null);
		layout.setPadding(EDGE_PADDING, EDGE_PADDING, EDGE_PADDING, EDGE_PADDING);
		layout.setGravity(Gravity.CENTER);

              for(int i= 0; i < mApp.size(); i++){
                    if(mApp.get(i).getAppName().equals(mList.get(position).getAppName())){
                            ImageView image = (ImageView)layout.findViewById(R.id.widget_icon);
                            image.setImageDrawable(mApp.get(i).getIcon());
                            break;
                    }
              }
	
		TextView text = (TextView)layout.findViewById(R.id.widget_name);
		text.setText(mList.get(position).getAppName());
		
		return layout;
	}

}
