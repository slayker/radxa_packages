package com.rockchip.settings;

//import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import android.util.Log;

/*
*  用于管理设置项的动态添加和删除
*  hh@rock-chips.com
*/

public class SettingItemAddManager 
{
	// 动态添加或者删除的SettingItem
	private SettingItem mSettingItem = null;
	// 动态添加或者删除的SettingItem的Id
	private int mId = -1;
	// 最大的ID号
	private static final int  MAXID = 0x7fffffff;
	private static final int  MINID = 0x7dffffff;
	// SettingItemAddManager的唯一实例,为了添加或者删除的正确执行,SettingItemAddManager只能有唯一的实例
	private static SettingItemAddManager mInstance = null;

	private ArrayList<Integer> mUserSettingId = new ArrayList<Integer>();

	private Map<String, ArrayList<SettingItem>> mMap = null;
	
	public static SettingItemAddManager getInstance()
	{
		if(mInstance == null)
		{
			mInstance = new SettingItemAddManager();
		}
		
		return mInstance;
	}

	/* 动态添加SettingItem
	* Para: content: 可选的值为 "system" ,"network","device","personal"
	*       item: 添加的SettingItem
	*       id  : 添加的位置,和after配合使用,当after为true时,添加到id表示资源的后面,为false时表示添加到id表示的资源的前面
	*/
	public boolean addSettingItem(String content,SettingItem item,int id,boolean after)
	{
		if((mMap != null) && (item != null) && (content != null))
		{
			ArrayList<SettingItem> arrayList = (ArrayList<SettingItem>)mMap.get(content);
			if(arrayList == null)
				return false;

			int pos = 0;
			for(pos = 0; pos < arrayList.size(); pos++)
			{
				SettingItem settingItem = arrayList.get(pos);
				if(settingItem.mId == id)
				{
					break;
				}
			}

			if(arrayList.size() == pos)
				return false;
			
			if(pos == 0) 
			{
				arrayList.add(item);
			}
			else
			{
				if(after)
					arrayList.add(pos+1,item);
				else
					arrayList.add(pos,item);
			}
			item.setAddFlag(true);
			
			return true;
		}
		return false;
	}

	// 用于删除添加的SettingItem
	public boolean deleteSettingItem(String content,int id)
	{
		if(mMap == null)
			return false;
		Log.d("SettingItemAddManager","deleteSettingItem(), content = "+content+",id = "+id);
		ArrayList<SettingItem> list = (ArrayList<SettingItem>)mMap.get(content);
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				SettingItem item = list.get(i);
				Log.d("SettingItemAddManager","deleteSettingItem(),item id = "+item.mId+", item is add? "+item.isAdd());
				if((item.mId == id) && item.isAdd())
				{
					list.remove(item);
					Log.d("SettingItemAddManager","delete settingItem' id = "+id+" from mMap");
					for(int pos = 0; pos < mUserSettingId.size(); pos ++)
					{
						Integer integer = mUserSettingId.get(pos);
						if(id == integer.intValue())
						{
							mUserSettingId.remove(integer);
							Log.d("SettingItemAddManager","delete id = "+id+" from mUserSettingId");
						}
					}
					
					return true;
				}
			}
		}

		return false;
	}

	public boolean deleteSettingItem(String content,SettingItem item)
	{
		if(item == null)
			return false;
		
		if(content != null)
		{
			return deleteSettingItem(content,item.getId());
		}

		return false;
	}
	
	// 从后往前查找可用的id,返回值:   -1:没有找到可用的id
	public int findID()
	{
		for(int key = MAXID ; key > MINID; key--)
		{
			if(!findID(key))
			{
				mUserSettingId.add(key);
				dump();
				return key;
			}
		}

		return -1;
	}

	private boolean findID(int id)
	{
		for(int i = mUserSettingId.size()-1; i >= 0; i --)
		{
			int key = mUserSettingId.get(i).intValue();
			if(id == key)
				return true;
		}

		return false;
	}
	
	public void setContentMap(Map<String, ArrayList<SettingItem>> map)
	{
		mMap = map;
	}

	private void dump()
	{
		for(int i = mUserSettingId.size()-1; i >= 0; i --)
		{
			int key = mUserSettingId.get(i).intValue();
			Log.d("SettingItemAddManager","id = "+key);
		}
	}
}
