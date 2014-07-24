package com.rockchip.settings;



public class SettingMacroDefine
{
	// UI 刷新有关的宏
	
	// 刷新界面
	public final static int upDateListView = 0;
	// 改变SettingItem的点击状态
	public final static int setSettingItemClickable = upDateListView+1;
	// 给SettingItem自定义View
	public final static int setSettingItemView = setSettingItemClickable+1;
	// 更新SettingItem的状态
	public final static int upSettingItemStatus = setSettingItemView+1;
	// 设置SettingItem的回调函数
	public final static int setSettingItemFucntion = upSettingItemStatus+1;
	// 刷新键盘设置
	public final static int upKeyBoardSetting = setSettingItemFucntion + 1;
	// 刷新备份设置
	public final static int upBackDataSetting= upKeyBoardSetting + 1;
	// 刷新证书状态
	public final static int upCredentialStatusSetting= upBackDataSetting + 1;

	
	public static final int SYSTEM = 0;
	public static final int NETWORK = 1;
	public static final int DEVICE = 2;
	public static final int PERSONAL = 3;

	// 定义Intent发送id时的字符串名称
	public final static String ID = "ID";
}

