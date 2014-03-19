package jp.takke.abc2014sv;

import java.util.ArrayList;

import jp.takke.abc2014sv.PaneInfo.PaneType;
import jp.takke.abc2014sv.util.MyLog;

import org.json.JSONArray;
import org.json.JSONException;

public class PaneInfoFactory {

    /**
     * デフォルトホーム生成
     * 
     * @param list 
     */
    public static ArrayList<PaneInfo> getDefaultHome(ArrayList<PaneInfo> list) {
        
        list.clear();
        
        // ライブ情報、ホーム
        list.add(new PaneInfo(PaneType.LIVE_INFO));
        list.add(new PaneInfo(PaneType.HOME));
        
        // カンファレンス(カテゴリID付き)
        list.add(new PaneInfo(PaneType.CONFERENCE, 1).setColor(C.COLOR_ORANGE));
        list.add(new PaneInfo(PaneType.CONFERENCE, 2).setColor(C.COLOR_ORANGE));
        list.add(new PaneInfo(PaneType.CONFERENCE, 3).setColor(C.COLOR_SKYBLUE));
        list.add(new PaneInfo(PaneType.CONFERENCE, 4).setColor(C.COLOR_SKYBLUE));
        list.add(new PaneInfo(PaneType.CONFERENCE, 5).setColor(C.COLOR_SKYBLUE));
        list.add(new PaneInfo(PaneType.CONFERENCE, 6).setColor(C.COLOR_GREEN));
        list.add(new PaneInfo(PaneType.CONFERENCE, 7).setColor(C.COLOR_GREEN));
        list.add(new PaneInfo(PaneType.CONFERENCE, 8).setColor(C.COLOR_GREEN));
        list.add(new PaneInfo(PaneType.CONFERENCE, 9).setColor(C.COLOR_GREEN));
        
        return list;
    }

    
    /**
     * JSON から復元する
     * 
     * @param paneInfoList 
     * @param paneInfoJson
     */
    public static void loadFromJson(ArrayList<PaneInfo> paneInfoList, String paneInfoJson) {
        
        paneInfoList.clear();
        try {
            final JSONArray array = new JSONArray(paneInfoJson);
            final int n = array.length();
            
            for (int i=0; i<n; i++) {
                final PaneInfo pi = PaneInfo.fromJson(array.getString(i));
                paneInfoList.add(pi);
            }
        } catch (JSONException e) {
            MyLog.e(e);
        }
    }


    /**
     * JSON に変換する
     * 
     * @param paneInfoList
     * @return
     */
    public static String makeJsonText(ArrayList<PaneInfo> paneInfoList) {
        
        final JSONArray array = new JSONArray();
        
        final int n = paneInfoList.size();
        for (int i=0; i<n; i++) {
            
            final PaneInfo pi = paneInfoList.get(i);
            array.put(pi.toJsonText());
        }
        
        return array.toString();
    }
}
