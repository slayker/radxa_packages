/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockchip.settings.dialog;

import com.rockchip.settings.R;
import com.rockchip.settings.ListViewAdapter;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.StorageUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.storage.StorageVolume;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.os.Environment;
import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.util.Log;


/**
 * Confirm and execute a format of the sdcard.
 * Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE SD CARD" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 */
public class MediaFormat extends AlertActivity {

    private String TAG = "MediaFormat";
    private static final int KEYGUARD_REQUEST = 55;

    private LayoutInflater mInflater;

    private View mInitialView;
    private Button mInitiateButton;

    private View mFinalView;
    private Button mFinalButton;

    private String mPath;
    private TextView mInitialText;
    private TextView mFinalText;

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Mount Service to format the SD card.
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {
            public void onClick(View v) {

                if (isMonkeyRunning()) {
                    return;
                }
                Intent intent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                // Transfer the storage volume to the new intent
                final StorageVolume storageVolume = getIntent().getParcelableExtra(
                        StorageVolume.EXTRA_STORAGE_VOLUME);
                intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, storageVolume);
                startService(intent);
                finish();
            }
     };

    public static boolean isMonkeyRunning() {
        return SystemProperties.getBoolean("ro.monkey", false);
    }

    /**
     *  Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
            establishFinalConfirmationState();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            finish();
        } 
    }

    private void establishFinalConfirmationState() {
        
         if(mPath.equals(StorageUtils.getSDcardDir())) {
            mFinalView = mInflater.inflate(R.layout.media_format_rockchip_final, null);
            mFinalButton =(Button) mFinalView.findViewById(R.id.execute_media_format);
            mFinalButton.setOnClickListener(mFinalClickListener);
         } 
	  if(mPath.equals(StorageUtils.getFlashDir())){
            mFinalView = mInflater.inflate(R.layout.media_format_rockchip_final, null);
            mFinalText = (TextView) mFinalView.findViewById(R.id.media_format_final_text);
            mFinalText.setText(R.string.media_format_final_desc_nand);
            mFinalButton =(Button) mFinalView.findViewById(R.id.execute_media_format);
            mFinalButton.setOnClickListener(mFinalClickListener);
         }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mInitialView = null;
        mFinalView = null;
        mInflater = LayoutInflater.from(this);

        // Get volume
        final StorageVolume storageVolume = getIntent().getParcelableExtra(
                        StorageVolume.EXTRA_STORAGE_VOLUME);
        mPath = storageVolume.getPath();

	establishFinalConfirmationState();
	mAlert.setView(mFinalView,0,0,0,0);
	this.setupAlert();
    }

    /** Abandon all progress through the confirmation sequence by returning
     * to the initial view any time the activity is interrupted (e.g. by
     * idle timeout).
     */
    @Override
    public void onPause() {
        super.onPause();

        if (!isFinishing()) {
            establishFinalConfirmationState();
        }
    }
}
