package hudl.ota.dialog;

import hudl.ota.R;
import hudl.ota.util.Util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class WebDialogFragment extends DialogFragment {

	public static final String EXTRA_LINK = "link";

	private WebView mDialogWebView;

	public WebDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		String url = getArguments().getString(EXTRA_LINK);

		mDialogWebView.loadUrl(url);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		ContextThemeWrapper wrapper = new ContextThemeWrapper(this.getActivity(), android.R.style.Theme_Holo);
		AlertDialog.Builder alert2 = new AlertDialog.Builder(wrapper);
		alert2.setTitle(getString(R.string.title_tc_webview));

		mDialogWebView = new WebView(this.getActivity());
		final ProgressBar myPB = new ProgressBar(this.getActivity());

		mDialogWebView.clearHistory();
		mDialogWebView.clearFormData();
		mDialogWebView.clearCache(true);

		WebSettings webSettings = mDialogWebView.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		mDialogWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				Util.log(" URL running " + url);

				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// Show webview and hide progress bar
				mDialogWebView.setVisibility(View.VISIBLE);
				myPB.setVisibility(View.INVISIBLE);
			}
		});

		LinearLayout llparent = new LinearLayout(this.getActivity());
		llparent.setOrientation(LinearLayout.VERTICAL);
		llparent.setLayoutParams(new LayoutParams(250, 250));

		llparent.setGravity(Gravity.CENTER);

		LinearLayout llh = new LinearLayout(this.getActivity());
		llh.setOrientation(LinearLayout.HORIZONTAL);
		llparent.setLayoutParams(new LayoutParams(250, 250));
		llh.setGravity(Gravity.CENTER);

		llh.addView(myPB);
		mDialogWebView.setVisibility(View.INVISIBLE);
		llparent.addView(mDialogWebView);
		llparent.addView(llh);
		alert2.setView(llparent);

		alert2.setNegativeButton(getString(R.string.close_text), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
			}
		});

		return alert2.create();
	}
}