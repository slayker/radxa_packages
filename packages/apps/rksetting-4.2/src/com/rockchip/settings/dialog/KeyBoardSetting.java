package com.rockchip.settings.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodManager;

//import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

import android.util.Log;
import com.rockchip.settings.SettingMacroDefine;

public class KeyBoardSetting
{
	private Context mContext = null;
	private Handler mHandler = null;
	private int mId = -1;
	
	public KeyBoardSetting(Context context,Handler handler,int id)
	{
		mContext = context;
		mHandler = handler;
		mId = id;
	}
	
	public String getKeyBoardDefault() {
		final Context context = mContext;
		ContentResolver resolver = context.getContentResolver();
		PackageManager pm = context.getPackageManager();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		List<InputMethodInfo>  imis = imm.getInputMethodList();

		if (resolver == null || imis == null) return null;
		
		final String currentInputMethodId = Settings.Secure.getString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD);
		if (TextUtils.isEmpty(currentInputMethodId)) return null;
		
		for (InputMethodInfo imi : imis) {
			if (currentInputMethodId.equals(imi.getId())) {
				final CharSequence imiLabel = imi.loadLabel(pm);
				final InputMethodSubtype subtype = imm.getCurrentInputMethodSubtype();
				final CharSequence summary = subtype != null
				? TextUtils.concat(subtype.getDisplayName(context,imi.getPackageName(), imi.getServiceInfo().applicationInfo),
				(TextUtils.isEmpty(imiLabel) ?"" : " - " + imiLabel)): imiLabel;
				return summary.toString();
			}
		}
		return null;
	}
	

	public void settingKeyBoard()
	{
		KeyBoardSettingAlterDialogActivity.setHandler(mHandler);
		Intent intent = new Intent(mContext, KeyBoardSettingAlterDialogActivity.class); 
		intent.putExtra(SettingMacroDefine.ID,mId);
		mContext.startActivity(intent);
	}
}
