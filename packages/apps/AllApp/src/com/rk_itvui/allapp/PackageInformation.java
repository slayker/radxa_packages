package com.rk_itvui.allapp;

import android.graphics.drawable.Drawable;

public class PackageInformation
{
	//app name
	private String mAppName = null;
	//pack name
	private String mPackageName = null;
	//activity name
	private String mActivityName = null;
	//icon
	private Drawable mIcon = null;
	private String mVersionName = null;
	private int mVersionCode = 0;
	
	public void setAppName(String name)
	{
		mAppName = name;
	}
	
	public void setPackageName(String packageName)
	{
		mPackageName = packageName;
	}
	
	public void setActivityName(String name)
	{
		mActivityName = name;
	}
	
	public void setIcon(Drawable drawable)
	{
		mIcon = drawable;
	}
	
	public void setVersionName(String name)
	{
		mVersionName = name;
	}
	
	public void setVersionCode(int version)
	{
		mVersionCode = version;
	}
	
	public String getAppName()
	{
		return mAppName;
	}
	
	public String getPackageName()
	{
		return mPackageName;
	}
	
	public String getActivityName()
	{
		return mActivityName;
	}
	
	public Drawable getIcon()
	{
		return mIcon;
	}
	
	public String getVersionName()
	{
		return mVersionName;
	}
	
	public int getVersionCode()
	{
		return mVersionCode;
	}
}