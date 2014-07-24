package com.rockchip.settings;


import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.view.LayoutInflater;
import android.app.Activity;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

// create by hh@rock-chips

public class ListViewAdapter extends BaseAdapter
{
	private Map<String,ArrayList<SettingItem>> mMap = null;
	private Context mContext = null;
	private LayoutInflater flater = null;
	// 用于指示当前应返回的资源，候选项为system(0),NetWork(1),Display(2),Others(3),这些值在SettingView定义
	private int mIndicator = 0;
	// 用于指示当前点显示目录的级别
	private int mLevel = 0;
	// 用于指示当前父类的id
	private int mParentId = -1;

	public void setParentId(int parent)
	{
		mParentId = parent;
	}

	public int getParentId()
	{
		return mParentId;
	}
	
	public void setLevel(int level)
	{
		mLevel = level;
	}

	public int getLevel()
	{
		return mLevel;
	}
	
	public ListViewAdapter(Context context, Map<String,ArrayList<SettingItem>> map)
	{
		mContext = context;
		mMap = map;
		flater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	public void invalidate()
	{
//		Log.d("ListViewAdapter","invalidate");
		notifyDataSetChanged();
	}

	public void setSelection(int i)
	{
		if((i >= SettingMacroDefine.SYSTEM) && (i <= SettingMacroDefine.PERSONAL))
		{
			mIndicator = i;
		}
		invalidate();
	}

	public int getSelection()
	{
		return mIndicator;
	}
	
	private int getCount(String name)
	{
//		Log.d("ListViewAdapter","getCount(), name = "+name);
		if(name == null)
			return 0;

		int count = 0;
		if(mMap != null)
		{ 
			ArrayList<SettingItem> list = (ArrayList<SettingItem>)mMap.get(name);
			if(list != null)
			{
				for(int i = 0; i < list.size(); i++)
				{
					SettingItem item = list.get(i);
					if((item.mParentId == mParentId) && (mLevel == item.mLevel))
					{
						count ++;
					}
				}
				Log.d("ListViewAdapter","getCount(), size = "+count);
				return count;
			}
		}

		return 0;
	}

	private SettingItem getContentId(int position)
	{
		String name = getContent();
		if((name == null) || (position < 0))
			return null;

		int count = 0;
		if(mMap != null)
		{ 
			ArrayList<SettingItem> list = (ArrayList<SettingItem>)mMap.get(name);
			if(list != null)
			{
				for(int i = 0; i < list.size(); i++)
				{
					SettingItem item = list.get(i);
					if((item.mLevel == mLevel) && (item.mParentId == mParentId))
					{
						if(count == position)
						{
							return item;//item.mId;
						}

						count ++;
					}
				}
			}
		}

		return null;
	}

	public String getContent()
	{
		String name = null;
		switch(mIndicator)
		{
			case SettingMacroDefine.SYSTEM:
				name = "system";
				break;
			case SettingMacroDefine.NETWORK:
				name = "network";
				break;
			case SettingMacroDefine.DEVICE:
				name = "device";
				break;
			case SettingMacroDefine.PERSONAL:
				name = "personal"; 
				break;
		}

		return name;
	}
	
	public int getCount() 
	{
		switch(mIndicator)
		{
			case SettingMacroDefine.SYSTEM:
				return getCount("system");
			case SettingMacroDefine.NETWORK:
				return getCount("network");
			case SettingMacroDefine.DEVICE:
				return getCount("device");
			case SettingMacroDefine.PERSONAL:
				return getCount("personal");
		}
		
		return 0;
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(mMap != null)
		{
			String name = getContent();
			if(name != null)
				return mMap.get(name);
		}

		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
//		Log.d("ListViewAdapter","getView() position = "+position);
		SettingItem item = getContentId(position);
		if(item == null)
			return null;
		// 如果自定义了View,则返回自定义的view来显示
		if(item.getView() != null)
			return item.getView();

		RelativeLayout layout = (RelativeLayout)flater.inflate(R.layout.listview_item,null);		
		int id = item.mId;
		int color = Color.WHITE;
		boolean canClick = item.getClickable();
		layout.setClickable(!canClick);
		if(!canClick) 
		{
			color = Color.GRAY;
		}
		else
		{
			color = Color.WHITE;
		}

		TextView text = null;
		if(item.getStatus() != null)
		{
			text = (TextView)layout.findViewById(R.id.list_status_text);
			text.setVisibility(View.VISIBLE);
			text.setTextColor(color);
			text.setText(item.getStatus());
			text.setTextSize(ScreenInformation.mScreenWidth/50f*ScreenInformation.mDpiRatio);
		}

		if(item.getSummary() != null)
		{
			text = (TextView)layout.findViewById(R.id.summary);
			text.setVisibility(View.VISIBLE);
			text.setTextColor(color);
			text.setText(item.getSummary());
			text.setTextSize(ScreenInformation.mScreenWidth/60f*ScreenInformation.mDpiRatio);
		}

		if(item.getDrawable() != null)
		{
			ImageView statusImg = (ImageView)layout.findViewById(R.id.list_status_img);
			statusImg.setVisibility(View.VISIBLE);
			Bitmap  map = bitMapScale(item.getDrawable(),ScreenInformation.mDpiRatio);
			if(map != null)
				statusImg.setImageBitmap(map);
		}

		if(id != -1)
		{
			TextView textview = (TextView)layout.findViewById(R.id.list_content);
			textview.setTextColor(color);
			textview.setTextSize(ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio);
			if((item.mText != null) && (item.isAdd()))
			{
				textview.setText(item.mText);
			}
			else
			{
				textview.setText(mContext.getResources().getString(id));
			}
			
			layout.setTag(id);
		}
		return layout;
	}
	
	private Bitmap bitMapScale(Bitmap map,float scaleParameter)
	{
		if(map == null)  return null;
		
		float scale = ScreenInformation.mScreenWidth/1280f*scaleParameter;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}
	
	private SettingItem findSettingItem(String content,int id)
	{
		ArrayList<SettingItem> list = (ArrayList<SettingItem>)mMap.get(content);
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				SettingItem item = list.get(i);
				if(item.mId == id)
				{
					return item;
				}
			}
		}

		return null;
	}

	public boolean setSettingItemClickable(int id,boolean click)
	{
		if(mMap != null)
		{
			SettingItem item = null;
			if(((item = findSettingItem("system",id)) != null) || ((item = findSettingItem("network",id)) != null) ||
							((item = findSettingItem("display",id)) != null) || ((item = findSettingItem("others",id)) != null))
			{
				item.setClickable(click);
				return true;
			}
			
		}
		return false;
	}
}
