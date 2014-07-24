package com.rockchip.settings.dialog;

import com.rockchip.settings.R;
import com.rockchip.settings.StorageUtils;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.res.Configuration;
import java.lang.Character;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.os.RemoteException;
import android.app.backup.BackupManager;
import android.view.View;
import android.os.SystemProperties;
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
import android.os.Environment;
import android.os.RemoteException;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageVolume;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import android.widget.SimpleAdapter;
import android.view.LayoutInflater;
import android.content.pm.PackageManager;
import android.util.Log;

public class StorageFormatAlterDialogActivity extends AlertActivity implements OnCancelListener {
	
	static final String TAG = "StorageFormat";

	SimpleAdapter mSchedule;
	boolean mNandMountToggleAdded = true;

	private static final int DLG_CONFIRM_UNMOUNT = 1;
	private static final int DLG_ERROR_UNMOUNT = 2;
	private static final boolean localLOGV = false;

	private IMountService mMountService = null;

	private StorageManager mStorageManager = null;

	private String mSdMount;
	private String mSdsummary;
	
	
	private HashMap<String, String> map = new HashMap<String, String>();
	ListView mListView = null;
	public void onCreate(Bundle savedInstanceState) 
	{		super.onCreate(savedInstanceState);
			if (mStorageManager == null) {
				mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
				mStorageManager.registerListener(mStorageListener);
			}
			LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mListView = (ListView)flater.inflate(R.layout.listview,null);
			mAlert.setView(mListView,-5,-5,-5,-5);
			if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
				this.updateStatusSDCard();
			updateListview(); 

			this.setupAlert();

			mListView.requestFocus();
			mListView.setSelection(0);
			mListView.setOnItemClickListener(mListItemClister);
		
	}


	private void updateListview(){
		CreatAdapter();
		mListView.setAdapter(mSchedule);
	}

	private SimpleAdapter CreatAdapter(){
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

		//this is SD card 
		if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
		{
			map = new HashMap<String, String>();
			map.put("storage_sort", getResources().getString(R.string.sd_memory));
			mylist.add(map);
			//SD card mount/unmount
			map = new HashMap<String, String>();
			map.put("mount", mSdMount);
			map.put("format", mSdsummary);
			mylist.add(map);

			//SD card format
			map = new HashMap<String, String>();
			map.put("mount", getResources().getString(R.string.sd_format));
			map.put("format", getResources().getString(R.string.sd_format_rockchip_summary));	
			mylist.add(map);
		}
		//this is NAND flash
		map = new HashMap<String, String>();
		map.put("storage_sort", getResources().getString(R.string.nand_memory));
		mylist.add(map);
		//NAND flash format
		map = new HashMap<String, String>();
		map.put("mount", getResources().getString(R.string.nand_format));
		map.put("format",  getResources().getString(R.string.nand_format_rockchip_summary));
		mylist.add(map);

		mSchedule = new SimpleAdapter(this,mylist,R.layout.storage_space_format,
	     						new String[] {"storage_sort", "mount","format"},   
	       						new int[] {R.id.storage_sort,R.id.mount,R.id.format});
		return mSchedule;

	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		   @Override
		   public void onReceive(Context context, Intent intent) {
		   }
	   };
	@Override
		protected void onResume() {
			super.onResume();
			
			IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
			intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			intentFilter.addDataScheme("file");
			registerReceiver(mReceiver, intentFilter);
	
			
		}
	StorageEventListener mStorageListener = new StorageEventListener() {

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "Received storage state changed notification that " +
                    path + " changed state from " + oldState +
                    " to " + newState);
			if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
            updateStatusSDCard();
        }
    };


@Override
public Dialog onCreateDialog(int id, Bundle args) {
	switch (id) {
	case DLG_CONFIRM_UNMOUNT:
		return new AlertDialog.Builder(this)
		/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
				.setTitle(R.string.dlg_confirm_unmount_title)
				.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						doUnmountSDCard(true);
					}})
				.setNegativeButton(R.string.cancel, null)
				.setMessage(R.string.dlg_confirm_unmount_text)
				.setOnCancelListener(this)
				.create();
	case DLG_ERROR_UNMOUNT:
		return new AlertDialog.Builder(this)
		/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
				.setTitle(R.string.dlg_error_unmount_title)
				.setNeutralButton(R.string.dlg_ok, null)
				.setMessage(R.string.dlg_error_unmount_text)
				.setOnCancelListener(this)
				.create();
	}
	return null;
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


private void updateStatusSDCard() {
	String status = StorageUtils.getSDcardState();

	if (status.equals(Environment.MEDIA_UNMOUNTED) ||
		status.equals(Environment.MEDIA_NOFS) ||
		status.equals(Environment.MEDIA_UNMOUNTABLE) ) {
		mSdMount=getResources().getString(R.string.sd_mount);
		mSdsummary=getResources().getString(R.string.sd_mount_summary);
	} else {
		mSdMount=getResources().getString(R.string.sd_mount);
		mSdsummary=getResources().getString(R.string.sd_insert_summary);
	}

	if(mListView!=null){
		CreatAdapter();
		mSchedule.notifyDataSetChanged();
		mListView.setAdapter(mSchedule);
	}
}

private synchronized IMountService getMountService() {
	if (mMountService == null) {
		IBinder service = ServiceManager.getService("mount");
		if (service != null) {
		   mMountService = IMountService.Stub.asInterface(service);
		} else {
		   Log.e(TAG, "Can't get mount service");
		}
	}
	return mMountService;
}


private void doUnmountSDCard(boolean force) {
	// Present a toast here
	Toast.makeText(this, R.string.unmount_inform_text, Toast.LENGTH_SHORT).show();
	IMountService mountService = getMountService();
	String path = StorageUtils.getSDcardDir();
	try {
		mountService.unmountVolume(path, force, force);
		updateStatusSDCard();
	} catch (RemoteException e) {
		// Informative dialog to user that
		// unmount failed.
		showDialogInner(DLG_ERROR_UNMOUNT);
	}
}

private void showDialogInner(int id) {
	removeDialog(id);
	showDialog(id);
}

private boolean hasAppsAccessingSDCard() throws RemoteException {
	String path = StorageUtils.getSDcardDir();
	IMountService mountService = getMountService();
	int stUsers[] = mountService.getStorageUsers(path);
	if (stUsers != null && stUsers.length > 0) {
		return true;
	}
	ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
	List<ApplicationInfo> list = am.getRunningExternalApplications();
	if (list != null && list.size() > 0) {
		return true;
	}
	return false;
}

private void unmountSDCard() {
	// Check if external media is in use.
	try {
	   if (hasAppsAccessingSDCard()) {
		   if (localLOGV) Log.i(TAG, "Do have storage users accessing media");
		   showDialogInner(DLG_CONFIRM_UNMOUNT);
	   } else {
		   doUnmountSDCard(true);
	   }
	} catch (RemoteException e) {
		// Very unlikely. But present an error dialog anyway
		Log.e(TAG, "Is MountService running?");
		showDialogInner(DLG_ERROR_UNMOUNT);
	}
}

private void mountSDCard() {
	IMountService mountService = getMountService();
	try {
		if (mountService != null) {
			mountService.mountVolume(StorageUtils.getSDcardDir());
		} else {
			Log.e(TAG, "Mount service is null, can't mount");
		}
	} catch (RemoteException ex) {
	}
}


private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
{
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
	{
		//mount/unmount SD card
		if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
		{
			if(position==1){
				String status = SystemProperties.get("EXTERNAL_STORAGE_STATE","unmounted");
				if (status.equals(Environment.MEDIA_MOUNTED)) {
					unmountSDCard();
				} else {
					mountSDCard();
				}
			}

			// SD card format
			if(position==2){
				StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
				String status = StorageUtils.getSDcardState();
				if (status.equals(Environment.MEDIA_MOUNTED)) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setClass(StorageFormatAlterDialogActivity.this, com.rockchip.settings.dialog.MediaFormat.class);
					intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, getVolumeByPath(StorageUtils.getSDcardDir()));
					startActivity(intent);
				}else{
					Toast.makeText(StorageFormatAlterDialogActivity.this, R.string.no_sdcard, Toast.LENGTH_SHORT).show();
				}
			}
		}
		// NAND flash format
		if(position==4){
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(StorageFormatAlterDialogActivity.this, com.rockchip.settings.dialog.MediaFormat.class);
			intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, getVolumeByPath(StorageUtils.getFlashDir()));
			startActivity(intent);
		}
	}
};

private StorageVolume getVolumeByPath(String path){
	StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
	StorageVolume volume = null;
	for(int i = 0; i < storageVolumes.length; i++){
		if(path.equals(storageVolumes[i].getPath())){
			volume = storageVolumes[i];
			Log.d(TAG, "============================");
			Log.d(TAG, "Current Operating Directory=" + path);
			Log.d(TAG, "============================");
		}
		
	}
	return volume;
}
public void onCancel(DialogInterface dialog) {
	finish();
}

}
