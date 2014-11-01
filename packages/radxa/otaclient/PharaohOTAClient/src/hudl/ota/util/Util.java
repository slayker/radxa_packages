package hudl.ota.util;

import hudl.ota.Constants;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;

public final class Util {
	private static String _MAC_HASH = null;

	public static final float minimumBatteryLevel = 0.3f; // 30%
	
	private final static Locale mLocale = Locale.ENGLISH;
	public final static boolean DEBUG = false;
	private final static String TAG = "DEBUG";
	
	public static float getBatteryLevel(Context context) {

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float) scale;
		Log.d("BatteryLevel", "Bat at " + batteryPct + "%");

		return batteryPct;		
	}

	/**
	 * Value is the percentage in this format: 0.80f = 80%
	 * 
	 * @param context
	 * @param value
	 * @return
	 */
	public static boolean isBatteryLevelAcceptable(Context context, float value) {

		return value <= getBatteryLevel(context);

	}

	public static boolean isChargingBattery(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);

		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		return isCharging;
	}

	public static void log(Exception e) {
		if (Constants.DEBUG) {
			e.printStackTrace();
		}

	}

	public static void log(String log) {
		if (Constants.DEBUG) {
			Log.d(Constants.TAG, log);
		}
	}

	public static boolean isThereEnoughInternalMemory(long spaceNeededBytes) {
		Util.log("Internal free space = " + getInternalFreeSpace());

		return getInternalFreeSpace() > spaceNeededBytes;
	}

	public static float getInternalFreeSpace() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static boolean isThereEnoughCacheMemory(long spaceNeededBytes) {
		return getCacheFreeSpace() > spaceNeededBytes;
	}

	public static float getCacheFreeSpace() {
		File path = Environment.getDownloadCacheDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static boolean trimCache(File dir) {
		try {
			if (dir != null && dir.isDirectory()) {
				Util.log("Trying to delete... " + dir.getName());
				return deleteDirUntillBytes(dir);
			} else
				return false;
		} catch (Exception e) {
			Util.log("\nTrimCache Exception... " + e);
			return false;
		}
	}

	/*
	 * dir has to be a directory inside of Cache memory MB: Memory you need
	 */
	public static boolean deleteDirUntillBytes(File dir) {


		if (dir != null && dir.isDirectory() && dir.canWrite()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirUntillBytes(new File(dir, children[i]));
				if (!success) {
					Util.log("Failing deleting... " + new File(dir, children[i]).getName());
				}
			}
		}

		// if (dir.canWrite()) {
		if (dir.isFile() && dir.getName().endsWith(".zip")) {
			Util.log("Deleting... " + dir.getName());
			boolean removed = dir.delete();
			Util.log("Deleting... " + removed);
			return removed;
		} else {
			return false;
		}		
	}

	public static void log(Throwable tr) {
		if (Constants.DEBUG) {
			Log.e(Constants.TAG, Constants.TAG, tr);
		}
	}

	public static String getWifiMACDeviceID(Context context) {
		if (_MAC_HASH == null) {
			try {
				WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				String mac = wm.getConnectionInfo().getMacAddress();

				if (mac != null && mac.length() > 0) {
					_MAC_HASH = byteArrayToHexString(md5(mac.getBytes()));
				}
			} catch (Exception e) {
				// should never happen...
				log(e);
			}
		}
		return _MAC_HASH;
	}

	public static final byte[] md5(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			// should never happen on this platform
			log(e);
			return null;
		}
	}

	public static boolean isDeviceConnectedOrConnecting(Context c) {

		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo infoNetwork = cm.getActiveNetworkInfo();
		return (infoNetwork != null && infoNetwork.isConnectedOrConnecting());

	}

	public static boolean isDeviceConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo infoNetwork = cm.getActiveNetworkInfo();
		return (infoNetwork != null && infoNetwork.isConnected());
	}

	@SuppressLint("DefaultLocale")
	public static String byteArrayToHexString(byte[] data) {
		StringBuffer buffer = new StringBuffer(data.length * 2);

		for (int i = 0; i < data.length; i++) {
			int v = data[i] & 0xff;
			if (v < 16) {
				buffer.append('0');
			}
			buffer.append(Integer.toHexString(v));
		}
		return buffer.toString().toUpperCase();
	}

	public static boolean isDeviceProvisioned(Context context) {
		return Settings.Secure.getInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) == 1;
	}
	public static String getHourOfDay() {
        Date now = new Date();
        String hourFormat = new SimpleDateFormat("H", mLocale).format(now);
        return hourFormat;
    }

    public static String getDayOfWeek() {
        Date now = new Date();
        String weekDayFormat = new SimpleDateFormat("EEEEE", mLocale).format(now);
        return weekDayFormat;
    }

    public static boolean isWeekend() {
        String dayOfWeek = getDayOfWeek();
        if(dayOfWeek.equalsIgnoreCase("Sunday") || dayOfWeek.equalsIgnoreCase("Saturday")) {
            return true;
        }
        return false;
    }

    public static String getFormattedTimestamp(String pattern) {
        Date now = new Date();
        String timestamp = new SimpleDateFormat(pattern, mLocale).format(now);
        return timestamp;
    }

    public Locale getLocale() {
        return mLocale;
    }

    public static void d(String d) {
        if (DEBUG) {
            Log.d(TAG, d);
        }
    }
}
