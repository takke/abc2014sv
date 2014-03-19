package jp.takke.abc2014sv.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import jp.takke.abc2014sv.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CustomCard extends Card {

    public boolean mExpandable = false;
    
    public CustomCard(Context context) {
        super(context, R.layout.inner_content);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view != null) {
            TextView titleView = (TextView) view.findViewById(it.gmariotti.cardslib.library.R.id.card_main_inner_simple_title);
            if (titleView != null) {
                titleView.setText(mTitle);

                if (mExpandable) {
                    ViewToClickToExpand viewToClickToExpand =
                            ViewToClickToExpand.builder()
                                    .setupView(view);
                    setViewToClickToExpand(viewToClickToExpand);
                }
            }
        }
    }
}