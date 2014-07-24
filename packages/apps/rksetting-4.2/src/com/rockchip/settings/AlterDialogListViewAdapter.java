package com.rockchip.settings;


import com.rockchip.settings.R;
import android.content.Context;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

/*
*create by hh
*/

public class AlterDialogListViewAdapter extends BaseAdapter
{
	private Context mContext = null;
	private ArrayList<String> mArrayList = null;
	private LayoutInflater flater = null;
	private int mSelection = -1;
	
	public AlterDialogListViewAdapter(Context context, ArrayList<String> array)
	{
		mContext = context;
		mArrayList = array;
		flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setSelection(int i)
	{
		if((i >= 0) && (i < mArrayList.size()))
		{
			mSelection = i;
		}
	}
	
	public int getCount() 
	{
		Log.d("AlterDialogListViewAdapter","getCount() = "+mArrayList.size());
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
		RelativeLayout layout = (RelativeLayout)flater.inflate(R.layout.alterdialog_listview_item,null);
		TextView textview = (TextView)layout.findViewById(R.id.list_content);
		ImageView image = (ImageView)layout.findViewById(R.id.list_img);

		textview.setText(mArrayList.get(position));
		textview.setTextSize(ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio);
		if(mSelection == position)
		{
			image.setVisibility(View.VISIBLE);
			image.setImageResource(R.drawable.selected);
		}
		else
		{
			image.setVisibility(View.INVISIBLE);
			image.setImageBitmap(null);
		}
		return layout;
	}
}
