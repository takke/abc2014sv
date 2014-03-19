package jp.takke.abc2014sv;

import java.util.ArrayList;
import java.util.HashMap;

import jp.takke.abc2014sv.util.TPUtil;
import android.app.Application;

public class App extends Application {

    // 起動時刻
    public static long sStartedAt = System.currentTimeMillis();
    
    //--------------------------------------------------
    // カンファレンスデータ
    //--------------------------------------------------
    
    public static class Speaker {
        public String speaker_id = "";
        public String name = "";
        public String profile = "";
        
        public Speaker() {}
        public Speaker(String n) { name = n; }
    }
    
    public static class Lecture {
        public String lec_id = "";
        public String title = "";
        public String abstract_ = "";
        public String lec_order_num = "";
        public String room_order_num = "";
        public String url = "";
        
        public ArrayList<Speaker> speakers = new ArrayList<Speaker>();
        
        public String start_time = null;
        public String end_time = null;
        
        public int room_id = 0;
        public String room = "";
        public int category_id = 0;
        public String category = "";
        
        public int time_frame = 0;
        
        public String getSpeakersText() {
            
            final StringBuilder sb = new StringBuilder();
            
            int i = 0;
            for (Speaker s : speakers) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(s.name);
                i++;
            }
            
            return sb.toString();
        }

        public String getStartEndText() {
            
            String start = "";
            if (start_time != null) {
                start = TPUtil.formatShortTime(start_time);
            }
            String end   = "";
            if (end_time != null) {
                end = TPUtil.formatShortTime(end_time);
            }
            
            if (start_time == null && end_time == null) {
                return "";
            }
            return start + " ～ " + end;
        }

        public String getStarKey() {
            
            // lec_id だけではユニークにならないので start_time も付加する
            
            return lec_id + start_time;
        }
    }
    
    public static class LecInfo {
        public final ArrayList<Lecture> lectures = new ArrayList<Lecture>();
        
        public String updateDate = "";
    }
    
    // カンファレンスデータ
    public static LecInfo sConferenceData = null;
    
    //--------------------------------------------------
    // ライブ情報データ
    //--------------------------------------------------
    
    public static class LiveInfoItem {
        public int room_id = 0;
        public String room_place = "";
        public String room_name = "";
        public int roomstatus_id = -1;
        public String roomstatus_text = "";
        public String update_time = "";
    }
    
    public static class LiveInfo {
        public final ArrayList<LiveInfoItem> items = new ArrayList<LiveInfoItem>();
    }
    
    // ライブ情報データ
    public static LiveInfo sLiveInfo = null;
    
    //--------------------------------------------------
    // お気に入りカンファレンスデータ
    //--------------------------------------------------
    // (lec_id+start_time) => star-level [1,3]
    public static HashMap<String, Integer> sStarMap = new HashMap<String, Integer>();
}
