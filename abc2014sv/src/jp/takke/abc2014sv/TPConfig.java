package jp.takke.abc2014sv;

import jp.takke.abc2014sv.util.MyLog;
import jp.takke.abc2014sv.util.TPUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class TPConfig {

	// Settings のキー
	private static final String CONFIG_KEY_FONT_SIZE_LIST = "FontSize";				// リスト系
	private static final String CONFIG_KEY_DEBUG_MODE = "DebugMode";
	private static final String CONFIG_KEY_TOOLBAR_HEIGHT = "ToolbarHeight";

	public static int theme = C.Theme_Light;

	public static int fontSizeList = C.FONT_SIZE_80;

	public static int toolbarHeight = 38;
	
	public static boolean debugMode = false;
	
	// タブのアイコン
	public static boolean showTabIcon = true;
	
	// 戻るキー
	public static boolean useBackToTimeline = true;
	
	// 機能別カラー
	public static int funcColorConfig = C.FUNC_COLOR_DEFAULT_CONFIG;		// 設定系
	public static int funcColorTwiccaDebug = C.COLOR_BLACK2;				// twiccaプラグイン・デバッグ関係(テーマによって変化する)
	
	
	/**
	 * アプリ共通の設定を取得する
	 * 
	 * @param context コンテキスト
	 * @return SharedPreferences
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		if (context == null) {
			return null;
		}
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	
	/**
	 * ロード
	 * 
	 * @param context コンテキスト
	 * @return 成功時true、失敗時false
	 */
	public static boolean load(Context context) {
		
		final SharedPreferences pref = getSharedPreferences(context);
		
		// テーマ
		final String themeString = pref.getString(C.PREF_KEY_THEME, C.PREF_THEME_DEFAULT);
		theme = ThemeColor.themeStringToThemeCode(themeString);
		
		fontSizeList = pref.getInt(CONFIG_KEY_FONT_SIZE_LIST, fontSizeList);
		
		toolbarHeight = pref.getInt(CONFIG_KEY_TOOLBAR_HEIGHT, toolbarHeight);
		
		debugMode = pref.getBoolean(CONFIG_KEY_DEBUG_MODE, debugMode);
		
		useBackToTimeline = pref.getBoolean(C.PREF_KEY_USE_BACK_TO_TIMELINE, true);
		
		// メニューアイコンカラー
//		try {
//			funcColorTwitterAction = pref.getInt(C.PREF_KEY_FUNC_COLOR_TWACT, C.FUNC_COLOR_DEFAULT_TWACT);
//			funcColorStreaming = pref.getInt(C.PREF_KEY_FUNC_COLOR_STREAMING, C.FUNC_COLOR_DEFAULT_STREAMING);
//			funcColorView = pref.getInt(C.PREF_KEY_FUNC_COLOR_VIEW, C.FUNC_COLOR_DEFAULT_VIEW);
//			funcColorShare = pref.getInt(C.PREF_KEY_FUNC_COLOR_SHARE, C.FUNC_COLOR_DEFAULT_SHARE);
//			funcColorConfig = pref.getInt(C.PREF_KEY_FUNC_COLOR_CONFIG, C.FUNC_COLOR_DEFAULT_CONFIG);
//		} catch (ClassCastException e) {
//			MyLog.e(e);
//		}
    	
		return true;
	}

	
	/**
	 * セーブ
	 * 
	 * @param context コンテキスト
	 * @return 成功時true、失敗時false
	 */
	@SuppressLint("CommitPrefEdits")
	public static boolean save(Context context) {
		
		final long startTick = System.currentTimeMillis();
		
		MyLog.d("config save: start ----------------------------------------");
		
		final SharedPreferences settings = getSharedPreferences(context);
		final SharedPreferences.Editor editor = settings.edit();
		
		editor.putInt(CONFIG_KEY_FONT_SIZE_LIST, fontSizeList);
		editor.putInt(CONFIG_KEY_TOOLBAR_HEIGHT, toolbarHeight);
		editor.putBoolean(CONFIG_KEY_DEBUG_MODE, debugMode);
		
		MyLog.dWithElapsedTime(" [{elapsed}ms] config save: x1", startTick);
		
		// 設定保存
		TPUtil.doSharedPreferencesEditorApplyOrCommit(editor);
		MyLog.dWithElapsedTime(" [{elapsed}ms] config save: done", startTick);
		
		return true;
	}
	
	
	
}
