package jp.takke.abc2014sv.ui;

import jp.takke.abc2014sv.C;
import jp.takke.abc2014sv.MainActivity;
import jp.takke.abc2014sv.R;
import jp.takke.abc2014sv.TPConfig;
import jp.takke.abc2014sv.util.TPUtil;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.atermenji.android.iconicdroid.icon.EntypoIcon;
import com.atermenji.android.iconicdroid.icon.FontAwesomeIcon;
import com.atermenji.android.iconicdroid.icon.Icon;

public class ConfigActivity extends PreferenceActivity {

	//--------------------------------------------------
	// その他
	//--------------------------------------------------
	
	// ConfigActivity用のアイコンカラー設定
//	private final static int CA_ICON_COLOR_DESIGN = C.COLOR_RED1;						// テーマ・デザイン
	private final static int CA_ICON_COLOR_APP = C.COLOR_BLUE;							// 全般設定
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		// アプリケーション設定のロード
		MainActivity.myInitApplicationConfigForAllView(this);
		
		// テーマ設定
		MainActivity.mySetTheme(this);
		
		super.onCreate(savedInstanceState);
		
		final PreferenceScreen ps = getPreferenceManager().createPreferenceScreen(this);
		
		final int configMode = getIntent().getIntExtra("ConfigMode", C.CONFIG_MODE_ALL);
		
		// テーマとデザイン設定
//		if (configMode == C.CONFIG_MODE_ALL) {
//			final PreferenceScreen pref = getPreferenceManager().createPreferenceScreen(this);
//			pref.setTitle(R.string.config_theme);
//			
//			mySetIcon(pref, EntypoIcon.BAG, CA_ICON_COLOR_DESIGN);
//			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//				
//				public boolean onPreferenceClick(Preference preference) {
//					
//					final Intent intent = new Intent(ConfigActivity.this, ThemeConfigActivity.class);
//					startActivity(intent);
//					
//					return true;
//				}
//			});
//			ps.addPreference(pref);
//		}
		
		// 表示設定
		if (configMode == C.CONFIG_MODE_ALL) {
			final PreferenceGroup pc = getPreferenceScreenOrCategory();
			pc.setTitle(R.string.config_display_settings_category);
			mySetIcon(pc, EntypoIcon.PICTURE, CA_ICON_COLOR_APP);
			ps.addPreference(pc);
			
			// フォントサイズ
//			{
//				final PreferenceScreen pref = getPreferenceManager().createPreferenceScreen(this);
//				pref.setTitle(R.string.config_font_size);
//				
//				mySetIcon(pref, FontAwesomeIcon.TEXT_WIDTH, CA_ICON_COLOR_APP);
//				pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//					public boolean onPreferenceClick(Preference preference) {
//
//						showFontSizeSettingDialog();
//						
//						return true;
//					}
//				});
//				pc.addPreference(pref);
//			}
			
			// ツールバーの高さ
			{
				final PreferenceScreen pref = getPreferenceManager().createPreferenceScreen(this);
				pref.setTitle(R.string.config_toolbar_height);
				
				mySetIcon(pref, FontAwesomeIcon.RESIZE_VERTICAL, CA_ICON_COLOR_APP);
				pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						showToolbarHeightSettingDialog();
						
						return true;
					}
				});
				pc.addPreference(pref);
			}
			
		}
		
		
		
		// デバッグ設定
		if (configMode == C.CONFIG_MODE_ALL && TPConfig.debugMode) {
			final PreferenceGroup pc = getPreferenceScreenOrCategory();
			pc.setTitle("Debug Config");
			mySetIcon(pc, EntypoIcon.TOOLS, TPConfig.funcColorTwiccaDebug);
			ps.addPreference(pc);
			
			// デバッグ用SDカードダンプ
			{
				final CheckBoxPreference pref = new CheckBoxPreference(this);
				pref.setKey(C.PREF_KEY_ENABLE_DEBUG_DUMP);
				pref.setTitle("Dump JSON");
				pref.setSummary("Dump json data to external storage");
				mySetIcon(pref, EntypoIcon.EXPORT, TPConfig.funcColorTwiccaDebug);
				
				pref.setDefaultValue(false);
				pc.addPreference(pref);
			}
		}
		
	
		setPreferenceScreen(ps);
	}
	



	/**
	 * PreferenceScreen を取得する。但し、Android 2.x では描画が乱れるので PreferenceCategory を返す
	 * 
	 * @return PreferenceScreen または PreferenceCategory
	 */
	@SuppressWarnings("deprecation")
	private PreferenceGroup getPreferenceScreenOrCategory() {
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			return getPreferenceManager().createPreferenceScreen(this);
		} else {
			// Android 2.x では PreferenceCategory を返す
			return new PreferenceCategory(this);
		}
	}

	
	@TargetApi(11)
	private void mySetIcon(Preference pref, final Icon iconType, int iconColor) {
		
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			pref.setIcon(TPUtil.createIconicFontDrawable(this, iconType, C.DEFAULT_ICON_SIZE_DIP, iconColor));
		}
	}


	/**
	 * Toolbar 高さ設定画面表示
	 */
	protected void showToolbarHeightSettingDialog() {
		
		final AlertDialog.Builder ad = new AlertDialog.Builder(this);
		final String[] nameList = new String[]{"32", "38", "42", "48", "52"};
		final int[] sizeList = new int[]{32, 38, 42, 48, 52};
		
		// 選択されているものに印を付ける
		int current = -1;
		for (int i=0; i<sizeList.length; i++) {
			if (TPConfig.toolbarHeight == sizeList[i]) {
				current = i;
				break;
			}
		}
		if (current >= 0) {
			nameList[current] = ">" + nameList[current];
		}
		
		
		final CharSequence[] items = nameList;
		
		ad.setItems(items, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				if (0 <= which && which < sizeList.length) {
					
	    			TPConfig.toolbarHeight = sizeList[which];
    				
					// 保存
    				TPConfig.save(getApplicationContext());
				}
			}
		});
		
		ad.show();
	}
}

