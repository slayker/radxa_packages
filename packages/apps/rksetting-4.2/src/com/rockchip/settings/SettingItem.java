package com.rockchip.settings;


import android.view.View;
import android.graphics.Bitmap;
import com.rockchip.settings.SettingItemClick;

// 定义每个设置项
// author:hh@rock-chips.com
public class SettingItem
{
	// 表示设置项的层级，System,Display,NetWork,Others为第0级，其他的依次类推
	public int mLevel = 0;
	// 用于保存父类的id
	public int mParentId = -1;
	// 用于保存自己的id
	public int mId = -1;
	// 表示该设置项是否可点击
	public boolean mClick = true;
	// 用于显示每个Setting项, 当该值没有被设置时,会采用系统设定的布局显示
	public View mView = null;
	
	// 设置每个Setting项的响应函数,当设置了该值时,点击Setting项，会调用该响应函数来对点击事件作出响应
	// 当该值没有设置时,将会调用ListView的响应函数来响应
	public SettingItemClick mSettingItemClick = null;

	// 该标志位用来表示是否是动态加载的Setting(Setting主Activity初始化时,统一同时加载的我称为静态加载)
	// 某些情况下,要将动态加载的Setting项删除掉,用该标志位来标识是否是动态加载的. true :动态加载; false: 静态加载
	public boolean mAdd = false; 

	private String mSummary = null;
	private String mStatusText = null;
//	private Drawable mDrawable = null;
	private Bitmap mBitmap = null;

	public String mText = null;
	
	public SettingItem(int level,int parent,int id)
	{
		mLevel = level;
		mParentId = parent;
		mId = id;
	}

	public SettingItem(int level,int parent,int id,String text,boolean add)
	{
		mLevel = level;
		mParentId = parent;
		mId = id;
		mText = text;
		mAdd = add;
	}

	public SettingItem(int level,int parent,int id,String status,String summary,Bitmap drawable)
	{
		mLevel = level;
		mParentId = parent;
		mId = id;
		mStatusText = status;
		mSummary = summary;
		mBitmap = drawable;
	}
	
	public void setClickable(boolean canClick)
	{
		mClick = canClick;
	}

	public boolean getClickable()
	{
		return mClick;
	}

	public void setTitle(String text)
	{
		mText = text;
	}
	
	public SettingItem setView(View view)
	{
		mView = view;
		mView.setTag(mId);
		return this;
	}

	public View getView()
	{
		return mView;
	}


	public boolean isAdd()
	{
		return mAdd;
	}

	public void setAddFlag(boolean add)
	{
		mAdd = add;
	}

	public void setOnSettingItemClick(SettingItemClick itemClick)
	{
		mSettingItemClick = itemClick;
	}

	public boolean onSettingItemClick(int id)
	{
		if((mClick) && (mSettingItemClick != null))
		{
			mSettingItemClick.onItemClick(this,id);
			return true;
		}

		return false;
	}

	public boolean onSettingItemLongClick(int id)
	{
		if((mClick) && (mSettingItemClick != null))
		{
			mSettingItemClick.onItemLongClick(this,id);
			return true;
		}

		return false;
	}
	
	public int getId()
	{
		return mId;
	}

	public String getStatus()
	{
		return mStatusText;
	}

	public void setStatus(String    text)
	{
		mStatusText = text;
	}

	public void setSummary(String summary)
	{
		mSummary = summary;
	}

	public String getSummary()
	{
		return mSummary;
	}
	
	public Bitmap getDrawable()
	{
		return mBitmap;
	}

	public void setDrawable(Bitmap drawable)
	{
		mBitmap = drawable;
	}
}
