package hudl.ota.util;

import java.util.Hashtable;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.adobe.adms.measurement.ADMS_Measurement;

public class TrackingHelper {

	private static final boolean TRACKING_ENABLED = Config.TRACKING_OMNITURE_ENABLED;
	
	private static final boolean TRACKING_LOGS_ENABLED = Config.DEBUG;
	private static final boolean TRACKING_SSL_ENABLED = true;
	private static final boolean TRACKING_OFFLINE_ENABLED = false;

	public static final String TRACKING_PAGE_NAME_CHECKING_FOR_UPDATES = "First Journey: Checking for Updates";
	public static final String TRACKING_PAGE_NAME_REGISTRATION_SUCCESS = "First Journey: Update - Battery Request";
	public static final String TRACKING_UPDATE_ERROR_PAGE_NAME = "First Journey: Update Error";
	public static final String TRACKING_UPDATE_BUSY_PAGE_NAME_PAGE = "First Journey: Update Busy - Error"; 
	public static final String TRACKING_UPDATE_AVAILABLE_PAGE = "First Journey: Update Available";
	public static final String TRACKING_UPDATE_DOWNLOAD_ERROR_PAGE = "First Journey: Update Download Error";
	public static final String TRACKING_DOWNLOAD_COMPLETE_PAGE = "First Journey: Dowload Complete";
	
	 //Verification
	public static final String TRACKING_CHECKING_UPDATE_PAGE = "First Journey: Checking Update";
	public static final String TRACKING_VERIFICATION_ERROR_PAGE = "First Journey: Verification Error";
	
	// Moving 
	public static final String TRACKING_MOVING_PAGE = "First Journey: Moving Update";
	public static final String TRACKING_MOVING_ERROR_PAGE = "First Journey: Moving Error";
	
	// Restart Request
	public static final String TRACKING_RESTART_REQUEST_PAGE = "First Journey: Restart Request";
	public static final String TRACKING_UPDATE_BATTERY_REQUEST_PAGE = "First Journey: Update - Battery Request";
	public static final String TRACKING_INSTALLING_PAGE = "First Journey: Installing";
	
	public static final String TRACKING_EVAR1 = "";
	public static final String TRACKING_EVAR2 = "";
	public static final String TRACKING_EVAR12 = "";
	public static final String TRACKING_EVAR22_STATE_LOGGED_IN = "Logged In";
	public static final String TRACKING_EVAR22_STATE_ANONYMOUS = "Anonymous";
	public static final String TRACKING_EVAR24_CUSTOMER_ID = "";
	public static final String TRACKING_EVAR48 = "eVar48";
	public static final String TRACKING_EVAR56_DEVICE_TYPE = "Pharaoh";
	public static final String TRACKING_PROP4 = "Updates";
	public static final String TRACKING_PROP10 = "Hudl";
	public static final String TRACKING_PROP32_IS_WEEKEND = "Weekend";
	public static final String TRACKING_PROP32_IS_WEEKDAY = "Weekday";

	public static final int KEY_TRACKING_EVAR1 = 1;
	public static final int KEY_TRACKING_EVAR2 = 2;
	public static final int KEY_TRACKING_EVAR12 = 12;
	public static final int KEY_TRACKING_EVAR15 = 15;
	public static final int KEY_TRACKING_EVAR22 = 22;
	public static final int KEY_TRACKING_EVAR24 = 24;
	public static final int KEY_TRACKING_EVAR45 = 45;
	public static final int KEY_TRACKING_EVAR48 = 48;
	public static final int KEY_TRACKING_EVAR56 = 56;
	public static final int KEY_TRACKING_PROP4 = 4;
	public static final int KEY_TRACKING_PROP10 = 10;
	public static final int KEY_TRACKING_PROP30 = 30;
	public static final int KEY_TRACKING_PROP31 = 31;
	public static final int KEY_TRACKING_PROP32 = 32;
	public static final int KEY_TRACKING_PROP36 = 36;

	public static final String TRACKING_EVENT_41 = "event41";
	
	private static final String TRACKING_RSID = Config.TRACKING_OMNITURE_RSID;
	private static final String TRACKING_SERVER = Config.TRACKING_OMNITURE_SERVER;

	private static final String TRACKING_TIMESTAMP_PATTERN = "yyyyMMdd-HH:mm:ss";

	private static ADMS_Measurement measurement;

	public static void startActivity(final Activity activity) {
        ADMS_Measurement measurement = ADMS_Measurement.sharedInstance(activity);
        try{
            measurement.startActivity(activity);
        }
        catch(Exception e){
        }
    }

    public static void stopActivity(final Activity activity) {
        ADMS_Measurement measurement = ADMS_Measurement.sharedInstance(activity);
        try{
            measurement.stopActivity();
        }
        catch(Exception e){
        }  
    }
    
	private static void setCommonParams(ADMS_Measurement measure, String pageName) {
		measure.setAppState(pageName);
		measure.setEvar(KEY_TRACKING_EVAR1, TRACKING_EVAR1);
		measure.setEvar(KEY_TRACKING_EVAR2, TRACKING_EVAR2);
		measure.setEvar(KEY_TRACKING_EVAR12, TRACKING_EVAR12);
		measure.setEvar(KEY_TRACKING_EVAR22, TRACKING_EVAR22_STATE_ANONYMOUS);
		measure.setEvar(KEY_TRACKING_EVAR24, TRACKING_EVAR24_CUSTOMER_ID);
		measure.setEvar(KEY_TRACKING_EVAR48, TRACKING_EVAR48);
		measure.setEvar(KEY_TRACKING_EVAR56, TRACKING_EVAR56_DEVICE_TYPE);
		measure.setProp(KEY_TRACKING_PROP10, TRACKING_PROP10);
		measure.setProp(KEY_TRACKING_PROP30, Util.getHourOfDay());
		measure.setProp(KEY_TRACKING_PROP31, Util.getDayOfWeek());
		measure.setProp(KEY_TRACKING_PROP32,
				Util.isWeekend() ? TRACKING_PROP32_IS_WEEKEND
						: TRACKING_PROP32_IS_WEEKDAY);
		measure.setProp(KEY_TRACKING_PROP36,
				Util.getFormattedTimestamp(TRACKING_TIMESTAMP_PATTERN));
	}

	public static void trackUpdate(Context context, String pageName) {
		if (TRACKING_ENABLED) {
			ADMS_Measurement measure = ADMS_Measurement.sharedInstance(context);
			setCommonParams(measure, pageName);
			track(measure);
		}
	}
	
	public static void trackUpdateErrors(Context context, String pageName, String errorCode) {
		if (TRACKING_ENABLED) {
			ADMS_Measurement measure = ADMS_Measurement.sharedInstance(context);
			setCommonParams(measure, pageName);

			measure.setEvar(KEY_TRACKING_EVAR48, errorCode);
			measure.setEvents(TRACKING_EVENT_41);
			track(measure);
		}
	}

	public static void trackScreenIsShown(Context activity, String pageName) {
		if (TRACKING_ENABLED) {
			ADMS_Measurement measure = ADMS_Measurement
					.sharedInstance(activity);
			setCommonParams(measure, pageName);
			measure.setEvar(KEY_TRACKING_EVAR15, "");
			measure.setProp(KEY_TRACKING_PROP4, TRACKING_PROP4);
			measure.setEvents("event10");
			track(measure);
		}
	}

	private static void track(ADMS_Measurement measure) {
		try{
			// perform tracking
			measure.track();
			measure.clearVars();
		}
		catch(Exception e){
			//Util.log(e);
		}

	}

	private static void track(Map<String, Object> contextData, ADMS_Measurement measure) {
		if (contextData == null)
			return;
		
		final Hashtable<String, Object> hashtable;

        // Get Hashtable
        if (contextData instanceof Hashtable)
            hashtable = (Hashtable<String, Object>) contextData;
        else
            hashtable = new Hashtable<String, Object>(contextData);

        measure.track(hashtable);
	}
	
	public static void trackEvents(final String events, final Map<String, Object> contextData, 
			ADMS_Measurement measure) {
        if (contextData == null) return;
        final Hashtable<String, Object> hashtable;

        // Get Hashtable
        if (contextData instanceof Hashtable)
            hashtable = (Hashtable<String, Object>) contextData;
        else
            hashtable = new Hashtable<String, Object>(contextData);

       measure.trackEvents(events, hashtable);
   }
	
	public static void configureAppMeasurement(Context activity) {
		if (TRACKING_ENABLED) {
			if (TrackingHelper.measurement == null) {
				Util.d("**********************************************************");
				Util.d("**********************************************************");
				Util.d("CONFIGURE TRACKING");
				Util.d("**********************************************************");
				Util.d("**********************************************************");
				TrackingHelper.measurement = ADMS_Measurement
						.sharedInstance(activity);
				TrackingHelper.measurement.configureMeasurement(TRACKING_RSID, TRACKING_SERVER);
				TrackingHelper.measurement.setDebugLogging(TRACKING_LOGS_ENABLED && Util.DEBUG);
				TrackingHelper.measurement.setOfflineTrackingEnabled(TRACKING_OFFLINE_ENABLED);
				TrackingHelper.measurement.setSSL(TRACKING_SSL_ENABLED);
			}
		}
	}

}
