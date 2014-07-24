package com.rockchip.settings.inputmethod.jelly_bean;

import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.view.IWindowManager;
import com.rockchip.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.ServiceManager;
import android.preference.SeekBarDialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.hardware.input.InputManager;
import android.os.Parcelable;
import android.os.Parcel;
import com.rockchip.settings.inputmethod.*;

public class PointerSpeedDialogActivity extends AlertActivity implements
		SeekBar.OnSeekBarChangeListener {
	private SeekBar mSeekBar;
	private Button mOKButton;
	private Button mCancelButton;
	private int mOldSpeed;
	private boolean mRestoredOldState;

	private boolean mSetOKState = false;

	private boolean mTouchInProgress;

//	private static final int MIN_SPEED = -7;
//	private static final int MAX_SPEED = 7;
	private ContentResolver resolver;
	private InputManager mIm;
	
	private ContentObserver mSpeedObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			onSpeedChanged();
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(getString(R.string.pointer_speed));
		setContentView(R.layout.pointer_speed_dialog);

		mIm = (InputManager)getSystemService(Context.INPUT_SERVICE);
		mSeekBar = (SeekBar) findViewById(R.id.pointer_speed_seekbar);
		mSeekBar.setMax(InputManager.MAX_POINTER_SPEED - InputManager.MIN_POINTER_SPEED);
		mOldSpeed = mIm.getPointerSpeed(this);
		mSeekBar.setProgress(mOldSpeed - InputManager.MIN_POINTER_SPEED);
		mSeekBar.setOnSeekBarChangeListener(this);

		mOKButton = (Button) findViewById(R.id.pointer_speed_ok);
		mOKButton.setOnClickListener(mOKButtonListener);
		mCancelButton = (Button) findViewById(R.id.pointer_speed_cancel);
		mCancelButton.setOnClickListener(mCancelButtonListener);

		resolver = this.getContentResolver();
	}

	private Button.OnClickListener mOKButtonListener = new Button.OnClickListener() {
		public void onClick(View v) {
			mSetOKState = true;
			Settings.System.putInt(resolver, Settings.System.POINTER_SPEED,
					mSeekBar.getProgress() + InputManager.MIN_POINTER_SPEED);

        	mRestoredOldState = false;
			finish();
		}
	};
	private Button.OnClickListener mCancelButtonListener = new Button.OnClickListener() {
		public void onClick(View v) {
			restoreOldState();
			finish();
		}
	};

	private void setSpeed(int speed) 
	{
		mIm.tryPointerSpeed(speed);
	}

	private void onSpeedChanged() {
		int speed = mIm.getPointerSpeed(this);
        mSeekBar.setProgress(speed - InputManager.MIN_POINTER_SPEED);
	}

	private void restoreOldState() {
		if (mRestoredOldState)
			return;

		setSpeed(mOldSpeed);
		mRestoredOldState = true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {
		setSpeed(progress + InputManager.MIN_POINTER_SPEED);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		mTouchInProgress = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mTouchInProgress = false;
		setSpeed(seekBar.getProgress() + InputManager.MIN_POINTER_SPEED);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.getContentResolver().registerContentObserver(
				Settings.System.getUriFor(Settings.System.POINTER_SPEED), true,
				mSpeedObserver);

		mRestoredOldState = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		this.getContentResolver().unregisterContentObserver(mSpeedObserver);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mSetOKState == false)
			restoreOldState();
	}
}
