package com.rockchip.settings.privacy;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.Display;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.rockchip.settings.R;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;
import com.rockchip.settings.ScreenInformation;

public class PrivacySettingAlterDialogActivity extends Activity {//AlertActivity
	private static final int KEYGUARD_REQUEST = 55;

	private LayoutInflater mInflater;

	private View mInitialView;
	private View mFinalView;
	private Button mFinalButton;
	private Button mInitiateButton;
	private View mExternalStorageContainer;
	private CheckBox mExternalStorage;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	this.setTitle(getString(R.string.master_clear_title));
		mInitialView = null;
		mFinalView = null;
		mInflater = LayoutInflater.from(this);
		// mLockUtils = new LockPatternUtils(this);
	//	this.setTitle(getString(R.string.privacy_settings));
		establishInitialState();
	}

	/**
	 * The user has gone through the multiple confirmation, so now we go ahead
	 * and invoke the Checkin Service to reset the device to its factory-default
	 * state (rebooting in the process).
	 */
	private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {
		public void onClick(View v) {
			if (Utils.isMonkeyRunning()) {
				return;
			}

			if (mExternalStorage.isChecked()) {
				Intent intent = new Intent(
						ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
				intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
				startService(intent);
			} else {
				sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
				// Intent handling is asynchronous -- assume it will happen
				// soon.
			}
		}
	};
	/**
	 * If the user clicks to begin the reset sequence, we next require a
	 * keyguard confirmation if the user has currently enabled one. If there is
	 * no keyguard available, we simply go to the final confirmation prompt.
	 */
	private Button.OnClickListener mInitiateListener = new Button.OnClickListener() {
		public void onClick(View v) {
			// if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
			establishFinalConfirmationState();
			// }
		}
	};

	/**
	 * Configure the UI for the final confirmation interaction
	 */
	private void establishFinalConfirmationState() {
		if (mFinalView == null) {
			mFinalView = mInflater.inflate(R.layout.format_clear_final, null);
			mFinalButton = (Button) mFinalView
					.findViewById(R.id.format_execute_master_clear);
			mFinalButton.setOnClickListener(mFinalClickListener);
			// Resources res=this.getResources();
			// Bitmap originalImage=BitmapFactory.decodeResource(res,
			// R.drawable.alter_bk);
			// originalImage = Bitmap.createScaledBitmap(originalImage,
			// mFinalView.getWidth(), mFinalView.getHeight(), true);
			// mFinalView.setBackgroundResource(originalImage);
		}

		setContentView(mFinalView);
		if (mFinalButton != null) {
			mFinalButton.setFocusable(true);
			mFinalButton.requestFocus();
		}
	}

	/**
	 * In its initial state, the activity presents a button for the user to
	 * click in order to initiate a confirmation sequence. This method is called
	 * from various other points in the code to reset the activity to this base
	 * state.
	 * 
	 * <p>
	 * Reinflating views from resources is expensive and prevents us from
	 * caching widget pointers, so we use a single-inflate pattern: we lazy-
	 * inflate each view, caching all of the widget pointers we'll need at the
	 * time, then simply reuse the inflated views directly whenever we need to
	 * change contents.
	 */
	private void establishInitialState() {
		if (mInitialView == null) {
			mInitialView = mInflater.inflate(R.layout.format_clear_primary,
					null);
			mInitiateButton = (Button) mInitialView
					.findViewById(R.id.format_initiate_master_clear);
			mInitiateButton.setOnClickListener(mInitiateListener);
			mExternalStorageContainer = mInitialView
					.findViewById(R.id.format_external_container);
			mExternalStorage = (CheckBox) mInitialView
					.findViewById(R.id.format_erase_external);

			float fontSize = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio;
			TextView view = (TextView)mInitialView.findViewById(R.id.master_clear_desc);
			view.setTextSize(fontSize);

			TextView eraseExternelOption = (TextView)mInitialView.findViewById(R.id.erase_external_option_text);
			eraseExternelOption.setTextSize(fontSize-5f);

			TextView eraseExternel = (TextView)mInitialView.findViewById(R.id.erase_external);
			eraseExternel.setTextSize(fontSize-5f);

			TextView erase_all = (TextView)mInitialView.findViewById(R.id.erase_all);
			erase_all.setTextSize(fontSize-5f);

			Button button = (Button)mInitialView.findViewById(R.id.format_initiate_master_clear);
			button.setTextSize(fontSize-5f);
			
	        boolean isExtStorageEmulated = Environment.isExternalStorageEmulated();
	        if (isExtStorageEmulated
	                || (!Environment.isExternalStorageRemovable() && isExtStorageEncrypted())) {
	            mExternalStorageContainer.setVisibility(View.GONE);

	            final TextView externalOption = (TextView)mInitialView.findViewById(R.id.erase_external_option_text);
	            externalOption.setVisibility(View.GONE);
				externalOption.setTextSize(fontSize);

	            final TextView externalAlsoErased = (TextView)mInitialView.findViewById(R.id.also_erases_external);
	            externalAlsoErased.setVisibility(View.VISIBLE);
				externalAlsoErased.setTextSize(fontSize);
	            // If it's not emulated, it is on a separate partition but it means we're doing
	            // a force wipe due to encryption.
	            mExternalStorage.setChecked(!isExtStorageEmulated);
	        } else {
	            mExternalStorageContainer.setOnClickListener(new View.OnClickListener() {

	                @Override
	                public void onClick(View v) {
	                    mExternalStorage.toggle();
	                }
	            });
	        }
			
			loadAccountList();
		}
		setContentView(mInitialView);
//		mAlert.setView(mInitialView,-10,-10,-10,-10);
//		this.setupAlert();
	}

    private boolean isExtStorageEncrypted() {
        String state = SystemProperties.get("vold.decrypt");
        return !"".equals(state);
    }
    
    private void loadAccountList() {
        View accountsLabel = mInitialView.findViewById(R.id.accounts_label);
        LinearLayout contents = (LinearLayout)mInitialView.findViewById(R.id.accounts);
        contents.removeAllViews();

//        Context context = getActivity();

        AccountManager mgr = AccountManager.get(this);
        Account[] accounts = mgr.getAccounts();
        final int N = accounts.length;
        if (N == 0) {
            accountsLabel.setVisibility(View.GONE);
            contents.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        AuthenticatorDescription[] descs = AccountManager.get(this).getAuthenticatorTypes();
        final int M = descs.length;

        for (int i=0; i<N; i++) {
            Account account = accounts[i];
            AuthenticatorDescription desc = null;
            for (int j=0; j<M; j++) {
                if (account.type.equals(descs[j].type)) {
                    desc = descs[j];
                    break;
                }
            }
            if (desc == null) {
                Log.w("PrivacySetting", "No descriptor for account name=" + account.name
                        + " type=" + account.type);
                continue;
            }
            Drawable icon = null;
            try {
                if (desc.iconId != 0) {
                    Context authContext = this.createPackageContext(desc.packageName, 0);
                    icon = authContext.getResources().getDrawable(desc.iconId);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("PrivacySetting", "No icon for account type " + desc.type);
            }

            TextView child = (TextView)inflater.inflate(R.layout.master_clear_account,
                    contents, false);
            child.setText(account.name);
            if (icon != null) {
                child.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
            contents.addView(child);
        }

        accountsLabel.setVisibility(View.VISIBLE);
        contents.setVisibility(View.VISIBLE);
    }
	
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
		} else {
			establishInitialState();
		}
	}

	/**
	 * Abandon all progress through the confirmation sequence by returning to
	 * the initial view any time the activity is interrupted (e.g. by idle
	 * timeout).
	 */
	@Override
	public void onPause() {
		super.onPause();

		if (!isFinishing()) {
			establishInitialState();
		}
	}
}
