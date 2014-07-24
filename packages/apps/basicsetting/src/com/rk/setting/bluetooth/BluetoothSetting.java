package com.rk.setting.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.rk.setting.R;
import android.widget.Button;
import android.widget.MyGallery;
import android.os.Handler;
import android.os.Message;
import android.bluetooth.BluetoothAdapter;
import java.util.ArrayList;
import com.rk.setting.bluetooth.RKBluetoothDevice;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.TextView;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import com.rk.setting.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;

import android.util.Log;


public class BluetoothSetting extends Activity
{
	public static final int Update_BluetoothState = 0; 
	public static final int UpdateListView = 1;

	
	public static final int BlueTooth_On = 0;
	public static final int BlutTooth_Off = 1;

	private Button mSwitch = null;
	private MyGallery mGallery = null;
	private BluetoothSettings mBluetoothSetting = null;
	private ArrayList<RKBluetoothDevice> mDeviceList = null;
	private BluetoothDeviceAdapter mAdapter = null;
	private TextView mNoDevice = null;
	private boolean mOpen = false;
	private boolean mBluetoothOpen = false;

	private void createTitle()
	{
		ImageView image = (ImageView)findViewById(R.id.title_image);
		Bitmap resize = bitMapScale(R.drawable.bluetooth,ScreenInformation.mDpiRatio);
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
	
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_CLEARABLE_FLAGS);
        setContentView(R.layout.bluetooth_setting);

		createTitle();

		TextView text = (TextView)findViewById(R.id.switch_title);
		text.setTextSize(ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio);
		
		mSwitch = (Button)findViewById(R.id.bluetooth_switch);
		Bitmap map = bitMapScale(R.drawable.switch_on,ScreenInformation.mDpiRatio);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(map.getWidth(),map.getHeight());
		mSwitch.setLayoutParams(params);
		
		mNoDevice= (TextView)findViewById(R.id.device_not_found);
		mNoDevice.setTextSize(ScreenInformation.mScreenWidth/30f*ScreenInformation.mDpiRatio);
		mBluetoothSetting = new BluetoothSettings(this,mHandler);
		Log.d("BluetoothSettings","mBluetoothSetting.getBluetoothState() = "+mBluetoothSetting.getBluetoothState());
		Log.d("BluetoothSettings","BluetoothAdapter.STATE_ON = "+BluetoothAdapter.STATE_ON);
		if(mBluetoothSetting.getBluetoothState() == BluetoothAdapter.STATE_ON)
		{
			mOpen = true;
			mSwitch.setBackgroundResource(R.drawable.switch_on_selected);
			mBluetoothSetting.setBluetoothEnabled(true);
		}
		else
		{
			mOpen = false;
			mSwitch.setBackgroundResource(R.drawable.switch_off_selected);
			mBluetoothSetting.setBluetoothEnabled(false);
		}
		if(mBluetoothSetting != null)
			mDeviceList = mBluetoothSetting.getBluetoothDeviceList();
		
		mSwitch.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				Log.d("BluetoothSettings","onFocusChange,hasFocus = "+hasFocus);
				if(hasFocus)
				{
					int drawable = mOpen?R.drawable.switch_on_selected:R.drawable.switch_off_selected;
					mSwitch.setBackgroundResource(drawable);
				}
				else
				{
					int drawable = mOpen?R.drawable.switch_on:R.drawable.switch_off;
					mSwitch.setBackgroundResource(drawable);
				}
			}
		});
		createGallery();
    }

	private void createGallery()
	{
		mGallery = (MyGallery)findViewById(R.id.bluetooth_device);
		mAdapter = new BluetoothDeviceAdapter(this,mDeviceList);
		mGallery.setAdapter(mAdapter);
		mGallery.setSpacing(50);
		mGallery.setOnItemClickListener(mListItemClickListener);
		mGallery.setOnItemLongClickListener(mItemLongClickListener);
	}

	private AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			if(mDeviceList != null)
			{
				RKBluetoothDevice device = mDeviceList.get(position);
				device.onClick();
			}
		}
	};

	private AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener()
	{
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
			if(mDeviceList != null)
			{
				RKBluetoothDevice device = mDeviceList.get(position);
				device.onLongClick();
			}

			return true;
		}
	};
	
	private void setSwitchState(int state)
	{
		int drawable = 0;
		if(mSwitch != null)
		{
			switch(state)
			{
			case BluetoothAdapter.STATE_TURNING_ON:
				mOpen = false;
				mSwitch.setBackgroundResource(R.drawable.switch_on_selected);
				if(mGallery != null)
				{
					mGallery.setVisibility(View.INVISIBLE);
					mNoDevice.setVisibility(View.GONE);
				}
                break;
            case BluetoothAdapter.STATE_ON:
				mOpen = true;
				if(mDeviceList.size() > 0)
				{
					mSwitch.setBackgroundResource(R.drawable.switch_on);
					if(mGallery != null)
					{
						mNoDevice.setVisibility(View.GONE);
						mGallery.setVisibility(View.VISIBLE);
						mGallery.requestFocus();
					}
				}
				else
				{
					mSwitch.setBackgroundResource(R.drawable.switch_on_selected);
					mGallery.setVisibility(View.GONE);
					mNoDevice.setVisibility(View.VISIBLE);
					mNoDevice.setText(R.string.no_device);
				}
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
			case BluetoothAdapter.STATE_OFF:
				mOpen = false;
				mSwitch.setBackgroundResource(R.drawable.switch_off_selected);
				mSwitch.requestFocus();
				if(mGallery != null)
				{
					mGallery.setVisibility(View.INVISIBLE);
					mNoDevice.setVisibility(View.GONE);
				}
                break;
			}
		}
	}

	public void onButtonClick(View v) 
	{
		ScaleAnimation animation = new ScaleAnimation(1f,0.8f,1f,0.8f,
							Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		animation.setDuration(50);
		v.startAnimation(animation);
		if(mBluetoothSetting != null)
		{
 			if(mBluetoothSetting.getBluetoothState() == BluetoothAdapter.STATE_ON)
			{
				mBluetoothOpen = true;
 			}
			else
			{
				mBluetoothOpen = false;
			}
			mBluetoothOpen = !mBluetoothOpen;
			mBluetoothSetting.setBluetoothEnabled(mBluetoothOpen);
		}
	}

	public void onResume()
	{
		super.onResume();
		if(mBluetoothSetting != null)
			mBluetoothSetting.Resume();
	}

	public void onPause()
	{
		super.onPause();
		if(mBluetoothSetting != null)
			mBluetoothSetting.Pause();
	}

	public Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{
				case UpdateListView:
					int state = mBluetoothSetting.getBluetoothState();
					
					if((state == BluetoothAdapter.STATE_OFF) && mBluetoothOpen)
						Toast.makeText(BluetoothSetting.this,R.string.open_fail,Toast.LENGTH_LONG).show();
					
					setSwitchState(state);
					if(mGallery.getVisibility() == View.VISIBLE)
					{
						if(mAdapter != null)
						{
							int mGallerySelect = mGallery.getCurrentSelection();
							if(mGallerySelect >= mDeviceList.size())
								mGallerySelect = mDeviceList.size()-1;
							if(mGallerySelect < 0)
								mGallerySelect = 0;
							mGallery.setSelection(mGallerySelect);
							mGallery.requestFocus();
							mGallery.setSelection(mGallerySelect);
							mAdapter.notifyDataSetChanged();
						}
					}
					break;
			}
		}
	};
}

