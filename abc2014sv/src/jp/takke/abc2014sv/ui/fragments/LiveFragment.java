package jp.takke.abc2014sv.ui.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;

import jp.takke.abc2014sv.App;
import jp.takke.abc2014sv.App.LiveInfoItem;
import jp.takke.abc2014sv.MyToolbarListener;
import jp.takke.abc2014sv.R;
import jp.takke.abc2014sv.cards.CustomCard;
import jp.takke.abc2014sv.util.MyLog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

/**
 * ライブ情報用Fragment
 */
public class LiveFragment extends MyFragment implements MyToolbarListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MyLog.d("LiveFragment.onCreate");
		
		// オプションメニューの存在設定(onOptionsItemSelectedが呼ばれるようにする)
		setHasOptionsMenu(true);
		
		MyLog.iWithElapsedTime("startupseq[{elapsed}ms] LiveFragment.onCreate done [" + mPositionInViewPager + "]", App.sStartedAt);
	}
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
		MyLog.d("LiveFragment.onCreateView");
		
        return inflater.inflate(R.layout.fragment_list_conference, container, false);
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

		MyLog.d("LiveFragment.initCards");
		
        //Init an array of Cards
        ArrayList<Card> cards = new ArrayList<Card>();
        
        if (App.sLiveInfo != null) {
        	
            for (int i=0; i<App.sLiveInfo.items.size(); i++){
            	
            	final LiveInfoItem item = App.sLiveInfo.items.get(i);
            	
                Card card = init_standard_header_with_expandcollapse_button_custom_area(item);
                cards.add(card);
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


    private Card init_standard_header_with_expandcollapse_button_custom_area(final LiveInfoItem li) {

        final CustomCard card = new CustomCard(getActivity());

        //--------------------------------------------------
        // ヘッダー
        //--------------------------------------------------
        final CardHeader header = new CardHeader(getActivity());
        header.setTitle(li.room_name);

        {        	
        	//--------------------------------------------------
        	// メニュー生成
        	//--------------------------------------------------
        	
	        // Add a popup menu. This method set OverFlow button to visible
	        header.setButtonOverflowVisible(true);
	        header.setPopupMenuListener(new CardHeader.OnClickCardHeaderPopupMenuListener() {
	            @Override
	            public void onMenuItemClick(BaseCard card, MenuItem item) {
	                // 開く
	            	getMainActivity().moveToConferencePage(li.room_id);
	            }
	        });
	
	        // Add a PopupMenuPrepareListener to add dynamically a menu entry
	        // it is optional.
	        header.setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
	            @Override
	            public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
	                popupMenu.getMenu().add(li.room_name + "タブに移動");
	                return true;
	            }
	        });
        }
        
        //Add Header to card
        card.addCardHeader(header);
 
        //--------------------------------------------------
        // カード
        //--------------------------------------------------
        String body = "";
   		body += li.roomstatus_text + "\n";
    	body += "\n";
        body += li.room_place + "\n";
    	body += li.update_time + "\n";
        card.setTitle(body);
        
        switch (li.roomstatus_id) {
        case 0:	// 未開催
        	card.setBackgroundResourceId(R.drawable.card_background_color0);
        	break;
        	
        case 1:	// 空有り
            card.setBackgroundResourceId(R.drawable.card_background_color1);
            break;
            
        case 2:	// 混雑
            card.setBackgroundResourceId(R.drawable.card_background_color2);
        	break;
        	
        case 3:	// 満席立見
        case 4:	// 満席入場不可
            card.setBackgroundResourceId(R.drawable.card_background_color5);
            break;
        	
        case 5:	// 終了
        case 6:	// 予約制
        	card.setBackgroundResourceId(R.drawable.card_background_color0);
        	break;
        	
        default:
        	card.setBackgroundResourceId(R.drawable.card_background_color0);
        	break;
        }

        return card;
    }
    
    
    /**
     * ViewPager 内でアクティブになった際の通知
     */
	public void onActivatedOnViewPager() {
		
		MyLog.d("LiveFragment.onActivatedOnViewPager");
	}
	
	
	@Override
	public boolean isLoading() {
		return false;
	}


	@Override
	public String getUrl() {
		
		// TODO ライブ情報を表示するページが用意されたらそちらに変更しましょう
		return "http://www.android-group.jp/conference/abc2014s/";
	}


}
