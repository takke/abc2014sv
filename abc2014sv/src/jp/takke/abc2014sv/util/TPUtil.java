package jp.takke.abc2014sv.util;

import java.io.File;

import jp.takke.abc2014sv.C;
import jp.takke.abc2014sv.FontSize;
import jp.takke.abc2014sv.util.IconAlertDialogUtil.IconItem;
import yanzm.products.quickaction.lib.ActionItem;
import yanzm.products.quickaction.lib.QuickAction;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.Icon;



public class TPUtil {

    // チェック高速化のためのキャッシュ
    private static boolean isEmulatorChecked = false;
    private static boolean isEmulatorCache = false;
    
    
    /**
     * QuickAction の項目を作るヘルパーメソッド
     * 
     * @param qa
     * @param context
     * @param title
     * @param drawableId
     * @param onClickListener
     */
    public static void addQuickActionItem(QuickAction qa, Context context, String title, int drawableId, View.OnClickListener onClickListener) {
        
        final ActionItem item1 = new ActionItem();
        item1.setTitle(title);
        if (drawableId != 0) {
            try {
                item1.setIcon(context.getResources().getDrawable(drawableId));
            } catch (OutOfMemoryError e) {
                MyLog.e(e);
            }
        }
        item1.setOnClickListener(onClickListener);
        qa.addActionItem(item1);
    }
    
    
    public static void addQuickActionItem(QuickAction qa, Context context, String title, Drawable drawable, View.OnClickListener onClickListener) {
        
        final ActionItem item1 = new ActionItem();
        item1.setTitle(title);
        if (drawable != null) {
            try {
                item1.setIcon(drawable);
            } catch (OutOfMemoryError e) {
                MyLog.e(e);
            }
        }
        item1.setOnClickListener(onClickListener);
        qa.addActionItem(item1);
    }
    
    

    /**
     * Android2.3 以上は editor.apply を、2.2 以前は commit を実行する
     * 
     * @param editor
     */
    public static void doSharedPreferencesEditorApplyOrCommit(SharedPreferences.Editor editor) {
        
        if (android.os.Build.VERSION.SDK_INT >= 9) {
            // Android 2.3以上は apply 実行
            try {
                SharedPreferences.Editor.class.getMethod("apply").invoke(editor);
            } catch (Throwable e) {
                MyLog.e(e);
            }
        } else {
            editor.commit();
        }
    }

    public static void dumpMemoryUsageLog(final String logPrefix) {
        
        final Runtime runtime = Runtime.getRuntime();
        {
            // Dalvik Heap
            final int total = (int)(runtime.totalMemory()/1024);
            final int free = (int)(runtime.freeMemory()/1024);
            final int used = total - free;
            final int max = (int)(runtime.maxMemory()/1024);
            MyLog.d(logPrefix + " mem(d): " +
                    "used[" + (int)(used) + "KB] " +
                    "total[" + (int)(total) + "KB] " +
                    "max[" + (int)(max) + "KB] (" + (int)(double)(100.0*used/max) + "%)");
        }
        {
            // Native Heap
            final int allocated = (int)(Debug.getNativeHeapAllocatedSize()/1024);
            final int heap = (int)(Debug.getNativeHeapSize()/1024);
            final int free = (int)(Debug.getNativeHeapFreeSize()/1024);
            MyLog.d(logPrefix + " mem(n): " +
                    "alloc[" + (int)(allocated) + "KB] " +
                    "heap[" + (int)(heap) + "KB] " +
                    "free[" + (int)(free) + "KB]");
        }
    }
    
    /**
     * IconicFontDrawable からアイコンのDrawableを生成する
     */
    public static IconicFontDrawable createIconicFontDrawable(Activity activity, Icon icon, int sizeDip, int color) {
        
        final IconicFontDrawable ifd = new IconicFontDrawable(activity);
        ifd.setIcon(icon);
        ifd.setIconColor(color);
        ifd.setIntrinsicHeight(TPUtil.dipToPixel(activity, sizeDip));
        ifd.setIntrinsicWidth(TPUtil.dipToPixel(activity, sizeDip));
        ifd.setIconPadding(TPUtil.dipToPixel(activity, 4));
        
        return ifd;
    }
    
    public static IconicFontDrawable createIconicFontDrawable(Activity activity, Icon icon, int sizeDip) {
        
        return createIconicFontDrawable(activity, icon, sizeDip, C.ICON_DEFAULT_COLOR);
    }
    
    public static IconicFontDrawable createIconicFontDrawable(Activity activity, Icon icon) {
        
        return createIconicFontDrawable(activity, icon, C.DEFAULT_ICON_SIZE_DIP);
    }
    
    
    public static IconItem createIconItem(Activity activity, int menuStringId, int iconId) {
        
        return new IconItem(activity.getString(menuStringId), iconId);
    }
    
    public static IconItem createIconItem(Activity activity, int menuStringId, Icon iconId) {
        
        return new IconItem(activity.getString(menuStringId), TPUtil.createIconicFontDrawable(activity, iconId));
    }
    
    public static IconItem createIconItem(Activity activity, int menuStringId, Icon iconId, int color) {
        
        return new IconItem(activity.getString(menuStringId), TPUtil.createIconicFontDrawable(activity, iconId, C.DEFAULT_ICON_SIZE_DIP, color));
    }
    
    public static IconItem createIconItem(Activity activity, String menuString, Icon iconId) {
        
        return new IconItem(menuString, TPUtil.createIconicFontDrawable(activity, iconId));
    }

    public static IconItem createIconItem(Activity activity, String menuString, Icon iconId, int color) {
        
        return new IconItem(menuString, TPUtil.createIconicFontDrawable(activity, iconId, C.DEFAULT_ICON_SIZE_DIP, color));
    }

    /**
     * ViewPager にアイコンを設定する
     * 
     * @param activity
     * @param drawable
     * @param sb
     * @param icon
     * @return
     */
    public static Drawable setViewPagerIcon(final Activity activity, Drawable drawable, final SpannableStringBuilder sb, final Icon icon) {
        
        {
            final int sizeDip = (int) (FontSize.listTitleSize*1.5); // FontSize=80% で 18dip
            final IconicFontDrawable ifd = new IconicFontDrawable(activity);
            ifd.setIcon(icon);
            
            ifd.setIconColor(0xFFF0F0F0);
//          final int color = mPaneInfoList.get(position).getColor();
//          ifd.setIconColor(color == C.SELECTABLE_COLOR_INVALID ? 0xFFF0F0F0 : color);
            
            ifd.setIntrinsicHeight(TPUtil.dipToPixel(activity, sizeDip));
            ifd.setIntrinsicWidth(TPUtil.dipToPixel(activity, sizeDip));
            ifd.setIconPadding((int)TPUtil.dipToPixel(activity, sizeDip/9.0f));
            drawable = ifd;
        }
        
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            final ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        return drawable;
    }


    public static String formatShortTime(String time_text) {
        return time_text.replaceAll("^[0-9]+-[0-9]+-[0-9]+ ", "").replaceAll(":00$", "");
    }


    /**
     * R.string.main_message を AndroidManifest.xml の文字列で置換したバージョン文字列を取得する
     * 
     * @param context コンテキスト
     * @return バージョン文字列
     */
    public static String getAppVersionString(Context context, int R_string_main_message) {
        
        // Version, Version code取得
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            MyLog.e(e);
        }
        String versionCode = "?";
        String version = "x.x.x";
        if (pinfo != null) {
            versionCode = String.valueOf(pinfo.versionCode);
            version = pinfo.versionName;
        }
    
        String versionString = context.getString(R_string_main_message).toString();
        versionString = versionString.replace("%VER%", version);
        versionString = versionString.replace("%REV%", versionCode);
        return versionString;
    }


    /**
     * リビジョン取得
     * 
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        
        // Version, Version code取得
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            MyLog.e(e);
        }
        if (pinfo == null) {
            return 0;
        }
        return pinfo.versionCode;
    }


    public static boolean isEmulator() {
            
            if (!isEmulatorChecked) {
                // 未チェック(未キャッシュ)の場合にのみ実際にチェックする
    //          isEmulatorCache = android.os.Build.MODEL.equals("sdk");
                isEmulatorCache = false;
                if (android.os.Build.DEVICE.equals("generic")) {
                     if (android.os.Build.BRAND.equals("generic")) {
                          isEmulatorCache = true;
                     }
                }
                isEmulatorChecked = true;
            }
            
            return isEmulatorCache;
        }


    public static int dipToPixel(Activity context, int dip) {
        return (int) dipToPixel(context, (float)dip);
    }


    public static float dipToPixel(Activity context, float dip) {
        if (context == null) {
            return dip;
        }
        final DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics); 
        return dip * metrics.density;
    }


    /**
     * 指定されたディレクトリへのファイルオブジェクトを取得する
     * 
     * @param directory ディレクトリ
     * @return File オブジェクト
     */
    public static File getExternalStorageFile(String directory, Context context) {
        
        final String status = Environment.getExternalStorageState();
        File fout;
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // 未マウントなのでデータディレクトリを返す
            if (context == null) {
                return null;
            }
            fout = new File(context.getApplicationInfo().dataDir + "/files/");
        } else {
            fout = new File(Environment.getExternalStorageDirectory() + "/" + directory + "/");
        }
        fout.mkdirs();
        return fout;
    }


    /**
     * 文字列としてURLを共有する
     * 
     * @param url URL
     * @param title タイトル
     */
    public static void doShareAsText(Context context, String url, String title) {
        
        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        
        // ブラウザのページ共有と同様にタイトルを SUBJECT として引き渡す
        if (title != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
        }
        context.startActivity(intent);
    }


}
