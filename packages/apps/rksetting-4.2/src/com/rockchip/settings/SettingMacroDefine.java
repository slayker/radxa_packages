package com.rockchip.settings;



public class SettingMacroDefine
{
	// UI ˢ���йصĺ�
	
	// ˢ�½���
	public final static int upDateListView = 0;
	// �ı�SettingItem�ĵ��״̬
	public final static int setSettingItemClickable = upDateListView+1;
	// ��SettingItem�Զ���View
	public final static int setSettingItemView = setSettingItemClickable+1;
	// ����SettingItem��״̬
	public final static int upSettingItemStatus = setSettingItemView+1;
	// ����SettingItem�Ļص�����
	public final static int setSettingItemFucntion = upSettingItemStatus+1;
	// ˢ�¼�������
	public final static int upKeyBoardSetting = setSettingItemFucntion + 1;
	// ˢ�±�������
	public final static int upBackDataSetting= upKeyBoardSetting + 1;
	// ˢ��֤��״̬
	public final static int upCredentialStatusSetting= upBackDataSetting + 1;

	
	public static final int SYSTEM = 0;
	public static final int NETWORK = 1;
	public static final int DEVICE = 2;
	public static final int PERSONAL = 3;

	// ����Intent����idʱ���ַ�������
	public final static String ID = "ID";
}

