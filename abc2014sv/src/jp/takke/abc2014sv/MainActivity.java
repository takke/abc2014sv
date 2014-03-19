package jp.takke.abc2014sv;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import jp.takke.abc2014sv.App.LecInfo;
import jp.takke.abc2014sv.App.Lecture;
import jp.takke.abc2014sv.App.LiveInfo;
import jp.takke.abc2014sv.App.LiveInfoItem;
import jp.takke.abc2014sv.App.Speaker;
import jp.takke.abc2014sv.PaneInfo.PaneType;
import jp.takke.abc2014sv.ui.AboutActivity;
import jp.takke.abc2014sv.ui.ConfigActivity;
import jp.takke.abc2014sv.ui.PageConfigActivity;
import jp.takke.abc2014sv.ui.fragments.ConferenceFragment;
import jp.takke.abc2014sv.ui.fragments.LiveFragment;
import jp.takke.abc2014sv.ui.fragments.MyFragment;
import jp.takke.abc2014sv.util.IconAlertDialogUtil;
import jp.takke.abc2014sv.util.MyLog;
import jp.takke.abc2014sv.util.TPUtil;
import net.simonvt.menudrawer.MenuDrawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import yanzm.products.quickaction.lib.QuickAction;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.EntypoIcon;

public class MainActivity extends FragmentActivity {

    protected final Handler mHandler = new Handler();

    // REQUEST CODE
    protected static final int REQUEST_SETTINGS = 3;
    protected static final int REQUEST_PAGE_CONFIG = 4;
    
    // ページ一覧
    private ArrayList<PaneInfo> mPaneInfoList = new ArrayList<PaneInfo>();
    
    // ページャ管理用アダプタ
    private SectionsPagerAdapter mSectionsPagerAdapter = null;

    // ページャ
    private ViewPager mViewPager = null;
    
    // ページャのドラッグ中フラグ
    protected boolean mIsViewPagerDragging = false;
    
    // サイドメニュー
    private MenuDrawer mDrawer = null;

    // アクティビティの動作種別
    private int mActivityType = C.ACTIVITY_TYPE_HOME;
    
    // フォアグラウンドフラグ
    public boolean mIsForeground = false;

    // タブ切り替え時刻(タブ連続切替中はタブロード遅延時間を長くするため)
    private final static int TAB_PAGE_CHANGED_TIMES_COUNT_MAX = 5;

    private LinkedList<Long> mTabPageChangedTimes = new LinkedList<Long>();
    
    // ShowcaseView表示状態
    private boolean mEnableShowcaseView = false;

    // Volley Queue
    private RequestQueue mQueue;
    
    private boolean mLoadingConfInfo = false;
    private boolean mLoadingLiveInfo = false;
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        
        MyLog.d("MainActivity.onCreate");
        
        final long elapsedUntilOnCreate = System.currentTimeMillis() - App.sStartedAt;
        App.sStartedAt = System.currentTimeMillis();
        MyLog.iWithElapsedTime("startupseq[{elapsed}ms][" + elapsedUntilOnCreate + "ms] MainActivity.onCreate() start [" + android.os.Process.myPid() + "]", App.sStartedAt);
        
        // アプリケーション設定のロード
        myInitApplicationConfigForAllView(this);
        
        // テーマ設定
        mySetTheme(this);
        
        // タイトルバーのプログレスバーを設定可能にする
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        super.onCreate(savedInstanceState);
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] called parent onCreate", App.sStartedAt);
        

        //-------------------------------------------------
        // サイドメニュー構築
        //-------------------------------------------------
        mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        mDrawer.setContentView(R.layout.activity_main);
        mDrawer.setMenuView(R.layout.sidebar);
        
        
        // デザインロード＆反映
        myLoadAndReflectTheme();
        
        // データ復元(実際の復元処理は setupDelayed1 内で実施する)
        if (savedInstanceState != null) {
            MyLog.d("MainActivity.onCreate: savedInstanceState[" + savedInstanceState.size() + "]");
        }
        
        //-------------------------------------------------
        // 遅延セットアップ
        //-------------------------------------------------
        mHandler.post(new Runnable() {
            
            @Override
            public void run() {
                setupDelayed1(savedInstanceState);
            }
        });
        
        MyLog.iWithElapsedTime("startupseq[{elapsed}ms] MainActivity.onCreate done", App.sStartedAt);
    }
    

    public static void myInitApplicationConfigForAllView(Activity context) {
        
        final long startTick = System.currentTimeMillis();
        
        MyLog.d("init app config start ------------------------------");
        
        // 設定値のロード
        TPConfig.load(context);
        MyLog.dWithElapsedTime("[{elapsed}ms] init app config: app-config loaded", startTick);
        
        // 文字サイズ反映
        FontSize.load(TPConfig.fontSizeList);
        MyLog.dWithElapsedTime("[{elapsed}ms] init app config: font size setup done", startTick);
        
    }


    /**
     * 現在の設定に応じてベーステーマを設定する
     */
    public static void mySetTheme(Context context) {
        
        if (ThemeColor.isLightTheme(TPConfig.theme)) {
            context.setTheme(R.style.MyAppTheme_Light);
        } else {
            context.setTheme(R.style.MyAppTheme_Black);
        }
    }
    
    
    /**
     * テーマのロードとデザイン反映
     */
    private void myLoadAndReflectTheme() {
        
        // デザインロード
        ThemeColor.load(this, TPConfig.theme);
    }

    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        final long start = System.currentTimeMillis();
        
        // その他のデータの保存
        if (mViewPager != null) {
            outState.putInt("view_pager_current_item", mViewPager.getCurrentItem());
        }
        
        MyLog.dWithElapsedTime("MainActivity.onSaveInstanceState: done. elapsed[{elapsed}ms]", start);
    }
    
    
    /**
     * 遅延セットアップ
     * 
     * @param savedInstanceState 
     */
    @SuppressLint("CommitPrefEdits")
    protected void setupDelayed1(Bundle savedInstanceState) {
        
        final long startTick = System.currentTimeMillis();
        MyLog.d("setupDelayed1: start ----------------------------------------");
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] MainActivity.setupDelayed1 start", App.sStartedAt);
        
        
        
        //--------------------------------------------------
        // データロード
        //--------------------------------------------------
//        {
//            final Intent intent = getIntent();
//            if (intent != null) {
//                mActivityType  = intent.getIntExtra("ACTIVITY_TYPE", C.ACTIVITY_TYPE_HOME);
//                
//                switch (mActivityType) {
//                case C.ACTIVITY_TYPE_HOME:
//                    break;
//                    
////              case C.ACTIVITY_TYPE_USERLIST:
////                  mTargetData = intent.getStringExtra("TARGET_DATA");     // ScreenName
////                  mTargetListId = intent.getLongExtra("LIST_ID", -1);
////                  mTargetListName = intent.getStringExtra("LIST_NAME");
////                  break;
//                }
//            }
//        }
        MyLog.dWithElapsedTime("setupDelayed1: ---------- data loaded [{elapsed}ms]", startTick);
        
        
        //--------------------------------------------------
        // お気に入りデータ取得
        //--------------------------------------------------
        loadStarMap();
        MyLog.dWithElapsedTime("setupDelayed1: ---------- star loaded [{elapsed}ms]", startTick);
        
        
        //--------------------------------------------------
        // ページ一覧初期化
        //--------------------------------------------------
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        switch (mActivityType) {
        case C.ACTIVITY_TYPE_HOME:
            {
                // ページ一覧のjson取得
//              final long userId = pref.getLong(C.PREF_KEY_TWITTER_USER_ID, 0);
                final long userId = 0;
                String paneInfoJson = pref.getString(C.PREF_KEY_HOME_PANEINFO_JSON_BASE + userId, null);
                
                if (paneInfoJson != null) {
                    PaneInfoFactory.loadFromJson(mPaneInfoList, paneInfoJson);
                    
                } else {
                    // 初期ページ一覧生成
                    PaneInfoFactory.getDefaultHome(mPaneInfoList);
                }
            }
            break;
            
//      case C.ACTIVITY_TYPE_USERTIMELINE:
//          {
//              final PaneInfo pi1 = new PaneInfo(PaneType.PROFILE);
//                  pi1.setParam("SCREEN_NAME", mTargetData);
//              mPaneInfoList.add(pi1);
//              
//              final PaneInfo pi2 = new PaneInfo(PaneType.MYTWEET);
//                  pi2.setParam("SCREEN_NAME", mTargetData);
//              mPaneInfoList.add(pi2);
//          }
//          break;
        }
        MyLog.dWithElapsedTime("setupDelayed1: ---------- page list initialized [{elapsed}ms][type=" + mActivityType + "]", startTick);
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] page list initialized", App.sStartedAt);

        
        //--------------------------------------------------
        // ページャ管理用アダプタ初期化
        //--------------------------------------------------
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        
        //--------------------------------------------------
        // ページャ初期化
        //--------------------------------------------------
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            
            private int mLastCurrentPage = -1;
            
            @Override
            public void onPageSelected(int position) {
                mLastCurrentPage = position;
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {
                
                
                final int position = mViewPager.getCurrentItem();
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    if (position != mLastCurrentPage) {
                        // ページ変更
                        MyLog.d("ViewPager.onPageScrollStateChanged(SCROLL_STATE_SETTLING)[" + position + "]");
                        doTabPageChangedEvent(position);
                        
                        // タブ切り替え時のインジケータカラー反映
                        setPagerTabStripColor(position);
                        
                        mLastCurrentPage = position;
                    }
                }
                
                
                // ドラッグ中フラグ保存
                switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    mIsViewPagerDragging = true;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                case ViewPager.SCROLL_STATE_IDLE:
                default:
                    mIsViewPagerDragging = false;
                    break;
                }
            }
        });
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] pager initialized", App.sStartedAt);
        
        
        //--------------------------------------------------
        // ページャの初期ページ選択
        //--------------------------------------------------
        final int lastViewPagerCurrentItem = savedInstanceState != null ? savedInstanceState.getInt("view_pager_current_item") : -1;
        if (lastViewPagerCurrentItem >= 0 && lastViewPagerCurrentItem < mPaneInfoList.size()) {
            // バックグラウンドになる前の初期ページを復元する
            // ※メモリ不足等でActivityが死んだルート：FMRで再現可能
            mViewPager.setCurrentItem(lastViewPagerCurrentItem);
            
            MyLog.d("MainActivity.setupDelayed1: ページャの初期ページ復元[" + lastViewPagerCurrentItem + "]");
            
        } else {
        
            switch (mActivityType) {
            case C.ACTIVITY_TYPE_HOME:
                {
                    // ホームタブに移動する
                    moveToHomePage();
                }
                break;
                
//          case C.ACTIVITY_TYPE_USERTIMELINE:
//              // Intent によって切り替える
//              {
//                  final Intent intent = getIntent();
//                  if (intent != null && intent.getBooleanExtra("SHOW_PROFILE", false)) {
//                      mViewPager.setCurrentItem(0);   // PROFILE
//                  } else {
//                      mViewPager.setCurrentItem(1);   // MYTWEET
//                  }
//              }
//              break;
            }
        }
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] pager init page selected", App.sStartedAt);
        
        
        //--------------------------------------------------
        // PagerTabStrip のカスタマイズ
        //--------------------------------------------------
        final PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
        strip.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize.listTitleSize+2);
        strip.setTextSpacing(50);       // ページタイトル同士の間隔
        strip.setNonPrimaryAlpha(0.3f);
//      strip.setDrawFullUnderline(true);
//      strip.setTabIndicatorColor(C.DEFAULT_TAB_INDICATOR_COLOR);
        // カラー反映
        final int currentItemPosition = mViewPager.getCurrentItem();
        setPagerTabStripColor(currentItemPosition);
        strip.setBackgroundColor(ThemeColor.tabColor);
        
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] pager customized", App.sStartedAt);
        
        
        //--------------------------------------------------
        // ツールバーボタンの初期化
        //--------------------------------------------------
        setupToolbar();
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] toolbar initialized", App.sStartedAt);

        
        //--------------------------------------------------
        // サイドメニューの初期化
        //--------------------------------------------------
        setupSidemenu();
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] sidemenu initialized", App.sStartedAt);
        
        
        //--------------------------------------------------
        // 初期データロード
        //--------------------------------------------------
        mQueue = Volley.newRequestQueue(this);
        
        doReload(false);
        
        
        MyLog.dWithElapsedTime("setupDelayed1: end ---------------------------------------- [{elapsed}ms]", startTick);
        MyLog.iWithElapsedTime("startupseq[{elapsed}ms] MainActivity.setupDelayed1 end", App.sStartedAt);
    }


    private void loadStarMap() {
        
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // json取得
        String starMapJsonText = pref.getString(C.PREF_KEY_STAR_MAP, null);
        
        if (starMapJsonText != null) {
            
            App.sStarMap.clear();
            try {
                final JSONObject json = new JSONObject(starMapJsonText);
                
                final JSONArray names = json.names();
                for (int i=0; i<names.length(); i++) {
                    
                    final String key = names.getString(i);
                    final int value = json.getInt(key);
                    App.sStarMap.put(key, value);
                }
            } catch (JSONException e) {
                MyLog.e(e);
            }
        }
    }


    /**
     * 全タブの再描画
     */
    public void resetAllTabs() {
        
        for (int i=0; i<mPaneInfoList.size(); i++) {
            final Fragment fragment = mSectionsPagerAdapter.getFragment(i);
            if (fragment instanceof MyFragment) {
                final MyFragment myFragment = (MyFragment) fragment;
                
                // 再描画
                myFragment.resetTabData();
            }
        }
    }


    protected LiveInfo parseLiveInfoXml(InputStream in) {
        
        final LiveInfo liveInfo = new LiveInfo();
        
        try {
            // XMLPullParserの使用準備
            final XmlPullParser parser = Xml.newPullParser();
            InputStreamReader isr = new InputStreamReader(in);
            parser.setInput(isr);

            // タグ名
            String tag = "";
            
            LiveInfoItem item = null;
            
            // XMLの解析
            for (int type = parser.getEventType(); type != XmlPullParser.END_DOCUMENT; type = parser.next()) {
                
                switch (type) {
                case XmlPullParser.START_TAG: // 開始タグ
                    {
                        tag = parser.getName();
//                      MyLog.d(" parseXml: start[" + tag + "]");
                        
                        // データ生成
                        if ("item".equals(tag)) {
                            item = new App.LiveInfoItem();
                            liveInfo.items.add(item);
                        }
                    }
                    
                    break;
                    
                case XmlPullParser.TEXT: // タグの内容
                    {
                        String value = parser.getText();
                        // 空白で取得したものは全て処理対象外とする
                        if (value.trim().length() != 0) {
                            
//                          MyLog.d(" parseXml: text[" + tag + "][" + value + "]");
                            
                            if (item != null) {
                                if (tag.equals("room_id")) {
                                    try {
                                        item.room_id = Integer.valueOf(value);
                                    } catch (Exception e) {
                                        MyLog.e(e);
                                    }
                                }
                                if (tag.equals("roomstatus_id")) {
                                    try {
                                        item.roomstatus_id = Integer.valueOf(value);
                                    } catch (Exception e) {
                                        MyLog.e(e);
                                    }
                                }
                                
                                if (tag.equals("room_place")) item.room_place = value;
                                if (tag.equals("room_name")) item.room_name = value;
                                if (tag.equals("roomstatus_text")) item.roomstatus_text = value;
                                if (tag.equals("update_time")) item.update_time = value;
                            }
                        }
                    }
                    break;
                    
                case XmlPullParser.END_TAG: // 終了タグ
                    {
                        String tagName = parser.getName();
//                      MyLog.d(" parseXml: end[" + tagName + "]");
                        
                        if ("item".equals(tagName)) {
                            item = null;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        
        return liveInfo;
    }


    protected LecInfo parseConferenceXml(InputStream in) {
        
        final LecInfo lecInfo = new LecInfo();
        
        try {
            // XMLPullParserの使用準備
            final XmlPullParser parser = Xml.newPullParser();
            InputStreamReader isr = new InputStreamReader(in);
            parser.setInput(isr);

            // タグ名
            String tag = "";
            
            Lecture lecture = null;
            Speaker speaker = null;
            
            // XMLの解析
            for (int type = parser.getEventType(); type != XmlPullParser.END_DOCUMENT; type = parser.next()) {
                
                switch (type) {
                case XmlPullParser.START_TAG: // 開始タグ
                    {
                        tag = parser.getName();
//                      MyLog.d(" parseXml: start[" + tag + "]");
                        
                        // 属性取得
                        if ("lecInfo".equals(tag)) {
                            lecInfo.updateDate = getAttribute(parser, "updateDate", null);
                        }
                        if (tag.equals("category")) {
                            try {
                                lecture.category_id = Integer.parseInt(getAttribute(parser, "id", ""));
                            } catch (Exception e) {
                                MyLog.e(e);
                            }
                        }
                        if (tag.equals("room")) {
                            try {
                                lecture.room_id = Integer.parseInt(getAttribute(parser, "id", ""));
                            } catch (Exception e) {
                                MyLog.e(e);
                            }
                        }

                        // データ生成
                        if ("lecture".equals(tag)) {
                            lecture = new App.Lecture();
                            lecInfo.lectures.add(lecture);
                        }
                        if (tag.equals("speaker")) {
                            speaker = new Speaker();
                            lecture.speakers.add(speaker);
                        }
                    }
                    
                    break;
                    
                case XmlPullParser.TEXT: // タグの内容
                    {
                        String value = parser.getText();
                        // 空白で取得したものは全て処理対象外とする
                        if (value.trim().length() != 0) {
                            
//                          MyLog.d(" parseXml: text[" + tag + "][" + value + "]");
                            
                            if (lecture != null) {
                                if (tag.equals("lec_id")) lecture.lec_id = value;
                                if (tag.equals("title")) lecture.title = value;
                                if (tag.equals("abstract")) lecture.abstract_ = value;
                                if (tag.equals("lec_order_num")) lecture.lec_order_num = value;
                                if (tag.equals("room_order_num")) lecture.room_order_num = value;
                                if (tag.equals("url")) lecture.url = value;
                                if (tag.equals("start_time")) lecture.start_time = value;
                                if (tag.equals("end_time")) lecture.end_time = value;
                                if (tag.equals("room")) {
                                    lecture.room = value;
                                }
                                if (tag.equals("category")) {
                                    lecture.category = value;
                                }
                                if (tag.equals("time_frame")) {
                                    try {
                                        lecture.time_frame = Integer.valueOf(value);
                                    } catch (Exception e) {
                                        MyLog.e(e);
                                    }
                                }
                            }
                            
                            if (speaker != null) {
                                if (tag.equals("speaker_id")) speaker.speaker_id = value;
                                if (tag.equals("name")) speaker.name = value;
                                if (tag.equals("profile")) speaker.profile = value;
                            }
                        }
                    }
                    break;
                    
                case XmlPullParser.END_TAG: // 終了タグ
                    {
                        String tagName = parser.getName();
//                      MyLog.d(" parseXml: end[" + tagName + "]");
                        
                        if ("lecture".equals(tagName)) {
                            lecture = null;
                        }
                        if ("speaker".equals(tagName)) {
                            speaker = null;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        
        return lecInfo;
    }


    private String getAttribute(XmlPullParser parser, String name, String fallback) {
        
        final int n = parser.getAttributeCount();
        for (int i=0; i<n; i++) {
            
            final String name1 = parser.getAttributeName(i);
            if (name.equals(name1)) {
                return parser.getAttributeValue(i);
            }
        }
        
        return fallback;
    }


    private void setPagerTabStripColor(final int position) {
        
        final PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);

        if (mActivityType == C.ACTIVITY_TYPE_HOME && position < mPaneInfoList.size()) {
            final int color = mPaneInfoList.get(position).getColor();
            strip.setTabIndicatorColor(color == C.SELECTABLE_COLOR_INVALID ? C.DEFAULT_TAB_INDICATOR_COLOR : color);
        } else {
            strip.setTabIndicatorColor(C.DEFAULT_TAB_INDICATOR_COLOR);
        }
    }



    /**
     * 遅延セットアップ
     */
    @SuppressLint("CommitPrefEdits")
    protected void setupDelayed2() {
        
        final long startTick = System.currentTimeMillis();
        MyLog.d("setupDelayed2: start ----------------------------------------");
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] MainActivity.setupDelayed2 start", App.sStartedAt);
        
        
        //-------------------------------------------------
        // 外部ストレージのログファイルを削除する
        //-------------------------------------------------
        // ※デバッグモードが有効な場合のみ実行される
        MyLog.deleteBigExternalLogFile();
        MyLog.dWithElapsedTime("setupDelayed2: ---------- deleteBigFiles [{elapsed}ms]", startTick);
        
        
        //-------------------------------------------------
        // PagerTabStrip のカスタマイズ
        //-------------------------------------------------
        final PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
        // 真ん中タップで一番上までスクロールする
        strip.setOnTouchListener(new View.OnTouchListener() {
            
            // Down時のX座標
            private float mLastDownX = 0.0f;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                
                
                final int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // X座標保存
                    mLastDownX = event.getX();
                } else if (action == MotionEvent.ACTION_UP) {
                    // Down時が真ん中タップで、Upもその周辺であれば「真ん中タップ」とみなし一番上までスクロールする
                    
                    final int width = v.getWidth();
                    final int centerThreshold = width/8;    // 真ん中判定幅
                    final float x = event.getX();
                    if (width/2-centerThreshold <= mLastDownX && mLastDownX <= width/2+centerThreshold &&
                        width/2-centerThreshold <= x && x <= width/2+centerThreshold) {
                        
                        // 一番上までスクロールする
                        final int index = mViewPager.getCurrentItem();
                        
                        MyLog.d(" onTouch: 真ん中タップ[" + x + "], index[" + index + "]");
                        
//                      final Fragment fragment = mSectionsPagerAdapter.getFragment(index);
//                      if (fragment instanceof TimelineFragment) {
//                          final TimelineFragment timelineFragment = (TimelineFragment) fragment;
//                          timelineFragment.scrollToTopOrReload();
//                      }
                    }
                }
                
                return false;
            }
        });
        MyLog.dWithElapsedTime("startupseq[{elapsed}ms] pager tap event set", App.sStartedAt);
        
        
        
        MyLog.dWithElapsedTime("setupDelayed2: end ---------------------------------------- [{elapsed}ms]", startTick);
        MyLog.iWithElapsedTime("startupseq[{elapsed}ms] MainActivity.setupDelayed2 end", App.sStartedAt);
    }
    




    /**
     * ツールバーの初期化
     */
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
                    
                    if (mEnableShowcaseView) {
                        // ShowcaseView 表示中なので反応させない
                        return;
                    }
                    
                    showMyOptionsMenu(v);
                }
            });
        }
        
        // button5:更新
        {
            final ImageButton button = (ImageButton) findViewById(R.id.button5);
            button.setImageDrawable(TPUtil.createIconicFontDrawable(this, EntypoIcon.CYCLE));
            button.setContentDescription(getString(R.string.menu_refresh));
            button.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
    
                    doReload(true);
                    
//                  // Fragment に通知する
//                  final int index = mViewPager.getCurrentItem();
//                  final Fragment fragment = mSectionsPagerAdapter.getFragment(index);
//                  if (fragment instanceof MyToolbarListener) {
//                      final MyToolbarListener myFragment = (MyToolbarListener) fragment;
//                      myFragment.onClickToolbarUpdateButton(v);
//                  }
                }
            });
//          button.setOnLongClickListener(new View.OnLongClickListener() {
//              
//              @Override
//              public boolean onLongClick(View v) {
//                  
//                  // Fragment に通知する
//                  final int index = mViewPager.getCurrentItem();
//                  final Fragment fragment = mSectionsPagerAdapter.getFragment(index);
//                  if (fragment instanceof MyToolbarListener) {
//                      final MyToolbarListener myFragment = (MyToolbarListener) fragment;
//                      return myFragment.onLongClickToolbarUpdateButton(v);
//                  }
//                  
//                  return false;
//              }
//          });
        }
        
        switch (mActivityType) {
        case C.ACTIVITY_TYPE_HOME:
            findViewById(R.id.button2).setVisibility(View.GONE);
            
            // button1:ライブ情報
            {
                final ImageButton button = (ImageButton) findViewById(R.id.button1);
                button.setVisibility(View.VISIBLE);
                button.setImageDrawable(TPUtil.createIconicFontDrawable(this, EntypoIcon.FLAG));
                button.setContentDescription("ライブ情報");
                button.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        
                        moveToLiveInfo();
                    }
                });
            }
            
            // button3:ホーム
            {
                final ImageButton button = (ImageButton) findViewById(R.id.button3);
                button.setVisibility(View.VISIBLE);
                button.setImageDrawable(TPUtil.createIconicFontDrawable(this, EntypoIcon.HOME));
                button.setContentDescription(getString(R.string.menu_home));
                button.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        
                        // ホームに戻る(既にホームならタブ一覧メニュー)
                        onClickHomeToolbarButton();
                    }
                });
                button.setOnLongClickListener(new View.OnLongClickListener() {
                    
                    @Override
                    public boolean onLongClick(View v) {
                        
                        // タブ一覧メニュー
                        showTabListMenu();
                        return true;
                    }
                });
            }
            
            break;
            
        default:
            findViewById(R.id.button2).setVisibility(View.GONE);
            findViewById(R.id.button3).setVisibility(View.GONE);
            break;
        }
        
    }

    
    protected void doReload(boolean clearCache) {
        
        if (clearCache) {
            mQueue.getCache().clear();
        }
        
        setProgressBarVisibility(true);
        setProgressBarIndeterminate(true);
        
        // カンファレンスデータ取得
        {
            mLoadingConfInfo = true;
            final String url = "http://www.android-group.jp/conference/abc2014s/api/conference/";
            final InputStreamRequest request = new InputStreamRequest(url, new Listener<InputStream>() {
                
                @Override
                public void onResponse(InputStream in) {
                    MyLog.d("InputStreamRequest.onResponse");
                    App.sConferenceData = parseConferenceXml(in);
                    mLoadingConfInfo = false;
                    
                    // 全カンファレンスタブ再生成
                    resetAllTabs();
                    
                    if (!mLoadingConfInfo && !mLoadingLiveInfo) {
                        setProgressBarVisibility(false);
                    }
                }
            }, new ErrorListener() {
    
                @Override
                public void onErrorResponse(VolleyError error) {
                    // error
                }
            });
            mQueue.add(request);
        }
        
        // ライブ情報取得
        {
            mLoadingLiveInfo = true;
            final String url = "http://www.android-group.jp/conference/abc2014s/api/live/";
            final InputStreamRequest request = new InputStreamRequest(url, new Listener<InputStream>() {
                
                @Override
                public void onResponse(InputStream in) {
                    MyLog.d("InputStreamRequest.onResponse");
                    App.sLiveInfo = parseLiveInfoXml(in);
                    mLoadingLiveInfo = false;
                    
                    // 全カンファレンスタブ再生成
                    resetAllTabs();
                    
                    if (!mLoadingConfInfo && !mLoadingLiveInfo) {
                        setProgressBarVisibility(false);
                    }
                }
            }, new ErrorListener() {
    
                @Override
                public void onErrorResponse(VolleyError error) {
                    // error
                }
            });
            mQueue.add(request);
        }
        
    }


    /**
     * ツールバー表示状態の更新
     */
//  public void myUpdateToolbars() {
//      
//      // 現在のFragmentの通信状態に応じて更新する
//      
//      MyLog.d("myUpdateToolbars");
//      
//      final ImageButton button = (ImageButton) findViewById(R.id.button5);
//      
//      final int index = mViewPager.getCurrentItem();
//      final Fragment fragment = mSectionsPagerAdapter.getFragment(index);
//      if (fragment instanceof MyFragment) {
//          
//          final MyFragment myFragment = (MyFragment) fragment;
//          
//          // button5:更新
//          if (myFragment.isLoading()) {
//              button.setImageDrawable(TPUtil.createIconicFontDrawable(this, EntypoIcon.PAUS));
//          } else {
//              button.setImageDrawable(TPUtil.createIconicFontDrawable(this, EntypoIcon.CYCLE));
//          }
//          
//      } else {
//          // button5:更新
//          button.setImageDrawable(TPUtil.createIconicFontDrawable(this, EntypoIcon.CYCLE));
//      }
//  }
    
    
    /**
     * 独自の OptionMenu を表示する
     * 
     * @param v
     */
    protected void showMyOptionsMenu(View v) {
        
        final QuickAction qa = new QuickAction(v);
        
        // Fragment取得
        final int index = mViewPager.getCurrentItem();
        final Fragment fragment = mSectionsPagerAdapter.getFragment(index);
        final MyFragment myFragment = (fragment instanceof MyFragment) ? (MyFragment) fragment : null;
        
        //--------------------------------------------------
        // Menu 追加
        //--------------------------------------------------
        
        // ブラウザで開く
        TPUtil.addQuickActionItem(qa, getApplicationContext(), "ブラウザで開く",
                TPUtil.createIconicFontDrawable(MainActivity.this, EntypoIcon.BROWSER, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
                new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View vClicked) {
                        
                        // QuickAction を閉じる
                        qa.dismiss();
                        
                        // タブごとに開くページを切り替える
                        openExternalBrowser(myFragment.getUrl());
                    }
                });
        
        
        // ホーム用
        if (mActivityType == C.ACTIVITY_TYPE_HOME) {

            
            // タブ一覧
            TPUtil.addQuickActionItem(qa, getApplicationContext(), getString(R.string.menu_show_tab_list),
                    TPUtil.createIconicFontDrawable(MainActivity.this, EntypoIcon.LIST, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
                    new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View vClicked) {
                            
                            // QuickAction を閉じる
                            qa.dismiss();
                            
                            showTabListMenu();
                        }
                    });
            
            // タブのカスタマイズ
//          TPUtil.addQuickActionItem(qa, getApplicationContext(), getString(R.string.menu_page_config),
//                  TPUtil.createIconicFontDrawable(MainActivity.this, EntypoIcon.NUMBERED_LIST, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
//                  new View.OnClickListener() {
//                      
//                      @Override
//                      public void onClick(View vClicked) {
//                          
//                          // QuickAction を閉じる
//                          qa.dismiss();
//                          
//                          // タブカスタマイズ
//                          final Intent intent = new Intent(MainActivity.this, PageConfigActivity.class);
//                          startActivityForResult(intent, REQUEST_PAGE_CONFIG);
//                      }
//                  });

            // 設定
            TPUtil.addQuickActionItem(qa, getApplicationContext(), getString(R.string.menu_config),
                    TPUtil.createIconicFontDrawable(MainActivity.this, EntypoIcon.TOOLS, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
                    new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View vClicked) {
                            
                            // QuickAction を閉じる
                            qa.dismiss();
                            
                            // 設定画面を開く
                            final Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                            startActivityForResult(intent, REQUEST_SETTINGS);
                        }
                    });
        }
        
        
        // アプリについて
        TPUtil.addQuickActionItem(qa, getApplicationContext(), getString(R.string.menu_about),
                TPUtil.createIconicFontDrawable(MainActivity.this, EntypoIcon.INFO, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
                new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View vClicked) {
                        
                        // QuickAction を閉じる
                        qa.dismiss();
                        
                        // このアプリについて
                        final Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent);
                    }
                });
        
        // キャッシュ情報
        if (App.sConferenceData != null) {
            TPUtil.addQuickActionItem(qa, getApplicationContext(), "更新:" + TPUtil.formatShortDateTime(App.sConferenceData.updateDate),
                    TPUtil.createIconicFontDrawable(MainActivity.this, EntypoIcon.MEGAPHONE, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorConfig),
                    new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View vClicked) {
                            
                            // QuickAction を閉じる
                            qa.dismiss();
                        }
                    });
        }
        
        // デバッグ
        if (TPConfig.debugMode) {
            
            // 前回の取得時刻からの経過時間表示
            
//          long lastLoadedTime = 0;
//          if (myFragment != null) {
//              lastLoadedTime = myFragment.getLastLoadedTime();
//          }
            
            String title;
//          if (lastLoadedTime > 0) {
//              // 経過時間表示
//              final long elapsedSec = (System.currentTimeMillis() - lastLoadedTime) / 1000;
//              title = getString(R.string.menu_debug) + " [" + elapsedSec + "sec]";
//          } else {
                title = getString(R.string.menu_debug);
//          }
            
            TPUtil.addQuickActionItem(qa, getApplicationContext(), title,
                    TPUtil.createIconicFontDrawable(MainActivity.this, EntypoIcon.FLAG, C.DEFAULT_ICON_SIZE_DIP, TPConfig.funcColorTwiccaDebug),
                    new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View vClicked) {
                            
                            // QuickAction を閉じる
                            qa.dismiss();
                            
                            doDebugMenu();
                        }
                    });
        }

        // QuickAction 表示
        qa.setAnimStyle(QuickAction.ANIM_AUTO);
        qa.setLayoutStyle(QuickAction.STYLE_LIST);
        
        // Menu キーで閉じるようにしておく
        qa.setOnKeyListener(new View.OnKeyListener() {
            
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                if (keyCode == KeyEvent.KEYCODE_MENU && 
                        event.getRepeatCount() == 0 && 
                        event.getAction() == KeyEvent.ACTION_UP) {
                    
                    qa.dismiss();
                    
                    return true;
                }     
                
                return false;
            }
        });
        
        qa.show();
    }


    protected void doDebugMenu() {
        
        // キャッシュ削除
//      App.mStatusCache.evictAll();
        
        // ページ追加テスト
//      final PaneInfo pi = new PaneInfo(PaneType.LIST);
//      pi.setParam("LIST_NAME", "test");
//      pi.setParam("LIST_ID", "92367934");
//      mPaneInfoList.add(pi);
//      mSectionsPagerAdapter.notifyDataSetChanged();
        
        // クラッシュテスト
//      mPaneInfoList = null;
        
        // 通知テスト
//      showNewReplyNotification(null);
        
        // デバッグ情報表示
        final AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        ab.setTitle("Debug");
        
        final ArrayList<String> a = new ArrayList<String>();
        
        
        // カレントタブの行数、キャッシュ容量
//      {
//          String line = "";
//          final int index = mViewPager.getCurrentItem();
//          final Fragment fragment = mSectionsPagerAdapter.getFragment(index);
//          if (fragment instanceof TimelineFragment) {
//              final TimelineFragment timelineFragment = (TimelineFragment) fragment;
//              
//              // カレントタブの行数
//              line += "ListView count: " + timelineFragment.getLineCount() + "\n";
//          
//              // カレントタブの行数
//              line += "StatusList count: " + timelineFragment.mStatusList.size() + "\n";
//          
//          }
//          
//          // 画像キャッシュ
//          {
//              final long total = ImageCache.getTotalBytes()/1024;
//              final long limit = ImageCache.getLimitBytes()/1024;
//              line += "ImageCache: " + total + "/" + limit + "KB (" + (total*100/limit) + "%)";
//          }
//          
//          a.add(line);
//      }
        
        // メモリ使用量
        final Runtime runtime = Runtime.getRuntime();
        {
            // Dalvik Heap
            final int total = (int)(runtime.totalMemory()/1024);
            final int free = (int)(runtime.freeMemory()/1024);
            final int used = total - free;
            final int max = (int)(runtime.maxMemory()/1024);
            a.add(  "Dalvik Heap: " +
                    "used[" + (int)(used) + "KB] " +
                    "total[" + (int)(total) + "KB] " +
                    "max[" + (int)(max) + "KB] (" + (int)(double)(100.0*used/max) + "%)");
        }
        {
            // Native Heap
            final int allocated = (int)(Debug.getNativeHeapAllocatedSize()/1024);
            final int heap = (int)(Debug.getNativeHeapSize()/1024);
            final int free = (int)(Debug.getNativeHeapFreeSize()/1024);
            a.add(  "Native Heap: " +
                    "alloc[" + (int)(allocated) + "KB] " +
                    "heap[" + (int)(heap) + "KB] " +
                    "free[" + (int)(free) + "KB]");
        }
        
        
        final CharSequence[] items = a.toArray(new CharSequence[]{});
        ab.setItems(items, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        });
        ab.setPositiveButton(R.string.common_ok, null);
        ab.create().show();
        
        // メモリ使用状況dump
        TPUtil.dumpMemoryUsageLog("DebugInfo");
    }


    /**
     * ホームボタン押下イベント
     */
    protected void onClickHomeToolbarButton() {
        
//      final int currentItem = mViewPager.getCurrentItem();
//        for (int i=0; i<mPaneInfoList.size(); i++) {
//          if (mPaneInfoList.get(i).type == PaneType.HOME) {
//              
//              if (currentItem==i) {
//                  // 既にホームタブなのでタブ一覧メニュー表示
//                  showTabListMenu();
//                  
//              } else {
//                  // ホームタブに移動
//                  final int ii = i;
//                  mHandler.postDelayed(new Runnable() {
//                      
//                      @Override
//                      public void run() {
//                          // ページ移動
//                          mViewPager.setCurrentItem(ii, true);
//                      }
//                  }, 100);
//              }
//              break;
//          }
//        }
        moveToHomePage();
    }
    
    
    /**
     * ホームタブに移動する
     */
    private void moveToHomePage() {
        
        for (int i=0; i<mPaneInfoList.size(); i++) {
            if (mPaneInfoList.get(i).type == PaneType.HOME) {
                try {
                    mViewPager.setCurrentItem(i);
                } catch (NullPointerException e) {
                    MyLog.e(e);
                }
                break;
            }
        }
    }


    /**
     * カンファレンスタブに移動する
     */
    public void moveToConferencePage(int categoryId) {
        
        for (int i=0; i<mPaneInfoList.size(); i++) {
            final PaneInfo v = mPaneInfoList.get(i);
            if (v.type == PaneType.CONFERENCE && v.getCategoryId() == categoryId) {
                try {
                    mViewPager.setCurrentItem(i);
                } catch (NullPointerException e) {
                    MyLog.e(e);
                }
                break;
            }
        }
    }


    /**
     * ライブ情報タブに移動する
     */
    private void moveToLiveInfo() {
        
        for (int i=0; i<mPaneInfoList.size(); i++) {
            if (mPaneInfoList.get(i).type == PaneType.LIVE_INFO) {
                try {
                    mViewPager.setCurrentItem(i);
                } catch (NullPointerException e) {
                    MyLog.e(e);
                }
                break;
            }
        }
    }


    @Override
    public void onDestroy() {
        
        MyLog.d("MainActivity.onDestroy");
        
        super.onDestroy();
    }
    
    
    @Override
    protected void onStop() {
        mIsForeground = false;
        super.onStop();
        
        MyLog.d("MainActivity.onStop");
    }
    
    
    @Override
    protected void onResume() {
        mIsForeground = true;
        super.onResume();
        
        final int paneInfoSize = (mPaneInfoList == null ? -1 : mPaneInfoList.size());
        final int viewPagerCount = (mViewPager == null ? -1 : mViewPager.getChildCount());
        final int adapterCount = (mSectionsPagerAdapter == null ? -1 : mSectionsPagerAdapter.getCount());
        MyLog.dWithElapsedTime("MainActivity.onResume[{elapsed}ms][" + paneInfoSize + "tabs]"
                + "[" + viewPagerCount + "pagers][adapter=" + adapterCount + "]", App.sStartedAt);
    }
    
    
    @Override
    protected void onPause() {
        mIsForeground = false;
        super.onPause();
        
        final int paneInfoSize = (mPaneInfoList == null ? -1 : mPaneInfoList.size());
        final int viewPagerCount = (mViewPager == null ? -1 : mViewPager.getChildCount());
        final int adapterCount = (mSectionsPagerAdapter == null ? -1 : mSectionsPagerAdapter.getCount());
        MyLog.dWithElapsedTime("MainActivity.onPause[{elapsed}ms][" + paneInfoSize + "tabs]"
                + "[" + viewPagerCount + "pagers][adapter=" + adapterCount + "]", App.sStartedAt);
    }
    
    
    // 初期フォーカス取得済みフラグ
    private boolean bFirstWindowFocused = false;
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        MyLog.dWithElapsedTime("MainActivity.onWindowFocusChanged[{elapsed}ms] (hasFocus=" + (hasFocus ? "true" : "false") + ")", App.sStartedAt);
        
        super.onWindowFocusChanged(hasFocus);
        
        // その他の初期化処理
        if (!bFirstWindowFocused) {
            bFirstWindowFocused = true;
            
            // 遅延ロード2開始
            mHandler.post(new Runnable() {
                
                @Override
                public void run() {
                    setupDelayed2();
                }
            });
            
            MyLog.iWithElapsedTime("startupseq[{elapsed}ms] onWindowFocusChanged done", App.sStartedAt);
        }
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // 画面回転など。
        
        // サイドメニューを閉じる
        mDrawer.closeMenu();
    }


    /**
     * PaneInfoリストの保存
     * 
     * @param paneInfoList
     */
    @SuppressLint("CommitPrefEdits")
    public void savePaneInfoList(ArrayList<PaneInfo> paneInfoList) {
        
        final String jsonText = PaneInfoFactory.makeJsonText(paneInfoList);
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = pref.edit();
        final long userId = 0;//pref.getLong(C.PREF_KEY_TWITTER_USER_ID, 0);
        editor.putString(C.PREF_KEY_HOME_PANEINFO_JSON_BASE + userId, jsonText);
        TPUtil.doSharedPreferencesEditorApplyOrCommit(editor);
        
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        
        // ページが追加されたかもしれないので追加する(このページがホームなら実行する)
        if (mActivityType == C.ACTIVITY_TYPE_HOME) {
            refreshPagesForAdd();
        }
        
        MyLog.d("MainActivity.onActivityResult: requestCode[" + requestCode + "], resultCode[" + resultCode  + "]");
        
        switch (requestCode) {
            
            
        case REQUEST_SETTINGS:
            // Config画面からの復帰
            {
                // 設定変更を反映する
                
                // テーマ変更の可能性があるので再起動する
                final Intent intent = new Intent();
                intent.setClass(this, this.getClass());
                this.startActivity(intent);
                finish();
            }
            break;
            
        case REQUEST_PAGE_CONFIG:
            if (resultCode == RESULT_OK) {
                
                // タブカスタマイズの可能性があるので再起動する
                final Intent intent = new Intent();
                intent.setClass(this, this.getClass());
                this.startActivity(intent);
                finish();
            }
            break;
            
        }
    }


    /**
     * URL を外部ブラウザで開く
     * 
     * @param url
     */
    public void openExternalBrowser(final String url) {
        
        try {
            MyLog.d("openExternalBrowser[" + url + "]");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception ex) {
            MyLog.e(ex.getMessage(), ex);
        }
    }

    
    // BackキーのDown中フラグ
    private boolean mBackKeyDown = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            
            final int repeat = event.getRepeatCount();
            MyLog.d("onKeyDown: BACK, BackKeyDown[" + mBackKeyDown + "], repeat[" + repeat + "]");
            if (repeat == 0) {
            
                event.startTracking();
                mBackKeyDown = true;
            }
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            
            MyLog.d("onKeyUp: BACK, BackKeyDown[" + mBackKeyDown + "]");
            
            if (!mBackKeyDown) {
                // 長押しで処理済みなので無視する
                return true;
            } else {
                // 戻る
                mBackKeyDown = false;
                
                if (mViewPager == null || mPaneInfoList == null) {
                    // なんらかの原因で、
                    // インスタンスが消えている状態で onKeyUp の処理が始まった場合このルート。
                    finish();
                    return true;
                }
                
                // サイドメニューを開いていたら閉じる
                final int drawerState = mDrawer.getDrawerState();
                if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
                    mDrawer.closeMenu();
                    return true;
                }
                
                // 性能試験用にここでリセットする
                App.sStartedAt = System.currentTimeMillis();
                
                // 既にホームなら終了する
                if (TPConfig.useBackToTimeline && mActivityType == C.ACTIVITY_TYPE_HOME) {
                    
                    final int index = mViewPager.getCurrentItem();
                    if (index < mPaneInfoList.size() && mPaneInfoList.get(index).type == PaneType.HOME) {
                        // 終了
                        finish();
                    } else {
                        // ホームタブに移動する
                        moveToHomePage();
                    }
                } else {
                    // 終了
                    finish();
                }
                    
                return true;
            }
        }
        
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // メニューボタン
            
            showMyOptionsMenu(findViewById(R.id.menu_button));
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Backキー長押し
            MyLog.d("onKeyLongPress: BACK, BackKeyDown[" + mBackKeyDown + "]");
            
            // タブ一覧メニューを表示する
            showTabListMenu();
            
            // Backキーの処理済みとする
            mBackKeyDown = false;
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
    

    /**
     * タブ一覧メニュー表示
     */
    private void showTabListMenu() {
        
        mDrawer.toggleMenu();
    }
    
    
    /**
     * サイドメニューの表示更新(未読件数など)
     */
    private void updateSidemenu() {
        
        final LinearLayout linearLayout = (LinearLayout) mDrawer.findViewById(R.id.linearLayout);
        final int n = linearLayout.getChildCount();
        for (int i=0; i<n; i++) {
            final View v = linearLayout.getChildAt(i);
            final Object tag = v.getTag();
            if (tag instanceof Integer) {
                final int index = (Integer) tag;
                
                // TextView 要素生成
                final TextView item = (TextView) v;
                
                if (0 <= index && index < mPaneInfoList.size()) {
                    final PaneInfo pi = mPaneInfoList.get(index);
                    
                    
                    // 未読件数取得
                    final Fragment fragment = mSectionsPagerAdapter.getFragment(index);
                    final String title = getSideMenuItemTitle(fragment, pi);
                    
                    item.setText(title);
                }
            }
        }
    }


    /**
     * サイドメニューの初期化
     */
    private void setupSidemenu() {

        //--------------------------------------------------
        // オープン時にメニューの項目値を更新する
        //--------------------------------------------------
        mDrawer.setOnDrawerStateChangeListener(new MenuDrawer.OnDrawerStateChangeListener() {
            
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                
                MyLog.d("MenuDrawer.onDrawerStateChange[" + stateToText(oldState) + "] => [" + stateToText(newState) + "]");
                
                if (oldState == MenuDrawer.STATE_CLOSED) {
                    // 開き始めたと想定して項目値更新
                    updateSidemenu();
                }
            }
            
            private String stateToText(int state) {
                switch (state) {
                case MenuDrawer.STATE_CLOSED:   return "CLOSED";    // クローズ
                case MenuDrawer.STATE_CLOSING:  return "CLOSING";   // 自動クローズ中(Animation)
                case MenuDrawer.STATE_DRAGGING: return "DRAGGING";  // ユーザードラッグ
                case MenuDrawer.STATE_OPEN:     return "OPEN";      // オープン
                case MenuDrawer.STATE_OPENING:  return "OPENING";   // 自動オープン中(Animation)
                default:
                    return "";
                }
            }
            
        });
        
        //--------------------------------------------------
        // TextView 要素生成
        //--------------------------------------------------
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                // サイドメニューを閉じる
                closeSideMenu();
                
                final Object tag = v.getTag();
                if (tag instanceof Integer) {
                    final Integer index = (Integer) tag;
                    
                    // タブ切り替え
                    // ※なぜかユーザタイムラインからプロフィールに移動するとタブが消えるので
                    //   index=0だけアニメーションさせる
                    mViewPager.setCurrentItem(index, index==0 ? true : false);
                    
                    // タブ切り替え時のインジケータカラー反映
                    setPagerTabStripColor(index);
                    
                    mHandler.postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            // 検索タブならリロード開始
                        }
                    }, 100);
                }
            }
        };
        final View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            
                
            @Override
            public boolean onLongClick(View v) {
                
                final Object tag = v.getTag();
                if (tag instanceof Integer) {
                    final Integer index = (Integer) tag;
                    
                    // メニュー表示は HOME のみ
                    if (mActivityType == C.ACTIVITY_TYPE_HOME) {
                        // メニュー表示
                        showSideMenuLongClickMenu(index);
                    }
                    
                    return true;
                }
                
                return false;
            }
        };
        
        
        // Menu 追加
        final ArrayList<TextView> items = new ArrayList<TextView>();
        {
            int i=0;
            for (final PaneInfo pi : mPaneInfoList) {
                
                final String title = getSideMenuItemTitle(null, pi);
                
                // TextView 要素生成
                final TextView item = new TextView(this, null, R.style.behindMenuItemLabel);
                item.setText(title);
                item.setTag(Integer.valueOf(i));
                item.setOnClickListener(onClickListener);
                item.setOnLongClickListener(onLongClickListener);
                
                final IconicFontDrawable d = TPUtil.createIconicFontDrawable(MainActivity.this, pi.getIconId(), 26);
                
                // カラー反映
                final int color = pi.getColor();
                if (color != C.SELECTABLE_COLOR_INVALID) {
                    d.setIconColor(color);
                }
                item.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                
                setSideMenuTextViewItemProps(item, color);
                
                items.add(item);
                
                i++;
            }
        }
        
        //--------------------------------------------------
        // 既に存在していれば削除する
        //--------------------------------------------------
        final LinearLayout linearLayout = (LinearLayout) mDrawer.findViewById(R.id.linearLayout);
        final int n = linearLayout.getChildCount();
        final ArrayList<View> removeItems = new ArrayList<View>();
        for (int i=0; i<n; i++) {
            final View v = linearLayout.getChildAt(i);
            final Object tag = v.getTag();
            if (tag instanceof Integer) {
                removeItems.add(v);
            }
        }
        if (removeItems.size() > 0) {
            for (int i=0; i<removeItems.size(); i++) {
                linearLayout.removeView(removeItems.get(i));
            }
        }
        
        //--------------------------------------------------
        // サイドメニューに追加
        //--------------------------------------------------
        final View baseItem = (View) mDrawer.findViewById(R.id.listDivider2);
        
        for (int i=0; i<n; i++) {
            final View v = linearLayout.getChildAt(i);
            if (v == baseItem) {
                int at = i+1;
                for (final TextView item : items) {
                    linearLayout.addView(item, at);
                    ++at;
                }
                break;
            }
        }
        
        //--------------------------------------------------
        // マージン設定(LinearLayout に登録後に設定可能)
        //--------------------------------------------------
        for (final TextView item : items) {
            setSideMenuTextViewItemMargin(item);
        }
        
        
        //--------------------------------------------------
        // タブのカスタマイズ
        //--------------------------------------------------
        {
            final Button button = (Button) findViewById(R.id.page_config_button);
            button.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // タブカスタマイズ
                    final Intent intent = new Intent(MainActivity.this, PageConfigActivity.class);
                    startActivityForResult(intent, REQUEST_PAGE_CONFIG);
                }
            });
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                // ICS以降ならボタンの文字は白にする
                button.setTextColor(Color.WHITE);
            }
        }
    }


    /**
     * サイドメニューの長押しメニュー表示
     * 
     * @param index
     */
    protected void showSideMenuLongClickMenu(int index) {
        
        if (index < 0 || index >= mPaneInfoList.size()) {
            return;
        }
        
        final PaneInfo pi = mPaneInfoList.get(index);
        final String title = pi.getDefaultPageTitle(this);
        
        final AlertDialog.Builder ab = new AlertDialog.Builder(this);
        
        ab.setTitle(getString(R.string.change_color) + " [" + title + "]");
        
        final ArrayList<IconAlertDialogUtil.IconItem> items = new ArrayList<IconAlertDialogUtil.IconItem>();
        
        // カラー変更
        for (int i=0; i<C.SELECTABLE_COLORS.length; i++) {
            
            items.add(TPUtil.createIconItem(this, C.SELECTABLE_COLOR_NAMES[i], pi.getIconId(), C.SELECTABLE_COLORS[i]));
        }
        
        // 末尾は「解除」
        items.add(TPUtil.createIconItem(this, R.string.default_color, pi.getIconId()));
        
        final ListAdapter adapter = IconAlertDialogUtil.createAdapter(this, items);
        ab.setAdapter(adapter, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                if (which < 0 || which >= C.SELECTABLE_COLORS.length+1) {
                    return;
                }
                
                if (which == C.SELECTABLE_COLORS.length) {
                    // 末尾は「解除」
                    pi.deleteParam("color");
                } else {
                    // カラー設定保存
                    final int color = C.SELECTABLE_COLORS[which];
                    pi.setParam("color", color+"");
                }
                
                // 保存(※タブの誤上書きを防ぐため確認する)
                if (mActivityType == C.ACTIVITY_TYPE_HOME) {
                    savePaneInfoList(mPaneInfoList);
                }
                
                // 反映
                setupSidemenu();
            }
        });
        ab.create().show();
    }


    protected void setSideMenuTextViewItemProps(final TextView item, int color) {
        
        // style がコンストラクタで全部反映されないので個別に設定する
        item.setTextAppearance(this, R.style.behindMenuItemLabel);
        
        if (color != C.SELECTABLE_COLOR_INVALID) {
            item.setTextColor(color);
        }
        
        final int px_4dp = TPUtil.dipToPixel(this, 4);
        item.setPadding(0, px_4dp, 0, px_4dp);
        item.setCompoundDrawablePadding(px_4dp*2);
        item.setBackgroundResource(R.color.sidebar_text);
    }


    protected void setSideMenuTextViewItemMargin(TextView item) {
        
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) item.getLayoutParams();
        lp.leftMargin = TPUtil.dipToPixel(this, 4);
        lp.bottomMargin = lp.topMargin = TPUtil.dipToPixel(this, 1);
        item.setLayoutParams(lp);
    }
    
    
    /**
     * サイドメニューを閉じる
     */
    private void closeSideMenu() {
        
        mDrawer.closeMenu(true);
//      mHandler.postDelayed(new Runnable() {
//          
//          @Override
//          public void run() {
//              // アニメーションなしで閉じる
//              mDrawer.closeMenu(false);
//          }
//      }, 500);
    }


    /**
     * ページャ管理用アダプタ
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final SparseArray<String> mFragmentTags;
        private final FragmentManager mFragmentManager;
        private Drawable mDrawable;
        
        
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            
            mFragmentManager = fm;
            mFragmentTags = new SparseArray<String>();
        }
        
        
        /**
         * ページ(Fragment)取得
         * 
         * ※必要になった時点で(隣のページが表示された時点で)呼び出される
         */
        @Override
        public Fragment getItem(int position) {
            
            MyLog.d("SectionsPagerAdapter.getItem[" + position + "]");

            if (position < 0 || position >= mPaneInfoList.size()) {
                return null;
            }
            
            final PaneInfo paneInfo = mPaneInfoList.get(position);
            switch (paneInfo.type) {
            case LIVE_INFO:
                {
                    final LiveFragment fragment = new LiveFragment();
                    fragment.setPaneInfo(position, paneInfo);
                    return fragment;
                }
                
            case HOME:
                {
                    final ConferenceFragment fragment = new ConferenceFragment();
                    fragment.setPaneInfo(position, paneInfo);
                    return fragment;
                }
                
            case CONFERENCE:
                {
                    final ConferenceFragment fragment = new ConferenceFragment();
                    fragment.setPaneInfo(position, paneInfo);
                    return fragment;
                }
                
            default:
                {
                    // ダミー
                    final Fragment fragment = new DummySectionFragment();
                    
                    // パラメータ設定
                    final Bundle args = new Bundle();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position);
                    fragment.setArguments(args);
                    
                    return fragment;
                }
            }
        }
        

        @Override
        public int getCount() {
            
//          MyLog.d("SectionsPagerAdapter.getCount[" + mPaneInfoList.size() + "]");
            
            return mPaneInfoList.size();
        }

        
        /**
         * 該当ページのタイトル
         */
        @Override
        public CharSequence getPageTitle(int position) {
            
            if (position < 0 || position >= mPaneInfoList.size()) {
                return null;
            }
            
            final PaneInfo pi = mPaneInfoList.get(position);
            
            if (!TPConfig.showTabIcon) {
                return pi.getDefaultPageTitle(getApplicationContext());
            } else {
                
                // space added before text for convenience
                final SpannableStringBuilder sb = new SpannableStringBuilder(" " + pi.getDefaultPageTitle(getApplicationContext()) + "  ");
    
                // アイコン設定
                mDrawable = TPUtil.setViewPagerIcon(MainActivity.this, mDrawable, sb, pi.getIconId());
    
                return sb;
            }
        }


        /**
         * 各インデックスのタグ名を保存するためオーバーライドする
         * 
         * @see http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                // record the fragment tag here.
                final Fragment f = (Fragment) obj;
                String tag = f.getTag();
                mFragmentTags.put(position, tag);
            }
            return obj;
        }
        

        /**
         * 指定されたインデックスのFragmentを取得する(未生成であればnullを返す)
         * 
         * @param position インデックス
         * @return Fragmentまたはnull
         */
        public Fragment getFragment(int position) {
            final String tag = mFragmentTags.get(position);
            if (tag == null)
                return null;
            
            return mFragmentManager.findFragmentByTag(tag);
        }
    }


    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        
        public DummySectionFragment() {
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            
            // パラメータ取得＆設定
            textView.setText("開発中です…");
            
            return textView;
        }
    }


    /**
     * ページが追加されたかもしれないので追加する
     */
    public void refreshPagesForAdd() {
        
        MyLog.d("refreshPagesForAdd");
        
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final long userId = 0;//pref.getLong(C.PREF_KEY_TWITTER_USER_ID, 0);
        final String paneInfoJson = pref.getString(C.PREF_KEY_HOME_PANEINFO_JSON_BASE + userId, null);
        if (paneInfoJson == null) {
            return;
        }
        
        // 現在値ロード
        final ArrayList<PaneInfo> paneInfoList = new ArrayList<PaneInfo>();
        PaneInfoFactory.loadFromJson(paneInfoList, paneInfoJson);
        
        // 足りないタブがあれば追加する
        final int n = paneInfoList.size();
        if (n != mPaneInfoList.size()) {
            
            for (int i=0; i<n; i++) {
                final PaneInfo pi = paneInfoList.get(i);
                final int m = mPaneInfoList.size();
                int j=0;
                for (; j<m; j++) {
                    if (mPaneInfoList.get(j).equals(pi)) {
                        break;
                    }
                }
                if (j==m) {
                    // not found
                    mPaneInfoList.add(pi);
                }
            }
            
            // 追加されたかもしれないので通知しておく
            if (mSectionsPagerAdapter == null) {
                return;
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
            
            // サイドメニュー再生成
            setupSidemenu();
        }
    }
    
    
    /**
     * 現在表示している Fragment を取得する (Visible判定用)
     * 
     * @return
     */
    public int getCurrentFragmentIndex() {
        
        if (mViewPager == null) {
            return -1;
        }
        return mViewPager.getCurrentItem();
    }
    
    
    /**
     * ViewPager ドラッグ中判定
     * 
     * @return ドラッグ中なら true それ以外(IDLE/SETTLING)は false
     */
    public boolean isViewPagerDragging() {
        
        return mIsViewPagerDragging;
    }


    public int getPaneInfoSize() {
        return mPaneInfoList.size();
    }
    
    public PaneInfo getPaneInfo(int i) {
        return mPaneInfoList.get(i);
    }
    
    public void setViewPagerCurrentItem(int i) {
        mViewPager.setCurrentItem(i, true);
    }
    
    
    /**
     * ページ表示完了通知
     */
    public void onTwitPanePageLoaded() {
        
        final int position = mViewPager.getCurrentItem();
        final Fragment fragment = mSectionsPagerAdapter.getFragment(position);
        
        if (fragment instanceof MyFragment) {
            final MyFragment myFragment = (MyFragment) fragment;
            
            // タイトル変更(未読件数更新)
            mySetTitle(myFragment);
            
            // サイドメニュー(未読件数)更新
            final LinearLayout linearLayout = (LinearLayout) mDrawer.findViewById(R.id.linearLayout);
            final int n = linearLayout.getChildCount();
            for (int i=0; i<n; i++) {
                final View v = linearLayout.getChildAt(i);
                final Object tag = v.getTag();
                if (tag instanceof Integer) {
                    final int index = (Integer) tag;
                    
                    if (index == position && 0 <= index && index < mPaneInfoList.size()) {
                        final PaneInfo pi = mPaneInfoList.get(index);
                        
                        final String title = getSideMenuItemTitle(myFragment, pi);
                        
                        final TextView item = (TextView) v;
                        item.setText(title);
                    }
                }
            }

        }
    }


    /**
     * サイドメニューのタイトル生成
     * 
     * @param fragment
     * @param pi
     * @return
     */
    private String getSideMenuItemTitle(final Fragment fragment, final PaneInfo pi) {
        
        String title = pi.getDefaultPageTitle(getApplicationContext());
        
        // 未読件数取得
//      if (fragment != null && fragment instanceof MyFragment) {
//          final MyFragment myFragment = (MyFragment) fragment;
//          final int unread = myFragment.getUnreadCount();
//          if (unread >= 1) {
//              title += " (" + unread + ")";
//          }
//      }
        
        return title;
    }
    
    
    /**
     * タブページの変更イベント
     * 
     * @param position
     */
    private void doTabPageChangedEvent(final int position) {
        
        final Fragment fragment = mSectionsPagerAdapter.getFragment(position);
        
        //--------------------------------------------------
        // タブ切替時刻更新
        //--------------------------------------------------
        final long now = System.currentTimeMillis();
        mTabPageChangedTimes.addFirst(now);
        if (mTabPageChangedTimes.size() > TAB_PAGE_CHANGED_TIMES_COUNT_MAX) {
            final int n = mTabPageChangedTimes.size() - TAB_PAGE_CHANGED_TIMES_COUNT_MAX;
            for (int i=0; i<n; i++) {
                mTabPageChangedTimes.removeLast();
            }
        }
        String logText = "doTabPageChangedEvent: ";
        for (int i=0; i<mTabPageChangedTimes.size(); i++) {
            logText += (now - mTabPageChangedTimes.get(i)) + " ";
        }
        logText += isFastTabChanging() ? "[FAST]" : "";
        MyLog.d(logText);
        
        //--------------------------------------------------
        // ページのアクティブ通知(自動ロードのため)
        //--------------------------------------------------
        if (fragment instanceof MyFragment) {
            final MyFragment myFragment = (MyFragment) fragment;
            myFragment.onActivatedOnViewPager();
            
            // タイトル変更
            mySetTitle(myFragment);
        }
    }


    /**
     * 連続タブ切替中の判定
     */
    public boolean isFastTabChanging() {
        
        final long now = System.currentTimeMillis();
        final int n = mTabPageChangedTimes.size();
        
        // "0 353 765 1149 1492" のようなデータが貯まっているので、
        // 3番目の要素が N 秒未満であれば連続切替中と判定する
        if (n >= 3 && (now - mTabPageChangedTimes.get(2)) < 1500) {
            return true;
        }
        
        return false;
    }


    /**
     * タイトルの更新
     * 
     * @param myFragment
     */
    private void mySetTitle(final MyFragment myFragment) {
        
//      MyLog.d("MainActivity.mySetTitle[" + App.mUserStreamStatus + "]");
        
//      final long startTick = System.currentTimeMillis();
        
//      final StringBuilder sb = new StringBuilder();
        
        // 未読件数
//      final int unread = myFragment.getUnreadCount();
//      if (unread >= 1) {
//          sb.append(" (" + unread + ")");
//      }
        
//      // デバッグ用データ
//      if (TPConfig.debugMode) {
//          
//          
//      }
        
//      setTitle(sb.toString());
        
//      MyLog.dWithElapsedTime("mySetTitle [{elapsed}ms]", startTick);
    }


    @SuppressLint("CommitPrefEdits")
    public void saveStarMap() {
        
        final String jsonText = makeStarMapJsonText(App.sStarMap);
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(C.PREF_KEY_STAR_MAP, jsonText);
        TPUtil.doSharedPreferencesEditorApplyOrCommit(editor);
    }


    private String makeStarMapJsonText(HashMap<String, Integer> sStarMap) {
        
        final JSONObject json = new JSONObject();
        try {
            
            for (Entry<String, Integer> it : sStarMap.entrySet()) {
                json.put(it.getKey(), it.getValue());
            }
            
        } catch (JSONException e) {
            MyLog.e(e);
        }
        
        return json.toString();
    }
    
}


