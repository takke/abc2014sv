package jp.takke.abc2014sv;

import java.util.ArrayList;

import android.annotation.TargetApi;


/**
 * 文字サイズ管理クラス
 */
@TargetApi(7)
public class FontSize {

	public static float listTitleSize = 14.0f;			// タイトルの文字サイズ
	public static float listDateSize = 11.0f;				// 日付の文字サイズ
	
	
	/**
	 * フォントサイズ反映
	 * 
	 * @param fontSize C.FONT_SIZE_*
	 */
	public static void load(int fontSize) {
		
		listTitleSize = 14.0f + (fontSize - C.FONT_SIZE_100) / 10.0f * 1.0f;
		listDateSize = 11.0f + (fontSize - C.FONT_SIZE_100) / 10.0f * 1.0f;
	}
	
	// フォントサイズ一覧キャッシュ
	private static int[] mFontSizeList = null;
	
	public static int[] getFontSizeList() {
		
		// キャッシュがなければ生成する
		if (mFontSizeList == null) {
			final int step = 5;	// [%]
			final int n = (C.FONT_SIZE_MAX_200 - C.FONT_SIZE_MIN_40)/step + 1;
			final int[] list = new int[n];
			
			int size = C.FONT_SIZE_MAX_200;
			for (int i=0; i<n; i++) {
				list[i] = size;
				
				size -= step;
			}
			
			// キャッシュ保存
			mFontSizeList = list;
		}
		
		return mFontSizeList;
	}

	public static String[] getFontNameList() {
		
		final ArrayList<String> list = new ArrayList<String>();
		final int[] sizes = getFontSizeList();
		for (final int s : sizes) {
			list.add(s + "%");
		}
		
		return list.toArray(new String[]{});
	}
}
