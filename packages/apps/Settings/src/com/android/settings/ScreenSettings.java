/* $_FOR_ROCKCHIP_RBOX_$ */
/*$_rbox_$_modify_$_zhengyang_20120220: add screen settings*/
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.android.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.DisplayOutputManager;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.Log;

public class ScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
	private static final String TAG = "ScreenSettings";
	private final boolean DBG = true;
	
	private static final String KEY_MAIN_DISPLAY_INTERFACE = "main_screen_interface";
    private static final String KEY_MAIN_DISPLAY_MODE = "main_screen_mode";
    
    private static final String KEY_AUX_DISPLAY_INTERFACE = "aux_screen_interface";
    private static final String KEY_AUX_DISPLAY_MODE = "aux_screen_mode";
	
    private ListPreference	mMainDisplay;
    private ListPreference	mMainModeList;

	private ListPreference	mAuxDisplay;
    private ListPreference	mAuxModeList;
	
	private DisplayOutputManager mDisplayManagement = null;
	
    private int mMainDisplay_last = -1;
    private int mMainDisplay_set = -1;
    private String mMainMode_last = null;
    private String mMainMode_set = null;
    
    private int mAuxDisplay_last = -1;
    private int mAuxDisplay_set = -1;
    private String mAuxMode_last = null;
    private String mAuxMode_set = null;
    
    private AlertDialog mDialog = null;
    private int mTime = -1;
    private Handler mHandler;
    private Runnable mRunnable;
    
    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screen_settings);
        if(DBG) Log.d(TAG, "ScreenSettings start");
        
        try {
        	mDisplayManagement = new DisplayOutputManager();
        }catch (RemoteException doe) {
            
        }
        
        int[] main_display = mDisplayManagement.getIfaceList(mDisplayManagement.MAIN_DISPLAY);
        if(main_display == null)	{
        	Log.e(TAG, "Can not get main display interface list");
        	return;
        }
        int[] aux_display = mDisplayManagement.getIfaceList(mDisplayManagement.AUX_DISPLAY);

        mMainDisplay = (ListPreference) findPreference(KEY_MAIN_DISPLAY_INTERFACE);
		mMainDisplay.setOnPreferenceChangeListener(this);
		mMainModeList = (ListPreference) findPreference(KEY_MAIN_DISPLAY_MODE);
		mMainModeList.setOnPreferenceChangeListener(this);
		
		int curIface = mDisplayManagement.getCurrentInterface(mDisplayManagement.MAIN_DISPLAY);
		mMainDisplay_last = curIface;
		
		if (aux_display == null) {
			mMainDisplay.setTitle(getString(R.string.screen_interface));
		} else {
			mMainDisplay.setTitle("1st " + getString(R.string.screen_interface));
		}
		// Fill main interface list.
		CharSequence[] IfaceEntries = new CharSequence[main_display.length];
		CharSequence[] IfaceValue = new CharSequence[main_display.length];		
		for(int i = 0; i < main_display.length; i++) {
			IfaceEntries[i] = getIfaceTitle(main_display[i]);
			IfaceValue[i] = Integer.toString(main_display[i]);
		}
		mMainDisplay.setEntries(IfaceEntries);
        mMainDisplay.setEntryValues(IfaceValue);
        mMainDisplay.setValue(Integer.toString(curIface));
		
		// Fill main display mode list.
		mMainModeList.setTitle(getIfaceTitle(curIface) + " " + getString(R.string.screen_mode_title));
     	SetModeList(mDisplayManagement.MAIN_DISPLAY, curIface);
     	String mode = mDisplayManagement.getCurrentMode(mDisplayManagement.MAIN_DISPLAY, curIface);
		if(mode != null) {
			mMainModeList.setValue(mode);
			mMainMode_last = mode;
			mMainDisplay_set = mMainDisplay_last;
			mMainMode_set = mMainMode_last;
     	}
		
		// Get Aux screen infomation
 		mAuxDisplay = (ListPreference) findPreference(KEY_AUX_DISPLAY_INTERFACE);
		mAuxDisplay.setOnPreferenceChangeListener(this);
		mAuxModeList = (ListPreference) findPreference(KEY_AUX_DISPLAY_MODE);
		mAuxModeList.setOnPreferenceChangeListener(this);
		if(aux_display != null) {
			curIface = mDisplayManagement.getCurrentInterface(mDisplayManagement.AUX_DISPLAY);
			mAuxDisplay_last = curIface;
			mAuxDisplay.setTitle("2nd " + getString(R.string.screen_interface));
			// Fill aux interface list.
			IfaceEntries = new CharSequence[aux_display.length];
			IfaceValue = new CharSequence[aux_display.length];		
			for(int i = 0; i < aux_display.length; i++) {
				IfaceEntries[i] = getIfaceTitle(aux_display[i]);
				IfaceValue[i] = Integer.toString(aux_display[i]);
			}
			mAuxDisplay.setEntries(IfaceEntries);
	        mAuxDisplay.setEntryValues(IfaceValue);
	        mAuxDisplay.setValue(Integer.toString(curIface));
			
			// Fill aux display mode list.
	        mAuxModeList.setTitle(getIfaceTitle(curIface) + " " + getString(R.string.screen_mode_title));
			SetModeList(mDisplayManagement.AUX_DISPLAY, curIface);
			mode = mDisplayManagement.getCurrentMode(mDisplayManagement.AUX_DISPLAY, curIface);
			if(mode != null) {
				mAuxModeList.setValue(mode);
				mAuxMode_last = mode;
				mAuxDisplay_set = mAuxDisplay_last;
				mAuxMode_set = mAuxMode_last;
			}
		} else {
			mAuxDisplay.setShouldDisableView(true);
			mAuxDisplay.setEnabled(false);
			mAuxModeList.setShouldDisableView(true);
			mAuxModeList.setEnabled(false);		
		}
     	
     	
     	AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setTitle(getString(R.string.screen_mode_switch_title));
    	builder.setCancelable(false);
    	builder.setNegativeButton(getString(R.string.screen_control_cancel_title), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//Restore display setting.
				mTime = -1;
				RestoreDisplaySetting();
    		}
		});
    	builder.setPositiveButton(getString(R.string.screen_control_ok_title), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Keep display setting
				mTime = -1;
				mMainDisplay_last = mMainDisplay_set;
				mMainMode_last = mMainMode_set;
				
				mAuxDisplay_last = mAuxDisplay_set;
				mAuxMode_last = mAuxMode_set;
			}
		});
    	mDialog = builder.create();
		mHandler = new Handler();
		
		mRunnable = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
			   if(mDialog == null || mTime < 0)
				   return;
			   if(mTime > 0) {
				   mTime--;
				   CharSequence text = getString(R.string.screen_control_ok_title) + " (" + String.valueOf(mTime) + ")";
				   mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(text);
				   mHandler.postDelayed(this, 1000);
			   }  else {
				   //Restore display setting.
				   RestoreDisplaySetting();
				   mDialog.dismiss();
			   }
			}
		};
    }
    
	private String getIfaceTitle(int iface) {
    	String ifaceTitle = null;
    	if(iface == mDisplayManagement.DISPLAY_IFACE_LCD)
    		ifaceTitle =  getString(R.string.screen_iface_lcd_title);
    	if(iface == mDisplayManagement.DISPLAY_IFACE_HDMI)
    		ifaceTitle =  getString(R.string.screen_iface_hdmi_title);
		else if(iface == mDisplayManagement.DISPLAY_IFACE_VGA)
			ifaceTitle = getString(R.string.screen_iface_vga_title);
		else if(iface == mDisplayManagement.DISPLAY_IFACE_YPbPr)
			ifaceTitle = getString(R.string.screen_iface_ypbpr_title);
		else if(iface == mDisplayManagement.DISPLAY_IFACE_TV)
			ifaceTitle = getString(R.string.screen_iface_tv_title);
    	
    	return ifaceTitle;
    }

	private void SetModeList(int display, int iface) {
		
		if(DBG) Log.d(TAG, "SetModeList display " + display + " iface " + iface);
		
    	String[] modelist = mDisplayManagement.getModeList(display, iface);
		CharSequence[] ModeEntries = new CharSequence[modelist.length];
		CharSequence[] ModeEntryValues = new CharSequence[modelist.length];
		for(int i = 0; i < modelist.length; i++) {
			ModeEntries[i] = modelist[i];
			if(iface == mDisplayManagement.DISPLAY_IFACE_TV) {
				String mode = modelist[i];
				if(mode.equals("720x576i-50")) {
					ModeEntries[i] = "CVBS: PAL";
				} else if(mode.equals("720x480i-60")) {
					ModeEntries[i] = "CVBS: NTSC";
				} else
					ModeEntries[i] = "YPbPr: " + modelist[i];
			}
				
			ModeEntryValues[i] = modelist[i];
		}
		if(display == mDisplayManagement.MAIN_DISPLAY) {
			mMainModeList.setEntries(ModeEntries);
			mMainModeList.setEntryValues(ModeEntryValues);
		} else {
			mAuxModeList.setEntries(ModeEntries);
			mAuxModeList.setEntryValues(ModeEntryValues);
		}
    }

	private void RestoreDisplaySetting() {
		if( (mMainDisplay_set != mMainDisplay_last) || (mMainMode_last.equals(mMainMode_set) == false) ) {
			if(mMainDisplay_set != mMainDisplay_last) {
				mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_set, false);
				mMainDisplay.setValue(Integer.toString(mMainDisplay_last));
				mMainModeList.setTitle(getIfaceTitle(mMainDisplay_last) + " " + getString(R.string.screen_mode_title));
				// Fill display mode list.
		     	SetModeList(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_last);
			}
			mMainModeList.setValue(mMainMode_last);
			mDisplayManagement.setMode(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_last, mMainMode_last);
			mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_last, true);
			mMainDisplay_set = mMainDisplay_last;
			mMainMode_set = mMainMode_last;
		}
		if(mDisplayManagement.getDisplayNumber() > 1) {
			if( (mAuxDisplay_set != mAuxDisplay_last) || (mAuxMode_last.equals(mAuxMode_set) == false) ) {
				if(mAuxDisplay_set != mAuxDisplay_last) {
					mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_set, false);
					mAuxDisplay.setValue(Integer.toString(mAuxDisplay_last));
					mAuxModeList.setTitle(getIfaceTitle(mAuxDisplay_last) + " " + getString(R.string.screen_mode_title));
					// Fill display mode list.
			     	SetModeList(mDisplayManagement.MAIN_DISPLAY, mAuxDisplay_last);
				}
				mAuxModeList.setValue(mAuxMode_last);
				mDisplayManagement.setMode(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_last, mAuxMode_last);
				mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_last, true);
				mAuxDisplay_set = mAuxDisplay_last;
				mAuxMode_set = mAuxMode_last;
			}
		}
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();
		if(DBG) Log.d(TAG, "onPreferenceChange, key = "+key);
		
		if ( key.equals(KEY_MAIN_DISPLAY_INTERFACE) ) {
        	mMainDisplay.setValue((String)newValue);
        	int iface = Integer.parseInt((String)newValue);
        	mMainDisplay_set = iface;
        	mMainModeList.setTitle(getIfaceTitle(iface) + " " + getString(R.string.screen_mode_title));
        	SetModeList(mDisplayManagement.MAIN_DISPLAY, iface);
        	String mode = mDisplayManagement.getCurrentMode(mDisplayManagement.MAIN_DISPLAY, iface);
        	if(mode != null) {
	       		mMainModeList.setValue(mode);
        	}
        }
        if( key.equals(KEY_MAIN_DISPLAY_MODE) ) {
        	String mode = (String)newValue;
        	mMainModeList.setValue(mode);
        	mMainMode_set = mode;
        	if( (mMainDisplay_set != mMainDisplay_last) || (mMainMode_last.equals(mMainMode_set) == false) ) {
        		if(mMainDisplay_set != mMainDisplay_last) {
        			mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_last, false);
             		mTime = 30;
        		} else
             		mTime = 15;
        		mDisplayManagement.setMode(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_set, mMainMode_set);
        		mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_set, true);
	        	mDialog.show();
	        	mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).requestFocus();
	        	CharSequence text = getString(R.string.screen_control_ok_title) + " (" + String.valueOf(mTime) + ")";
	        	mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(text);
	        	mHandler.postDelayed(mRunnable, 1000);
        	}
        }
        
        if ( key.equals(KEY_AUX_DISPLAY_INTERFACE) ) {
        	mAuxDisplay.setValue((String)newValue);
        	int iface = Integer.parseInt((String)newValue);
        	mAuxDisplay_set = iface;
        	mAuxModeList.setTitle(getIfaceTitle(iface) + " " + getString(R.string.screen_mode_title));
        	SetModeList(mDisplayManagement.AUX_DISPLAY, iface);
        	String mode = mDisplayManagement.getCurrentMode(mDisplayManagement.AUX_DISPLAY, iface);
        	if(mode != null) {
	       		mAuxModeList.setValue(mode);
        	}
        }
        if( key.equals(KEY_AUX_DISPLAY_MODE) ) {
        	String mode = (String)newValue;
        	mAuxModeList.setValue(mode);
        	mAuxMode_set = mode;
        	if( (mAuxDisplay_set != mAuxDisplay_last) || (mAuxMode_last.equals(mAuxMode_set) == false) ) {
        		if(mAuxDisplay_set != mAuxDisplay_last) {
        			mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_last, false);
             		mTime = 30;
        		} else
             		mTime = 15;
        		mDisplayManagement.setMode(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_set, mAuxMode_set);
        		mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_set, true);
	        	mDialog.show();
	        	mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).requestFocus();
	        	CharSequence text = getString(R.string.screen_control_ok_title) + " (" + String.valueOf(mTime) + ")";
	        	mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(text);
	        	mHandler.postDelayed(mRunnable, 1000);
        	}
        }
		return false;
	}
}
