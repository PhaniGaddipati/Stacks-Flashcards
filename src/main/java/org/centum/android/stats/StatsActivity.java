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
package org.centum.android.stats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/21/14.
 */
public class StatsActivity extends Activity {

    private GridView statsGridView;
    private StatsListAdapter listAdapter;
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private Stack stack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get().getCurrentTheme());
        setContentView(R.layout.stats_activity);

        statsGridView = (GridView) findViewById(R.id.stats_list);
        emptyTextView = (TextView) findViewById(R.id.textView);
        emptyImageView = (ImageView) findViewById(R.id.imageView);

        ViewTreeObserver viewTreeObserver = statsGridView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateColumns();
                }
            });
        }

        String stackName = getIntent().getExtras().getString("stack");
        stack = StackManager.get().getStack(stackName);

        if (stack != null && stack.getNumberOfPlaySessions() > 0) {
            setTitle(stackName + " Stats");
            listAdapter = new StatsListAdapter(this, R.layout.stats_card_item, stack, stack.getPlayStatsWithEnabledSessions());
            statsGridView.setAdapter(listAdapter);
            emptyImageView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            emptyImageView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stats, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_edit_sessions:
                editSessionsActionPerformed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapter();
        updateColumns();
    }

    public void updateColumns() {
        float density = getResources().getDisplayMetrics().density;
        if (statsGridView.getWidth() > Math.round((float) 400 * density)) {
            statsGridView.setNumColumns(Math.round(statsGridView.getWidth() / (400 * density)));
        } else {
            statsGridView.setNumColumns(1);
        }
    }


    private void editSessionsActionPerformed() {
        if (stack != null && stack.getNumberOfPlaySessions() > 0) {
            Intent intent = new Intent(this, SessionActivity.class);
            intent.putExtra("stack", stack.getName());
            startActivity(intent);
        }
    }

    private void updateAdapter() {
        if (listAdapter != null && stack != null)
            listAdapter.setPlayStats(stack.getPlayStatsWithEnabledSessions());
    }

}
