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
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cocosw.undobar.UndoBarController;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.play.PlaySession;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phani on 1/27/14.
 */
public class SessionActivity extends Activity implements View.OnClickListener {

    private ListView listView;
    private SessionListAdapter sessionListAdapter;
    private Stack stack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get().getCurrentTheme());
        setContentView(R.layout.session_dialog_activity);

        listView = (ListView) findViewById(R.id.session_listView);
        listView.setDivider(null);
        listView.setAdapter(sessionListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((SessionView) view).setChecked(!((SessionView) view).isChecked());
            }
        });
        final Activity ctx = this;
        SwipeDismissListViewTouchListener dismissListener
                = new SwipeDismissListViewTouchListener(listView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                final List<PlaySession> removedPlaySessions = new ArrayList<PlaySession>();
                for (int position : reverseSortedPositions) {
                    removedPlaySessions.add(stack.getPlaySession(position));
                }

                for (PlaySession playSession : removedPlaySessions) {
                    stack.removePlaySession(playSession);
                }

                sessionListAdapter.notifyDataSetChanged();
                UndoBarController.show(ctx, "Session removed.", new UndoBarController.UndoListener() {
                    @Override
                    public void onUndo(Parcelable token) {
                        for (PlaySession playSession : removedPlaySessions) {
                            stack.addPlaySession(playSession);
                        }
                        sessionListAdapter.notifyDataSetChanged();
                    }
                }, UndoBarController.UNDOSTYLE);
            }
        });
        listView.setOnTouchListener(dismissListener);
        listView.setOnScrollListener(dismissListener.makeScrollListener());

        Stack stack = StackManager.get().getStack(getIntent().getExtras().getString("stack"));
        setStack(stack);
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

    private void setStack(Stack stack) {
        this.stack = stack;
        updateAdapter();
    }

    private void updateAdapter() {
        sessionListAdapter = new SessionListAdapter(this, R.layout.session_list_item, stack);
        if (listView != null) {
            listView.setAdapter(sessionListAdapter);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {

    }

}
