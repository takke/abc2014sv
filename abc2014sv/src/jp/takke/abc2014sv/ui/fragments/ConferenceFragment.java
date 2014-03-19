package jp.takke.abc2014sv.ui.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;

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
import android.widget.Toast;

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
        
        initCards();
        
    }
    
    
    private void initCards() {

        MyLog.d("ConferenceFragment.initCards[" + mCategoryId + "]");
        
        //Init an array of Cards
        ArrayList<Card> cards = new ArrayList<Card>();
        
        if (App.sConferenceData != null) {
            
            // 会場情報設定
            Lecture pi = new Lecture();
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
                Card card = init_standard_header_with_expandcollapse_button_custom_area(pi, 0);
                cards.add(card);
            }
            
            
            // 該当カテゴリのものを追加する
            for (int i=0; i<App.sConferenceData.lectures.size(); i++){
                
                final Lecture lecture = App.sConferenceData.lectures.get(i);
                
                if (mCategoryId == lecture.category_id) {
                    
                    Card card = init_standard_header_with_expandcollapse_button_custom_area(lecture, i);
//                    Card card = init_standard_header_with_expandcollapse_button_custom_area("Header "+i, i);
                    cards.add(card);
                }
            }
        }
        
        //Init an array of Cards
//        ArrayList<Card> cards = new ArrayList<Card>();
//        for (int i=0;i<200;i++){
//            Card card = init_standard_header_with_expandcollapse_button_custom_area("Header "+i, i);
//            cards.add(card);
//        }
        
        

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
    private Card init_standard_header_with_expandcollapse_button_custom_area(final Lecture lecture, int i) {

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
                    case 0: // お気に入りに追加
                        Toast.makeText(getActivity(), "開発中です・・・", Toast.LENGTH_SHORT).show();
                        break;
                        
                    case 1: // 共有
                        doShare(lecture);
                        break;
                        
                    case 2: // ブラウザで開く
                        // TODO なんかアンカーのルールがよくわかんないので適当に。
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
                    popupMenu.getMenu().add(0, 0, 0, "お気に入りに追加");
                    popupMenu.getMenu().add(0, 1, 1, "共有");
                    popupMenu.getMenu().add(0, 2, 2, "ブラウザで開く");
    
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
        
        //Just an example to expand a card
//        if (i==2 || i==7 || i==9)
//            card.setExpanded(true);

        //Swipe
//        card.setSwipeable(true);

        //Animator listener
//        card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
//            @Override
//            public void onExpandEnd(Card card) {
//                Toast.makeText(getActivity(),"Expand "+card.getCardHeader().getTitle(),Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        card.setOnCollapseAnimatorEndListener(new Card.OnCollapseAnimatorEndListener() {
//            @Override
//            public void onCollapseEnd(Card card) {
//                Toast.makeText(getActivity(),"Collpase " +card.getCardHeader().getTitle(),Toast.LENGTH_SHORT).show();
//            }
//        });

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
