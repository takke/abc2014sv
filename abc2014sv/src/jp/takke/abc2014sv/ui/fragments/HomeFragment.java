package jp.takke.abc2014sv.ui.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;

import jp.takke.abc2014sv.App;
import jp.takke.abc2014sv.App.Lecture;
import jp.takke.abc2014sv.MyToolbarListener;
import jp.takke.abc2014sv.R;
import jp.takke.abc2014sv.cards.CustomCard;
import jp.takke.abc2014sv.cards.CustomExpandCard;
import jp.takke.abc2014sv.util.MyLog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

/**
 * ホーム用Fragment
 */
public class HomeFragment extends MyFragment implements MyToolbarListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MyLog.d("HomeFragment.onCreate");
		
		// オプションメニューの存在設定(onOptionsItemSelectedが呼ばれるようにする)
		setHasOptionsMenu(true);
		
		MyLog.iWithElapsedTime("startupseq[{elapsed}ms] HomeFragment.onCreate done [" + mPositionInViewPager + "]", App.sStartedAt);
	}
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
		MyLog.d("HomeFragment.onCreateView");
		
        return inflater.inflate(R.layout.fragment_list_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initCards();
    }

	
	public void resetTabData() {
		
		initCards();
	}
	
	
    private void initCards() {

		MyLog.d("HomeFragment.initCards");
		
        //Init an array of Cards
        ArrayList<Card> cards = new ArrayList<Card>();
        
        // TODO お気に入りカンファレンス一覧とか
        if (App.sConferenceData != null) {
        	
//        	// 会場情報設定
//        	Lecture pi = new Lecture();
//        	switch (mCategoryId) {
//        	case 1:	pi.title = "基調講演/Reborn";				pi.speakers.add(new Speaker("秋葉原ダイビル2F"));	break;
//        	case 2:	pi.title = "デザイン・開発";				pi.speakers.add(new Speaker("秋葉原ダイビル2F"));	break;
//        	case 3:	pi.title = "メーカー・キャリア";			pi.speakers.add(new Speaker("秋葉原ダイビル5F 5A"));	break;
//        	case 4:	pi.title = "コンテンツ・ビジネス";			pi.speakers.add(new Speaker("秋葉原ダイビル5F 5B"));	break;
//        	case 5:	pi.title = "開発";							pi.speakers.add(new Speaker("秋葉原ダイビル5F 5C"));	break;
//        	case 6:	pi.title = "デバイス";						pi.speakers.add(new Speaker("UDX 6F タイプ350"));	break;
//        	case 7:	pi.title = "EffectiveAndroidコラボ開発";	pi.speakers.add(new Speaker("UDX 6F タイプ120"));	break;
//        	case 8:	pi.title = "LT";							pi.speakers.add(new Speaker("UDX 6F タイプ120"));	break;
//        	case 9:	pi.title = "ビジネス・教育";				pi.speakers.add(new Speaker("UDX 6F タイプ120"));	break;
//        	}
//        	{
//                Card card = init_standard_header_with_expandcollapse_button_custom_area(pi, 0);
//                cards.add(card);
//        	}
//        	
//        	
//        	// 該当カテゴリのものを追加する
//            for (int i=0; i<App.sConferenceData.lectures.size(); i++){
//            	
//            	final Lecture lecture = App.sConferenceData.lectures.get(i);
//            	
//            	if (mCategoryId == lecture.category_id) {
//            		
//                    Card card = init_standard_header_with_expandcollapse_button_custom_area(lecture, i);
////                    Card card = init_standard_header_with_expandcollapse_button_custom_area("Header "+i, i);
//                    cards.add(card);
//            	}
//            }
        }
        
        {
        	final Lecture pi = new Lecture();
        	pi.title = "";
        	pi.title += "※講演内容、講演時間は予告無く変更される場合があります。ご了承下さい。\n";
        	
            Card card = init_standard_header_with_expandcollapse_button_custom_area(pi);
            cards.add(card);
        }
        {
        	final Lecture pi = new Lecture();
        	pi.title = "";
        	
        	pi.title += "このタブは開発中です。\n";
        	pi.title += "左側に「ライブ情報」タブがあります。開催前はダミーデータが表示されます。\n";
        	pi.title += "\n";
        	pi.title += "ここにお気に入り登録したカンファレンスが表示されるといいですねぇ。\n";
            Card card = init_standard_header_with_expandcollapse_button_custom_area(pi);
            cards.add(card);
        }
        
        //Init an array of Cards
//        for (int i=0;i<200;i++){
//        	Lecture pi = new Lecture();
//        	pi.title = "Entry" + i;
//            Card card = init_standard_header_with_expandcollapse_button_custom_area(pi, i);
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


    /**
     * This method builds a standard header with a custom expand/collpase
     */
    private Card init_standard_header_with_expandcollapse_button_custom_area(Lecture lecture) {

        final CustomCard card = new CustomCard(getActivity());

        //--------------------------------------------------
        // ヘッダー
        //--------------------------------------------------
        final CardHeader header = new CardHeader(getActivity());

        String title = "";
//        switch (lecture.time_frame) {
//        case 11:	title += "10:00～";	break;
//        case 12:	title += "11:15～";	break;
//        case 13:	title += "13:15～";	break;
//        case 14:	title += "14:15～";	break;
//        case 15:	title += "15:15～";	break;
//        case 16:	title += "16:15～";	break;
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
	                Toast.makeText(getActivity(), "Click on " + item.getTitle() + "-" + ((Card) card).getCardHeader().getTitle(), Toast.LENGTH_SHORT).show();
	            }
	        });
	
	        // Add a PopupMenuPrepareListener to add dynamically a menu entry
	        // it is optional.
	        header.setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
	            @Override
	            public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
	                popupMenu.getMenu().add("お気に入りに追加");
	                popupMenu.getMenu().add("共有");
	                popupMenu.getMenu().add("ブラウザで開く");
	
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
        
        return card;
    }
    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		return super.onOptionsItemSelected(item);
	}
	


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	

//	/**
//	 * ツールバー「更新」ボタンの押下イベント
//	 */
//	@Override
//	public boolean onClickToolbarUpdateButton(View v) {
//		
//		// button5:更新
//		// リロード
////		if (mCurrentTask != null) {
////    		// 既に通信中なのでキャンセルする
////			doCancelTask();
////    		
////		} else {
////			// 通信開始
////			doReload();
////		}
//		
//		return true;
//	}
	
	
	/**
     * ViewPager 内でアクティブになった際の通知
     */
	public void onActivatedOnViewPager() {
		
		MyLog.d("HomeFragment.onActivatedOnViewPager");
	}
	
	
	@Override
	public boolean isLoading() {
		return false;
	}


	@Override
	public String getUrl() {
		return "http://www.android-group.jp/conference/abc2014s/";
	}


}
