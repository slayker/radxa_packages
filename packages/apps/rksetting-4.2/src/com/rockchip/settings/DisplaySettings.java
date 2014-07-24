package com.rockchip.settings;

import java.util.zip.Inflater;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * 
 * @author cx
 *
 */
public class DisplaySettings {
	private static final String TAG = "DisplaySettings";
	private static final boolean DEBUG = true;
	private void LOG(String msg){
		if (DEBUG){
			Log.d(TAG,msg);
		}
	}
	
	private Context mContext = null;
	private Handler mHandler = null;
	private LayoutInflater mInflater = null;
	private Dialog mDialog = null;
	private int mCurIndex;

	private final Configuration mCurConfig = new Configuration();
	
	public DisplaySettings(Context context,Handler handler){
		mContext = context;
		mHandler = handler;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}
	
	public void Resume(){
		LOG("readFontSizePreference");
		readFontSizePreference();
	}
	
	public void Pause(){
		
	}
	
	public void OnClick(){
		LOG("OnClick");
//		final Resources res = mContext.getResources();
//        final String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
//        
//		View view = mInflater.inflate(R.layout.fontsize, null);
//		final RadioGroup radiogroup = (RadioGroup) view.findViewById(R.id.font_set);
//		final RadioButton radiobutton0 = (RadioButton) view.findViewById(R.id.font_small);
//		final RadioButton radiobutton1 = (RadioButton) view.findViewById(R.id.font_normal);
//		final RadioButton radiobutton2 = (RadioButton) view.findViewById(R.id.font_large);
//		final RadioButton radiobutton3 = (RadioButton) view.findViewById(R.id.font_super_large);
//		
//		radiobutton0.setText(fontSizeNames[0]);
//		radiobutton1.setText(fontSizeNames[1]);
//		radiobutton2.setText(fontSizeNames[2]);
//		radiobutton3.setText(fontSizeNames[3]);
//		
//		int checkid = -1;
//		
//		switch (mCurIndex) {
//		case 0:
//			checkid = R.id.font_small;
//			break;
//			
//		case 1:
//			checkid = R.id.font_normal;
//			break;
//			
//		case 2:
//			checkid = R.id.font_large;
//			break;
//			
//		case 3:
//			checkid = R.id.font_super_large;
//			break;
//
//		default:
//			break;
//		}
//		
//		radiogroup.check(checkid);
//		
//		radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(RadioGroup group, int checkedId) {
//				// TODO Auto-generated method stub
//				LOG("onCheckedChanged");
//				switch (checkedId) {
//				case R.id.font_small:
//					mCurIndex = 0;
//					break;
//
//				case R.id.font_normal:
//					mCurIndex = 1;
//					break;
//					
//				case R.id.font_large:
//					mCurIndex = 2;
//					break;
//					
//				case R.id.font_super_large:
//					mCurIndex = 3;
//					break;
//					
//				default:
//					break;
//				}
//				
//				LOG("curindex:"+mCurIndex);
//				String[] indices = mContext.getResources().getStringArray(R.array.entryvalues_font_size);
//				
//				writeFontSizePreference(indices[mCurIndex]);
//				
//				String status = res.getString(R.string.title_font_size)+":"+fontSizeNames[mCurIndex];
//		        ((RKSettings) mContext).updateSettingItem(R.string.display_settings, status, null, null);	        
//		        mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
//		        
//				mDialog.dismiss();
//			}
//		});
//		
//		mDialog = new AlertDialog.Builder(mContext).setTitle(R.string.dialog_title_font_size)
//						.setView(view)
//						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//							
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//							}
//						}).create();
		InitDialog();
		
		mDialog.show();
	}
	
	private void InitDialog(){
		final Resources res = mContext.getResources();
        final String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        
        DialogInterface.OnClickListener OnItemClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mCurIndex = which;
				
				LOG("curindex:"+mCurIndex);
				String[] indices = mContext.getResources().getStringArray(R.array.entryvalues_font_size);
				
				writeFontSizePreference(indices[mCurIndex]);
				
				String status = res.getString(R.string.title_font_size)+":"+fontSizeNames[mCurIndex];
		        ((RKSettings) mContext).updateSettingItem(R.string.display_settings, status, null, null);	        
		        mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
		        
				mDialog.dismiss();
			}
		};
		
		mDialog = new AlertDialog.Builder(mContext).setTitle(R.string.dialog_title_font_size)
		/*	.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
			.setSingleChoiceItems(fontSizeNames, mCurIndex, OnItemClickListener)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			}).create();
	}
	
	int floatToIndex(float val) {
        String[] indices = mContext.getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }
	
	private void readFontSizePreference() {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        mCurIndex = index;

        
        // report the current size in the summary text
        final Resources res = mContext.getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
//        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
//                fontSizeNames[index]));
        String status = res.getString(R.string.title_font_size)+":"+fontSizeNames[index];
        ((RKSettings) mContext).updateSettingItem(R.string.display_settings, status, null, null);
        
        mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
    }
	
	private void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }
}
