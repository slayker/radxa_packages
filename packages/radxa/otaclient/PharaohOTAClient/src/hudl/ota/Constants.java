package hudl.ota;

import hudl.ota.util.Config;
import android.app.AlarmManager;
import android.os.Environment;

public class Constants {

	public static final boolean DEBUG = Config.DEBUG;
	public static final String TAG = "OTA_CLIENT";

	public static final String URL_AMAZON = "https://device.mobile.tesco.com/ota/";

	public static final String URL_AMAZON_STAGING = "http://device.mobile.inf.stormreply.net/ota/";

	public static final String URL_HUDL_TC = "https://device.mobile.tesco.com/legal/hudl_terms.html";
	public static final String URL_HUDL_PRIVACY = "https://device.mobile.tesco.com/legal/hudl_privacy.html";

	public static final String EXTRA_UPDATE_FROM_HOME = "com.tesco.ota.extra.fromhome";
	public static final String EXTRA_UPDATE_INFO = "com.tesco.ota.extra.update_info";
	public static final String EXTRA_UPDATE_FILE_PATH = "com.tesco.ota.extra.file_path";
	public static final String EXTRA_DOWNLOAD_ID = "com.tesco.ota.extra.download_id";

	public static final String ACTION_DOWNLOAD_UPDATE = "com.tesco.ota.action.downloadupdate";

	protected static final String CACHE_TARGET = Environment.getDownloadCacheDirectory().getAbsolutePath()
			+ "/update.zip";;

	public static final String SHARED_VERSION_NUMBER = "version_number";
	public static final String SHARED_DOWNLOAD_ID = "shared_download_id";
	public static final String SHARED_TIME_TO_CHEK = "time_to_check";
	public static final String SHARED_LAST_CHECK_TIMESTAMP = "last_check_timestamp";
	public static final String SHARED_LAST_KNOWN_VERSION_NUMBER = "last_known_version_number";
	public static final String SHARED_INFO_UPDATE = "shared_info_update";
	public static final String SHARED_LOCATION_UPDATE = "shared_location_update";
	public static final String SHARED_FROM_CHECK_BUTTON = "shared_from_check_button"; // MPV
																						// -
																						// TIP-1082
	public static final String SHARED_LAST_UPDATED_TIME = "shared_last_updated_time"; // MPV
																						// -
																						// TIP-1082

	public static final String SHARED_ACTIVE_NEW_DOWNLOAD_NOTIFICATION = "shared_active_new_download_notification";

	public static final int NOTIFICATION_DOWNLOAD_COMPLETE_ID = 333;
	public static final int NOTIFICATION_DOWNLOAD_NEW_AVAILABLE = 334;
	public static final int NOTIFICATION_UPDATE_PERFORMED = 335;
	public static final int NOTIFICATION_DOWNLOAD_RUNNING = 336;
	public static final int NOTIFICATION_DOWNLOAD_READY_TO_INSTALL = 337;

	public static final long ALARM_INTERVAL = AlarmManager.INTERVAL_DAY;
	public static final long CHECK_INTERVAL = AlarmManager.INTERVAL_DAY * 7;

	public interface INTENT {

		public static final String SETTINGS_UPDATE = "com.tesco.ota.settings_update";
		public static final String NOTIFICATION_CLICKED = "com.tesco.ota.notification_clicked";
		public static final String NOTIFICATION_DOWNLOAD_DONE = "com.tesco.ota.notification_done";
	}

	public static final String SHARED_STATUS_OTA_UPDATE = "ota_status_update";

	public interface STATUS {

		public static final int STATUS_IDLE = 1;
		public static final int STATUS_INFO_UPDATE_ACTIVITY = 2;
		public static final int STATUS_CLIENT_DOWNLOADER = 3;
		public static final int STATUS_VERIFY_OTA_UPDATE = 4;
		public static final int STATUS_INSTALL_UPDATE = 5;
		public static final int STATUS_BATTERY_CHECK = 6;
	}

	public static final int WATCHDOG_THRESHOLD = 15 * 1000; // 15 seconds
}
