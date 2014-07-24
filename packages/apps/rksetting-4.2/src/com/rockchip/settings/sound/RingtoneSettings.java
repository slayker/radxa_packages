package com.rockchip.settings.sound;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * @author cx
 *
 */
public class RingtoneSettings {
	private static final String TAG = "RingtoneSettings";
	private static final boolean DEBUG = true;
	private void LOG(String msg){
		if (DEBUG) {
			Log.d(TAG,msg);
		}
	}
	
	private Context mContext = null;
	private Handler mUIHandler = null;
	private Resources mRes = null;
	
	private int mRingtoneType;
    private boolean mShowDefault;
    private boolean mShowSilent;
    
    private int mRequestCode;
    
    public static final int REQUEST_CODE_RINGTONE_PICKER = 1001;
    
	public RingtoneSettings(Context context,Handler handler){
		mContext = context;
		mUIHandler = handler;
		mRes = context.getResources();
		
		mRingtoneType = RingtoneManager.TYPE_NOTIFICATION;
		mShowDefault = true;
		mShowSilent = true;
		
		mRequestCode = REQUEST_CODE_RINGTONE_PICKER;
	}
	
	/**
     * Returns the sound type(s) that are shown in the picker.
     * 
     * @return The sound type(s) that are shown in the picker.
     * @see #setRingtoneType(int)
     */
    public int getRingtoneType() {
        return mRingtoneType;
    }

    /**
     * Sets the sound type(s) that are shown in the picker.
     * 
     * @param type The sound type(s) that are shown in the picker.
     * @see RingtoneManager#EXTRA_RINGTONE_TYPE
     */
    public void setRingtoneType(int type) {
        mRingtoneType = type;
    }

    /**
     * Returns whether to a show an item for the default sound/ringtone.
     * 
     * @return Whether to show an item for the default sound/ringtone.
     */
    public boolean getShowDefault() {
        return mShowDefault;
    }

    /**
     * Sets whether to show an item for the default sound/ringtone. The default
     * to use will be deduced from the sound type(s) being shown.
     * 
     * @param showDefault Whether to show the default or not.
     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_DEFAULT
     */
    public void setShowDefault(boolean showDefault) {
        mShowDefault = showDefault;
    }

    /**
     * Returns whether to a show an item for 'Silent'.
     * 
     * @return Whether to show an item for 'Silent'.
     */
    public boolean getShowSilent() {
        return mShowSilent;
    }

    /**
     * Sets whether to show an item for 'Silent'.
     * 
     * @param showSilent Whether to show 'Silent'.
     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_SILENT
     */
    public void setShowSilent(boolean showSilent) {
        mShowSilent = showSilent;
    }

    
    public void onClick() {
        // Launch the ringtone picker
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        onPrepareRingtonePickerIntent(intent);
//        PreferenceFragment owningFragment = getPreferenceManager().getFragment();
//        if (owningFragment != null) {
//            owningFragment.startActivityForResult(intent, mRequestCode);
//        } else {
            ((RKSettings)mContext).startActivityForResult(intent, mRequestCode);
//        }
    }

    /**
     * Prepares the intent to launch the ringtone picker. This can be modified
     * to adjust the parameters of the ringtone picker.
     * 
     * @param ringtonePickerIntent The ringtone picker intent that can be
     *            modified by putting extras.
     */
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {

        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                onRestoreRingtone());
        
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, mShowDefault);
        if (mShowDefault) {
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    RingtoneManager.getDefaultUri(getRingtoneType()));
        }

        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, mShowSilent);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, mRingtoneType);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, mRes.getString(R.string.notification_sound_dialog_title));
        
        /*
         * Since this preference is for choosing the default ringtone, it
         * doesn't make sense to show a 'Default' item.
         */
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
    }
    
    /**
     * Called when a ringtone is chosen.
     * <p>
     * By default, this saves the ringtone URI to the persistent storage as a
     * string.
     * 
     * @param ringtoneUri The chosen ringtone's {@link Uri}. Can be null.
     */
    protected void onSaveRingtone(Uri ringtoneUri) {
//        persistString(ringtoneUri != null ? ringtoneUri.toString() : "");
    	 RingtoneManager.setActualDefaultRingtoneUri(mContext, getRingtoneType(), ringtoneUri);
    }

    /**
     * Called when the chooser is about to be shown and the current ringtone
     * should be marked. Can return null to not mark any ringtone.
     * <p>
     * By default, this restores the previous ringtone URI from the persistent
     * storage.
     * 
     * @return The ringtone to be marked as the current ringtone.
     */
    protected Uri onRestoreRingtone() {
//        final String uriString = getPersistedString(null);
//        return !TextUtils.isEmpty(uriString) ? Uri.parse(uriString) : null;
    	return RingtoneManager.getActualDefaultRingtoneUri(mContext, getRingtoneType());
    }
    

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }


    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;
        
        /*
         * This method is normally to make sure the internal state and UI
         * matches either the persisted value or the default value. Since we
         * don't show the current value in the UI (until the dialog is opened)
         * and we don't keep local state, if we are restoring the persisted
         * value we don't need to do anything.
         */
        if (restorePersistedValue) {
            return;
        }
        
        // If we are setting to the default value, we should persist it.
        if (!TextUtils.isEmpty(defaultValue)) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }

    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
//        super.onAttachedToHierarchy(preferenceManager);
        
//        preferenceManager.registerOnActivityResultListener(this);
//        mRequestCode = preferenceManager.getNextRequestCode();
        
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == mRequestCode) {
            
            if (data != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                
//                if (callChangeListener(uri != null ? uri.toString() : "")) {
                    onSaveRingtone(uri);
//                }
            }
            
            return true;
        }
        
        return false;
    }

}
