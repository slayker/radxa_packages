package com.rk.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.content.ComponentName;
import android.widget.Button;
import com.rk.setting.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;

public class SystemSetting extends Activity
{
	private boolean mWifiEnable = false;
	private boolean mEthernetEnable = false;
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_CLEARABLE_FLAGS);
		
		mEthernetEnable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_ETHERNET);
		mWifiEnable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);

		if(!mEthernetEnable && !mWifiEnable)
			setContentView(R.layout.screen_setting_no_network);
		else
        	setContentView(R.layout.system_setting);
		
		createTitle();
		createButton();
    }

	private void createTitle()
	{
		ImageView image = (ImageView)findViewById(R.id.title_image);
		Bitmap resize = bitMapScale(R.drawable.system,ScreenInformation.mDpiRatio);
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
		Button version = (Button)findViewById(R.id.version);
		Button storage = (Button)findViewById(R.id.storage);

		version.setTextSize(ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio);
		storage.setTextSize(ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio);

		Bitmap map = bitMapScale(R.drawable.version,1f);
		Drawable drawable = new BitmapDrawable(map);
		version.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);

		map = bitMapScale(R.drawable.storage,1f);
		drawable = new BitmapDrawable(map);
		storage.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);

		if(mEthernetEnable || mWifiEnable)
		{
			Button network = (Button)findViewById(R.id.network);
			network.setTextSize(ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio);
			map = bitMapScale(R.drawable.network,1f);
			drawable = new BitmapDrawable(map);
			network.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);
		}
		
	}
	
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.version:
				{
					Intent intent = new Intent(this, Version.class);
					startActivity(intent);
				}
				break;

			case R.id.storage:
				{
					Intent intent = new Intent(this, Storage.class);
					startActivity(intent);
				}
				break;

			case R.id.network:
				{
					Intent intent = new Intent(this, Network.class);
					startActivity(intent);
				}
				break;
		}
	}
}

