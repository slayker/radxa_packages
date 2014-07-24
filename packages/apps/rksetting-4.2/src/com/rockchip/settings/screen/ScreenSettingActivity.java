package com.rockchip.settings.screen;

import android.content.Context;
import android.os.Handler;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.os.DisplayOutputManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.rockchip.settings.R;
import android.widget.ImageView;
import android.view.View;
import android.graphics.PixelFormat;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.rockchip.settings.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;
import android.util.Log;


public class ScreenSettingActivity extends Activity
{
	private final int RightButtonPressed = 0;
	private final int LeftButttonPressed = 1;
	private final int UpButtonPressed = 2;
	private final int DownButtonPressed = 3;
	private final int RightButtonResume = 4;
	private final int LeftButtonResume = 5;
	private final int UpButtonResume = 6;
	private final int DownButtonResume = 7;

	private ImageView mDirctionButton;
	private ImageView mRightButton;
	private ImageView mLeftButton;
	private ImageView mUpButton;
	private ImageView mDownButton;
	
	private DisplayOutputManager mDisplayOutputManager = null;
	
	private static final int MAX_SCALE = 100;
	private static final int MIN_SCALE = 80;
	
	public static final int SYSTEM_UI_FLAG_SHOW_FULLSCREEN = 0x00000004;

	private Bitmap bitMapScale(int id)
	{
		Bitmap map = BitmapFactory.decodeResource(this.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*ScreenInformation.mDpiRatio;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}

	private void createView()
	{
		int centerX = ScreenInformation.mScreenWidth/2;
		int y = ScreenInformation.mScreenHeight/5;
		
		ImageView touch_up = (ImageView)findViewById(R.id.screen_touch_up);
		Bitmap map = bitMapScale(R.drawable.screen_vertical_reduce);
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		param.setMargins(centerX-map.getWidth()/2, y, 0, 0);
		
		y += map.getHeight()+10;
		touch_up.setLayoutParams(param);

		mUpButton = (ImageView)findViewById(R.id.button_up);
		map = bitMapScale(R.drawable.button_vertical_up);
		y += map.getHeight()+10;
		param = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param.addRule(RelativeLayout.BELOW, R.id.screen_touch_up);
		param.setMargins(centerX-map.getWidth()/2, 10, 0, 0);
		mUpButton.setLayoutParams(param);
		
		ImageView ok = (ImageView)findViewById(R.id.button_ok);
		Bitmap okmap = bitMapScale(R.drawable.ok);
		y += okmap.getHeight()/2;
		param = new RelativeLayout.LayoutParams(okmap.getWidth(),okmap.getHeight());
		param.addRule(RelativeLayout.BELOW, R.id.button_up);
		param.setMargins(centerX-okmap.getWidth()/2, 10, 0, 0);
		ok.setLayoutParams(param);

		mDownButton = (ImageView)findViewById(R.id.button_down);
		map = bitMapScale(R.drawable.button_vertical_down);
		param = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param.addRule(RelativeLayout.BELOW, R.id.button_ok);
		param.setMargins(centerX-map.getWidth()/2, 10, 0, 0);
		mDownButton.setLayoutParams(param);
		
		ImageView touch_down = (ImageView)findViewById(R.id.screen_touch_down);
		map = bitMapScale(R.drawable.screen_vertical_add);
		param = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param.addRule(RelativeLayout.BELOW, R.id.button_down);
		param.setMargins(centerX-map.getWidth()/2, 10, 0, 0);
		touch_down.setLayoutParams(param);

		
		mLeftButton = (ImageView)findViewById(R.id.button_left);
		map = bitMapScale(R.drawable.button_left);
		int x = -okmap.getWidth()/2-10-map.getWidth()/2;
		param = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param.addRule(RelativeLayout.ALIGN_LEFT, R.id.button_ok);
		param.setMargins(x, y-map.getHeight()/2, 0, 0);
		mLeftButton.setLayoutParams(param);
		
		ImageView left = (ImageView)findViewById(R.id.screen_button_left);
		map = bitMapScale(R.drawable.screen_horizontal_reduce);
		x += -map.getWidth()/2-10;
		RelativeLayout.LayoutParams param0 = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param0.addRule(RelativeLayout.ALIGN_LEFT, R.id.button_left);
		param0.setMargins(x, y-map.getHeight()/2, 0, 0);
		left.setLayoutParams(param0);

		mRightButton = (ImageView)findViewById(R.id.button_right);
		map = bitMapScale(R.drawable.button_right);
		x = okmap.getWidth()/2+10+map.getWidth()/2;
		param = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param.addRule(RelativeLayout.ALIGN_RIGHT, R.id.button_ok);
		param.setMargins(0, y-map.getHeight()/2,-x, 0);
		mRightButton.setLayoutParams(param);

		ImageView right = (ImageView)findViewById(R.id.screen_button_right);
		map = bitMapScale(R.drawable.screen_horizontal_add);
		x += 10+map.getWidth()/2;
		param = new RelativeLayout.LayoutParams(map.getWidth(),map.getHeight());
		param.addRule(RelativeLayout.ALIGN_RIGHT, R.id.button_right);
		param.setMargins(0, y-map.getHeight()/2,-x, 0);
		right.setLayoutParams(param);

		mRightButton.setOnClickListener(mOnClick);
		mLeftButton.setOnClickListener(mOnClick);
		mUpButton.setOnClickListener(mOnClick);
		mDownButton.setOnClickListener(mOnClick);
	}
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//full screen
		getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_SHOW_FULLSCREEN);
		
		setContentView(R.layout.screen_setting);

		getWindow().setFormat(PixelFormat.RGBA_8888);            
//		BitmapFactory.setDefaultConfig(Bitmap.Config.ARGB_8888);

		createView();
	/*	
		mRightButton = (ImageView)findViewById(R.id.button_right);
		mLeftButton = (ImageView)findViewById(R.id.button_left);
		mUpButton = (ImageView)findViewById(R.id.button_up);
		mDownButton = (ImageView)findViewById(R.id.button_down);

		mRightButton.setOnClickListener(mOnClick);
		mLeftButton.setOnClickListener(mOnClick);
		mUpButton.setOnClickListener(mOnClick);
		mDownButton.setOnClickListener(mOnClick);*/
		
		try {
        	mDisplayOutputManager = new DisplayOutputManager();
        }catch (RemoteException doe) {
            
        }
	}

	private View.OnClickListener mOnClick = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			int id = v.getId();
			int scalevalue;
			switch(id)
			{
				case R.id.button_right:
					LOGD("button_right");
					// add code here
					if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X) + 1;
					if (scalevalue > MAX_SCALE){
						scalevalue = MAX_SCALE;
					}
					if(scalevalue >=0 )
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, scalevalue);
					}
					break;
				case R.id.button_left:
					LOGD("button_left");
					// add code here
					if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X) - 1;
					if (scalevalue < MIN_SCALE){
						scalevalue = MIN_SCALE;
					}
					if(scalevalue >=0)
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, scalevalue);
					}
					break;
				case R.id.button_up:
					LOGD("touch_up");
					// add code here
					if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y) - 1;
					if (scalevalue < MIN_SCALE){
						scalevalue = MIN_SCALE;
					}
					if(scalevalue >=0)
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, scalevalue);
					}
					break;
				case R.id.button_down:
					LOGD("touch_down");
					// add code here
					if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y) + 1;
					if (scalevalue > MAX_SCALE){
						scalevalue = MAX_SCALE;
					}
					if(scalevalue >=0 )
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, scalevalue);
					}
					break;
			}
		}
	};
	
	public boolean dispatchKeyEvent(KeyEvent event) 
	{
    	int keyCode = event.getKeyCode();
    	LOGD("keyCode = "+keyCode);
    	int scalevalue;
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_DPAD_RIGHT:  // 水平方向增加
				mRightButton.setImageResource(R.drawable.button_right_pressed);
				mHandler.removeMessages(RightButtonResume);
				// add code here
				if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X) + 1;
					if (scalevalue > MAX_SCALE){
						scalevalue = MAX_SCALE;
					}
					if(scalevalue >=0 )
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, scalevalue);
				}
				mHandler.sendEmptyMessageDelayed(RightButtonResume,100);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:   // 水平方向减小
				mHandler.removeMessages(LeftButtonResume);
				mLeftButton.setImageResource(R.drawable.button_left_pressed);
				// add code here
				if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X) - 1;
					if (scalevalue < MIN_SCALE){
						scalevalue = MIN_SCALE;
					}
					if(scalevalue >=0)
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_X, scalevalue);
				}
				mHandler.sendEmptyMessageDelayed(LeftButtonResume,100);
				break;
			case KeyEvent.KEYCODE_DPAD_UP:     //  竖直方向减小
				mHandler.removeMessages(UpButtonResume);
				mUpButton.setImageResource(R.drawable.button_vertical_up_pressed);
				// add code here
				if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y) - 1;
					if (scalevalue < MIN_SCALE){
						scalevalue = MIN_SCALE;
					}
					if(scalevalue >=0)
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, scalevalue);
				}
				mHandler.sendEmptyMessageDelayed(UpButtonResume,100);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:   //  竖直方向增加
				mHandler.removeMessages(DownButtonResume);
				mDownButton.setImageResource(R.drawable.button_vertical_down_pressed);
				// add code here
				if(mDisplayOutputManager != null) {
					scalevalue = mDisplayOutputManager.getScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y) + 1;
					if (scalevalue > MAX_SCALE){
						scalevalue = MAX_SCALE;
					}
					if(scalevalue >=0 )
						mDisplayOutputManager.setScreenScale(mDisplayOutputManager.DISPLAY_SCALE_Y, scalevalue);
				}
				mHandler.sendEmptyMessageDelayed(DownButtonResume,100);
				break;
		}

		return super.dispatchKeyEvent(event);
	}

	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case RightButtonResume:
					mRightButton.setImageResource(R.drawable.button_vertical_right);
					break;
				case LeftButtonResume:
					mLeftButton.setImageResource(R.drawable.button_vertical_left);
					break;
				case UpButtonResume:
					mUpButton.setImageResource(R.drawable.button_up);
					break;
				case DownButtonResume:
					mDownButton.setImageResource(R.drawable.button_down);
					break;
			}
		}
	};
	
	private void LOGD(String msg)
	{
		if(true)
			Log.d("ScreenSettingActivity",msg);
	}
}
