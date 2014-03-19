package jp.takke.abc2014sv.ui;

import java.util.ArrayList;

import jp.takke.abc2014sv.App;
import jp.takke.abc2014sv.C;
import jp.takke.abc2014sv.MainActivity;
import jp.takke.abc2014sv.PaneInfo;
import jp.takke.abc2014sv.PaneInfoFactory;
import jp.takke.abc2014sv.R;
import jp.takke.abc2014sv.TPConfig;
import jp.takke.abc2014sv.ThemeColor;
import jp.takke.abc2014sv.util.IconAlertDialogUtil;
import jp.takke.abc2014sv.util.MyLog;
import jp.takke.abc2014sv.util.TPUtil;
import yanzm.products.quickaction.lib.QuickAction;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.EntypoIcon;
import com.commonsware.cwac.tlv.TouchListView;

public class PageConfigActivity extends ListActivity {

	private IconicAdapter mAdapter = null;

	private ArrayList<PaneInfo> mPaneInfoList = new ArrayList<PaneInfo>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// アプリケーション設定のロード
		MainActivity.myInitApplicationConfigForAllView(this);
		
		// テーマ設定
		MainActivity.mySetTheme(this);
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_page_config);

		
		//--------------------------------------------------
		// データロード
		//--------------------------------------------------
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		// ページ一覧のjson取得
		final long userId = 0;//pref.getLong(C.PREF_KEY_TWITTER_USER_ID, 0);
		final String paneInfoJson = pref.getString(C.PREF_KEY_HOME_PANEINFO_JSON_BASE + userId, null);
		if (paneInfoJson != null) {
			PaneInfoFactory.loadFromJson(mPaneInfoList, paneInfoJson);
		} else {
			// 初期ページ一覧生成
			PaneInfoFactory.getDefaultHome(mPaneInfoList);
		}
		
		//--------------------------------------------------
		// UI に反映
		//--------------------------------------------------
		mAdapter = new IconicAdapter();
		setListAdapter(mAdapter);

		final TouchListView tlv = (TouchListView) getListView();
		tlv.setDropListener(onDrop);
//		tlv.setRemoveListener(onRemove);
		
		// クリックリスナー設定
		tlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				
				// メニュー表示
				showItemMenu(position);
			}
		});
		tlv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				// メニュー表示
				showItemMenu(position);
				
				return true;
			}
		});
		
		//--------------------------------------------------
		// ツールバーボタンの初期化
		//--------------------------------------------------
		setupToolbar();
		MyLog.dWithElapsedTime("startupseq[{elapsed}ms] toolbar initialized", App.sStartedAt);
	}

	
	private void setupToolbar() {
		
		// ツールバーの高さ
		{
			final LinearLayout toolbar = (LinearLayout) findViewById(R.id.toolbar);
			final ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) toolbar.getLayoutParams();
			lp.height = TPUtil.dipToPixel(this, TPConfig.toolbarHeight);
			toolbar.setLayoutParams(lp);
		}
		
		// 疑似メニューボタン
		{
			final ImageButton button = (ImageButton) findViewById(R.id.menu_button);
			button.setImageResource(ThemeColor.isLightTheme(TPConfig.theme)
					? R.drawable.ic_menu_moreoverflow_normal_holo_light
					: R.drawable.ic_menu_moreoverflow_normal_holo_dark);
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					showMyOptionsMenu(v);
				}
			});
		}
		
		// ok_button
		{
			final Button button = (Button) findViewById(R.id.ok_button);
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doSaveAndFinish();
				}
			});
			button.setTextColor(ThemeColor.isLightTheme(TPConfig.theme) ? Color.BLACK : Color.WHITE);
		}
		
		// cancel_button
		{
			final Button button = (Button) findViewById(R.id.cancel_button);
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			button.setTextColor(ThemeColor.isLightTheme(TPConfig.theme) ? Color.BLACK : Color.WHITE);
		}
		
	}


	@SuppressLint("CommitPrefEdits")
	protected void doSaveAndFinish() {
		
		// dump
		for (int i=0; i<mPaneInfoList.size(); i++) {
			MyLog.d(" dump [" + i + "] : " + mPaneInfoList.get(i).getDefaultPageTitle(getApplicationContext()));
		}
		
		// データ保存
		final String jsonText = PaneInfoFactory.makeJsonText(mPaneInfoList);
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final SharedPreferences.Editor editor = pref.edit();
		final long userId = 0;//pref.getLong(C.PREF_KEY_TWITTER_USER_ID, 0);
		editor.putString(C.PREF_KEY_HOME_PANEINFO_JSON_BASE + userId, jsonText);
		TPUtil.doSharedPreferencesEditorApplyOrCommit(editor);
		
		// 終了
		final Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}


	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
		
		@Override
		public void drop(int from, int to) {
			
			MyLog.d("DropListener.drop: from[" + from + "] to[" + to + "]");
			
			final PaneInfo item = mAdapter.getItem(from);
			mAdapter.remove(item);
			mAdapter.insert(item, to);
			
			// dump
			for (int i=0; i<mPaneInfoList.size(); i++) {
				MyLog.d(" DropListener [" + i + "] : " + mPaneInfoList.get(i).getDefaultPageTitle(getApplicationContext()));
			}
		}
	};
	
	
	/**
	 * 独自の OptionMenu を表示する
	 * 
	 * @param v
	 */
	protected void showMyOptionsMenu(View v) {
		

    	final QuickAction qa = new QuickAction(v);
        
    	//--------------------------------------------------
        // Menu 追加
    	//--------------------------------------------------
    	
    	// タブの追加
		TPUtil.addQuickActionItem(qa, getApplicationContext(), getString(R.string.menu_add),
				TPUtil.createIconicFontDrawable(PageConfigActivity.this, EntypoIcon.ADD_TO_LIST, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
				new View.OnClickListener() {
					
					@Override
					public void onClick(View vClicked) {
						
						// QuickAction を閉じる
						qa.dismiss();
						
						doAddTab();
					}
				});
		
    	// デフォルトに戻す
		TPUtil.addQuickActionItem(qa, getApplicationContext(), getString(R.string.menu_restore_default),
				TPUtil.createIconicFontDrawable(PageConfigActivity.this, EntypoIcon.BACK, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
				new View.OnClickListener() {
					
					@Override
					public void onClick(View vClicked) {
						
						// QuickAction を閉じる
						qa.dismiss();
						
						doRestoreDefault();
					}
				});
		
		// QuickAction 表示
		qa.setAnimStyle(QuickAction.ANIM_AUTO);
		qa.setLayoutStyle(QuickAction.STYLE_LIST);
		qa.show();
	}


    /**
     * タブの追加
     */
	private void doAddTab() {
		
		final AlertDialog.Builder ab = new AlertDialog.Builder(this);
		
		ab.setTitle(R.string.add_tab);
		final ArrayList<IconAlertDialogUtil.IconItem> items = new ArrayList<IconAlertDialogUtil.IconItem>();
		
//		items.add(TPUtil.createIconItem(this, "*" + getString(R.string.pane_name_lists), EntypoIcon.NUMBERED_LIST, TPConfig.funcColorConfig));
//		
//		items.add(TPUtil.createIconItem(this, "*" + getString(R.string.pane_name_search), EntypoIcon.SEARCH, TPConfig.funcColorConfig));
		
		// デフォルトホームと同じ項目がなければ追加する
		final ArrayList<PaneInfo> addedPaneInfo = new ArrayList<PaneInfo>();
		final int defaultPanesItemIndexBase = items.size();
		{
			final ArrayList<PaneInfo> defaultPaneInfoList = new ArrayList<PaneInfo>();
			PaneInfoFactory.getDefaultHome(defaultPaneInfoList);
			for (final PaneInfo defaultPaneInfo : defaultPaneInfoList) {
				
				boolean found = false;
				for (final PaneInfo pi : mPaneInfoList) {
					if (defaultPaneInfo.equals(pi)) {
						found = true;
					}
				}
				if (!found) {
					// mPaneInfoList に含まれないので追加候補とする
					addedPaneInfo.add(defaultPaneInfo);
					
					items.add(TPUtil.createIconItem(this, defaultPaneInfo.getDefaultPageTitle(getApplicationContext()),
							defaultPaneInfo.getIconId()));
				}
			}
		}
		
		
		
		final ListAdapter adapter = IconAlertDialogUtil.createAdapter(this, items);
		ab.setAdapter(adapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				switch (which) {
					
				default:
					{
						final int index = which - defaultPanesItemIndexBase;
						if (0 <= index && index < addedPaneInfo.size()) {
							final PaneInfo pi = addedPaneInfo.get(index);
							// UI に追加
							mPaneInfoList.add(pi);
							
					    	// 変更通知
							mAdapter.notifyDataSetChanged();
						}
					}
					break;
				}
			}
		});
		ab.create().show();
	}


	/**
	 * デフォルトに戻す
	 */
	private void doRestoreDefault() {
		
		new AlertDialog.Builder(this)
		.setTitle(R.string.menu_restore_default)
		.setMessage(R.string.restore_default_confirm_message)
		.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// 初期ページ一覧生成
				PaneInfoFactory.getDefaultHome(mPaneInfoList);
			
				// UI に反映
				// ※mPaneInfoList の参照先が変わったので作り直す
				mAdapter = new IconicAdapter();
				setListAdapter(mAdapter);
			}
		})
		.setNegativeButton(R.string.common_no, null)
		.show();
	}



	/**
	 * メニュー表示
	 */
	public void showItemMenu(final int position) {
		
		// 検索とタイムラインは削除不可
		final PaneInfo pi = mPaneInfoList.get(position);
		switch (pi.type) {
		case HOME:
			return;
		}
		
		
		// メニュー表示
		final AlertDialog.Builder ad = new AlertDialog.Builder(PageConfigActivity.this);
		
		ad.setTitle(mAdapter.getItem(position).getDefaultPageTitle(getApplicationContext()));
		
		final ArrayList<IconAlertDialogUtil.IconItem> items = new ArrayList<IconAlertDialogUtil.IconItem>();
		
		// タブの削除
		items.add(TPUtil.createIconItem(this, R.string.menu_delete_tab, EntypoIcon.TRASH, TPConfig.funcColorConfig));
		// キャンセル
		items.add(TPUtil.createIconItem(this, R.string.menu_cancel, EntypoIcon.STOP, C.COLOR_TRANSPARENT));
		
		
		final ListAdapter adapter = IconAlertDialogUtil.createAdapter(PageConfigActivity.this, items);
		ad.setAdapter(adapter, new DialogInterface.OnClickListener() {
		
			public void onClick(DialogInterface dialog, int which) {
				
				switch (which) {
				case 0:
					// タブの削除
					{
						final PaneInfo item = mAdapter.getItem(position);
						mAdapter.remove(item);
					}
					
					break;
				}
				
			}
		});
		ad.show();
	}


	class IconicAdapter extends ArrayAdapter<PaneInfo> {
		
		IconicAdapter() {
			super(PageConfigActivity.this, R.layout.page_config_list_row, mPaneInfoList);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row = convertView;

			if (row == null) {
				final LayoutInflater inflater = getLayoutInflater();

				row = inflater.inflate(R.layout.page_config_list_row, parent, false);
			}

			if (position < mPaneInfoList.size()) {
				final TextView label = (TextView) row.findViewById(R.id.label);
				
				final PaneInfo pi = mPaneInfoList.get(position);
				label.setText(pi.getDefaultPageTitle(getApplicationContext()));
				
				final int sizeDip = 32;
				
				final IconicFontDrawable ifd = new IconicFontDrawable(PageConfigActivity.this);
				ifd.setIcon(pi.getIconId());
				
				final int color = pi.getColor();
				if (color != C.SELECTABLE_COLOR_INVALID) {
					ifd.setIconColor(color);
				} else {
					ifd.setIconColor(Color.argb(0xff, 0x90, 0x90, 0x90));
				}
				
				ifd.setIntrinsicHeight(TPUtil.dipToPixel(PageConfigActivity.this, sizeDip));
				ifd.setIntrinsicWidth(TPUtil.dipToPixel(PageConfigActivity.this, sizeDip));
				ifd.setIconPadding(TPUtil.dipToPixel(PageConfigActivity.this, 8));
				
				label.setCompoundDrawablesWithIntrinsicBounds(ifd, null, null, null);
			}
			
			return row;
		}
	}
}
