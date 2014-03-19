package jp.takke.abc2014sv.ui.fragments;

import jp.takke.abc2014sv.App;
import jp.takke.abc2014sv.MainActivity;
import jp.takke.abc2014sv.PaneInfo;
import jp.takke.abc2014sv.util.MyLog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

public abstract class MyFragment extends Fragment {

    public final Handler mHandler = new Handler();
    
    // ViewPager 内の position
    protected int mPositionInViewPager = -1;    
    
    // Peneデータ
    protected PaneInfo mPaneInfo = null;
    
    // 前回ロードした時刻
    protected long mLastLoadedTime = 0;
    
    // Fragment 生存フラグ
    protected boolean mFragmentAlive = false;
    
    
    /**
     * Pane情報の設定
     * 
     * @param position
     * @param mPaneInfo
     */
    public void setPaneInfo(int position, PaneInfo paneInfo) {
        this.mPaneInfo = paneInfo;
        this.mPositionInViewPager = position;
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        final String paneTitle = mPaneInfo==null ? null : mPaneInfo.getDefaultPageTitle(getActivity());
        MyLog.d("MyFragment.onCreate [" + paneTitle + "]");
        
        mFragmentAlive = true;
        
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            
            // mPaneInfo の復元
            MyLog.d(" mPaneInfo の復元");
            final String paneInfoJson = savedInstanceState.getString("pane_info_json");
            if (paneInfoJson != null) {
                mPaneInfo = PaneInfo.fromJson(paneInfoJson);
            }
            
            // その他のデータ
            mPositionInViewPager = savedInstanceState.getInt("PositionInViewPager", mPositionInViewPager);
            mLastLoadedTime = savedInstanceState.getLong("LastLoadedTime", mLastLoadedTime);
        }
    }       
    
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        final String paneTitle = mPaneInfo==null ? null : mPaneInfo.getDefaultPageTitle(getActivity());
        MyLog.d("MyFragment.onSaveInstanceState [" + paneTitle + "]");
        
        // mPaneInfo のシリアライズ＆保存
        if (mPaneInfo != null) {
            outState.putString("pane_info_json", mPaneInfo.toJsonText());
        }
        
        // その他のデータ
        outState.putInt("PositionInViewPager", mPositionInViewPager);
        outState.putLong("LastLoadedTime", mLastLoadedTime);
    }
    
    
    @Override
    public void onResume() {
        super.onResume();
        
        final String title = (mPaneInfo == null ? "-" : mPaneInfo.getDefaultPageTitle(getActivity()));
        MyLog.dWithElapsedTime("MyFragment.onResume[{elapsed}ms] [" + title + "]", App.sStartedAt);
        
        mFragmentAlive = true;
    }
    
    
    @Override
    public void onPause() {
        super.onPause();
        
        final String title = (mPaneInfo == null ? "-" : mPaneInfo.getDefaultPageTitle(getActivity()));
        MyLog.dWithElapsedTime("MyFragment.onPause[{elapsed}ms] [" + title + "]", App.sStartedAt);
        
        mFragmentAlive = false;
    }
    
    
    /**
     * ページのActivated通知
     */
    abstract public void onActivatedOnViewPager();
    
    
    /**
     * TwitPaneBase の取得
     * 
     * @return
     */
    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }


    public boolean isCurrentFragment() {
        
        final MainActivity tp = getMainActivity();
        if (tp == null) {
            // 既に終了しているので処理続行不能
            return false;
        }
        return mPositionInViewPager == tp.getCurrentFragmentIndex();
    }


    public boolean isFragmentAlive() {
        return mFragmentAlive;
    }


    abstract public boolean isLoading();
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
    }


    abstract public String getUrl();


    abstract public void resetTabData();
}
