package com.rockchip.settings.accounts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.google.android.collect.Maps;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.Display;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ViewGroup.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.rockchip.settings.R;

import android.util.Log;

import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.accounts.AuthenticatorDescription;

public class ManageAccountsAlterDialogActivity extends AlertActivity {

	private Button mInitiateButton;
	private TextView mErrorInfoView;

	private AccountAdapter accountAdapter = null;
	private List<Drawable> typeList;
	private List<String> nameList;
	private List<String> syncList;
	private List<Drawable> syncDrawList;

	Account[] accounts;
	private View mListView = null;

	public static final int SYNC_ENABLED = 0; // all know sync adapters are
												// enabled and OK
	public static final int SYNC_DISABLED = 1; // no sync adapters are enabled
	public static final int SYNC_ERROR = 2; // one or more sync adapters have a
											// problem
	private static final String TAG = "ManageAccountsAlterDialogActivity";
	
    private Map<String, AuthenticatorDescription> mTypeToAuthDescription
    = new HashMap<String, AuthenticatorDescription>();
    private AuthenticatorDescription[] mAuthDescs;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setTitle(getString(R.string.sync_settings));
		setContentView(R.layout.manage_accounts_screen);
		mErrorInfoView = (TextView) findViewById(R.id.sync_settings_error_info);
		mErrorInfoView.setVisibility(View.GONE);
		mErrorInfoView.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.ic_list_syncerror), null, null, null);
		mInitiateButton = (Button) findViewById(R.id.add_account_button);
		mInitiateButton.setOnClickListener(mButtonListener);
		
		updateAuthDescriptions();
	}

	// 更新账户列表的方法
	protected void accountUpdated() {
		typeList = new ArrayList<Drawable>();
		nameList = new ArrayList<String>();
		syncList = new ArrayList<String>();
		syncDrawList = new ArrayList<Drawable>();

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean backgroundDataSetting = connManager.getBackgroundDataSetting();
		boolean masterSyncAutomatically = ContentResolver
				.getMasterSyncAutomatically();

		// iterate over all the preferences, setting the state properly for each
		SyncInfo currentSync = ContentResolver.getCurrentSync();
		boolean anySyncFailed = false; // true if sync on any account failed
		// only track userfacing sync adapters when deciding if account is
		// synced or not
		final SyncAdapterType[] syncAdapters = ContentResolver
				.getSyncAdapterTypes();
		HashSet<String> userFacing = new HashSet<String>();
		for (int k = 0, n = syncAdapters.length; k < n; k++) {
			final SyncAdapterType sa = syncAdapters[k];
			if (sa.isUserVisible()) {
				userFacing.add(sa.authority);
			}
		}

		accounts = AccountManager.get(this).getAccounts();

		// 本循环用于更新accountAdapter将要放置的账户名及更新状态
		for (int i = 0, n = accounts.length; i < n; i++) {
			final Account account = accounts[i];

			// Log.d("hhq", account.name + "");
			// Log.e("hhq", account.type + "");
			
	        Drawable icon = null;
	        if (mTypeToAuthDescription.containsKey(account.type)) {
	            try {
	                AuthenticatorDescription desc = mTypeToAuthDescription.get(account.type);
	                Context authContext = createPackageContext(desc.packageName, 0);
	                icon = authContext.getResources().getDrawable(desc.iconId);
	            } catch (PackageManager.NameNotFoundException e) {
	                // TODO: place holder icon for missing account icons?
	                Log.w(TAG, "No icon name for account type " + account.type);
	            } catch (Resources.NotFoundException e) {
	                // TODO: place holder icon for missing account icons?
	                Log.w(TAG, "No icon resource for account type " + account.type);
	            }
	        }
	            typeList.add(icon);


			nameList.add(account.name);
			ArrayList<String> authorities = getAuthoritiesForAccountType(account.type);
			int syncCount = 0;
			boolean syncIsFailing = false;
			if (authorities != null) {
				for (String authority : authorities) {
					SyncStatusInfo status = ContentResolver.getSyncStatus(
							account, authority);
					boolean syncEnabled = ContentResolver.getSyncAutomatically(
							account, authority)
							&& masterSyncAutomatically
							&& backgroundDataSetting
							&& (ContentResolver.getIsSyncable(account,
									authority) > 0);
					boolean authorityIsPending = ContentResolver.isSyncPending(
							account, authority);
					boolean activelySyncing = currentSync != null
							&& currentSync.authority.equals(authority)
							&& new Account(currentSync.account.name,
									currentSync.account.type).equals(account);
					boolean lastSyncFailed = status != null
							&& syncEnabled
							&& status.lastFailureTime != 0
							&& status.getLastFailureMesgAsInt(0) != ContentResolver.SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS;
					if (lastSyncFailed && !activelySyncing
							&& !authorityIsPending) {
						syncIsFailing = true;
						anySyncFailed = true;
					}
					syncCount += syncEnabled && userFacing.contains(authority) ? 1
							: 0;
				}
			} else {
				if (Log.isLoggable(TAG, Log.VERBOSE)) {
					// Log.v(TAG, "no syncadapters found for " + account);
				}
			}
			int syncStatus = SYNC_DISABLED;
			if (syncIsFailing) {
				syncStatus = SYNC_ERROR;
			} else if (syncCount == 0) {
				syncStatus = SYNC_DISABLED;
			} else if (syncCount > 0) {
				syncStatus = SYNC_ENABLED;
			}

			switch (syncStatus) {
			case SYNC_ENABLED:
				syncList.add(getString(R.string.sync_enabled));
				syncDrawList.add(this.getResources().getDrawable(
						R.drawable.ic_sync_green));
				break;
			case SYNC_DISABLED:
				syncList.add(getString(R.string.sync_disabled));
				syncDrawList.add(this.getResources().getDrawable(
						R.drawable.ic_sync_grey));
				break;
			case SYNC_ERROR:
				syncList.add(getString(R.string.sync_error));
				syncDrawList.add(this.getResources().getDrawable(
						R.drawable.ic_sync_red));
				break;
			default:
				syncList.add(getString(R.string.sync_error));
				syncDrawList.add(this.getResources().getDrawable(
						R.drawable.ic_sync_red));
				// Log.e(TAG, "Unknown sync status: " + syncStatus);
			}
		}
		mErrorInfoView.setVisibility(anySyncFailed ? View.VISIBLE : View.GONE);

		TextView mNoAccountView = (TextView) findViewById(R.id.sync_add_an_account);
		mNoAccountView.setVisibility(accounts.length > 0 ? View.GONE
				: View.VISIBLE);

		accountAdapter = new AccountAdapter(this, typeList, nameList, syncList,
				syncDrawList);
		ManageAccountsAlterDialogActivity.this.getListView().setAdapter(
				accountAdapter);

		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Account clickaccount = accounts[position];
				Intent intent = new Intent(
						"android.rockchip.settings.ACCOUNT_SYNC_SETTINGS");
				intent.putExtra("account", clickaccount);
				startActivity(intent);
			}
		});
	}

	public ArrayList<String> getAuthoritiesForAccountType(String type) {
		HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = null;
		if (mAccountTypeToAuthorities == null) {
			mAccountTypeToAuthorities = Maps.newHashMap();
			SyncAdapterType[] syncAdapters = ContentResolver
					.getSyncAdapterTypes();
			for (int i = 0, n = syncAdapters.length; i < n; i++) {
				final SyncAdapterType sa = syncAdapters[i];
				ArrayList<String> authorities = mAccountTypeToAuthorities
						.get(sa.accountType);
				if (authorities == null) {
					authorities = new ArrayList<String>();
					mAccountTypeToAuthorities.put(sa.accountType, authorities);
				}
				authorities.add(sa.authority);
			}
		}
		return mAccountTypeToAuthorities.get(type);
	}

    private void updateAuthDescriptions() {
        mAuthDescs = AccountManager.get(this).getAuthenticatorTypes();
        for (int i = 0; i < mAuthDescs.length; i++) {
            mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
        }
    }
	
	// 获取当前ListView
	private ListView getListView() {
		return (ListView) this.findViewById(R.id.account_lv);
	}

	private Button.OnClickListener mButtonListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(
					"android.rockchip.settings.ADD_ACCOUNT_SETTINGS");
			intent.putExtra("authorities",
					getIntent().getStringArrayExtra("authorities"));
			startActivity(intent);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		// 用于更新ListView里面的内容
		accountUpdated();
	}
}
