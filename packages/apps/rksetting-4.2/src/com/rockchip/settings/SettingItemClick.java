package com.rockchip.settings;

// author:hh@rock-chips.com

// ���ڶ���Setting��ÿ�����¼�����Ӧ
// ����object1,object2,object3�������κ����͵Ĳ���,��ʹ��ʱ,�����ʵ��������ǿ��װ��
// ����ֵObject,�����ʵ����������װ��
public abstract class SettingItemClick
{
	// �̰�
	void onItemClick(SettingItem item,int id){};
	// ����
	void onItemLongClick(SettingItem item,int id){};
}