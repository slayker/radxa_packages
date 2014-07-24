package com.rockchip.settings;

// author:hh@rock-chips.com

// 用于定义Setting中每项点击事件的响应
// 参数object1,object2,object3可以是任何类型的参数,在使用时,请根据实际类型做强制装换
// 返回值Object,请根据实际类型自行装换
public abstract class SettingItemClick
{
	// 短按
	void onItemClick(SettingItem item,int id){};
	// 长按
	void onItemLongClick(SettingItem item,int id){};
}