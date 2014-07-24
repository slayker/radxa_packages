package com.rk.setting.bluetooth;


import android.text.TextWatcher;
import android.text.Editable;
import android.app.Dialog;
import android.app.AlertDialog;

public class RenameEditTextPreference implements TextWatcher{
		private Dialog mDialog;
		public void setDialog(Dialog dialog)
		{
			mDialog = dialog;
		}
        public void afterTextChanged(Editable s) {
            Dialog d = mDialog;
            if (d instanceof AlertDialog) {
                ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
            }
        }

        // TextWatcher interface
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // not used
        }

        // TextWatcher interface
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // not used
        }
    }
