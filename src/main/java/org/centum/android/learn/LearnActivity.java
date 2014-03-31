/**
 Stacks Flashcards - A flashcards application for Android devices 4.0+
 Copyright (C) 2014  Phani Gaddipati

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.centum.android.learn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.google.analytics.tracking.android.EasyTracker;

import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 2/1/14.
 */
public class LearnActivity extends Activity implements View.OnClickListener {

    private ImageView closeImageButton;
    private ViewPager viewPager;
    private LearnPagerAdapter learnPagerAdapter;
    private View rightMargin;
    private View leftMargin;
    private Stack stack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get().getCurrentTheme());
        setContentView(R.layout.learn_activity);
        getActionBar().hide();

        leftMargin = findViewById(R.id.left_margin_view);
        rightMargin = findViewById(R.id.right_margin_view);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        closeImageButton = (ImageView) findViewById(R.id.close_imageButton);
        closeImageButton.setOnClickListener(this);

        setStack(StackManager.get().getStack(getIntent().getExtras().getString("stack")));
        if (stack != null && savedInstanceState != null) {
            if (savedInstanceState.getString("stack").equals(stack.getName())) {
                viewPager.setCurrentItem(savedInstanceState.getInt("page"));
            }
        }

        leftMargin.setOnClickListener(this);
        rightMargin.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (stack != null) {
            savedInstanceState.putInt("page", viewPager.getCurrentItem());
            savedInstanceState.putString("stack", stack.getName());
        }
    }

    private void setStack(Stack stack) {
        this.stack = stack;
        learnPagerAdapter = new LearnPagerAdapter(viewPager, stack);
        viewPager.setAdapter(learnPagerAdapter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 16) {
            viewPager.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.close_imageButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stop Learn Session?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    onBackPressed();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        } else if (viewPager.getCurrentItem() > 0 && view.getId() == R.id.left_margin_view) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        } else if (view.getId() == R.id.right_margin_view) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_PREF_ANALYTICS_OPTOUT, false))
            EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_PREF_ANALYTICS_OPTOUT, false))
            EasyTracker.getInstance(this).activityStop(this);
    }

}
