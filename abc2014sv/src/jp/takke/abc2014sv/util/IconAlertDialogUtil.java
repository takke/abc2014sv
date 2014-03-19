package jp.takke.abc2014sv.util;

import java.util.ArrayList;
import java.util.List;

import jp.takke.abc2014sv.R;
import jp.takke.abc2014sv.ThemeColor;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class IconAlertDialogUtil {

    public static class IconItem {
        private String text;
        private final int iconId;
        private Drawable iconDrawable;
        
        public abstract static interface OnIconItemClickListener {
            public abstract void onClickRightIcon(int position);
            public abstract void onClickText(int position);
        }
        private final OnIconItemClickListener iconItemClickListener;
        
        
        public IconItem(String text, int iconId) {
            this.text = text;
            this.iconId = iconId;
            this.iconDrawable = null;
            this.iconItemClickListener = null;
        }

        public IconItem(String text, Drawable icon) {
            this.text = text;
            this.iconId = 0;
            this.iconDrawable = icon;
            this.iconItemClickListener = null;
        }

        public IconItem(String text, Drawable icon, OnIconItemClickListener iconItemClickListener) {
            this.text = text;
            this.iconId = 0;
            this.iconDrawable = icon;
            this.iconItemClickListener = iconItemClickListener;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public void setDrawable(Drawable drawable) {
            this.iconDrawable = drawable;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    static class MyListAdapter extends ArrayAdapter<IconItem> {

        public MyListAdapter(Context context, int resource,
                int textViewResourceId, List<IconItem> objects) {
            
            super(context, resource, textViewResourceId, objects);
        }
    }
    
    public static ArrayAdapter<IconItem> createAdapter(final Context context, final ArrayList<IconItem> items) {
        
        return createAdapter(context, items, null);
    }
    
    public static ArrayAdapter<IconItem> createAdapter(final Context context, final ArrayList<IconItem> items,
                                              final DialogInterface.OnClickListener onClickListener) {
        
        MyListAdapter adapter = new MyListAdapter(
                context,
                R.layout.my_select_dialog_item,
                android.R.id.text1,
                items) {
            
            public View getView(final int position, View convertView, ViewGroup parent) {
                
                // User super class to create the View
                final View v = super.getView(position, convertView, parent);
                final TextView tv = (TextView) v.findViewById(android.R.id.text1);

                // Put the image on the TextView
                final IconItem item = items.get(position);
                if (item.iconDrawable != null) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(
                            item.iconDrawable, null, null, null);
                } else {
                    tv.setCompoundDrawablesWithIntrinsicBounds(
                            item.iconId, 0, 0, 0);
                }

                // Add margin between image and text (support various screen densities)
                int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5*2);
                
                //--------------------------------------------------
                // 右側の矢印アイコン
                //--------------------------------------------------
                final ImageButton rightIcon = (ImageButton) v.findViewById(android.R.id.icon1);
                if (item.iconItemClickListener != null) {
                    rightIcon.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            
                            item.iconItemClickListener.onClickRightIcon(position);
                        }
                    });
                    
                    rightIcon.setImageResource(ThemeColor.mySelectDialogItemRightIcon);
                    
                    rightIcon.setVisibility(View.VISIBLE);
                    
                    // TextView のクリックイベントも自動設定されないので設定する
                    tv.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            
                            item.iconItemClickListener.onClickText(position);
                        }
                    });
                    
                } else {
                    rightIcon.setVisibility(View.GONE);
                    
                    if (onClickListener != null) {
                        tv.setOnClickListener(new View.OnClickListener() {
                            
                            @Override
                            public void onClick(View v) {
                                
                                onClickListener.onClick(null, position);
                            }
                        });
                    }
                }

                return v;
            }
        };
        
        return adapter;
    }

}
