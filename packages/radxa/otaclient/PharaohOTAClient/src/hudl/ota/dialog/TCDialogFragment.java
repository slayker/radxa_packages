package hudl.ota.dialog;

import hudl.ota.Constants;
import hudl.ota.InfoUpdateActivity;
import hudl.ota.R;
import hudl.ota.util.Util;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TCDialogFragment extends DialogFragment {

	public TCDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		ContextThemeWrapper wrapper = new ContextThemeWrapper(this.getActivity(), android.R.style.Theme_Holo);
		AlertDialog.Builder alert2 = new AlertDialog.Builder(wrapper);

		LinearLayout llparent = new LinearLayout(this.getActivity());
		// llparent.setPadding(padding, padding, padding, padding);
		llparent.setOrientation(LinearLayout.VERTICAL);
		llparent.setGravity(Gravity.CENTER);
		llparent.addView(getTextViewClickable());

		alert2.setView(llparent);

		alert2.setPositiveButton(getString(R.string.continue_text), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(getActivity()!=null){
					((InfoUpdateActivity) getActivity()).onTryAgain();
				}
			}
		});

		alert2.setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				if(getActivity()!=null){
					((InfoUpdateActivity) getActivity()).onCancel();	
				}
			}
		});

		return alert2.create();
	}

	public TextView getTextViewClickable() {
		//MPV - TIP-1069  (Matching the Dialog with the one on the First Journey - JR)
		SpannableStringBuilder builtTags = new SpannableStringBuilder();
		String textTermsAndC = getString(R.string.terms_cds_text);
		// SpannableString ss = new SpannableString(textTermsAndC);
		SpannableStringBuilder ssb = new SpannableStringBuilder(textTermsAndC);
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				WebDialogFragment editNameDialog = new WebDialogFragment();
				Bundle b = new Bundle();
				b.putString(WebDialogFragment.EXTRA_LINK, Constants.URL_HUDL_TC);
				editNameDialog.setArguments(b);
				// editNameDialog.show(getFragmentManager(), "");
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(editNameDialog, null);
				ft.commitAllowingStateLoss();
			}
		};
		ssb.setSpan(clickableSpan, 46, 64, 0);

		builtTags.append(ssb);
		
		//String string1 = textTermsAndC.substring(0, 64);
//		SpannableStringBuilder ssb = new SpannableStringBuilder(string1);
//		ClickableSpan clickableSpan = new ClickableSpan() {
//			@Override
//			public void onClick(View textView) {
//				WebDialogFragment editNameDialog = new WebDialogFragment();
//				Bundle b = new Bundle();
//				b.putString(WebDialogFragment.EXTRA_LINK, Constants.URL_HUDL_PRIVACY);
//				editNameDialog.setArguments(b);
//				// editNameDialog.show(getFragmentManager(), "");
//				FragmentTransaction ft = getFragmentManager().beginTransaction();
//				ft.add(editNameDialog, null);
//				ft.commitAllowingStateLoss();
//			}
//		};
//		ssb.setSpan(clickableSpan, 43, string1.length() - 1, 0);
//
//		// Creating second...
//		builtTags.append(ssb);
//		String string2 = textTermsAndC.substring(64, textTermsAndC.length());
//		SpannableStringBuilder ssb2 = new SpannableStringBuilder(string2);
//		ClickableSpan clickableSpan2 = new ClickableSpan() {
//			@Override
//			public void onClick(View textView) {
//				WebDialogFragment editNameDialog = new WebDialogFragment();
//				Bundle b = new Bundle();
//				b.putString(WebDialogFragment.EXTRA_LINK, Constants.URL_HUDL_TC);
//				editNameDialog.setArguments(b);
//				// editNameDialog.show(getFragmentManager(), "");
//				FragmentTransaction ft = getFragmentManager().beginTransaction();
//				ft.add(editNameDialog, null);
//				ft.commitAllowingStateLoss();
//			}
//		};
//		ssb2.setSpan(clickableSpan2, 4, string2.length() - 1, 0);
//
//		// Creating second...
//		builtTags.append(ssb2);
		// Your use of this device is governed by our Terms and Conditions
		// and Privacy Policy.

		TextView tv = new TextView(getActivity());
		tv.setTextColor(getActivity().getResources().getColor(R.color.WHITE));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getActivity().getResources().getDimension(R.dimen.dialog_body_text_font_size));
		tv.setText(builtTags);
		Util.log("Text = " + tv.getText());

		tv.setMovementMethod(LinkMovementMethod.getInstance());

		// Setting margins
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		int lateralMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources()
				.getDimensionPixelOffset(R.dimen.dialog_lateral_margin), getResources().getDisplayMetrics());

		int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources()
				.getDimensionPixelOffset(R.dimen.dialog_top_margin), getResources().getDisplayMetrics());

		int downMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources()
				.getDimensionPixelOffset(R.dimen.dialog_down_margin), getResources().getDisplayMetrics());

		layoutParams.setMargins(lateralMargin, topMargin, lateralMargin, downMargin);

		tv.setLayoutParams(layoutParams);

		return tv;
	}
}
