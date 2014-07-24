package com.rockchip.settings.accounts;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rockchip.settings.R;

public class AccountAdapter extends BaseAdapter {
	private List<Drawable> typeList = null;
	private List<String> nameList = null;
	private List<String> syncList = null;
	private List<Drawable> syncDrawList = null;
	private Context mContext = null;
	private LayoutInflater inflater = null;

	AccountViewHolder holder;

	public AccountAdapter(Context context, List<Drawable> typeList,
			List<String> nameList, List<String> syncList,
			List<Drawable> syncDrawList) {
		mContext = context;
		this.typeList = typeList;
		this.nameList = nameList;
		this.syncList = syncList;
		this.syncDrawList = syncDrawList;
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return nameList.size();
	}

	public Object getItem(int position) {
		return nameList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup parent) {

		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.account_list, null);
		holder = new AccountViewHolder();
		holder.type = (ImageView) layout
				.findViewById(R.id.account_providerIcon);
		holder.accountname = (TextView) layout.findViewById(R.id.account_title);
		holder.accountstatus = (TextView) layout
				.findViewById(R.id.account_summary);
		holder.syncstatus = (ImageView) layout
				.findViewById(R.id.account_syncStatusIcon);
		holder.type.setImageDrawable(typeList.get(position));
		holder.accountname.setText(nameList.get(position));
		holder.accountstatus.setText(syncList.get(position));
		holder.syncstatus.setImageDrawable(syncDrawList.get(position));
		return layout;
	}

	class AccountViewHolder {
		ImageView type;
		TextView accountname;
		TextView accountstatus;
		ImageView syncstatus;
	}
}
