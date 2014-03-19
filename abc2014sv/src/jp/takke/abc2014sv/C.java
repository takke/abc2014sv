package jp.takke.abc2014sv;




/// constants
public class C {

    public static final String LOG_NAME = "abc2014sv";
    
    public static final String EXTERNAL_FILE_DIRNAME = "abc2014sv";
    
    // SDカード保存用のログファイル
    public static final String EXTERNAL_LOG_FILENAME = "log.txt";
    
    // SDカード上の各種ログファイルの結合一時ファイル
    public static final String EXTERNAL_ATTACH_FILENAME = "attach.txt";

    // デバッグログの送信先
    public static final String AUTHOR_MAIL = "takke30@gmail.com";

    
    //-------------------------------------------
    // テーマ
    //-------------------------------------------
    public static final String PREF_KEY_THEME = "Theme";
    public static final String PREF_THEME_DEFAULT = "Light";
    
    public static final int Theme_Black = android.R.style.Theme_Black;
    public static final int Theme_Light = android.R.style.Theme_Light;
    public static final int Theme_Paris = 100;
    public static final int Theme_Sakura = 200;
    
    //-------------------------------------------
    // フォントサイズ
    //-------------------------------------------
    public static final int FONT_SIZE_MAX_200 = 200;
    public static final int FONT_SIZE_100 = 100;
    public static final int FONT_SIZE_80 = 80;
    public static final int FONT_SIZE_MIN_40 = 40;
    
    
    
    //-------------------------------------------
    // 詳細設定関連
    //-------------------------------------------
    
    // ホームのタブ配置(後ろにユーザーIDを付加したキーとし、マルチアカウント対応とすること)
    public static final String PREF_KEY_HOME_PANEINFO_JSON_BASE = "HomePaneinfoJson_";
    
    // StarMap
    public static final String PREF_KEY_STAR_MAP = "StarMap";
    
    // 戻るキーでタイムラインに戻る
    public static final String PREF_KEY_USE_BACK_TO_TIMELINE = "UseBackToTimeline";
    
    // デバッグモードのSDカードダンプ
    public static final String PREF_KEY_ENABLE_DEBUG_DUMP = "EnableDebugDump";
    

    //--------------------------------------------------
    // カラー関連
    //--------------------------------------------------
    
    // カラー一覧
    public static final int COLOR_TRANSPARENT= 0x00000000;
    public static final int COLOR_WHITE1     = 0xffffffff;
    public static final int COLOR_WHITE2     = 0xffa0a0a0;
    public static final int COLOR_BLACK1     = 0xff000000;
    public static final int COLOR_BLACK2     = 0xff202020;
    public static final int COLOR_RED1       = 0xffff4444;
    public static final int COLOR_RED2       = 0xffcc0000;
    public static final int COLOR_ORANGE     = 0xfff27318;
    public static final int COLOR_YELLOW     = 0xfffdce10;
    public static final int COLOR_LIGHTGREEN = 0xff7bb026;
    public static final int COLOR_GREEN      = 0xff009944;
    public static final int COLOR_SKYBLUE    = 0xff1ab0d6;
    public static final int COLOR_BLUE       = 0xff1478ff;
    public static final int COLOR_PURPLE1    = 0xffaa66cc;
    public static final int COLOR_PURPLE2    = 0xff9933cc;
    public static final int COLOR_DEEPPURPLE = 0xff1f446a;
    public static final int COLOR_PINK       = 0xfff2aab2;
    
    // 機能別カラー初期値
    public static final int FUNC_COLOR_DEFAULT_VIEW = COLOR_SKYBLUE;        // 表示系(「RTしたユーザー一覧」など)
    public static final int FUNC_COLOR_DEFAULT_SHARE = COLOR_PURPLE1;       // 「ブラウザで開く」「共有」など
    public static final int FUNC_COLOR_DEFAULT_CONFIG = COLOR_BLUE;         // 設定系
    
    // メニューアイコンカラー設定用キー
    public static final String PREF_KEY_FUNC_COLOR_VIEW = "FuncColorView";
    public static final String PREF_KEY_FUNC_COLOR_SHARE = "FuncColorShare";
    public static final String PREF_KEY_FUNC_COLOR_CONFIG = "FuncColorConfig";

    // 選択可能なカラー
    public static final int[] SELECTABLE_COLORS = new int[] {
        COLOR_WHITE1, COLOR_WHITE2,     // ホワイト1, 2
        COLOR_BLACK1, COLOR_BLACK2,     // ブラック1, 2
        COLOR_RED1, COLOR_RED2,         // レッド1, 2
        COLOR_ORANGE,                   // オレンジ
        COLOR_YELLOW,                   // イエロー
        COLOR_LIGHTGREEN,               // ライトグリーン
        COLOR_GREEN,                    // グリーン
        COLOR_SKYBLUE,                  // スカイブルー
        COLOR_BLUE,                     // ブルー
        COLOR_PURPLE1, COLOR_PURPLE2,   // パープル1, 2
        COLOR_DEEPPURPLE,               // ディープパープル
        COLOR_PINK,                     // ピンク
        };
    
    // 選択可能なカラー名
    public static final String[] SELECTABLE_COLOR_NAMES = new String[] {
        "ホワイト1", "ホワイト2",
        "ブラック1", "ブラック2",
        "レッド1", "レッド2",
        "オレンジ",
        "イエロー",
        "ライトグリーン",
        "グリーン",
        "スカイブルー",
        "ブルー",
        "パープル1", "パープル2",
        "ディープパープル",
        "ピンク",
        };

    public static final int DEFAULT_TAB_INDICATOR_COLOR = 0xff9acd32;

    // 無効なカラー
    public static final int SELECTABLE_COLOR_INVALID = 0x00000000;

    public static final int LABEL_COLOR_NONE = 0xff000000;
    
    public static final int LABEL_COLOR_DEFAULT = 0xff009944;
    
    public static final int ICON_DEFAULT_COLOR = 0xff909090;
    
    // 各カラー名の設定値
    public static final String PREF_KEY_LABEL_COLOR_NAME_PREFIX = "LabelColorName";
    
    
    //--------------------------------------------------
    // その他
    //--------------------------------------------------
    
    // アクティビティ動作種別
    public static final int ACTIVITY_TYPE_HOME = 0;
    
    // 設定画面のモード
    public static final int CONFIG_MODE_ALL = 1;
    
    // デフォルトのアイコンサイズ
    public static final int DEFAULT_ICON_SIZE_DIP = 32;

}
