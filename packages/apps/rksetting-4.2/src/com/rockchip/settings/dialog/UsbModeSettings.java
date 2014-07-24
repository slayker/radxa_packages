package com.rockchip.settings.dialog;

import com.rockchip.settings.R;
import com.rockchip.settings.ListViewAdapter;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.os.Handler;
import java.io.File; 
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.content.Intent;
import android.content.IntentFilter;

import android.util.Log;

public class UsbModeSettings
{
	private static final String TAG = "USBmodeselect";
    private static final String	HOST_MODE = new String("1");
    private static final String	SLAVE_MODE = new String("2");
	
	private File file = null;

	private StorageManager mStorageManager = null;
	private String mMode = null;
	

	private Context mContext;
	private Handler mHandler;
	private ListViewAdapter mListViewAdapter;

	private boolean mLock = false;
	
	public UsbModeSettings(Context context,Handler handler,ListViewAdapter adapter)
	{
		mContext = context;
		mHandler = handler;
		mListViewAdapter = adapter;
		
		mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
		file = new File("/sys/bus/platform/drivers/usb20_otg/force_usb_mode");
   		if(file.exists())
		{
			Log.d("UsbModeSelect","/data/otg.cfg not exist,but temp file exist");
			mMode = ReadFromFile(file);
		}
		else
		{
			mMode = HOST_MODE;
		}
		int state = mMode.equals(HOST_MODE)?R.string.host_mode_title:R.string.slave_mode_title;
		((RKSettings)mContext).updateSettingItem(R.string.usb_setting,state,-1,-1);
	}
	
	private String ReadFromFile(File file)
	{
		 if((file != null) && file.exists())
		 {
		 		try
		 		{
					FileInputStream fin= new FileInputStream(file);
					BufferedReader reader= new BufferedReader(new InputStreamReader(fin));
					String config = reader.readLine();
					fin.close();
					return config;
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
		 }
		 
		 return null;
	}

	private void Write2File(File file,String mode)
	{
		 Log.d("UsbModeSelect","Write2File,write mode = "+mode);
		 if((file == null) || (!file.exists()) || (mode == null))
		 		return ;
		 		
		 try
		 {
				FileOutputStream fout = new FileOutputStream(file);
				PrintWriter pWriter = new PrintWriter(fout); 
				pWriter.println(mode);
				pWriter.flush();
				pWriter.close();
				fout.close();
		}
		catch(IOException re)
		{
	  	}
	}

	public void onUsbModeClick()
	{
		if(mLock) // 不能使用信号量，因为使用信号量时，主线程会阻塞在这个地方
			return ;

		mLock = true;
		Log.d("UsbModeSettings","onUsbModeClick*******************");
		if(mMode.equals(HOST_MODE))
		{
			mMode = SLAVE_MODE;
		}
		else
		{
			mMode = HOST_MODE;
		}
//		Write2File(file,mMode);
		synchronized (this) 
		{
            mHandler.removeCallbacks(mUsbSwitch);
            mHandler.post(mUsbSwitch);
        }
	}

	private  Runnable mUsbSwitch = new Runnable()
	{
		public synchronized void run()
		{
			Log.d("UsbModeSettings","mUsbSwitch Runnable() in*******************");
			if(mStorageManager != null)
			{
				if(mMode == HOST_MODE)
				{
					mStorageManager.disableUsbMassStorage();
					Log.d("UsbModeSettings","mStorageManager.disableUsbMassStorage()*******************");
					Write2File(file,mMode);
					((RKSettings)mContext).updateSettingItem(R.string.usb_setting,R.string.host_mode_title,-1,-1);
				}
				else
				{
					Write2File(file,mMode);
					Log.d("UsbModeSettings","mStorageManager.enableUsbMassStorage()  in *******************");
					mStorageManager.enableUsbMassStorage();
					Log.d("UsbModeSettings","mStorageManager.enableUsbMassStorage()   out*******************");
					((RKSettings)mContext).updateSettingItem(R.string.usb_setting,R.string.slave_mode_title,-1,-1);
				}
				mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
			}
			Log.d("UsbModeSettings","mUsbSwitch Runnable() out*******************");
			mLock = false;
		}
	};
}


