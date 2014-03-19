package jp.takke.abc2014sv;

import android.app.Activity;

/**
 * 文字配色設定
 */
public class ThemeColor {
	
	public static int tabColor = 0xff33b5e5;		// タブカラー
	
	public static int mySelectDialogItemRightIcon = R.drawable.right_arrow_light;	// メニューの右矢印
	
	
	
	/**
	 * テーマ反映
	 * 
	 * テーマ変更時、起動時に呼び出せばOK
	 * 
	 * @param theme テーマ定数
	 */
	public static void load(Activity activity, int theme) {
		
    	switch (theme) {
    	case C.Theme_Black:
    		// ブラック系
    		tabColor = 0xff33b5e5;
    		break;
    		
    	case C.Theme_Paris:
    		tabColor = 0xff1f446a;
    		break;
    		
    	case C.Theme_Sakura:
    		tabColor = 0xffe5a0bc;
    		break;
    		
    	case C.Theme_Light:
    	default:
    		// ホワイト系
    		tabColor = 0xff33b5e5;
    		break;
    	}
    	
    	// Light系/Black系共通
    	if (isLightTheme(theme)) {
    		mySelectDialogItemRightIcon = R.drawable.right_arrow_light;
    	} else {
    		
    		// 2.x系だとメニューの背景色が明るい色なので暗めの矢印にする
    		if (android.os.Build.VERSION.SDK_INT <= 10) {
        		mySelectDialogItemRightIcon = R.drawable.right_arrow_light;
    		} else {
    			// Android 3.0以降なので明るめの矢印にする
        		mySelectDialogItemRightIcon = R.drawable.right_arrow_dark;
    		}
    	}
	}

	public static boolean isLightTheme(int theme) {
		
		switch (theme) {
		case C.Theme_Light:
		case C.Theme_Sakura:
			return true;
			
		case C.Theme_Black:
		case C.Theme_Paris:
		default:
			return false;
		}
	}
	
	public static int themeStringToThemeCode(String themeString) {
		
		if (themeString.equals("Light")) {
			return C.Theme_Light;
		} else if (themeString.equals("Paris")) {
			return C.Theme_Paris;
		} else if (themeString.equals("Sakura")) {
			return C.Theme_Sakura;
		} else {
			return C.Theme_Black;
		}
	}
}
