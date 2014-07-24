package com.rockchip.settings.pppoe;


import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.content.Context;
import android.os.Handler;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.graphics.Color;
import android.widget.TabWidget;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.AdapterView;
import android.view.ViewGroup;
import com.rockchip.settings.R;
import android.net.Uri;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.widget.EditText;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import com.rockchip.settings.ScreenInformation;
import android.util.Log;

public class PppoeAccountsSetting extends AlertActivity
{
	private TabWidget mTabWidget = null;
	private ListView mList = null;
	private TextView mTextView = null;
	private EditText mEdit = null;
	private Button mButton = null;
	private Toast mToast = null;
	
	ArrayList<PppoeAccounts> mAccountList = new ArrayList<PppoeAccounts>();
	ArrayList<PppoeAddAccount> content = new ArrayList<PppoeAddAccount>();
	// 用于指示当前显示的TabWidget
	private int mTabWidgetSelected = 0;
	// 用于指示当前选择的账户
	private MyAdapter mAddAccountAdapter = null;
	private PppoeAccountAdapter mAccountAapter = null;

	private static final int ID_INDEX = 0;
	private static final int NAME_INDEX = 1;
	private static final int USER_INDEX = 2;
	private static final int DNS1_INDEX = 3;
	private static final int DNS2_INDEX = 4;
	private static final int PASSWORD_INDEX = 5;

	public static final String PREFERRED_PPPOE_ACCOUNTS_URI_STR = "content://pppoe/accounts/preferaccount";
	private static final Uri PREFERPPPOE_ACCOUNT_URI = Uri.parse(PREFERRED_PPPOE_ACCOUNTS_URI_STR);

	private PppoeAccounts mAccount = null;

	private static final String[] sProjection = new String[] {
			PppoeAccountsContentMeta.PppoeAccountsColumns._ID,
			PppoeAccountsContentMeta.PppoeAccountsColumns.NAME,
			PppoeAccountsContentMeta.PppoeAccountsColumns.USER,
			PppoeAccountsContentMeta.PppoeAccountsColumns.DNS1,
			PppoeAccountsContentMeta.PppoeAccountsColumns.DNS2,
			PppoeAccountsContentMeta.PppoeAccountsColumns.PASSWORD, };

	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout)flater.inflate(R.layout.pppoe_account,null);

		createTabWidget(view);
		createAddAccountListView(view);
		setTabWidgetBackground(0);
		mAlert.setView(view ,-10,-10,-10,-10);
		mAccountAapter = new PppoeAccountAdapter(this,mAccountList);
		mAccount = new PppoeAccounts(null,null,null,null,null,null);
		
		this.setupAlert();	    
	}

	private void createAddAccountListView(View view)
	{
		if(view == null)
			return ;
		mTextView = (TextView)view.findViewById(R.id.no_account);
		mTextView.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
		mButton = (Button)view.findViewById(R.id.add_account_button);
		mButton.setOnClickListener(mButtonLister);
		mList = (ListView)view.findViewById(R.id.listview);
		mList.setOnItemClickListener(mListItemClister);
		mList.setOnItemLongClickListener(mLongItemClickLister);
		if(content!= null)
		{
			content.add(new PppoeAddAccount(getString(R.string.pppoe_accounts_name),null));
			content.add(new PppoeAddAccount(getString(R.string.pppoe_accounts_user),null));
			content.add(new PppoeAddAccount(getString(R.string.pppoe_accounts_password),null));
			content.add(new PppoeAddAccount(getString(R.string.pppoe_accounts_dns1),null));
			content.add(new PppoeAddAccount(getString(R.string.pppoe_accounts_dns2),null));
		}

		mAddAccountAdapter = new MyAdapter(this,content);
		mList.setAdapter(mAddAccountAdapter);
	}
	
	private void createTabWidget(View parent)
	{
		if(parent != null)
		{
			mTabWidget = (TabWidget)parent.findViewById(R.id.tabwidget);
			if(mTabWidget == null)
				return ;
			mTabWidget.setCurrentTab(0);
			for (int i = mTabWidget.getChildCount() - 1; i >= 0; i--)
			{
				TextView child = (TextView)mTabWidget.getChildAt(i);
				child.setTag(i);
				child.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
				child.setOnFocusChangeListener(new View.OnFocusChangeListener() 
				{
					public void onFocusChange(View v, boolean hasFocus) 
					{
						if (hasFocus) 
						{
							for (int j = 0; j < mTabWidget.getTabCount(); j++) 
							{
								if (mTabWidget.getChildTabViewAt(j) == v) 
								{
									mTabWidgetSelected = j;
									mTabWidget.setCurrentTab(j);
									setTabWidgetBackground(j);
									updateView(j);
								}
							}
						}
				}});
            
				child.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v) 
					{
						int current = ((Integer) v.getTag()).intValue();
						mTabWidgetSelected = current;
						mTabWidget.setCurrentTab(current);
						setTabWidgetBackground(current);
						updateView(current);
					}
				});
			}
		}
	}

	private void updateView(int i)
	{
		if(i == 0)
		{
			mList.setVisibility(View.VISIBLE);
			mList.setAdapter(mAddAccountAdapter);
			mButton.setVisibility(View.VISIBLE);
			mTextView.setVisibility(View.GONE);
		}
		else if(i == 1)
		{
			mButton.setVisibility(View.GONE);
			fillAccountListAdapter();
			LOG("mAccountList.size() = "+mAccountList.size());
			if(mAccountList.size() == 0)
			{
				mTextView.setVisibility(View.VISIBLE);
				mList.setVisibility(View.GONE);
			}
			else
			{
				mList.setVisibility(View.VISIBLE);
				mList.setAdapter(mAccountAapter);
				mTextView.setVisibility(View.GONE);
			}
		}
	}
	
	private void setTabWidgetBackground(int selected)
	{
		if((selected < 0) || (selected >= mTabWidget.getTabCount()))
			return ;

		for (int i = 0; i < mTabWidget.getTabCount(); i++) 
		{
			TextView child = (TextView)mTabWidget.getChildAt(i);
			if(child != null)
			{
				if(i == selected)
				{
					child.setTextColor(Color.WHITE);
					child.setBackgroundResource(R.drawable.background_small_selected);
				}
				else
				{
					child.setTextColor(Color.GRAY);
					child.setBackgroundResource(R.drawable.background_small);
				}
			}
		}
	}

	private View.OnClickListener mButtonLister = new View.OnClickListener() 
	{
		// @Override
		public void onClick(View v) 
		{
			if(validateAndSave())
			{
				mAccount = new PppoeAccounts(null,null,null,null,null,null);
				for(int i = 0; i < content.size(); i++)
				{
					content.get(i).summary = null;
				}
				mAddAccountAdapter.notifyDataSetInvalidated();
				showToast(PppoeAccountsSetting.this.getString(R.string.pppoe_add_new_pppoe));
			}
		}
	};
	
	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			if(mTabWidgetSelected == 0)
			{
				addAccount(position);
			}
			else if(mTabWidgetSelected == 1)
			{
				// 更新UI
				mAccountAapter.setSelection(position);
				mAccountAapter.notifyDataSetInvalidated();
				// 保存key
				setSelectedPppoeAccountsKey(mAccountList.get(position).key);
			}
			
		}
	};

	private AdapterView.OnItemLongClickListener mLongItemClickLister = new AdapterView.OnItemLongClickListener()
	{
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
			temp = position;
			new AlertDialog.Builder(PppoeAccountsSetting.this)
					/*	.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
						.setTitle(R.string.pppoe_accounts_menu_delete)
						.setMessage(R.string.delete_question)
						.setPositiveButton(R.string.dlg_ok,new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								if(mTabWidgetSelected == 1)
								{
									String key = mAccountList.get(temp).key;
									mAccountAapter.setSelection(-1);
									mAccountList.remove(temp);
									DeletePppoeAccounts(key);
									if(mAccountList.size() == 0)
										updateView(1);
								}
							}
						})
						.setNegativeButton(R.string.dlg_cancel,new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								
							}
						})
						.show();
			return true;
		}
	};
	
	private void addAccount(int position)
	{
		LOG("position = "+position);
		switch(position)
		{
			case 0: createDialog(0,R.string.pppoe_accounts_name); break;
			case 1: createDialog(1,R.string.pppoe_accounts_user);break;
			case 2: createDialog(2,R.string.pppoe_accounts_password);break;
			case 3: createDialog(3,R.string.pppoe_accounts_dns1);break;
			case 4: createDialog(4,R.string.pppoe_accounts_dns2);break;
		}
	}

	private int temp = -1;
	private void createDialog(int position,int title)
	{
		temp = position;
		mEdit = new EditText(this);
		new AlertDialog.Builder(this)
			/*			.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
						.setTitle(title)
						.setView(mEdit)
						.setPositiveButton(R.string.dlg_ok,new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								PppoeAddAccount addAccount = content.get(temp);
								if(addAccount != null)
								{
									if(temp == 2) // set password,then show "*" repalce the real password
									{
										String text = mEdit.getText().toString();
										if(text == null)
											return ;
										int length = text.length();
										String password = "";
										for(int i =0 ;i < length; i++)
										{
											password += "*";
										}
										addAccount.summary = password;	
									}
									else
									{
										addAccount.summary = mEdit.getText().toString();
									}
								}
								mAddAccountAdapter.notifyDataSetInvalidated();
								setContent(temp,mEdit.getText().toString());
							}
						})
						.show();
	}

	private boolean setContent(int position,String text)
	{
		switch(position)
		{
			case 0: mAccount.name = text; break;
			case 1: mAccount.user = text; break;
			case 2: mAccount.password = text; break;
			case 3: mAccount.dns1 = text; break;
			case 4: mAccount.dns2 = text; break;
			default :return false;
		}
		return true;
	}

	private boolean saveToDataBase()
	{
		LOG("saveToDataBase()");
		Uri mUri = getContentResolver().insert(PppoeAccountsContentMeta.PppoeAccountsColumns.CONTENT_URI,new ContentValues());
		Cursor mCursor = managedQuery(
				mUri,
				sProjection,
				null,
				null,
				PppoeAccountsContentMeta.PppoeAccountsColumns.DEFAULT_SORT_ORDER);
		
		if (!mCursor.moveToFirst()) 
		{
			LOG("Could not go to the first row in the Cursor when saving data.");
			return false;
		}

		// If it's a new account and a name or user haven't been entered, then
		// erase the entry
	/*	if (force && mNewAccount && name.length() < 1 && user.length() < 1) 
		{
			getContentResolver().delete(mUri, null, null);
			return false;
		}*/

		ContentValues values = new ContentValues();
		// Add a dummy name "Untitled", if the user exits the screen without
		// adding a name but
		// entered other information worth keeping.
		values.put(PppoeAccountsContentMeta.PppoeAccountsColumns.NAME, mAccount.name
				.length() < 1 ? this.getString(R.string.untitled_pppoe_account)
				: mAccount.name);
		values.put(PppoeAccountsContentMeta.PppoeAccountsColumns.USER, mAccount.user);
		values.put(PppoeAccountsContentMeta.PppoeAccountsColumns.DNS1, mAccount.dns1);
		values.put(PppoeAccountsContentMeta.PppoeAccountsColumns.DNS2, mAccount.dns2);
		values.put(PppoeAccountsContentMeta.PppoeAccountsColumns.PASSWORD,mAccount.password);

		getContentResolver().update(mUri, values, null, null);
		mCursor.close();
		return true;
	}

	// 判断是否是合法的地址
	private boolean isValidAddress(String value) 
	{
		if(value == null)
			return false;
		
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;
        
        while (start < value.length()) {
            
            if ( -1 == end ) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    LOG("isValidIpAddress() : invalid 'block', block = " + block);
                    return false;
                }
            } catch (NumberFormatException e) {
                LOG("isValidIpAddress() : e = " + e);
                return false;
            }
            
            numBlocks++;
            
            start = end + 1;
            end = value.indexOf('.', start);
        }
        
        return numBlocks == 4;
    }
	
	private boolean validateAndSave() 
	{
		String errMsg = null;
		if ((mAccount.name == null) || (mAccount.name.length() < 1))
		{
			errMsg = this.getString(R.string.pppoe_accounts_error_name_empty);
		} 
		else if ((mAccount.user == null) || (mAccount.user.length() < 1))
		{
			errMsg = this.getString(R.string.pppoe_accounts_error_user_empty);
		} 
		else if ((mAccount.password == null) || (mAccount.password.length() < 1)) 
		{
			errMsg = this.getString(R.string.pppoe_accounts_error_password_empty);
		}

		if(((mAccount.dns1 != null) && !isValidAddress(mAccount.dns1)) ||
				((mAccount.dns2 != null) && !isValidAddress(mAccount.dns2)))
		{
			errMsg = this.getString(R.string.pppoe_dns_error);
		}
		

		if (null != errMsg) 
		{
			showToast(errMsg);
			return false;
		}
		
		return saveToDataBase();
	}

	private void showToast(String msg)
	{
		if(mToast == null)
		{
			mToast = Toast.makeText(this,msg, 3);
			mToast.show();
		}
		else
		{
			mToast.cancel();
			mToast.setText(msg);
			mToast.show();
		}
	}
	
	private class MyAdapter extends BaseAdapter
	{
		private Context mContext = null;
		private ArrayList<PppoeAddAccount> mArrayList = null;
		private LayoutInflater flater = null;
		private int mSelection = 0;
		public MyAdapter(Context context, ArrayList<PppoeAddAccount> List)
		{
			mContext = context;
			mArrayList = List;
			flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setSelection(int i)
		{
			if((i >= 0) && (i < mArrayList.size()))
			{
				mSelection = i;
			}
		}
		
		public int getCount() 
		{
			if(mArrayList != null)
				return mArrayList.size();

			return 0;
		}

		public Object getItem(int position) 
		{
			if(mArrayList != null)
			{
				return mArrayList.get(position);
			}

			return null;
		}
		
		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) 
		{
			RelativeLayout layout = (RelativeLayout)flater.inflate(R.layout.alterdialog_listview_item,null);

			TextView textview = (TextView)layout.findViewById(R.id.list_content);
			ImageView image = (ImageView)layout.findViewById(R.id.list_img);
			TextView summary = (TextView)layout.findViewById(R.id.summary);

			summary.setTextSize(ScreenInformation.mScreenWidth/60f*ScreenInformation.mDpiRatio);
			textview.setTextSize(ScreenInformation.mScreenWidth/45f*ScreenInformation.mDpiRatio);
			
			if(mTabWidgetSelected == 0)
			{				
				summary.setVisibility(View.VISIBLE);
				PppoeAddAccount account = mArrayList.get(position);
				if(account.summary == null)
					summary.setText(R.string.pppoe_accounts_not_set);
				else summary.setText(account.summary);
				image.setVisibility(View.GONE);
				textview.setText(account.title);
				return layout;
			}			

			return null;
			}
		
	}

	private String getSelectedPppoeAccountsKey() 
	{
		String key = null;

		Cursor cursor = managedQuery(
				PREFERPPPOE_ACCOUNT_URI,
				new String[] { "_id" },
				null,
				null,
				PppoeAccountsContentMeta.PppoeAccountsColumns.DEFAULT_SORT_ORDER);
		if (cursor.getCount() > 0) 
		{
			cursor.moveToFirst();
			key = cursor.getString(ID_INDEX);
		}
		cursor.close();
		return key;
	}

	private void setSelectedPppoeAccountsKey(String key) {
		ContentResolver resolver = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(PppoeAccountsContentMeta.PppoeAccountsColumns.PREFERRED_PPPOE_ID, key);
		resolver.update(PREFERPPPOE_ACCOUNT_URI, values, null, null);
	}

	// 从数据库中读取保存的账户信息
	private void fillAccountListAdapter() 
	{
		String[] projection = new String[] 
		{
				PppoeAccountsContentMeta.PppoeAccountsColumns.ID,
				PppoeAccountsContentMeta.PppoeAccountsColumns.NAME,
				PppoeAccountsContentMeta.PppoeAccountsColumns.USER,
				PppoeAccountsContentMeta.PppoeAccountsColumns.DNS1,
				PppoeAccountsContentMeta.PppoeAccountsColumns.DNS2,
				PppoeAccountsContentMeta.PppoeAccountsColumns.PASSWORD, 
		};

		Cursor c = managedQuery(
				PppoeAccountsContentMeta.PppoeAccountsColumns.CONTENT_URI,
				projection,
				null,
				null,
				PppoeAccountsContentMeta.PppoeAccountsColumns.DEFAULT_SORT_ORDER);

		mAccountList.clear();
		String accountKey = getSelectedPppoeAccountsKey();
		LOG("fillAccountListAdapter,accountKey = "+accountKey);
		c.moveToFirst();
		int i = 0;
		while (!c.isAfterLast()) {
			String key = c.getString(ID_INDEX);
			String name = c.getString(NAME_INDEX);
			String user = c.getString(USER_INDEX);
			String dns1 = c.getString(DNS1_INDEX);
			String dns2 = c.getString(DNS2_INDEX);
			String password = c.getString(PASSWORD_INDEX);

			PppoeAccounts account = new PppoeAccounts(key,name,user,dns1,dns2,password);
			mAccountList.add(account);
			if((accountKey != null) && (accountKey.equals(key)))
			{
				mAccountAapter.setSelection(i);
			}

			i++;
			c.moveToNext();
		}
		c.close();
		if (mAccountList.size() == 1) {
			mAccountAapter.setSelection(0);
			setSelectedPppoeAccountsKey(mAccountList.get(0).key);
		}
	}

	private void DeletePppoeAccounts(String key) 
	{
		if(null != key)
		{
			Uri url = ContentUris.withAppendedId(
					PppoeAccountsContentMeta.PppoeAccountsColumns.CONTENT_URI, Integer.parseInt(key));
			ContentResolver resolver = getContentResolver();
			resolver.delete(
					url,
					null, null);
			fillAccountListAdapter();
			mAccountAapter.notifyDataSetInvalidated();
			
		}
	}
	
	public static class PppoeAccounts
	{
		public String key = null;
		public String name = null;
		public String user = null;
		public String dns1 = null;
		public String dns2 = null;
		public String password = null;

		public PppoeAccounts(String key,String name,String user,String dns1,String dns2,String password)
		{
			this.key = key;
			this.name = name;
			this.user = user;
			this.dns1 = dns1;
			this.dns2 = dns2;
			this.password = password;
		}
	}

	public class PppoeAddAccount
	{
		public String title = null;
		public String summary = null;
		public PppoeAddAccount(String title,String summary)
		{
			this.title = title;
			this.summary = summary;
		}
	}
	
	private void LOG(String msg)
	{
		if(true)
			Log.d("PPPOEAccountSetting",msg);
	}
}
