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
package org.centum.android.stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cocosw.undobar.UndoBarController;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.events.StackManagerEvent;
import org.centum.android.model.events.StackManagerListener;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phani on 1/24/14.
 */
public class ArchivedStacksActivity extends Activity implements StackManagerListener, AdapterView.OnItemClickListener, SwipeDismissListViewTouchListener.DismissCallbacks {

    private ListView stackList;
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private ArchiveStackListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get(this).getCurrentTheme());
        setTitle(R.string.archived_stacks);
        setContentView(R.layout.stack_fragment);
        emptyTextView = (TextView) findViewById(R.id.empty_textView);
        emptyImageView = (ImageView) findViewById(R.id.empty_imageView);
        stackList = (ListView) findViewById(R.id.stack_list);

        stackList.setChoiceMode(ListView.CHOICE_MODE_NONE);
        emptyTextView.setText(R.string.no_archived_stacks);
        listAdapter = new ArchiveStackListAdapter(this);
        stackList.setAdapter(listAdapter);
        stackList.setOnItemClickListener(this);
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(stackList, this);
        stackList.setOnTouchListener(touchListener);
        stackList.setOnScrollListener(touchListener.makeScrollListener());

        StackManager.get().addListener(this);
        updateEmptyViews();
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
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.archive, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEmptyViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_restore_all:
                onRestoreAll();
                return true;
            case R.id.action_delete_all:
                onDeleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDeleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permanently_delete_all);
        builder.setMessage(R.string.permanently_delete_all_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StackManager.get().removeAllArchivedStacks();
                listAdapter.notifyDataSetChanged();
                dialogInterface.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void onRestoreAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirm));
        builder.setMessage(getString(R.string.restore_all_stacks));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StackManager.get().restoreAllArchivedStacks();
                dialogInterface.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void updateEmptyViews() {
        setEmptyViewsVisible(StackManager.get().getNumberOfArchivedStacks() == 0);
    }

    private void setEmptyViewsVisible(boolean b) {
        emptyImageView.setVisibility(b ? View.VISIBLE : View.GONE);
        emptyTextView.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.restore_stack);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StackManager.get().restoreArchivedStack(position);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void eventFired(StackManagerEvent evt) {
        updateEmptyViews();
    }

    @Override
    public boolean canDismiss(int position) {
        return true;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        final List<Stack> removedStacks = new ArrayList<Stack>();
        for (int position : reverseSortedPositions) {
            if (position < StackManager.get().getNumberOfArchivedStacks()) {
                removedStacks.add(StackManager.get().getArchivedStack(position));
            }
        }

        for (Stack stack : removedStacks) {
            StackManager.get().removeArchivedStack(stack);
        }

        UndoBarController.show(ArchivedStacksActivity.this, getResources().getString(R.string.stack_deleted), new UndoBarController.UndoListener() {
            @Override
            public void onUndo(Parcelable token) {
                for (Stack stack : removedStacks) {
                    StackManager.get().addArchiveStack(stack);
                }
            }
        }, UndoBarController.UNDOSTYLE);
    }
}
