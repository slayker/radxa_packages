package com.rockchip.settings.screen;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.DisplayOutputManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import com.rockchip.settings.SettingItemAddManager;
import com.rockchip.settings.SettingItem;
import com.rockchip.settings.SettingItemClick;

public class ScreenSettings {
	private static final String TAG = "ScreenSettings";
	private static final boolean DEBUG = true;
	private void LOG(String msg){
		if (DEBUG){
			Log.d(TAG,msg);
		}
	}
	
	private Context mContext = null;
	private Handler mUIHandler = null;
	private LayoutInflater mInflater = null;
	
//	private ScreenScaleSettings mScreenScale = null;
	
	private DisplayOutputManager mDisplayManagement = null;
	
	private AlertDialog mDialog = null;
    private int mTime = -1;
    private Handler mHandler;
/*
    private int mInterface_last;
    private int mInterface_set;
    private String mMode_last;
    private String mMode_set;  
*/
	private int mMainDisplay_last = -1;
    private int mMainDisplay_set = -1;
    private String mMainMode_last = null;
    private String mMainMode_set = null;
    
    private int mAuxDisplay_last = -1;
    private int mAuxDisplay_set = -1;
    private String mAuxMode_last = null;
    private String mAuxMode_set = null;

	
    private Runnable mRunnable;
    
    private CharSequence mMainIfaceEntries[] = null;
    private CharSequence mMainIfaceValue[] = null;
	private CharSequence mAuxIfaceEntries[] = null;
    private CharSequence mAuxIfaceValue[] = null;
    
    private CharSequence mMainModeEntries[] = null;
    private CharSequence mMainModeValue[] = null;

	private CharSequence mAuxModeEntries[] = null;
    private CharSequence mAuxModeValue[] = null;
    
    private DialogInterface.OnClickListener mIfaceItemClickListener = null;
    private Dialog mOutputInterfaceDialog = null;
	private Dialog mAuxOutputInterfaceDialog = null;
    
    private DialogInterface.OnClickListener mModeItemClickListener = null;
    private Dialog mModeDialog = null;   
	private Dialog mAuxModeDialog = null;

	private SettingItemAddManager mSettingItemAddManager = null;
	private int mInterfaceId = -1;
	private int mModeId = -1;
	
	public ScreenSettings(Context context,Handler handler){
		mContext = context;
		mUIHandler = handler;
		mSettingItemAddManager = SettingItemAddManager.getInstance();
		try {
        	mDisplayManagement = new DisplayOutputManager();
        }catch (RemoteException doe) {
            
        }
		
		mRunnable = new Runnable(){
			@Override
			public void run() {
			// TODO Auto-generated method stub
				LOG("time:"+mTime);
			   if(mDialog == null || mTime < 0)
				   return;
			   if(mTime > 0) {
				   mTime--;
				   CharSequence text = mContext.getString(R.string.screen_control_ok_title) + " (" + String.valueOf(mTime) + ")";
				   mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(text);
				   mHandler.postDelayed(this, 1000);
			   }  else {
				   //Restore display setting.
				   RestoreDisplaySetting();
				   mDialog.dismiss();
			   }
			} 
    	};
    	
//    	if (mScreenScale == null){
//			mScreenScale = new ScreenScaleSettings(mContext, mUIHandler);
//		}
//		String status = mScreenScale.getScreenScale() + "%";
//		((RKSettings) mContext).updateSettingItem(R.string.screenscale, status, null, null);
		
		Resume();
		mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
    	
	}
	
	private int getIfaceIndex(CharSequence[] ifaceValue,int iface){
		String intface = Integer.toString(iface);
		for (int i = 0;i < ifaceValue.length;i++){
			if (intface.equals(ifaceValue[i].toString()))
				return i;
		}
		return 0;
	}
	
	private int getModeIndex(CharSequence[] modeValue,String mode){
		for (int i = 0;i < modeValue.length; i++){
			if (mode.equals(modeValue[i].toString()))
				return i;
		}
		return 0;
	}
	
	private String getIfaceTitle(int iface) {
    	String ifaceTitle = null;
    	if(iface == mDisplayManagement.DISPLAY_IFACE_LCD)
    		ifaceTitle =  mContext.getString(R.string.screen_iface_lcd_title);
    	if(iface == mDisplayManagement.DISPLAY_IFACE_HDMI)
    		ifaceTitle =  mContext.getString(R.string.screen_iface_hdmi_title);
		else if(iface == mDisplayManagement.DISPLAY_IFACE_VGA)
			ifaceTitle = mContext.getString(R.string.screen_iface_vga_title);
		else if(iface == mDisplayManagement.DISPLAY_IFACE_YPbPr)
			ifaceTitle = mContext.getString(R.string.screen_iface_ypbpr_title);
		else if(iface == mDisplayManagement.DISPLAY_IFACE_TV)
			ifaceTitle = mContext.getString(R.string.screen_iface_tv_title);
    	
    	return ifaceTitle;
    }

	private void SetModeList(int display, int iface) {
		LOG("SetModeList display " + display + " iface " + iface);
		
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
			mMainModeEntries = ModeEntries;
			mMainModeValue = ModeEntryValues;
		} else {
			mAuxModeEntries = ModeEntries;
    		mAuxModeValue = ModeEntryValues;
		}
    }
/*
	private void RestoreDisplaySetting() {
		LOG("restoreDisplaySetting");
		if(mInterface_last != mInterface_set) {
			mDisplayManagement.setInterface(mInterface_set, false);
//			mInterface.setValue(Integer.toString(mInterface_last));
//			mDisplayModeList.setTitle(getIfaceTitle(mInterface_last) + " " + getString(R.string.screen_mode_title));
			((RKSettings) mContext).updateSettingItem(R.string.screen_interface, getIfaceTitle(mInterface_last), null, null);
			// Fill display mode list.
	     	SetModeList(mInterface_last);
		}
		String curMode = null;
     	if (mMode_last != null){
     		LOG("mode index:"+getModeIndex(mMode_last));
     		curMode = mModeEntries[getModeIndex(mMode_last)].toString();
     	}
     	((RKSettings) mContext).updateSettingItem(R.string.screen_mode_title, curMode, null, null);
//		mDisplayModeList.setValue(mMode_last);
		mDisplayManagement.setMode(mInterface_last, mMode_last);
		mDisplayManagement.setInterface(mInterface_last, true);
		mInterface_set = mInterface_last;
		mMode_set = mMode_last;
		
		mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}*/
	
	public void Resume(){
		LOG("resume fill interface and mode");

		CharSequence[] IfaceEntries = null;
		CharSequence[] IfaceValue = null;
		int curIface = 0;
		String mode = null;
		
		 // Fill main interface list.
		int[] mainFace = mDisplayManagement.getIfaceList(mDisplayManagement.MAIN_DISPLAY);
		if(mainFace == null)	return;
		
		if(mainFace != null) {
			IfaceEntries = new CharSequence[mainFace.length];
			IfaceValue = new CharSequence[mainFace.length];		
			for(int i = 0; i < mainFace.length; i++) {
				IfaceEntries[i] = getIfaceTitle(mainFace[i]);
				IfaceValue[i] = Integer.toString(mainFace[i]);
			}
			
			mMainIfaceEntries = IfaceEntries;
			mMainIfaceValue = IfaceValue;
	        // get current main iface
	        curIface = mDisplayManagement.getCurrentInterface(mDisplayManagement.MAIN_DISPLAY);
			mMainDisplay_last = curIface;
			
//	        mInterface.setValue(Integer.toString(curIface));
	        String curInterface = getIfaceTitle(curIface);
//	        mDisplayModeList.setTitle(getIfaceTitle(curIface) + " " + getString(R.string.screen_mode_title));
	        LOG("cur interface:"+curInterface);
	        ((RKSettings) mContext).updateSettingItem(R.string.screen_interface, curInterface, null, null);
	        
//	        mInterface_last = curIface;
//	        mInterface_set = mInterface_last;
//			mInterfaceValue.setTitle(getString(R.string.screen_interface_current) + " " + getIfaceTitle(curIface));
	        
	        // Fill display mode list.
	     	SetModeList(mDisplayManagement.MAIN_DISPLAY,curIface);
			mode = mDisplayManagement.getCurrentMode(mDisplayManagement.MAIN_DISPLAY, curIface);
			if(mode != null) {
				mMainMode_last = mode;
				mMainDisplay_set = mMainDisplay_last;
				mMainMode_set = mMainMode_last;
	     	}
//			if(mode != null)
//				mDisplayModeList.setValue(mode);
	     	
	     	String curMode = null;
	     	if (mode != null){
	     		LOG("mode index:"+getModeIndex(mMainModeValue,mode));
	     		curMode = mMainModeEntries[getModeIndex(mMainModeValue,mode)].toString();
	     	}
	     	((RKSettings) mContext).updateSettingItem(R.string.screen_mode_title, curMode, null, null);
//			mMode_last = mode;
//			mMode_set = mMode_last;
			
		}

		int[] aux_display = mDisplayManagement.getIfaceList(mDisplayManagement.AUX_DISPLAY);
		if(aux_display != null) {
			mInterfaceId = mSettingItemAddManager.findID();
			SettingItem item0 = new SettingItem(1,R.string.screen_settings,mInterfaceId,mContext.getString(R.string.screen_interface)+"1", true);
			mSettingItemAddManager.addSettingItem("device",item0,R.string.screen_mode_title,true);
			item0.setOnSettingItemClick(mItemClickListener);

			mModeId = mSettingItemAddManager.findID();
			SettingItem item1 = new SettingItem(1,R.string.screen_settings,mModeId,mContext.getString(R.string.screen_mode_title)+"1", true);
			mSettingItemAddManager.addSettingItem("device",item1,mInterfaceId,true);
			item1.setOnSettingItemClick(mItemClickListener);
			
			curIface = mDisplayManagement.getCurrentInterface(mDisplayManagement.AUX_DISPLAY);
			mAuxDisplay_last = curIface;

			((RKSettings) mContext).updateSettingItem(mInterfaceId, getIfaceTitle(curIface), null, null);
	//		mAuxDisplay.setTitle("2nd " + getString(R.string.screen_interface));
			// Fill aux interface list.
			IfaceEntries = new CharSequence[aux_display.length];
			IfaceValue = new CharSequence[aux_display.length];		
			for(int i = 0; i < aux_display.length; i++) {
				IfaceEntries[i] = getIfaceTitle(aux_display[i]);
				IfaceValue[i] = Integer.toString(aux_display[i]);
			}
//			mAuxDisplay.setEntries(IfaceEntries);
//	        mAuxDisplay.setEntryValues(IfaceValue);
//	        mAuxDisplay.setValue(Integer.toString(curIface));
			mAuxIfaceEntries = IfaceEntries;
			mAuxIfaceValue = IfaceValue;
			// Fill aux display mode list.
//	        mAuxModeList.setTitle(getIfaceTitle(curIface) + " " + getString(R.string.screen_mode_title));
			SetModeList(mDisplayManagement.AUX_DISPLAY, curIface);
			mode = mDisplayManagement.getCurrentMode(mDisplayManagement.AUX_DISPLAY, curIface);
			if(mode != null) {
				mAuxMode_last = mode;
				mAuxDisplay_set = mAuxDisplay_last;
				mAuxMode_set = mAuxMode_last;
			}

			String curMode = null;
	     	if (mode != null){
	     		LOG("mode index:"+getModeIndex(mAuxModeValue,mode));
	     		curMode = mAuxModeEntries[getModeIndex(mAuxModeValue,mode)].toString();
	     	}
			
			((RKSettings) mContext).updateSettingItem(mModeId, curMode, null, null);
		} 
	}

	private SettingItemClick mItemClickListener = new SettingItemClick()
	{
		public void onItemClick(SettingItem item,int id)
		{
            if(id == mInterfaceId)
        	{
        		InitAuxIfaceDialog(mContext);
				mAuxOutputInterfaceDialog.show();
        	}
			else  if(id == mModeId)
			{
				createAuxModeDialog(mContext);
				mAuxModeDialog.show();
			}
		}

		public void onItemLongClick(SettingItem item,int id)
		{
		}
	};
	
	private void RestoreDisplaySetting() {
		LOG("RestoreDisplaySetting,mMainDisplay_set = "+mMainDisplay_set+",mMainDisplay_last = "+mMainDisplay_last);
		if( (mMainDisplay_set != mMainDisplay_last) || (mMainMode_last.equals(mMainMode_set) == false) ) 
		{
			if(mMainDisplay_set != mMainDisplay_last) 
			{
				mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_set, false);
				LOG("RestoreDisplaySetting(),mMainMode_set = "+mMainMode_set+",mMainDisplay_last = "+mMainDisplay_last);
				((RKSettings) mContext).updateSettingItem(R.string.screen_interface, getIfaceTitle(mMainDisplay_last), null, null);
//				mMainModeList.setTitle(getIfaceTitle(mMainDisplay_last) + " " + getString(R.string.screen_mode_title));
				// Fill display mode list.
		     	SetModeList(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_last);
			}
			String mode;
			if(mMainMode_last.equals("720x576i-50")) {
				mode = "PAL";
			} else if(mMainMode_last.equals("720x480i-60")) {
				mode = "NTSC";
			} else
				mode = mMainMode_last;
			((RKSettings) mContext).updateSettingItem(R.string.screen_mode_title, mode, null, null);
			mDisplayManagement.setMode(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_last, mMainMode_last);
			mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY, mMainDisplay_last, true);
			mMainDisplay_set = mMainDisplay_last;
			mMainMode_set = mMainMode_last;
		}
		if(mDisplayManagement.getDisplayNumber() > 1) {
			if( (mAuxDisplay_set != mAuxDisplay_last) || (mAuxMode_last.equals(mAuxMode_set) == false) ) {
				if(mAuxDisplay_set != mAuxDisplay_last) {
					mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_set, false);
					((RKSettings) mContext).updateSettingItem(mInterfaceId, getIfaceTitle(mAuxDisplay_last), null, null);
//					mAuxDisplay.setValue(Integer.toString(mAuxDisplay_last));
//					mAuxModeList.setTitle(getIfaceTitle(mAuxDisplay_last) + " " + getString(R.string.screen_mode_title));
					// Fill display mode list.
			     	SetModeList(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_last);
				}
//				mAuxModeList.setValue(mAuxMode_last);
				String mode;
				if(mAuxMode_last.equals("720x576i-50")) {
					mode = "PAL";
				} else if(mAuxMode_last.equals("720x480i-60")) {
					mode = "NTSC";
				} else
					mode = mAuxMode_last;
				if(mAuxModeEntries != null && mAuxModeEntries.length > 0 
							&& (mAuxDisplay_last < mAuxModeEntries.length) && (mAuxDisplay_last >= 0))
				{
					((RKSettings) mContext).updateSettingItem(mModeId, mode, null, null);
				}
				mDisplayManagement.setMode(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_last, mAuxMode_last);
				mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY, mAuxDisplay_last, true);
				mAuxDisplay_set = mAuxDisplay_last;
				mAuxMode_set = mAuxMode_last;
			}
		}
		mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}
	
	public void Pause(){
		
	}
	
	public void OnClick(int Id){
		switch (Id) {
		case R.string.screen_settings:
			LOG("screensettings onClick");
			
			break;

		case R.string.screenscale:
			LOG("screen scale onClick");
//			if (mScreenScale == null) {
//				mScreenScale = new ScreenScaleSettings(mContext, mUIHandler);
//			}
//			mScreenScale.OnClick();
			((RKSettings) mContext).StartActivitySafely(new Intent(mContext, 
								com.rockchip.settings.screen.ScreenSettingActivity.class));
			break;
			
		case R.string.screen_interface:
			LOG("screen interface onClick");
			InitIfaceDialog(mContext);
			mOutputInterfaceDialog.show();
			break;
			
		case R.string.screen_mode_title:
			LOG("screen mode onClick");
		    InitModeDialog(mContext);
			mModeDialog.show();
			break;
			
		default:
			break;
		}
	}
	
	private void InitDialog(Context context){
		mContext = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	/*	builder.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium);*/
		builder.setTitle(mContext.getString(R.string.screen_mode_switch_title));
    	builder.setCancelable(false);
    	builder.setNegativeButton(mContext.getString(R.string.screen_control_cancel_title), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//Restore display setting.
				mTime = -1;
				RestoreDisplaySetting();
    		}
		});
    	builder.setPositiveButton(mContext.getString(R.string.screen_control_ok_title), new DialogInterface.OnClickListener() {
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
	}
	
	
	private void InitIfaceDialog(Context context){
		mContext = context;
		
		mIfaceItemClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				mInterface.setValue((String)objValue);
				LOG("item index:"+which);
	        	int iface = Integer.parseInt(mMainIfaceValue[which].toString());
	        	mMainDisplay_set = iface;
	        	LOG("InitIfaceDialog(),mMainDisplay_set = "+mMainDisplay_set);
	        	((RKSettings) mContext).updateSettingItem(R.string.screen_interface, mMainIfaceEntries[which].toString(), null, null);
//	        	mDisplayModeList.setTitle(getIfaceTitle(iface) + " " + getString(R.string.screen_mode_title));
	        	SetModeList(mDisplayManagement.MAIN_DISPLAY,iface);
	        	String mode = mDisplayManagement.getCurrentMode(mDisplayManagement.MAIN_DISPLAY,iface);
	        	String curMode = null;
	        	if(mode != null) {
	        		LOG("mode index:"+getModeIndex(mMainModeValue,mode));
	         		curMode = mMainModeEntries[getModeIndex(mMainModeValue,mode)].toString();
	         	}
	         	((RKSettings) mContext).updateSettingItem(R.string.screen_mode_title, curMode, null, null);
	         	mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	         	
	         	mOutputInterfaceDialog.cancel();
			}
		};
		
		mOutputInterfaceDialog = new AlertDialog.Builder(mContext).setTitle(R.string.screen_interface)
				/*			.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
				   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
				   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
							.setSingleChoiceItems(mMainIfaceEntries, getIfaceIndex(mMainIfaceValue,mMainDisplay_set), mIfaceItemClickListener)
							.setOnCancelListener(new DialogInterface.OnCancelListener() {
								
								@Override
								public void onCancel(DialogInterface dialog) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							}).create();
	}
	
	private void InitModeDialog(Context context){
		mContext = context;
		
		mModeItemClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				String mode = (String)objValue;
//	        	mDisplayModeList.setValue(mode);
				LOG("item index:"+which);
				String mode = mMainModeValue[which].toString();
	        	mMainMode_set = mode;
	        	LOG("mode:"+mMainMode_set);
				LOG("mMainDisplay_set = "+mMainDisplay_set+",mMainDisplay_last = "+mMainDisplay_last);
				LOG("mMainMode_set = "+mMainMode_set+",mMainMode_last = "+mMainMode_last);
	        	if( (mMainDisplay_set != mMainDisplay_last) || (mMainMode_last.equals(mMainMode_set) == false) ) {
	        		if(mMainDisplay_set != mMainDisplay_last) {
	        			mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY,mMainDisplay_last, false);
	             		mTime = 30;
	        		} else
	             		mTime = 15;
	        		mDisplayManagement.setMode(mDisplayManagement.MAIN_DISPLAY,mMainDisplay_set, mMainMode_set);
	        		mDisplayManagement.setInterface(mDisplayManagement.MAIN_DISPLAY,mMainDisplay_set, true);
	        		if (mDialog == null){
	        			InitDialog(mContext);
	        		}
		        	mDialog.show();
		        	mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).requestFocus();
		        	CharSequence text = mContext.getString(R.string.screen_control_ok_title) + " (" + String.valueOf(mTime) + ")";
		        	mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(text);
		        	if (mHandler == null){
		        		mHandler = new Handler();
		        	}
		        	mHandler.postDelayed(mRunnable, 1000);
		        	
		        	((RKSettings) mContext).updateSettingItem(R.string.screen_mode_title, mMainModeEntries[which].toString(), null, null);
		         	mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	        	}
	        	
	        	mModeDialog.cancel();
			}
		};
		String mode = mDisplayManagement.getCurrentMode(mDisplayManagement.MAIN_DISPLAY,mMainDisplay_set);
		mMainMode_set = mode;
		LOG("mMainMode_set = "+mMainMode_set+",mMainDisplay_set = "+mMainDisplay_set+",mMainDisplay_last = "+mMainDisplay_last);
		mModeDialog = new AlertDialog.Builder(mContext).setTitle(R.string.screen_mode_title)
			/*				.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
				   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
				   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
							.setSingleChoiceItems(mMainModeEntries, getModeIndex(mMainModeValue,mMainMode_set), mModeItemClickListener)
							.setOnCancelListener(new DialogInterface.OnCancelListener() {
								
								@Override
								public void onCancel(DialogInterface dialog) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							}).create();
	}

	private void InitAuxIfaceDialog(Context context){
		mContext = context;
		
		DialogInterface.OnClickListener auxIfaceItemClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				mInterface.setValue((String)objValue);
				LOG("item index:"+which);
	        	int iface = Integer.parseInt(mAuxIfaceValue[which].toString());
	        	LOG("iface:"+iface);
	        	mAuxDisplay_set = iface;
	        	((RKSettings) mContext).updateSettingItem(mInterfaceId, mAuxIfaceEntries[which].toString(), null, null);
//	        	mDisplayModeList.setTitle(getIfaceTitle(iface) + " " + getString(R.string.screen_mode_title));
	        	SetModeList(mDisplayManagement.AUX_DISPLAY,iface);
	        	String mode = mDisplayManagement.getCurrentMode(mDisplayManagement.AUX_DISPLAY,iface);
	        	String curMode = null;
	        	if(mode != null) {
	        		LOG("mode index:"+getModeIndex(mAuxModeValue,mode));
					if((mAuxModeEntries != null) && mAuxModeEntries.length > 0)
	         			curMode = mAuxModeEntries[getModeIndex(mAuxModeValue,mode)].toString();
	         	}
	         	((RKSettings) mContext).updateSettingItem(mModeId, curMode, null, null);
	         	mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	         	
	         	mAuxOutputInterfaceDialog.cancel();
			}
		};
		
		mAuxOutputInterfaceDialog = new AlertDialog.Builder(mContext).setTitle(R.string.screen_interface)
			/*				.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
				   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
				   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
							.setSingleChoiceItems(mAuxIfaceEntries, getIfaceIndex(mAuxIfaceValue,mAuxDisplay_set), auxIfaceItemClickListener)
							.setOnCancelListener(new DialogInterface.OnCancelListener() {
								
								@Override
								public void onCancel(DialogInterface dialog) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							}).create();
	}
	
	private void createAuxModeDialog(Context context)
	{
		DialogInterface.OnClickListener ItemClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				String mode = (String)objValue;
//	        	mDisplayModeList.setValue(mode);
				LOG("item index:"+which);
				String mode = mAuxModeValue[which].toString();
	        	mAuxMode_set = mode;
	        	LOG("mode:"+mAuxMode_set);
	        	if( (mAuxDisplay_set != mAuxDisplay_last) || (mAuxMode_last.equals(mAuxMode_set) == false) ) {
	        		if(mAuxDisplay_set != mAuxDisplay_last) {
	        			mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY,mAuxDisplay_last, false);
	             		mTime = 30;
	        		} else
	             		mTime = 15;
	        		mDisplayManagement.setMode(mDisplayManagement.AUX_DISPLAY,mAuxDisplay_set, mAuxMode_set);
	        		mDisplayManagement.setInterface(mDisplayManagement.AUX_DISPLAY,mAuxDisplay_set, true);
	        		if (mDialog == null){
	        			InitDialog(mContext);
	        		}
		        	mDialog.show();
		        	mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).requestFocus();
		        	CharSequence text = mContext.getString(R.string.screen_control_ok_title) + " (" + String.valueOf(mTime) + ")";
		        	mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(text);
		        	if (mHandler == null){
		        		mHandler = new Handler();
		        	}
		        	mHandler.postDelayed(mRunnable, 1000);
		        	
		        	((RKSettings) mContext).updateSettingItem(mModeId, mAuxModeEntries[which].toString(), null, null);
		         	mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	        	}
	        	
	        	mAuxModeDialog.cancel();
			}
		};
		String mode = mDisplayManagement.getCurrentMode(mDisplayManagement.AUX_DISPLAY,mAuxDisplay_set);
		mAuxMode_set = mode;
		LOG("mAuxMode_set = " + mAuxMode_set + ",mAuxDisplay_set = " + mAuxDisplay_set + ",mAuxnDisplay_last = "+mAuxDisplay_last);
		mAuxModeDialog = new AlertDialog.Builder(mContext).setTitle(R.string.screen_mode_title)
			/*				.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
				   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
				   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
							.setSingleChoiceItems(mAuxModeEntries, getModeIndex(mAuxModeValue,mAuxMode_set), ItemClickListener)
							.setOnCancelListener(new DialogInterface.OnCancelListener() {
								
								@Override
								public void onCancel(DialogInterface dialog) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							}).create();
	}
}
