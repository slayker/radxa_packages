package com.rk.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ActivityNotFoundException;
import android.widget.Button;
import java.io.File; 
import com.rk.setting.ScreenInformation;
import android.util.DisplayMetrics;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.util.Log;

public class Settings extends Activity 
{
	private Button mWifi;
	private Button mBluetooth;
	private Button mDisplay;
	private Button mSystem;
	private Button mAdvance;

	private boolean mWifiEnable = false;
	private boolean mBluetoothEnable = false;
	
	
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_CLEARABLE_FLAGS);		
		mBluetoothEnable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
		mWifiEnable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
		// 根据是否有wifi和蓝牙来动态加载不同的布局	
		if(mWifiEnable && mBluetoothEnable)
			setContentView(R.layout.main);
		else if(!mWifiEnable && mBluetoothEnable)
 			setContentView(R.layout.settings_no_wifi);
		else if(!mBluetoothEnable && mWifiEnable)
			setContentView(R.layout.settings_no_bluetooth);
		else
			setContentView(R.layout.settings_no_wifi_bluetooth);

		getScreenSize();
		createTitle();
		createButton();
    }

	private void getScreenSize()	
	{		
		DisplayMetrics displayMetrics = new DisplayMetrics();	        
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);	        
		ScreenInformation.mScreenWidth = displayMetrics.widthPixels;	        
		ScreenInformation.mScreenHeight = displayMetrics.heightPixels;		 
		ScreenInformation.mDensityDpi = displayMetrics.densityDpi;		 
		ScreenInformation.mDpiRatio = ((float)ScreenInformation.mDefaultDpi)/(float)displayMetrics.densityDpi;	        
		Log.d("mainActivity","displayMetrics.densityDpi is: " + ScreenInformation.mDensityDpi);	        
		Log.d("mainActivity","displayMetrics.widthPixels is: " + ScreenInformation.mScreenWidth);	        
		Log.d("mainActivity","displayMetrics.heightPixels is: " + ScreenInformation.mScreenHeight);	
	}

	private void createTitle()
	{
		ImageView image = (ImageView)findViewById(R.id.title_image);
		Bitmap resize = bitMapScale(R.drawable.setting,ScreenInformation.mDpiRatio);
		image.setScaleType(ImageView.ScaleType.CENTER);
		image.setImageBitmap(resize);

		TextView title = (TextView)findViewById(R.id.title_text);
		title.setTextSize(ScreenInformation.mScreenWidth/25f*ScreenInformation.mDpiRatio);
	}

	private Bitmap bitMapScale(int id,float scaleParameter)
	{
		Bitmap map = BitmapFactory.decodeResource(this.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*scaleParameter;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}
	
	private void createButton()
	{
		Bitmap map = null;
		Drawable drawable = null;
		
		if(mWifiEnable)
		{
			mWifi = (Button)findViewById(R.id.wifi);
			mWifi.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
			map = bitMapScale(R.drawable.wifi,1f);
			drawable = new BitmapDrawable(map);
			mWifi.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);
			mWifi.setOnClickListener(clickListener);
		}

		if(mBluetoothEnable)
		{
			mBluetooth = (Button)findViewById(R.id.bluetooth);
			mBluetooth.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
			map = bitMapScale(R.drawable.bluetooth,1f);
			drawable = new BitmapDrawable(map);
			mBluetooth.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);
			mBluetooth.setOnClickListener(clickListener);
		}
		
		mDisplay = (Button)findViewById(R.id.display);
		mSystem = (Button)findViewById(R.id.system);
		mAdvance = (Button)findViewById(R.id.advance);

		mDisplay.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
		mSystem.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
		mAdvance.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);

		map = bitMapScale(R.drawable.display,1f);
		drawable = new BitmapDrawable(map);
		mDisplay.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);

		map = bitMapScale(R.drawable.system,1f);
		drawable = new BitmapDrawable(map);
		mSystem.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);

		map = bitMapScale(R.drawable.advance,1f);
		drawable = new BitmapDrawable(map);
		mAdvance.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);
		
		mDisplay.setOnClickListener(clickListener);
		mSystem.setOnClickListener(clickListener);
		mAdvance.setOnClickListener(clickListener);
	}

	View.OnClickListener clickListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			int id = v.getId();
			switch(id)
			{
				case R.id.wifi:
					{
						String action = StorageUtils.getAction();
						if(action != null)
						{
							Intent intent = new Intent(StorageUtils.getAction());
							startActivity(intent);
						}
						else
						{
							Toast.makeText(Settings.this,R.string.version_error,Toast.LENGTH_LONG).show();
						}
					}
					break;
				case R.id.bluetooth:
					{
						Intent intent = new Intent(Settings.this, com.rk.setting.bluetooth.BluetoothSetting.class);
						startActivity(intent);
					}
					break;
				case R.id.display:
					{
						Intent intent = new Intent(Settings.this, ScreenSettingActivity.class);
						startActivity(intent);
					}
					break;
				case R.id.system:
					{
						Intent intent = new Intent(Settings.this, SystemSetting.class);
						startActivity(intent);
					}
					break;
				case R.id.advance:
					try
					{
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setComponent(new ComponentName("com.rockchip.settings", 
								"com.rockchip.settings.RKSettings"));
						startActivity(intent); 
					}
					catch(ActivityNotFoundException e)
					{
						
					}
					break;
			}
		}
	};
}
