/* $_FOR_ROCKCHIP_RBOX_$ */
/*$_rbox_$_modify_$_zhengyang_20120220: screen scale setting*/
package com.android.settings;

import android.content.Context;
import android.os.DisplayOutputManager;
import android.os.RemoteException;
import android.preference.SeekBarDialogPreference;
import android.widget.SeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class ScreenScalePreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener {
	private static final String TAG = "ScreenScalePreference";
	
	private SeekBar mSeekBar;
	private DisplayOutputManager mDisplayOutputManager = null;
	private int OldScaleValue = 100;
	
	public ScreenScalePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
        	mDisplayOutputManager = new DisplayOutputManager();
        }catch (RemoteException doe) {
            
        }
    }

	@Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar = getSeekBar(view);
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
		Log.d(TAG, "progress " + progress);
		if(mDisplayOutputManager != null) {
			mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, 90 + progress);
			mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, 90 + progress);
		}
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
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
    	super.onDialogClosed(positiveResult);
    	if (positiveResult) {
    	}
    	else {
    		if(mDisplayOutputManager != null) {
    			mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, OldScaleValue);
				mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, OldScaleValue);
			}
    	}
    }
}
