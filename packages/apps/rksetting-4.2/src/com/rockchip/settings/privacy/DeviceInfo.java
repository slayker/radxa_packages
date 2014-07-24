package com.rockchip.settings.privacy;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemProperties;
import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

import android.os.Build;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import com.rockchip.settings.StorageUtils;
import android.widget.Toast;
import android.util.Log;

public class DeviceInfo {
	private Context mContext;
	private Handler mHandler;

	private String TAG = "DeviceInfo";
    private static final String PRODUCT_VERSION= SystemProperties.get("ro.rksdk.version","rockchip");   

	public DeviceInfo(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	public void getDeviceInfor() {
		((RKSettings) mContext).updateSettingItem(R.string.wifi_status, -1,
				R.string.device_status_summary, -1);
		((RKSettings) mContext).updateSettingItem(R.string.model_number,
				Build.MODEL, null, null);
		((RKSettings) mContext).updateSettingItem(R.string.firmware_version,
				Build.VERSION.RELEASE, null, null);
		((RKSettings) mContext).updateSettingItem(R.string.kernel_version,
				null, getFormattedKernelVersion(), null);
		((RKSettings) mContext).updateSettingItem(R.string.build_number, null,
				PRODUCT_VERSION + "\n" + Build.DISPLAY, null);
		// mHandler.sendEmptyMessageDelayed(
		// SettingMacroDefine.upDateListView);
	}

	private String getFormattedKernelVersion() {
		String procVersionStr;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/version"), 256);
			try {
				procVersionStr = reader.readLine();
			} finally {
				reader.close();
			}

			final String PROC_VERSION_REGEX = "\\w+\\s+" + /* ignore: Linux */
			"\\w+\\s+" + /* ignore: version */
			"([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
			"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /*
														 * group 2:
														 * (xxxxxx@xxxxx
														 * .constant)
														 */
			"\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /* ignore: (gcc ..) */
			"([^\\s]+)\\s+" + /* group 3: #26 */
			"(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
			"(.+)"; /* group 4: date */

			Pattern p = Pattern.compile(PROC_VERSION_REGEX);
			Matcher m = p.matcher(procVersionStr);

			if (!m.matches()) {
				Log.e("Deviceinfo", "Regex did not match on /proc/version: "
						+ procVersionStr);
				return "Unavailable";
			} else if (m.groupCount() < 4) {
				Log.e("Deviceinfo",
						"Regex match on /proc/version only returned "
								+ m.groupCount() + " groups");
				return "Unavailable";
			} else {
				return (new StringBuilder(m.group(1)).append("\n")
						.append(m.group(2)).append(" ").append(m.group(3))
						.append("\n").append(m.group(4))).toString();
			}
		} catch (IOException e) {
			Log.e(TAG,
					"IO Exception when getting kernel version for Device Info screen",
					e);

			return "Unavailable";
		}
	}

	public void getDeviceStatus() {
		String action = StorageUtils.getDeviceStatusAction();
		if(action != null)
		{
			Intent intent = new Intent(action);
			mContext.startActivity(intent);
		}
		else
		{
			Toast.makeText(mContext,R.string.version_error,Toast.LENGTH_LONG).show();
		}
	}

	public void getLicense() {
		Intent intent = new Intent(mContext, SettingsLicenseActivity.class);
		mContext.startActivity(intent);
	}
	public void getTos() {
		Intent intent = new Intent();
		intent.setClassName("com.google.android.gsf", "com.google.android.gsf.settings.SettingsTosActivity");
		mContext.startActivity(intent);
	}
}
