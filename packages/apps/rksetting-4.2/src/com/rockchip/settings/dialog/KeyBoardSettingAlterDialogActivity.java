package com.rockchip.settings.dialog;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.content.Context;
import android.content.res.Configuration;
import android.content.pm.ApplicationInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import android.widget.ListView;
import java.util.List;
import com.rockchip.settings.R;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.view.View;
import android.content.Intent;
import android.util.Log;
import com.rockchip.settings.SettingMacroDefine;

public class KeyBoardSettingAlterDialogActivity extends AlertActivity
{
	private boolean mHaveHardKeyboard = false;
	private List<InputMethodInfo> mInputMethodList;

	private String mLastInputMethodId;
	private String mLastTickedInputMethodId;

	private ListView mListView = null;
	private AlterDialogListViewAdapter mAdapter = null;
	private ArrayList<String> mArrayList = new ArrayList<String>();
	private int mSelection = -1;
	private static Handler mHandler = null;
	private int mID = -1;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		int margin = -5;
		Intent intent = this.getIntent();
		mID = intent.getIntExtra(SettingMacroDefine.ID,-1);
		
		onCreateIMM();
		createListView();
		mAlert.setView(mListView,margin,margin,margin,margin);
		
		this.setupAlert();
	}

	public static void setHandler(Handler handler)
	{
		mHandler = handler;
	}
	
	private boolean isSystemIme(InputMethodInfo property) 
	{
        return (property.getServiceInfo().applicationInfo.flags
                & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

	private void onCreateIMM() 
	{
		mLastInputMethodId = Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mInputMethodList = imm.getInputMethodList();
		int N = (mInputMethodList == null ? 0 : mInputMethodList.size());
		for (int i = 0; i < N; ++i) {
			InputMethodInfo property = mInputMethodList.get(i);
			String prefKey = property.getId();
			if(-1 != prefKey.indexOf("com.google.android.voicesearch")){
				mInputMethodList.remove(i);
				break;
			}
		}

		N = (mInputMethodList == null ? 0 : mInputMethodList.size());
		for (int i = 0; i < N; ++i) {
			InputMethodInfo property = mInputMethodList.get(i);
			String prefKey = property.getId();
			CharSequence label = property.loadLabel(getPackageManager());
			boolean systemIME = isSystemIme(property);
			// Add a check box.
			// Don't show the toggle if it's the only keyboard in the system, or it's a system IME.
			Configuration config = this.getResources().getConfiguration();
			if (config.keyboard == Configuration.KEYBOARD_QWERTY) {
				mHaveHardKeyboard = true;
			}
			if (mHaveHardKeyboard || (N > 1)) {
				LOGD("onCreateIMM,label = "+label.toString());
				mArrayList.add(label.toString());
				if((prefKey!= null) && (mLastInputMethodId != null) &&(prefKey.equals(mLastInputMethodId)))
				{
					mSelection = i;
				}
			}
		}
	}

	private void createListView()
	{
		LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = (ListView)flater.inflate(R.layout.listview,null);
		mAdapter = new AlterDialogListViewAdapter(this,mArrayList);
		mAdapter.setSelection(mSelection);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mListItemClister);
	}

	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			mLastInputMethodId = mInputMethodList.get(position).getId();
			mAdapter.setSelection(position);
			mAdapter.notifyDataSetChanged();
			if((mHandler != null) && (mID != -1))
			{	
				CharSequence label = mInputMethodList.get(position).loadLabel(getPackageManager());
				Message msg = new Message();
				msg.what = SettingMacroDefine.upKeyBoardSetting;
				msg.arg1 = mID;                                           //resource id
				msg.obj = label.toString();                              // input method name
				mHandler.sendMessage(msg);
			}
		}
	};

	protected void onPause() 
	{
		super.onPause();

		StringBuilder builder = new StringBuilder(256);
		StringBuilder disabledSysImes = new StringBuilder(256);

		int firstEnabled = -1;
		int N = mInputMethodList.size();
		for (int i = 0; i < N; ++i) {
		    final InputMethodInfo property = mInputMethodList.get(i);
		    final String id = property.getId();
		    boolean hasIt = id.equals(mLastInputMethodId);
		    boolean systemIme = isSystemIme(property); 
			LOGD("id = "+id+",mLastInputMethodId = "+mLastInputMethodId);
		    if (((N == 1 || systemIme) && !mHaveHardKeyboard) || hasIt)
			{
		        if (builder.length() > 0) builder.append(':');
		        builder.append(id);
		        if (firstEnabled < 0) 
				{
		            firstEnabled = i;
		        }
		    } 
		    // If it's a disabled system ime, add it to the disabled list so that it
		    // doesn't get enabled automatically on any changes to the package list
		    if (systemIme && mHaveHardKeyboard && !hasIt) {
		        if (disabledSysImes.length() > 0) disabledSysImes.append(":");
		        disabledSysImes.append(id);
		    }
		}

		// If the last input method is unset, set it as the first enabled one.
		if (null == mLastInputMethodId || "".equals(mLastInputMethodId)) {
		    if (firstEnabled >= 0) {
		        mLastInputMethodId = mInputMethodList.get(firstEnabled).getId();
		    } else {
		        mLastInputMethodId = null;
		    }
		}
		LOGD("onPause,Settings.Secure.ENABLED_INPUT_METHODS, builder.toString() = "+builder.toString());
		LOGD("onPause,Settings.Secure.DISABLED_SYSTEM_INPUT_METHODS, disabledSysImes.toString() = "+disabledSysImes.toString());
		LOGD("onPause,Settings.Secure.DEFAULT_INPUT_METHOD, mLastInputMethodId = "+(mLastInputMethodId != null ? mLastInputMethodId : ""));
		Settings.Secure.putString(getContentResolver(),
		Settings.Secure.ENABLED_INPUT_METHODS, builder.toString());
		Settings.Secure.putString(getContentResolver(),
		Settings.Secure.DISABLED_SYSTEM_INPUT_METHODS, disabledSysImes.toString());
		Settings.Secure.putString(getContentResolver(),
		Settings.Secure.DEFAULT_INPUT_METHOD,
		mLastInputMethodId != null ? mLastInputMethodId : "");
	}
	
	public void LOGD(String msg)
	{
		if(true) Log.d("KeyBoardSettingAlterDialogActivity",msg);
	}
}
