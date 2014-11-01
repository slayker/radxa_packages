package hudl.ota;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/*
 * This activity is triggered when an update has been performed. 
 */
public class UpdatePerformedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_update_performed);

		String versionString = getString(R.string.hudl_update_performed) +" "+ Build.VERSION.INCREMENTAL;

		TextView textTitleView = (TextView) findViewById(R.id.title);
		textTitleView.setText(R.string.hudl_update_performed_title);

		TextView textDescriptionView = (TextView) findViewById(R.id.description);

		textDescriptionView.setText(versionString);

		Button closeButton = (Button) findViewById(R.id.button_close);
		closeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(0, 0);
			}
		});
	}
}
