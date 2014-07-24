package com.rockchip.settings.dialog;

import com.rockchip.settings.R;
import com.rockchip.settings.StorageUtils;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.res.Configuration;
import java.text.Collator;
import java.util.Arrays;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.widget.AdapterView;
import android.os.RemoteException;
import android.app.backup.BackupManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Environment;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.text.format.Formatter;
import android.widget.Toast;
import android.os.SystemProperties;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.content.pm.PackageManager;
import android.util.Log;

public class StorageShowAlterDialogActivity extends AlertActivity{

	static final String TAG = "StorageShow";
	
	SimpleAdapter mSchedule;
	private IMountService mMountService = null;
	private StorageManager mStorageManager = null;
	private HashMap<String, String> map = new HashMap<String, String>();
	ListView mListView = null;
	
	private String mSdTotalSpace;
	private String mSdAvailableSpace;
	private String mNANDTotalSpace;
	private String mNANDAvailableSpace;
	private String mExternalTotalSpace;

	public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			mStorageManager.registerListener(mStorageListener);
		}
	}

	private SimpleAdapter CreatAdapter(){
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
			//this is SD card
			if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
			{
				map = new HashMap<String, String>();
				map.put("storage_sort", getResources().getString(R.string.sd_memory));
				mylist.add(map);
				//SD card TotalSpace
				map = new HashMap<String, String>();
				map.put("totalspace", getResources().getString(R.string.memory_size));
				map.put("availablespace", mSdTotalSpace);	
				mylist.add(map);
				//SD card AvailableSpace
				map = new HashMap<String, String>();
				map.put("totalspace", getResources().getString(R.string.memory_available));
				map.put("availablespace", mSdAvailableSpace);
				mylist.add(map);  
			}
			//this is NAND flash
			map = new HashMap<String, String>();
			map.put("storage_sort", getResources().getString(R.string.nand_memory));
			mylist.add(map);
			//NAND flash TotalSpace
			map = new HashMap<String, String>();
			map.put("totalspace", getResources().getString(R.string.memory_size));
			map.put("availablespace",  mNANDTotalSpace);
			mylist.add(map);
			//NAND flash AvailableSpace
			map = new HashMap<String, String>();
			map.put("totalspace", getResources().getString(R.string.memory_available));
			map.put("availablespace", mNANDAvailableSpace);
       	 	mylist.add(map); 
			//this is internal_storage
			map = new HashMap<String, String>();
			map.put("storage_sort", getResources().getString(R.string.internal_storage));
			mylist.add(map);
			//internal_storage TotalSpace
			map = new HashMap<String, String>();
			map.put("totalspace", getResources().getString(R.string.memory_available));
			map.put("availablespace", mExternalTotalSpace);
       	 	mylist.add(map);
			mSchedule = new SimpleAdapter(this,mylist,  R.layout.storage_space_qurey,            
	                  new String[] {"storage_sort","totalspace","availablespace"},   
	                  new int[] {R.id.storage_sort,R.id.totalspace,R.id.availablespace});
			return mSchedule;


	}

	StorageEventListener mStorageListener = new StorageEventListener() {
		@Override
		public void onStorageStateChanged(String path, String oldState, String newState) {
			Log.i(TAG, "Received storage state changed notification that " +
					path + " changed state from " + oldState + " to " + newState);
			updateStatusForStorage();
		}
	};
	
	protected void onResume()
	{
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addDataScheme("file");
		registerReceiver(mReceiver, intentFilter);

		LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = (ListView)flater.inflate(R.layout.listview,null);
		mAlert.setView(mListView,-5,-5,-5,-5);
		
		this.updateStatusForStorage();
		this.setupAlert();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onDestroy() {
		if (mStorageManager != null && mStorageListener != null) {
			mStorageManager.unregisterListener(mStorageListener);
		}
		super.onDestroy();
	}
	
	 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	private void updateStatusForStorage() {
		if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
			updateStatusSDCard();
		
		updateStatusNandflash();
		updateStatusInternal();
		//update and reflesh available storage
		if(mListView!=null){
			CreatAdapter();
			mSchedule.notifyDataSetChanged();
			mListView.setAdapter(mSchedule);
		}
	}
		
	//calculate the space of storage
	private String formatSize(long size) {
	    return Formatter.formatFileSize(this, size);
	}

	public void onCancel(DialogInterface dialog) {
	    finish();
	}

	//update available space of SD-Card
	private void updateStatusSDCard(){
		String status = StorageUtils.getSDcardState();
		String readOnly = "";
		if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			status = Environment.MEDIA_MOUNTED;
			readOnly = getResources().getString(R.string.read_only);
		}

		// Calculate the space of SDcard
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				String path = StorageUtils.getSDcardDir();
				StatFs stat = new StatFs(path);
				long blockSize = stat.getBlockSize();
				long totalBlocks = stat.getBlockCount();
				long availableBlocks = stat.getAvailableBlocks();
				mSdTotalSpace=formatSize(totalBlocks * blockSize);
				mSdAvailableSpace=formatSize(availableBlocks * blockSize) + readOnly;
			} catch (IllegalArgumentException e) {
				status = Environment.MEDIA_REMOVED;
			}
		}else{
			mSdTotalSpace=getResources().getString(R.string.sd_unavailable);
			mSdAvailableSpace=getResources().getString(R.string.sd_unavailable);
		}
	}

	//update available space of Nandflash
	private void updateStatusNandflash(){
		String status = StorageUtils.getFlashState();
		String readOnly = "";
		if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			status = Environment.MEDIA_MOUNTED;
			readOnly = getResources().getString(R.string.read_only);
		}

		// Calculate the space of SDcard
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				String path = StorageUtils.getFlashDir();
				StatFs stat = new StatFs(path);
				long blockSize = stat.getBlockSize();
				long totalBlocks = stat.getBlockCount();
				long availableBlocks = stat.getAvailableBlocks();
				mNANDTotalSpace=formatSize(totalBlocks * blockSize);
				mNANDAvailableSpace=formatSize(availableBlocks * blockSize) + readOnly;
			} catch (IllegalArgumentException e) {
				status = Environment.MEDIA_REMOVED;
				mNANDTotalSpace=getResources().getString(R.string.nand_unavailable);
				mNANDAvailableSpace=getResources().getString(R.string.nand_unavailable);
			}
		}else{
			mNANDTotalSpace=getResources().getString(R.string.nand_unavailable);
			mNANDAvailableSpace=getResources().getString(R.string.nand_unavailable);
		}
	}

	//update available space of internal storage 
	private void updateStatusInternal(){
		File dataPath = Environment.getDataDirectory();
		StatFs stat = new StatFs(dataPath.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		//show the space of internal storage
		mExternalTotalSpace=formatSize(availableBlocks * blockSize);
	}	 
}

