package com.rockchip.settings.screen;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.os.DisplayOutputManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

public class ScreenScaleSettings implements SeekBar.OnSeekBarChangeListener {
	private static final String TAG = "ScreenScaleSettings";
	private static final boolean DEBUG = true;
	private void LOG(String msg){
		if (DEBUG){
			Log.d(TAG,msg);
		}
	}
	
	private Context mContext = null;
	private Handler mUIHandler = null;
	private LayoutInflater mInflater = null;
	private Dialog mDialog = null;
	
	private SeekBar mSeekBar;
	private DisplayOutputManager mDisplayOutputManager = null;
	private int OldScaleValue = 100;
	
	
	public ScreenScaleSettings(Context context,Handler handler){
		mContext = context;
		mUIHandler = handler;
		try {
        	mDisplayOutputManager = new DisplayOutputManager();
        }catch (RemoteException doe) {
            
        }
	}
	
	public void OnClick(){
		mInflater = (LayoutInflater) mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		
		View view = (View) mInflater.inflate(R.layout.screenscale_dialog, null);
		
		onBindDialogView(view);
		
		mDialog = new AlertDialog.Builder(mContext).setTitle(R.string.screenscale)
			/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
					.setView(view)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							onDialogClosed(true);
							String status = Integer.toString(mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X))+"%";
							((RKSettings) mContext).updateSettingItem(R.string.screenscale, status, null, null);
							mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
							dialog.dismiss();
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							onDialogClosed(false);
							String status = OldScaleValue+"%";
							((RKSettings) mContext).updateSettingItem(R.string.screenscale, status, null, null);
							mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
							dialog.dismiss();
						}
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							onDialogClosed(false);
							String status = OldScaleValue+"%";
							((RKSettings) mContext).updateSettingItem(R.string.screenscale, status, null, null);
							mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
							dialog.dismiss();
						}
					}).create();
		
		mDialog.show();
	}

	protected void onBindDialogView(View view) {
//        super.onBindDialogView(view);
		view.setFocusableInTouchMode(true);
        view.requestFocus();

        mSeekBar = (SeekBar) view.findViewById(R.id.scalebar);
        mSeekBar.requestFocus();
        mSeekBar.setMax(10);
        if(mDisplayOutputManager != null) {
        	OldScaleValue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X);
        	mSeekBar.setProgress(OldScaleValue - 90);
        }
        mSeekBar.setOnSeekBarChangeListener(this);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		LOG("progress " + progress);
		if(mDisplayOutputManager != null)
			mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, 90 + progress);
			mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, 90 + progress);
		mSeekBar.setProgress(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	protected void onDialogClosed(boolean positiveResult) {
//    	super.onDialogClosed(positiveResult);
    	if (positiveResult) {
    		if(mDisplayOutputManager != null)
				OldScaleValue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X); 
    	}
    	else {
    		if(mDisplayOutputManager != null)
    			mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, OldScaleValue);
				mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, OldScaleValue);
    	}
    }
	
	public int getScreenScale(){
		int scale = OldScaleValue;
		if (mDisplayOutputManager != null){
			scale = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X);
		}
		return scale;
	}
}
