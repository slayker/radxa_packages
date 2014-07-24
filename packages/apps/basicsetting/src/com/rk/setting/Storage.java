package com.rk.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.text.format.Formatter;
import java.io.File;
import android.content.Context;
import android.widget.TextView;
import com.rk.setting.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;


public class Storage extends Activity
{
	private String mSdTotalSpace;
	private String mSdAvailableSpace;
	
	private String mNANDTotalSpace;
	private String mNANDAvailableSpace;
	
	private String mExternalAvailableSpace;

	private IMountService mMountService = null;
	private StorageManager mStorageManager = null;

	private TextView mSdCardTotal = null;
	private TextView mSdCardAvailable = null;
	private TextView mNandTotal = null;
	private TextView mNandAvailable = null;
	private TextView mInternalAvailable = null;
	
	private void createTitle()
	{
		ImageView image = (ImageView)findViewById(R.id.title_image);
		Bitmap resize = bitMapScale(R.drawable.storage);
		image.setScaleType(ImageView.ScaleType.CENTER);
		image.setImageBitmap(resize);

		TextView title = (TextView)findViewById(R.id.title_text);
		title.setTextSize(ScreenInformation.mScreenWidth/25f*ScreenInformation.mDpiRatio);
	}

	private Bitmap bitMapScale(int id)
	{
		Bitmap map = BitmapFactory.decodeResource(this.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*ScreenInformation.mDpiRatio;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}
	
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_CLEARABLE_FLAGS);
        setContentView(R.layout.storage);
		createTitle();

		createContentTitle();
		setUpView();
		updateStatusForStorage();
    }

	private void createContentTitle()
	{
		float size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio;
		float off = 5f;
		
		TextView view_sdcard = (TextView)findViewById(R.id.sdcard);
		view_sdcard.setTextSize(size+off);

		TextView title = (TextView)findViewById(R.id.sdcard_total_title);
		title.setTextSize(size);

		TextView view = (TextView)findViewById(R.id.sdcard_available_title);
		view.setTextSize(size);
			
		if(getPackageManager().hasSystemFeature("android.settings.sdcard"))
		{
			view_sdcard.setVisibility(View.GONE);
			title.setVisibility(View.GONE);
			view.setVisibility(View.GONE);
		}

		view = (TextView)findViewById(R.id.nandflash);
		view.setTextSize(size+off);
		view = (TextView)findViewById(R.id.nand_total_title);
		view.setTextSize(size);
		view = (TextView)findViewById(R.id.nand_available_title);
		view.setTextSize(size);

		view = (TextView)findViewById(R.id.internal_flash);
		view.setTextSize(size+off);
		view = (TextView)findViewById(R.id.internal_available_title);
		view.setTextSize(size);
	}
	
	private void setUpView()
	{
		mSdCardTotal = (TextView)findViewById(R.id.sdcard_total);
		mSdCardAvailable = (TextView)findViewById(R.id.sdcard_available);
		mNandTotal = (TextView)findViewById(R.id.nand_total);
		mNandAvailable = (TextView)findViewById(R.id.nand_available);
		mInternalAvailable = (TextView)findViewById(R.id.internal_available);

		if(getPackageManager().hasSystemFeature("android.settings.sdcard"))
		{
			mSdCardTotal.setVisibility(View.GONE);
			mSdCardAvailable.setVisibility(View.GONE);
		}
		
		float size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio;
		mSdCardTotal.setTextSize(size);
		mSdCardAvailable.setTextSize(size);
		mNandTotal.setTextSize(size);
		mNandAvailable.setTextSize(size);
		mInternalAvailable.setTextSize(size);
	}
	
	StorageEventListener mStorageListener = new StorageEventListener() {
		@Override
		public void onStorageStateChanged(String path, String oldState, String newState) {
	//		Log.i(TAG, "Received storage state changed notification that " +
	//				path + " changed state from " + oldState + " to " + newState);
			updateStatusForStorage();
		}
	};

	protected void onResume()
	{
		super.onResume();
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			mStorageManager.registerListener(mStorageListener);
		}
	}

	protected void onPause() {
		super.onPause();
		if (mStorageManager != null && mStorageListener != null) {
			mStorageManager.unregisterListener(mStorageListener);
		}
	}

	private void updateStatusForStorage()
	{
		if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
			updateStatusSDCard();
		
		updateStatusNandflash();
		updateStatusInternal();
	}
	
	private String formatSize(long size) {
	    return Formatter.formatFileSize(this, size);
	}

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
			mSdTotalSpace=getResources().getString(R.string.status_unavailable);
			mSdAvailableSpace=getResources().getString(R.string.status_unavailable);
		}

		mSdCardTotal.setText(mSdTotalSpace);
		mSdCardAvailable.setText(mSdAvailableSpace);
	}

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
				mNANDTotalSpace=getResources().getString(R.string.status_unavailable);
				mNANDAvailableSpace=getResources().getString(R.string.status_unavailable);
			}
		}else{
			mNANDTotalSpace=getResources().getString(R.string.status_unavailable);
			mNANDAvailableSpace=getResources().getString(R.string.status_unavailable);
		}

		mNandTotal.setText(mNANDTotalSpace);
		mNandAvailable.setText(mNANDAvailableSpace);
	}

	private void updateStatusInternal(){
		File dataPath = Environment.getDataDirectory();
		StatFs stat = new StatFs(dataPath.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		//show the space of internal storage
		mExternalAvailableSpace=formatSize(availableBlocks * blockSize);
		mInternalAvailable.setText(mExternalAvailableSpace);
	}
}

