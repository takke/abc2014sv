package jp.takke.abc2014sv.ui.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jp.takke.abc2014sv.App;
import jp.takke.abc2014sv.App.Lecture;
import jp.takke.abc2014sv.App.Speaker;
import jp.takke.abc2014sv.MyToolbarListener;
import jp.takke.abc2014sv.R;
import jp.takke.abc2014sv.cards.CustomCard;
import jp.takke.abc2014sv.cards.CustomExpandCard;
import jp.takke.abc2014sv.util.MyLog;
import jp.takke.abc2014sv.util.TPUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

/**
 * カンファレンス用Fragment
 */
public class ConferenceFragment extends MyFragment implements MyToolbarListener {

    int mCategoryId = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCategoryId = mPaneInfo.getCategoryId();
        
        MyLog.d("ConferenceFragment.onCreate[" + mCategoryId + "]");
        
        // オプションメニューの存在設定(onOptionsItemSelectedが呼ばれるようにする)
        setHasOptionsMenu(true);
        
        MyLog.iWithElapsedTime("startupseq[{elapsed}ms] ConferenceFragment.onCreate done [" + mPositionInViewPager + "]", App.sStartedAt);
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        MyLog.d("ConferenceFragment.onCreateView[" + mCategoryId + "]");
        
        return inflater.inflate(R.layout.fragment_list_conference, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initCards();
    }

    
    @Override
    public void resetTabData() {
        
        CardListView listView = null;
        final View v = getView();
        if (v != null) {
            listView = (CardListView) v.findViewById(R.id.carddemo_list_expand);
        }
        
        // スクロール位置保存
        int position = 0;
        int y = 0;
        if (listView!=null){
            position = listView.getFirstVisiblePosition();
            final View child = listView.getChildAt(0);
            if (child != null) {
                y = child.getTop();
            }
        }
        
        // 再生成
        initCards();
        
        // スクロール位置復帰
        if (listView!=null) {
            listView.setSelectionFromTop(position, y);
        }
        
    }
    
    
    private void initCards() {

        MyLog.d("ConferenceFragment.initCards[" + mCategoryId + "]");
        
        // Init an array of Cards
        ArrayList<Card> cards = new ArrayList<Card>();
        
        if (mCategoryId == 0) {
            // ホーム
            {
                final Lecture pi = new Lecture();
                pi.title = "";
                pi.title += "※講演内容、講演時間は予告無く変更される場合があります。ご了承下さい。";
                
                final Card card = init_standard_header_with_expandcollapse_button_custom_area(pi);
                cards.add(card);
            }
            
            //--------------------------------------------------
            // お気に入り登録したデータの表示
            //--------------------------------------------------
            final ArrayList<Lecture> lectures = new ArrayList<App.Lecture>();
            if (App.sConferenceData != null) {
                
                for (int i=0; i<App.sConferenceData.lectures.size(); i++){
                    
                    final Lecture lecture = App.sConferenceData.lectures.get(i);
                    
                    if (App.sStarMap.containsKey(lecture.getStarKey())) {
                        lectures.add(lecture);
                    }
                }
                if (lectures.size() > 0) {
                    
                    // 枠順、部屋順、開催順にソート
                    Collections.sort(lectures, new Comparator<Lecture>() {

                        @Override
                        public int compare(Lecture lhs, Lecture rhs) {
                            
                            // time_frame
                            final int c0 = lhs.time_frame - rhs.time_frame;
                            if (c0 != 0) {
                                return c0;
                            }
                            
                            // room_id
                            final int c1 = lhs.room_id - rhs.room_id;
                            if (c1 != 0) {
                                return c1;
                            }
                            
                            // start_time
                            if (lhs.start_time != null && rhs.start_time != null) {
                                final int c2 = lhs.start_time.compareTo(rhs.start_time);
                                return c2;
                            }
                            
                            return 0;
                        }
                    });
                    
                    
                    for (Lecture lecture : lectures) {
                        final Card card = init_standard_header_with_expandcollapse_button_custom_area(lecture);
                        cards.add(card);
                    }
                }
            }
            
            if (lectures.size() == 0) {
                final Lecture pi = new Lecture();
                pi.title = "";

                pi.title += "左側に「ライブ情報」タブがあります。開催前はダミーデータが表示されます。\n"
                         + "このタブにはお気に入りに登録したカンファレンスが表示されます";
                final Card card = init_standard_header_with_expandcollapse_button_custom_area(pi);
                cards.add(card);
            }
            
        } else {
        
            if (App.sConferenceData != null) {
                
                //--------------------------------------------------
                // 会場情報設定
                //--------------------------------------------------
                final Lecture pi = new Lecture();
                switch (mCategoryId) {
                case 1: pi.title = "T0:基調講演/Reborn";            break;
                case 2: pi.title = "T1:デザイン・開発";             break;
                case 3: pi.title = "T2:メーカー・キャリア";         break;
                case 4: pi.title = "T3:コンテンツ・ビジネス";       break;
                case 5: pi.title = "T4:開発";                       break;
                case 6: pi.title = "T5:デバイス";                   break;
                case 7: pi.title = "T6:EffectiveAndroidコラボ開発"; break;
                case 8: pi.title = "T7:LT";                         break;
                case 9: pi.title = "T8:ビジネス・教育";             break;
                }
                // 場所
                pi.speakers.add(new Speaker(getPlace()));
                {
                    final Card card = init_standard_header_with_expandcollapse_button_custom_area(pi);
                    cards.add(card);
                }
                
                //--------------------------------------------------
                // 該当カテゴリのカンファレンスデータを追加する
                //--------------------------------------------------
                for (int i=0; i<App.sConferenceData.lectures.size(); i++){
                    
                    final Lecture lecture = App.sConferenceData.lectures.get(i);
                    
                    if (mCategoryId == lecture.category_id) {
                        
                        final Card card = init_standard_header_with_expandcollapse_button_custom_area(lecture);
                        cards.add(card);
                    }
                }
            }
        }
        
        final CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);

        final View v = getView();
        if (v != null) {
            
            final CardListView listView = (CardListView) v.findViewById(R.id.carddemo_list_expand);
            if (listView!=null){
                listView.setAdapter(mCardArrayAdapter);
            }
        }
    }


    private String getPlace() {
        switch (mCategoryId) {
        case 1: return "秋葉原ダイビル2F";
        case 2: return "秋葉原ダイビル2F";
        case 3: return "秋葉原ダイビル5F 5A";
        case 4: return "秋葉原ダイビル5F 5B";
        case 5: return "秋葉原ダイビル5F 5C";
        case 6: return "UDX 6F タイプ350";
        case 7: return "UDX 6F タイプ120";
        case 8: return "UDX 6F タイプ120";
        case 9: return "UDX 6F タイプ120";
        }
        return "";
    }


    /**
     * This method builds a standard header with a custom expand/collpase
     */
    private Card init_standard_header_with_expandcollapse_button_custom_area(final Lecture lecture) {

        final CustomCard card = new CustomCard(getActivity());

        //--------------------------------------------------
        // ヘッダー
        //--------------------------------------------------
        final CardHeader header = new CardHeader(getActivity());

        String title = "";
//        switch (lecture.time_frame) {
//        case 11:  title += "10:00～"; break;
//        case 12:  title += "11:15～"; break;
//        case 13:  title += "13:15～"; break;
//        case 14:  title += "14:15～"; break;
//        case 15:  title += "15:15～"; break;
//        case 16:  title += "16:15～"; break;
//        }
//        title += " ";
        
        final Integer starLevel = App.sStarMap.get(lecture.getStarKey());
        if (starLevel != null) {
            switch (starLevel) {
            case 1: title += "★ ";       break;
            case 2: title += "★★ ";     break;
            case 3: title += "★★★ ";   break;
            }
        }
        
        header.setTitle(title + lecture.title);

        //Set visible the expand/collapse button
//        header.setButtonExpandVisible(true);
        
        if (lecture.lec_id != null && lecture.lec_id.length() > 0) {
            
            //--------------------------------------------------
            // メニュー生成
            //--------------------------------------------------
            
            // Add a popup menu. This method set OverFlow button to visible
            header.setButtonOverflowVisible(true);
            header.setPopupMenuListener(new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
//                  Toast.makeText(getActivity(), "Click on " + item.getTitle() + "-" + ((Card) card).getCardHeader().getTitle(), Toast.LENGTH_SHORT).show();
                    
                    switch (item.getItemId()) {
                    case 0: // ★★★
                    case 1: // ★★
                    case 2: // ★
                        switch (item.getItemId()) {
                        case 0: // ★★★
                            App.sStarMap.put(lecture.getStarKey(), 3);
                            break;
                        case 1: // ★★
                            App.sStarMap.put(lecture.getStarKey(), 2);
                            break;
                        case 2: // ★
                            App.sStarMap.put(lecture.getStarKey(), 1);
                            break;
                        }
                        getMainActivity().resetAllTabs();
                        getMainActivity().saveStarMap();
                        break;
                        
                    case 3: // ☆
                        // お気に入り削除
                        App.sStarMap.remove(lecture.getStarKey());
                        getMainActivity().resetAllTabs();
                        getMainActivity().saveStarMap();
                        break;
                        
                    case 10: // 共有
                        doShare(lecture);
                        break;
                        
                    case 20: // ブラウザで開く
                        // TODO なんかアンカーのルールがよくわかんないのでカテゴリページを開く
                        getMainActivity().openExternalBrowser(getUrl());
                        break;
                    }
                    
                }
            });
    
            // Add a PopupMenuPrepareListener to add dynamically a menu entry
            // it is optional.
            header.setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
                @Override
                public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
                    popupMenu.getMenu().add(0,  0,  0, "★★★");
                    popupMenu.getMenu().add(0,  1,  1, "★★");
                    popupMenu.getMenu().add(0,  2,  2, "★");
                    popupMenu.getMenu().add(0,  3,  3, "☆");
                    popupMenu.getMenu().add(0, 10, 10, "共有");
                    popupMenu.getMenu().add(0, 20, 20, "ブラウザで開く");
    
                    //return false; You can use return false to hidden the button and the popup
                    return true;
                }
            });
        }
        
        //Add Header to card
        card.addCardHeader(header);
 
        //--------------------------------------------------
        // カード
        //--------------------------------------------------
        String body = lecture.getSpeakersText() + "\n";
        if (lecture.lec_id != null && lecture.lec_id.length() > 0) {
            body += "[" + lecture.lec_id + "] " + lecture.getStartEndText() + "\n"
                    + "\n\n";
            
        } else {
            body += "\n\n\n";
        }
        card.setTitle(body);
        
        switch (lecture.time_frame) {
        case 11:
        case 12:
            card.setBackgroundResourceId(R.drawable.card_background_color0);
            break;
            
        case 13:
            card.setBackgroundResourceId(R.drawable.card_background_color1);
            break;
        case 14:
            card.setBackgroundResourceId(R.drawable.card_background_color2);
            break;
        case 15:
            card.setBackgroundResourceId(R.drawable.card_background_color3);
            break;
        case 16:
            card.setBackgroundResourceId(R.drawable.card_background_color4);
            break;
        case 17:
            card.setBackgroundResourceId(R.drawable.card_background_color5);
            break;
            
        default:
            card.setBackgroundResourceId(R.drawable.card_background_color6);
            break;
        }

        //--------------------------------------------------
        // Expandカード
        //--------------------------------------------------
        if (lecture.lec_id != null && lecture.lec_id.length() > 0) {
            CustomExpandCard expand = new CustomExpandCard(getActivity(), lecture);
            
            card.mExpandable = true;
        
            //Add Expand Area to Card
            card.addCardExpand(expand);
        }
        
        //Swipe
//        card.setSwipeable(true);

        return card;
    }
    
    
    protected void doShare(Lecture lecture) {
        
        String text = "";
        
        text += " ";
        text += "[" + lecture.lec_id + "]";
        text += lecture.title;
        text += "(" + getPlace() + ")";
//      text += " " + lecture.url;
        // URLデータが不正なのでルーム別のページにする
        text += " " + getUrl();
        text += " #abc2014s";
        
        TPUtil.doShareAsText(getActivity(), text, null);
    }


    /**
     * ViewPager 内でアクティブになった際の通知
     */
    public void onActivatedOnViewPager() {
        
        MyLog.d("ConferenceFragment.onActivatedOnViewPager[" + mCategoryId + "]");
    }
    
    
    @Override
    public boolean isLoading() {
        return false;
    }


    @Override
    public String getUrl() {
        
        switch (mCategoryId) {
        case 1: return "http://www.android-group.jp/conference/abc2014s/conference/keynote/";   // 基調講演/Reborn
        case 2: return "http://www.android-group.jp/conference/abc2014s/conference/design/";    // デザイン・開発
        case 3: return "http://www.android-group.jp/conference/abc2014s/conference/maker/";     // メーカー・キャリア
        case 4: return "http://www.android-group.jp/conference/abc2014s/conference/content/";   // コンテンツ・ビジネス
        case 5: return "http://www.android-group.jp/conference/abc2014s/conference/dev/";       // 開発
        case 6: return "http://www.android-group.jp/conference/abc2014s/conference/device/";    // デバイス
        case 7: return "http://www.android-group.jp/conference/abc2014s/conference/effective/"; // EffectiveAndroidコラボ開発
        case 8: return "http://www.android-group.jp/conference/abc2014s/conference/lt/";        // LT
        case 9: return "http://www.android-group.jp/conference/abc2014s/conference/business/";  // ビジネス・教育
        }
        
        return "http://www.android-group.jp/conference/abc2014s/";
    }


}
