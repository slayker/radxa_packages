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

package com.rockchip.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockchip.settings.R;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;

public class AccountSyncSettings extends PreferenceActivity {

	protected static final String TAG = "AccountSettings";
	public static final String ACCOUNT_KEY = "account";
	protected static final int MENU_REMOVE_ACCOUNT_ID = Menu.FIRST;
	private static final int MENU_SYNC_NOW_ID = Menu.FIRST + 1;
	private static final int MENU_SYNC_CANCEL_ID = Menu.FIRST + 2;
	private static final int REALLY_REMOVE_DIALOG = 100;
	private static final int FAILED_REMOVAL_DIALOG = 101;
	private static final int CANT_DO_ONETIME_SYNC_DIALOG = 102;
	private TextView mUserId;
	private TextView mProviderId;
	private ImageView mProviderIcon;
	private TextView mErrorInfoView;
	private Button mRemoveButton;
	private Button mSyncNowButton;
	private Button mSyncCancelButton;
	private Button mAccountSettingButton;
	private java.text.DateFormat mDateFormat;
	private java.text.DateFormat mTimeFormat;
	private Account mAccount;
	// List of all accounts, updated when accounts are added/removed
	// We need to re-scan the accounts on sync events, in case sync state
	// changes.
	private Account[] mAccounts;
	private ArrayList<SyncStateCheckBoxPreference> mCheckBoxes = new ArrayList<SyncStateCheckBoxPreference>();
	private ArrayList<String> mInvisibleAdapters = Lists.newArrayList();
	private Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap<String, AuthenticatorDescription>();
	private AuthenticatorDescription[] mAuthDescs;

	final Activity activity = this;

	@Override
	public Dialog onCreateDialog(final int id) {
		Dialog dialog = null;
		if (id == REALLY_REMOVE_DIALOG) {
			dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.really_remove_account_title)
					.setMessage(R.string.really_remove_account_message)
			/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(R.string.remove_account_label,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									AccountManager
											.get(AccountSyncSettings.this)
											.removeAccount(
													mAccount,
													new AccountManagerCallback<Boolean>() {
														public void run(
																AccountManagerFuture<Boolean> future) {
															boolean failed = true;
															try {
																if (future
																		.getResult() == true) {
																	failed = false;
																}
															} catch (OperationCanceledException e) {
																// handled below
															} catch (IOException e) {
																// handled below
															} catch (AuthenticatorException e) {
																// handled below
															}
															if (failed) {
																showDialog(FAILED_REMOVAL_DIALOG);
															} else {
																finish();
															}
														}
													}, null);
								}
							}).create();
		} else if (id == FAILED_REMOVAL_DIALOG) {
			dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.really_remove_account_title)
			/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
					.setPositiveButton(android.R.string.ok, null)
					.setMessage(R.string.remove_account_failed).create();
		} else if (id == CANT_DO_ONETIME_SYNC_DIALOG) {
			dialog = new AlertDialog.Builder(this)
			/*		.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
					.setTitle(R.string.cant_sync_dialog_title)
					.setMessage(R.string.cant_sync_dialog_message)
					.setPositiveButton(android.R.string.ok, null).create();
		}
		return dialog;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		updateAuthDescriptions();

		setContentView(R.layout.account_sync_screen);
		addPreferencesFromResource(R.xml.account_sync_settings);

		mErrorInfoView = (TextView) findViewById(R.id.sync_settings_error_info);
		mErrorInfoView.setVisibility(View.GONE);

		mUserId = (TextView) findViewById(R.id.user_id);
		mProviderId = (TextView) findViewById(R.id.provider_id);
		mProviderIcon = (ImageView) findViewById(R.id.provider_icon);

		mRemoveButton = (Button) findViewById(R.id.remove_account_button);
		mRemoveButton.setOnClickListener(mRemoveButtonListener);
		mSyncNowButton = (Button) findViewById(R.id.sync_now_button);
		mSyncNowButton.setOnClickListener(mSyncNowButtonListener);
		mSyncCancelButton = (Button) findViewById(R.id.sync_cancel_button);
		mSyncCancelButton.setOnClickListener(mSyncCancelButtonListener);

		mAccountSettingButton = (Button) findViewById(R.id.account_setting);
		mAccountSettingButton.setOnClickListener(mAccountSettingListener);
		mAccountSettingButton.setVisibility(View.GONE);

		final Activity activity = this;

		mDateFormat = DateFormat.getDateFormat(activity);
		mTimeFormat = DateFormat.getTimeFormat(activity);

		Intent intent = this.getIntent();
		Bundle arguments = intent.getExtras();
		mAccount = (Account) arguments.getParcelable(ACCOUNT_KEY);
		if (mAccount != null) {
			if (Log.isLoggable(TAG, Log.VERBOSE))
				Log.v(TAG, "Got account: " + mAccount);
			mUserId.setText(mAccount.name);
			mProviderIcon.setImageDrawable(getDrawableForType(mAccount.type));
			mProviderId.setText(getLabelForType(mAccount.type));

			if (!mAccount.type.equals("com.google"))
				mAccountSettingButton.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onResume() {

		// AccountManager.get(activity).addOnAccountsUpdatedListener(this, null,
		// false);
		updateAuthDescriptions();
		onAccountsUpdated(AccountManager.get(activity).getAccounts());
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		// AccountManager.get(this).removeOnAccountsUpdatedListener(this);
	}

	private Button.OnClickListener mRemoveButtonListener = new Button.OnClickListener() {
		public void onClick(View v) {
			showDialog(REALLY_REMOVE_DIALOG);
		}
	};

	private Button.OnClickListener mSyncNowButtonListener = new Button.OnClickListener() {
		public void onClick(View v) {
			startSyncForEnabledProviders();
			onAccountsUpdated(AccountManager.get(activity).getAccounts());
		}
	};

	private Button.OnClickListener mSyncCancelButtonListener = new Button.OnClickListener() {
		public void onClick(View v) {
			cancelSyncForEnabledProviders();
			onAccountsUpdated(AccountManager.get(activity).getAccounts());
		}
	};

	private Button.OnClickListener mAccountSettingListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(
					"com.android.email.activity.setup.ACCOUNT_MANAGER_ENTRY");
			intent.putExtra("account", mAccount);
			activity.startActivity(intent);
		}
	};

	private void addSyncStateCheckBox(Account account, String authority) {
		SyncStateCheckBoxPreference item = new SyncStateCheckBoxPreference(
				this, account, authority);
		item.setPersistent(false);
		final ProviderInfo providerInfo = getPackageManager()
				.resolveContentProvider(authority, 0);
		CharSequence providerLabel = providerInfo != null ? providerInfo
				.loadLabel(getPackageManager()) : null;
		if (TextUtils.isEmpty(providerLabel)) {
			Log.e(TAG, "Provider needs a label for authority '" + authority
					+ "'");
			providerLabel = authority;
		}
		String title = getString(R.string.sync_item_title, providerLabel);
		item.setTitle(title);
		item.setKey(authority);
		mCheckBoxes.add(item);
	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// super.onCreateOptionsMenu(menu, inflater);
	//
	// MenuItem removeAccount = menu.add(0, MENU_REMOVE_ACCOUNT_ID, 0,
	// getString(R.string.remove_account_label))
	// .setIcon(R.drawable.ic_menu_delete_holo_dark);
	// MenuItem syncNow = menu.add(0, MENU_SYNC_NOW_ID, 0,
	// getString(R.string.sync_menu_sync_now))
	// .setIcon(R.drawable.ic_menu_refresh_holo_dark);
	// MenuItem syncCancel = menu.add(0, MENU_SYNC_CANCEL_ID, 0,
	// getString(R.string.sync_menu_sync_cancel))
	// .setIcon(com.android.internal.R.drawable.ic_menu_close_clear_cancel);
	//
	// removeAccount.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER |
	// MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// syncNow.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER |
	// MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// syncCancel.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER |
	// MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// }

	// @Override
	// public void onPrepareOptionsMenu(Menu menu) {
	// super.onPrepareOptionsMenu(menu);
	// boolean syncActive = ContentResolver.getCurrentSync() != null;
	// menu.findItem(MENU_SYNC_NOW_ID).setVisible(!syncActive);
	// menu.findItem(MENU_SYNC_CANCEL_ID).setVisible(syncActive);
	// }

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case MENU_SYNC_NOW_ID:
	// startSyncForEnabledProviders();
	// return true;
	// case MENU_SYNC_CANCEL_ID:
	// cancelSyncForEnabledProviders();
	// return true;
	// case MENU_REMOVE_ACCOUNT_ID:
	// showDialog(REALLY_REMOVE_DIALOG);
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferences,
			Preference preference) {
		if (preference instanceof SyncStateCheckBoxPreference) {
			SyncStateCheckBoxPreference syncPref = (SyncStateCheckBoxPreference) preference;
			String authority = syncPref.getAuthority();
			Account account = syncPref.getAccount();
			boolean syncAutomatically = ContentResolver.getSyncAutomatically(
					account, authority);
			if (syncPref.isOneTimeSyncMode()) {
				requestOrCancelSync(account, authority, true);
			} else {
				boolean syncOn = syncPref.isChecked();
				boolean oldSyncState = syncAutomatically;
				if (syncOn != oldSyncState) {
					// if we're enabling sync, this will request a sync as well
					ContentResolver.setSyncAutomatically(account, authority,
							syncOn);
					// if the master sync switch is off, the request above will
					// get dropped. when the user clicks on this toggle,
					// we want to force the sync, however.
					if (!ContentResolver.getMasterSyncAutomatically()
							|| !syncOn) {
						requestOrCancelSync(account, authority, syncOn);
					}
				}
			}
			return true;
		} else {
			return super.onPreferenceTreeClick(preferences, preference);
		}
	}

	private void startSyncForEnabledProviders() {
		requestOrCancelSyncForEnabledProviders(true /* start them */);
		this.invalidateOptionsMenu();
	}

	private void cancelSyncForEnabledProviders() {
		requestOrCancelSyncForEnabledProviders(false /* cancel them */);
		this.invalidateOptionsMenu();
	}

	private void requestOrCancelSyncForEnabledProviders(boolean startSync) {
		// sync everything that the user has enabled
		int count = getPreferenceScreen().getPreferenceCount();
		for (int i = 0; i < count; i++) {
			Preference pref = getPreferenceScreen().getPreference(i);
			if (!(pref instanceof SyncStateCheckBoxPreference)) {
				continue;
			}
			SyncStateCheckBoxPreference syncPref = (SyncStateCheckBoxPreference) pref;
			if (!syncPref.isChecked()) {
				continue;
			}
			requestOrCancelSync(syncPref.getAccount(), syncPref.getAuthority(),
					startSync);
		}
		// plus whatever the system needs to sync, e.g., invisible sync adapters
		if (mAccount != null) {
			for (String authority : mInvisibleAdapters) {
				requestOrCancelSync(mAccount, authority, startSync);
			}
		}
	}

	private void requestOrCancelSync(Account account, String authority,
			boolean flag) {
		if (flag) {
			Bundle extras = new Bundle();
			extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(account, authority, extras);
		} else {
			ContentResolver.cancelSync(account, authority);
		}
	}

	private boolean isSyncing(List<SyncInfo> currentSyncs, Account account,
			String authority) {
		for (SyncInfo syncInfo : currentSyncs) {
			if (syncInfo.account.equals(account)
					&& syncInfo.authority.equals(authority)) {
				return true;
			}
		}
		return false;
	}

	protected void onSyncStateUpdated() {
		setFeedsState();
	}

	private void setFeedsState() {
		// iterate over all the preferences, setting the state properly for each
		Date date = new Date();
		List<SyncInfo> currentSyncs = ContentResolver.getCurrentSyncs();
		boolean syncIsFailing = false;

		// Refresh the sync status checkboxes - some syncs may have become
		// active.
		updateAccountCheckboxes(mAccounts);

		for (int i = 0, count = getPreferenceScreen().getPreferenceCount(); i < count; i++) {
			Preference pref = getPreferenceScreen().getPreference(i);
			if (!(pref instanceof SyncStateCheckBoxPreference)) {
				continue;
			}
			SyncStateCheckBoxPreference syncPref = (SyncStateCheckBoxPreference) pref;

			String authority = syncPref.getAuthority();
			Account account = syncPref.getAccount();

			SyncStatusInfo status = ContentResolver.getSyncStatus(account,
					authority);
			boolean syncEnabled = ContentResolver.getSyncAutomatically(account,
					authority);
			boolean authorityIsPending = status == null ? false
					: status.pending;
			boolean initialSync = status == null ? false : status.initialize;

			boolean activelySyncing = isSyncing(currentSyncs, account,
					authority);
			boolean lastSyncFailed = status != null
					&& status.lastFailureTime != 0
					&& status.getLastFailureMesgAsInt(0) != ContentResolver.SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS;
			if (!syncEnabled)
				lastSyncFailed = false;
			if (lastSyncFailed && !activelySyncing && !authorityIsPending) {
				syncIsFailing = true;
			}
			if (Log.isLoggable(TAG, Log.VERBOSE)) {
				Log.d(TAG, "Update sync status: " + account + " " + authority
						+ " active = " + activelySyncing + " pend ="
						+ authorityIsPending);
			}

			final long successEndTime = (status == null) ? 0
					: status.lastSuccessTime;
			if (successEndTime != 0) {
				date.setTime(successEndTime);
				final String timeString = mDateFormat.format(date) + " "
						+ mTimeFormat.format(date);
				syncPref.setSummary(timeString);
			} else {
				syncPref.setSummary("");
			}
			int syncState = ContentResolver.getIsSyncable(account, authority);

			syncPref.setActive(activelySyncing && (syncState >= 0)
					&& !initialSync);
			syncPref.setPending(authorityIsPending && (syncState >= 0)
					&& !initialSync);

			syncPref.setFailed(lastSyncFailed);
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			final boolean masterSyncAutomatically = ContentResolver
					.getMasterSyncAutomatically();
			final boolean backgroundDataEnabled = connManager
					.getBackgroundDataSetting();
			final boolean oneTimeSyncMode = !masterSyncAutomatically
					|| !backgroundDataEnabled;
			syncPref.setOneTimeSyncMode(oneTimeSyncMode);
			syncPref.setChecked(oneTimeSyncMode || syncEnabled);
		}
		mErrorInfoView.setVisibility(syncIsFailing ? View.VISIBLE : View.GONE);
		this.invalidateOptionsMenu();
	}

	public void onAccountsUpdated(Account[] accounts) {
		mAccounts = accounts;
		updateAccountCheckboxes(accounts);
		onSyncStateUpdated();
	}

	private void updateAccountCheckboxes(Account[] accounts) {
		mInvisibleAdapters.clear();

		SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypes();
		HashMap<String, ArrayList<String>> accountTypeToAuthorities = Maps
				.newHashMap();
		for (int i = 0, n = syncAdapters.length; i < n; i++) {
			final SyncAdapterType sa = syncAdapters[i];
			if (sa.isUserVisible()) {
				ArrayList<String> authorities = accountTypeToAuthorities
						.get(sa.accountType);
				if (authorities == null) {
					authorities = new ArrayList<String>();
					accountTypeToAuthorities.put(sa.accountType, authorities);
				}
				if (Log.isLoggable(TAG, Log.VERBOSE)) {
					Log.d(TAG, "onAccountUpdated: added authority "
							+ sa.authority + " to accountType "
							+ sa.accountType);
				}
				authorities.add(sa.authority);
			} else {
				// keep track of invisible sync adapters, so sync now forces
				// them to sync as well.
				mInvisibleAdapters.add(sa.authority);
			}
		}

		for (int i = 0, n = mCheckBoxes.size(); i < n; i++) {
			getPreferenceScreen().removePreference(mCheckBoxes.get(i));
		}
		mCheckBoxes.clear();

		for (int i = 0, n = accounts.length; i < n; i++) {
			final Account account = accounts[i];
			if (Log.isLoggable(TAG, Log.VERBOSE)) {
				Log.d(TAG, "looking for sync adapters that match account "
						+ account);
			}
			final ArrayList<String> authorities = accountTypeToAuthorities
					.get(account.type);
			if (authorities != null
					&& (mAccount == null || mAccount.equals(account))) {
				for (int j = 0, m = authorities.size(); j < m; j++) {
					final String authority = authorities.get(j);
					// We could check services here....
					int syncState = ContentResolver.getIsSyncable(account,
							authority);
					if (Log.isLoggable(TAG, Log.VERBOSE)) {
						Log.d(TAG, "  found authority " + authority + " "
								+ syncState);
					}
					if (syncState > 0) {
						addSyncStateCheckBox(account, authority);
					}
				}
			}
		}

		Collections.sort(mCheckBoxes);
		for (int i = 0, n = mCheckBoxes.size(); i < n; i++) {
			getPreferenceScreen().addPreference(mCheckBoxes.get(i));
		}
	}

	/**
	 * Updates the titlebar with an icon for the provider type.
	 */
	// @Override
	// protected void onAuthDescriptionsUpdated() {
	// super.onAuthDescriptionsUpdated();
	// getPreferenceScreen().removeAll();
	// if (mAccount != null) {
	// mProviderIcon.setImageDrawable(getDrawableForType(mAccount.type));
	// mProviderId.setText(getLabelForType(mAccount.type));
	// PreferenceScreen prefs = addPreferencesForType(mAccount.type);
	// if (prefs != null) {
	// updatePreferenceIntents(prefs);
	// }
	// }
	// addPreferencesFromResource(R.xml.account_sync_settings);
	// }

	private void updatePreferenceIntents(PreferenceScreen prefs) {
		for (int i = 0; i < prefs.getPreferenceCount(); i++) {
			Intent intent = prefs.getPreference(i).getIntent();
			if (intent != null) {
				intent.putExtra(ACCOUNT_KEY, mAccount);
				// This is somewhat of a hack. Since the preference screen we're
				// accessing comes
				// from another package, we need to modify the intent to launch
				// it with
				// FLAG_ACTIVITY_NEW_TASK.
				// TODO: Do something smarter if we ever have PreferenceScreens
				// of our own.
				intent.setFlags(intent.getFlags()
						| Intent.FLAG_ACTIVITY_NEW_TASK);
			}
		}
	}

	/**
	 * Gets an icon associated with a particular account type. If none found,
	 * return null.
	 * 
	 * @param accountType
	 *            the type of account
	 * @return a drawable for the icon or null if one cannot be found.
	 */
	protected Drawable getDrawableForType(final String accountType) {
		Drawable icon = null;
		if (mTypeToAuthDescription.containsKey(accountType)) {
			try {
				AuthenticatorDescription desc = mTypeToAuthDescription
						.get(accountType);
				Context authContext = createPackageContext(desc.packageName, 0);
				icon = authContext.getResources().getDrawable(desc.iconId);
			} catch (PackageManager.NameNotFoundException e) {
				// TODO: place holder icon for missing account icons?
				Log.w(TAG, "No icon name for account type " + accountType);
			} catch (Resources.NotFoundException e) {
				// TODO: place holder icon for missing account icons?
				Log.w(TAG, "No icon resource for account type " + accountType);
			}
		}
		return icon;
	}

	/**
	 * Gets the label associated with a particular account type. If none found,
	 * return null.
	 * 
	 * @param accountType
	 *            the type of account
	 * @return a CharSequence for the label or null if one cannot be found.
	 */
	protected CharSequence getLabelForType(final String accountType) {
		CharSequence label = null;
		if (mTypeToAuthDescription.containsKey(accountType)) {
			try {
				AuthenticatorDescription desc = mTypeToAuthDescription
						.get(accountType);
				Context authContext = createPackageContext(desc.packageName, 0);
				label = authContext.getResources().getText(desc.labelId);
			} catch (PackageManager.NameNotFoundException e) {
				Log.w(TAG, "No label name for account type " + accountType);
			} catch (Resources.NotFoundException e) {
				Log.w(TAG, "No label resource for account type " + accountType);
			}
		}
		return label;
	}

	private void updateAuthDescriptions() {
		mAuthDescs = AccountManager.get(this).getAuthenticatorTypes();
		for (int i = 0; i < mAuthDescs.length; i++) {
			mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
		}
	}
}
