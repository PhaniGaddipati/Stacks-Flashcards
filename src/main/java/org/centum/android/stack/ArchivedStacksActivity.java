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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

import org.centum.android.events.StackManagerEvent;
import org.centum.android.events.StackManagerListener;
import org.centum.android.model.StackManager;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;

/**
 * Created by Phani on 1/24/14.
 */
public class ArchivedStacksActivity extends Activity implements StackManagerListener, AdapterView.OnItemClickListener {

    private ListView stackList;
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private ArchiveStackListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get().getCurrentTheme());
        setTitle(getString(R.string.archived_stacks));
        setContentView(R.layout.stack_fragment);
        emptyTextView = (TextView) findViewById(R.id.empty_textView);
        emptyImageView = (ImageView) findViewById(R.id.empty_imageView);
        stackList = (ListView) findViewById(R.id.stack_list);

        stackList.setChoiceMode(ListView.CHOICE_MODE_NONE);
        emptyTextView.setText("You have no archived stacks");
        listAdapter = new ArchiveStackListAdapter(this);
        stackList.setAdapter(listAdapter);
        stackList.setOnItemClickListener(this);
        final Context ctx = this;
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(stackList,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            public void onDismiss(ListView listView, final int[] reverseSortedPositions) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                builder.setTitle("Permanently Delete?");
                                builder.setMessage("This cannot be undone!");
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        for (int position : reverseSortedPositions) {
                                            StackManager.get().removeArchivedStack(position);
                                        }
                                        listAdapter.notifyDataSetChanged();
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.show();

                            }
                        }
                );
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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_restore_all) {
            onRestoreAll();
            return true;
        } else if (item.getItemId() == R.id.action_delete_all) {
            onDeleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDeleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permanently Delete?");
        builder.setMessage("All archived Stacks will be deleted! This cannot be undone!");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StackManager.get().removeAllArchivedStacks();
                listAdapter.notifyDataSetChanged();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void onRestoreAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Restore all archived Stacks?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StackManager.get().restoreAllArchivedStacks();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
        builder.setTitle("Confirm");
        builder.setMessage("Restore Stack?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StackManager.get().restoreArchivedStack(position);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
}
