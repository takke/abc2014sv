package jp.takke.abc2014sv.cards;
/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */



import it.gmariotti.cardslib.library.internal.CardExpand;
import jp.takke.abc2014sv.App.Lecture;
import jp.takke.abc2014sv.App.Speaker;
import jp.takke.abc2014sv.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This class provides an example of custom expand/collapse area.
 * It uses carddemo_example_inner_expand layout.
 * <p/>
 * You have to override the {@link #setupInnerViewElements(android.view.ViewGroup, android.view.View)});
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class CustomExpandCard extends CardExpand {

    Lecture mLecture;

    public CustomExpandCard(Context context) {
        super(context, R.layout.inner_expand);
    }

    public CustomExpandCard(Context context, Lecture lecture) {
        super(context, R.layout.inner_expand);
        mLecture = lecture;
    }

    //You can set you properties here (example buttons visibility)

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view == null) return;

        final TextView tx1 = (TextView) view.findViewById(R.id.carddemo_expand_text1);
        
        tx1.setText(mLecture.abstract_);
        
        TextView tx2 = (TextView) view.findViewById(R.id.carddemo_expand_text2);
        

        String ss = "";
        for (Speaker s : mLecture.speakers) {
        	
        	ss += s.name + "\n";
        	ss += s.profile + "\n";
        }
        
        tx2.setText(ss);
    }
}
