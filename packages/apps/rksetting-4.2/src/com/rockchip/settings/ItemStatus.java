package com.rockchip.settings;

import android.graphics.drawable.Drawable;
//state discription of setting item
public class ItemStatus
{
	//using string to descripte state of setting item
	private String mStatusText = null;
	//using image to descripte state of setting item
	private Drawable mDrawable = null;

	public ItemStatus(String text,Drawable drawable)
	{
		mStatusText = text;
		mDrawable = drawable;
	}

	public String getString()
	{
		return mStatusText;
	}

	public void setString(String  text)
	{
		mStatusText = text;
	}
	
	
	public Drawable getDrawable()
	{
		return mDrawable;
	}

	public void setDrawable(Drawable drawable)
	{
		mDrawable = drawable;
	}
}

