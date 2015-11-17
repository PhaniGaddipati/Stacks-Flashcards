package org.centum.android.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.webkit.WebView;

/**
 * Created by Phani on 5/8/2014.
 */
public class ReleaseNotes {

    private static int releaseNum;
    private final Context context;

    public ReleaseNotes(Context context) {
        this.context = context;
        releaseNum = getVersion();
    }

    public void showReleaseNotesWithoutRepeat() {
        int shownNum = context.getSharedPreferences("releasenotes", 0).getInt("shownNum", -1);
        if (shownNum < releaseNum) {
            showReleaseNotes();
        }
    }

    public void showReleaseNotes() {
        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/release_notes.html");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Release Notes");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(webView);
        builder.show();
        context.getSharedPreferences("releasenotes", 0).edit().putInt("shownNum", releaseNum).commit();
    }

    public int getVersion() {
        int v = 0;
        try {
            v = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // Huh? Really?
        }
        return v;
    }

}
