package hudl.ota;

import hudl.ota.dialog.AlertDialogInterface;
import hudl.ota.dialog.TCDialogFragment;
import hudl.ota.model.UpdateInfo;
import hudl.ota.util.Util;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class InfoUpdateActivity extends OTAActivity implements AlertDialogInterface {

	private UpdateInfo mUpdateInfo;
	private TextView mTitleText;
	private ProgressBar mProgressBar;
	private TextView mDescriptionText;
	private LinearLayout mButtonsContainer;
	private Button skipUpdateButton, downloadUpdateButton;
	private boolean batError = false, memError = false, mDownloadLock = false;

	@Override
	protected void onResume() {
		super.onResume();
		mDownloadLock = false;
		batError = false;
		long bytesNeeded = 0;

		try {

			bytesNeeded = Long.parseLong(mUpdateInfo.getSize());
			// bytesNeeded = 1024 * 1024 * 1024 * 20;
			Util.log("We need " + bytesNeeded + " bytes to download the update");
		} catch (NumberFormatException e) {
			Util.log(e.toString());
		}

		if (Util.isThereEnoughInternalMemory(bytesNeeded)) {
			Util.log("We HAVE more than" + bytesNeeded + " bytes to download the update");

			if (Util.isThereEnoughCacheMemory(bytesNeeded)) {
				Util.log("We HAVE more than" + bytesNeeded + " bytes to download the update (Cache)");
				// Everything ok
			} else {
				// Clean cache
				Util.log("We DO NOT have more than" + bytesNeeded + " bytes to download the update (Cache)");

				Util.trimCache(Environment.getDownloadCacheDirectory());

			}

			memError = false;
		} else {
			// to avoid showing text more than once:
			if (!memError) {
				// Ask user to clean memory
				mDescriptionText.setText(mDescriptionText.getText() + "\n\n"
						+ getString(R.string.memory_message_insufficient));
			}

			setupButtonForDownloadDisabled();

			memError = true;
		}
		// }

		if (!batError) {
			downloadUpdateButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					if (isFromHome()) {
						if (!mDownloadLock) {
							mDownloadLock = true;
							downloadUpdate();
						}
					} else {
						TCDialogFragment alertTCDialog = new TCDialogFragment();
						FragmentTransaction ft = getFragmentManager().beginTransaction();
						ft.add(alertTCDialog, null);
						ft.commitAllowingStateLoss();
					}
				}
			});
		} else {
			// Retry
			downloadUpdateButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!mDownloadLock) {
						mDownloadLock = true;
						onResume();
					}
				}
			});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Fixing TIP-1159
		super.onCreate(savedInstanceState);
		
		if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
			Toast.makeText(this, R.string.toast_notification_only_owner, Toast.LENGTH_LONG).show();
			overridePendingTransition(0, 0);
			finish();
			overridePendingTransition(0, 0);
			return;
		}

		setContentView(R.layout.main_ota_client_checker);

		mTitleText = (TextView) findViewById(R.id.title);

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mProgressBar.setIndeterminate(true);

		mDescriptionText = (TextView) findViewById(R.id.description);

		mButtonsContainer = (LinearLayout) findViewById(R.id.container_buttons);

		Intent i = getIntent();

		mUpdateInfo = (UpdateInfo) i.getParcelableExtra(Constants.EXTRA_UPDATE_INFO);

		if (mUpdateInfo != null) {

			mTitleText.setText(R.string.label_hudl_system_update);

			mProgressBar.setIndeterminate(false);
			mProgressBar.setEnabled(false);

			mDescriptionText.setText(getString(R.string.new_update_coming) + "\n" + mUpdateInfo.getDescription());

		}

		mButtonsContainer.setVisibility(View.VISIBLE);

		skipUpdateButton = (Button) findViewById(R.id.button_skip_update);
		skipUpdateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				skip();
			}
		});

		skipUpdateButton.setVisibility(View.GONE);

		downloadUpdateButton = (Button) findViewById(R.id.button_download_update);

	}

	@Override
	protected void onStart() {

		mShared.edit().putInt(Constants.SHARED_STATUS_OTA_UPDATE, Constants.STATUS.STATUS_INFO_UPDATE_ACTIVITY)
				.commit();

		super.onStart();
	}

	@Override
	protected void onStop() {

		Util.log(" ON STOP");

		super.onStop();
		if (!isFromHome() && UserHandle.myUserId() == UserHandle.USER_OWNER) {
			finish();
			overridePendingTransition(0, 0);
		}
	}

	private void downloadUpdate() {

		Intent intent = new Intent(this, OTAClientDownloader.class);
		intent.putExtra(Constants.EXTRA_UPDATE_INFO, mUpdateInfo);
		intent.putExtra(Constants.EXTRA_UPDATE_FROM_HOME, isFromHome());

		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);
		removeNotification();
	}

	private void removeNotification() {

		Util.log("REMOVING NOTIFICATION");
		NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		nm.cancelAsUser(null, Constants.NOTIFICATION_DOWNLOAD_NEW_AVAILABLE, UserHandle.ALL);
		mShared.edit().putBoolean(Constants.SHARED_ACTIVE_NEW_DOWNLOAD_NOTIFICATION, false).commit();
	}

	@Override
	public void onTryAgain() {
		downloadUpdate();
	}

	@Override
	public void onCancel() {

	}

	private void setupButtonForDownloadDisabled() {
		if (mUpdateInfo.isMandatory()) {
			skipUpdateButton.setVisibility(View.INVISIBLE);
		} else {
			skipUpdateButton.setVisibility(View.VISIBLE);
		}
		downloadUpdateButton.setVisibility(View.INVISIBLE);
	}

}
