package jp.takke.abc2014sv.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import jp.takke.abc2014sv.C;
import jp.takke.abc2014sv.R;
import jp.takke.abc2014sv.TPConfig;
import jp.takke.abc2014sv.util.MyLog;
import jp.takke.abc2014sv.util.TPUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstance) {
        
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_about);

        //-------------------------------------------------
        // Version 設定
        //-------------------------------------------------
        final TextView appNameTextView = (TextView) findViewById(R.id.app_name_text);
        appNameTextView.setText(TPUtil.getAppVersionString(getApplicationContext(), R.string.main_message));

        
        //-------------------------------------------------
        // WebView 設定
        //-------------------------------------------------
        final WebView webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.loadUrl("file:///android_asset/about_html/abc2014sv.html");
        
        
        //-------------------------------------------------
        // ボタンのハンドラ登録
        //-------------------------------------------------

        // 戻るボタン
        final Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                // 戻る
                setResult(RESULT_OK);
                finish();
            }

        });

        
        //-------------------------------------------------
        // アイコンのハンドラ登録
        //-------------------------------------------------

        // アイコンの長押し
        final ImageView iconView = (ImageView) findViewById(R.id.ImageView01);
        iconView.setLongClickable(true);
        iconView.setOnLongClickListener(new OnLongClickListener() {
            
            public boolean onLongClick(View v) {
                
                if (TPConfig.debugMode) {
                    final AlertDialog.Builder ad = new AlertDialog.Builder(AboutActivity.this);
                    
                    final CharSequence[] selections = {
                            "デバッグモードの解除",
                            "デバッグログの送信",
                            "キャンセル" };
                    ad.setTitle("デバッグモードメニュー");
                    ad.setItems(selections, new DialogInterface.OnClickListener() {
                        
                        public void onClick(DialogInterface dialog, int which) {
                            
                            switch (which) {
                            case 0:
                                // デバッグモードの解除
                                TPConfig.debugMode = false;
                                TPConfig.save(getApplicationContext());
                                
                                Toast
                                .makeText(getApplicationContext(), "デバッグモードを解除しました", Toast.LENGTH_SHORT)
                                .show();
                                break;
                                
                            case 1:
                                // デバッグログの送信
                                sendDebugLog(AboutActivity.this, "about", "");
                                break;
                                
                            default:
                                break;
                            }
                        }

                    });
                    ad.create().show();
                    
                } else {
                    final AlertDialog.Builder ad = new AlertDialog.Builder(AboutActivity.this);
                    ad.setTitle("デバッグモード確認");
                    ad.setMessage("デバッグモードを設定します。よろしいですか？");
                    ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        
                        public void onClick(DialogInterface dialog, int which) {
                            // 設定変更
                            TPConfig.debugMode = true;
                            TPConfig.save(getApplicationContext());
                            
                            Toast
                            .makeText(getApplicationContext(), "デバッグモードを設定しました", Toast.LENGTH_SHORT)
                            .show();
                        }
                    });
                    ad.setNegativeButton("No", null);
                    ad.show();
                }
                
                return false;
            }
        });
    }


    /**
     * AndroidMarket を開く
     */
    public static void startAndroidMarketActivity(final Context context) {
        
        try {
//          MZ3Util.trackPageView("/check_update");
            
            final String url = "https://market.android.com/details?id=" + context.getPackageName();

            if (url != null) {
            
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }

        } catch (Exception ex) {
            MyLog.e(ex);
        }
    }
    
    /**
     * デバッグログの送信
     */
    public static void sendDebugLog(final Context context, final String title, final String message) {
        
        final AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle("デバッグログの送信確認");
        ad.setMessage("ご迷惑をおかけして申し訳ありません。\n" +
                "\n" +
                "Twitterの仕様変更により利用できなくなっている可能性があります。" +
                "\n" +
                "既に修正版が公開されている可能性がありますのでGoogle Play Storeでアップデートの確認をお願いします\n" +
                "(公開されていない場合は公開までいましばらくお待ち下さい。)\n" +
                "\n" +
                "また、デバッグログを作者に送付することで今後のバージョンでの修正に協力することが出来ます。\n" +
                "但し、ログには個人情報を含む場合がありますのであらかじめご了承下さい。\n" +
                "デバッグログを送付する場合は、次の画面で「Gmail」を選択して下さい。"
                );

        ad.setPositiveButton("アップデート確認", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                startAndroidMarketActivity(context);
            }
        });
        
        ad.setNeutralButton("送付する", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Gmail 送付

                try {
                    //
                    // 一時ファイル生成
                    //
                    final File directory = TPUtil.getExternalStorageFile(C.EXTERNAL_FILE_DIRNAME, context);
                    final String attachFilePath = directory.getAbsolutePath() + "/" + C.EXTERNAL_ATTACH_FILENAME;
                    
                    // ファイルに書き込む
                    final FileOutputStream out = new FileOutputStream(attachFilePath, false);   // create
    
                    // ファイルロード
                    final String files[] = new String[]{
                            C.EXTERNAL_LOG_FILENAME,
                            
//                          C.DEBUG_DUMP_JSON_FILENAME,
                        };

                    String body = "";
                    body += "" + TPUtil.getAppVersionString(context, R.string.main_message) + "\n";
                    body += "MODEL: " + android.os.Build.MODEL + "\n";
                    body += "OS: " + android.os.Build.VERSION.RELEASE + "\n";
                    body += "FINGERPRINT: " + android.os.Build.FINGERPRINT + "\n";
                    body += "\n" + message + "\n";
                    
                    out.write(body.getBytes());
                    
                    for (final String file : files) {
                        
                        // ファイル名出力
                        out.write("----------------------------------------------------------------------\n".getBytes());
                        out.write(("----- " + file + " -----\n").getBytes());
                        out.write("----------------------------------------------------------------------\n".getBytes());

                        // ファイルの内容を出力
                        try {
                            final FileInputStream is = new FileInputStream(directory + "/" + file);
                            
                            // read
                            final byte[] readBytes = new byte[is.available()];
                            is.read(readBytes);
                            is.close();
                            
                            // write
                            out.write(readBytes);

                        } catch (Exception e) {
                            // 例えば FileNotFoundException
                            MyLog.e(e);
                        }
                    }
                    
                    // 書き込み完了
                    out.flush();
                    out.close();
    


                    //
                    // 送信
                    //
//                  final Uri uriAuthorMail = Uri.parse("mailto:" + C.AUTHOR_MAIL);
                    final Intent it = new Intent(Intent.ACTION_SEND);
                    
                    final String[] mailto = {C.AUTHOR_MAIL};
                    it.putExtra(Intent.EXTRA_EMAIL, mailto);
//                  it.setData(uriAuthorMail);
                    it.putExtra(Intent.EXTRA_SUBJECT, "[TwitPane] Debug Logs : " + title);
                    it.putExtra(Intent.EXTRA_TEXT, "TwitPane, debug log files.\n" + body);
                    it.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + attachFilePath));
                    it.setType("text/plain");
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(Intent.createChooser(it, "Choose Gmail"));
                    
                } catch (Exception e) {
                    MyLog.e(e);
                }

            }
        });
        ad.setNegativeButton("キャンセル", null);
        ad.show();
    }
}
