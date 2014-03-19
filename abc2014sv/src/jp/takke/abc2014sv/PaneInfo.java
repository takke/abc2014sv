package jp.takke.abc2014sv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import jp.takke.abc2014sv.util.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.atermenji.android.iconicdroid.icon.EntypoIcon;
import com.atermenji.android.iconicdroid.icon.Icon;

public class PaneInfo {

    public enum PaneType {
        
        LIVE_INFO,              // ライブ情報
        HOME,                   // ホーム
        CONFERENCE,             // カンファレンス
//      BAZAAR,                 // バザール
    };
    
    public PaneType type = PaneType.HOME;
    
    private HashMap<String, String> paramMap = null;
    
    
    public PaneInfo(PaneType type) {
        this.type = type;
    }
    
    public PaneInfo(PaneType type, int categoryId) {
        this.type = type;
        setParam("CATEGORY_ID", String.valueOf(categoryId));
    }
    
    
    /**
     * デフォルトページタイトルの取得
     * ※リスト等は別途生成が必要
     * 
     * @return ページタイトル
     */
    public String getDefaultPageTitle(Context context) {
        
        if (context == null) {
            return null;
        }
        
        switch (type) {
        case LIVE_INFO: return "ライブ情報";
        case HOME:      return context.getString(R.string.pane_name_timeline);
        
        case CONFERENCE:
            {
                final int categoryId = getCategoryId();
                switch (categoryId) {
                case 1: return "基調講演/Reborn";
                case 2: return "デザイン・開発";
                case 3: return "メーカー・キャリア";
                case 4: return "コンテンツ・ビジネス";
                case 5: return "開発";
                case 6: return "デバイス";
                case 7: return "EffectiveAndroidコラボ開発";
                case 8: return "LT";
                case 9: return "ビジネス・教育";
                }
                
                return "Conference";
            }

        }

        return "";
    }
    
    public int getCategoryId() {
        final String s = getParam("CATEGORY_ID");
        
        if (s != null) {
            return Integer.valueOf(s);
        }
        
        return 0;
    }

    /**
     * パラメータの設定
     * 
     * @param key
     * @param value
     */
    public void setParam(String key, String value) {
        if (paramMap == null) {
            paramMap = new HashMap<String, String>();
        }
        paramMap.put(key, value);
    }
    
    /**
     * パラメータの取得
     * 
     * @param key
     * @return キーがない場合はnull
     */
    public String getParam(String key) {
        if (paramMap == null || !paramMap.containsKey(key)) {
            return null;
        }
        return paramMap.get(key);
    }
    
    /**
     * パラメータの削除
     * 
     * @param key
     */
    public void deleteParam(String key) {
        if (paramMap != null) {
            paramMap.remove(key);
        }
    }
    
    /**
     * パラメータ一覧の取得
     * 
     * @return
     */
    public HashMap<String, String> getParamMap() {
        return paramMap;
    }
    
    /**
     * JSON 文字列に変換
     * 
     * @return
     */
    public String toJsonText() {
        
        final JSONObject json = new JSONObject();
        
        try {
            json.put("type", type.toString());
            
            if (paramMap != null) {
                final JSONObject param = new JSONObject();
                for (Entry<String, String> kv : paramMap.entrySet()) {
                    param.put(kv.getKey(), kv.getValue());
                }
                json.put("param", param);
            }
            
        } catch (JSONException e) {
            MyLog.e(e);
        }
        
        return json.toString();
    }
    
    public static PaneInfo fromJson(final String jsonText) {

        final PaneInfo paneInfo = new PaneInfo(PaneType.HOME);
        
        try {
            
            final JSONObject json = new JSONObject(jsonText);
            final String type = json.optString("type", null);
            if (type != null) {
                try {
                    paneInfo.type = PaneType.valueOf(type);
                } catch (RuntimeException e) {
                    // データ不正で type 定義が変わった可能性があるので無視する
                    MyLog.w(e);
                }
            }
            
            final JSONObject param = json.optJSONObject("param");
            if (param != null) {
                paneInfo.paramMap = new HashMap<String, String>();
                
                final Iterator<?> keys = param.keys();
                while (keys.hasNext()) {
                    final String key = (String) keys.next();
                    final String value = param.getString(key);
                    paneInfo.paramMap.put(key, value);
                }
            }
            
        } catch (JSONException e) {
            MyLog.e(e);
        }
        
        return paneInfo;
    }
    
    public boolean equals(final PaneInfo o) {
        
        // 種別チェック
        if (this.type != o.type) {
            return false;
        }
        
        // "color" 以外の全パラメータが同一であること
        HashMap<String, String> myParams = this.paramMap;
        HashMap<String, String> oParams = o.paramMap;
        
        if (myParams==null && oParams==null) {
            return true;
        }
        
        
        // "color" 以外をチェックするためにデータを統一する
        if (myParams==null) {
            
            if (oParams.size() == 0) {
                // oParamsの要素数が0なら両方パラメータなしなので同一とみなす
                return true;
            } else if (oParams.size() == 1 && oParams.containsKey("color")) {
                // oParamsの要素数が1で"color"のみであれば同一とみなす
                return true;
            } else {
                return false;
            }
        }
        if (oParams==null) {
            if (myParams.size() == 0) {
                // myParamsの要素数が0なら両方パラメータなしなので同一とみなす
                return true;
            } else if (myParams.size() == 1 && myParams.containsKey("color")) {
                // myParamsの要素数が1で"color"のみであれば同一とみなす
                return true;
            } else {
                return false;
            }
        }
        
        // myParamsもoParamsも非null
        
        // "color"以外の全キーが同一であること
        
        // "color"値を削除しても元のmapには残るようにcloneを作る
        final Set<String> myKeys = new HashSet<String>(myParams.keySet());
        final Set<String> oKeys = new HashSet<String>(oParams.keySet());
        myKeys.remove("color");
        oKeys.remove("color");
        if (myKeys.size() != oKeys.size()) {
            // "color"を除外してもパラメータ数が異なるので違う
            return false;
        } else {
            // "color"以外のキー数が一致するので、それらの値が同一であること
            for (Entry<String, String> kv : myParams.entrySet()) {
                
                // カラーは無視する
                if (kv.getKey().equals("color")) {
                    continue;
                }
                
                if (!kv.getValue().equals(oParams.get(kv.getKey()))) {
                    return false;
                }
            }
        }
        
        return true;
    }


    /**
     * type に応じたアイコンIDの取得
     * 
     * @return アイコンID
     */
    public Icon getIconId() {
        
        switch (type) {
        case LIVE_INFO: return EntypoIcon.FLAG;
        case HOME:      return EntypoIcon.HOME;
        case CONFERENCE:return EntypoIcon.MEGAPHONE;
        
        default:        return EntypoIcon.DOC;
        }
    }


    /**
     * "color" パラメータから文字・アイコンカラーを取得する
     * 
     * @return カラー、未設定時は 0 を返す
     */
    public int getColor() {
        
        final String colorText = getParam("color");
        if (colorText != null) {
            try {
                return Integer.parseInt(colorText);
            } catch (NumberFormatException e) {
                MyLog.e(e);
            }
        }
        return C.SELECTABLE_COLOR_INVALID;
    }

    public PaneInfo setColor(int color) {
        
        setParam("color", String.valueOf(color));
        
        return this;
    }
    
}
