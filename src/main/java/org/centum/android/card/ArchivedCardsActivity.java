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
package org.centum.android.card;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.apps.dashclock.ui.SwipeDismissGridViewTouchListener;

import org.centum.android.events.StackEvent;
import org.centum.android.events.StackListener;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.sql.StacksDatabaseHelper;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/24/14.
 */
public class ArchivedCardsActivity extends Activity implements StackListener, AdapterView.OnItemClickListener {

    private GridView cardList;
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private ArchiveCardListAdapter cardListAdapter;
    private Stack stack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get().getCurrentTheme());
        setTitle(getString(R.string.archived_cards));
        setContentView(R.layout.card_fragment);
        emptyTextView = (TextView) findViewById(R.id.empty_textView);
        emptyImageView = (ImageView) findViewById(R.id.empty_imageView);
        cardList = (GridView) findViewById(R.id.card_list);
        emptyTextView.setText("You have no archived cards");
        stack = StackManager.get().getStack(getIntent().getExtras().getString("stack"));
        stack.addListener(this);

        ViewTreeObserver viewTreeObserver = cardList.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateColumns();
                }
            });
        }

        cardListAdapter = new ArchiveCardListAdapter(this, stack);
        cardList.setAdapter(cardListAdapter);
        cardList.setOnItemClickListener(this);
        final ArchivedCardsActivity ctx = this;
        SwipeDismissGridViewTouchListener touchListener =
                new SwipeDismissGridViewTouchListener(cardList,
                        new SwipeDismissGridViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            public void onDismiss(GridView gridView, final int[] reverseSortedPositions) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                builder.setTitle("Permanently Delete?");
                                builder.setMessage("This cannot be undone!");
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        for (int position : reverseSortedPositions) {
                                            stack.removeArchivedCard(position);
                                        }
                                        cardListAdapter.notifyDataSetChanged();
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
        cardList.setOnTouchListener(touchListener);
        cardList.setOnScrollListener(touchListener.makeScrollListener());
        updateColumns();
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
        builder.setMessage("All archived Cards will be deleted! This cannot be undone!");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        stack.removeAllArchivedCards();
                    }
                };
                StacksDatabaseHelper.get(null).executeBulkOperation(runnable);
                cardListAdapter.notifyDataSetChanged();
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
        builder.setMessage("Restore all archived Cards?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        stack.restoreAllArchivedCards();
                    }
                };
                StacksDatabaseHelper.get(null).executeBulkOperation(runnable);
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
    public void onResume() {
        super.onResume();
        updateEmptyViews();
        updateColumns();
    }

    public void updateColumns() {
        float density = getResources().getDisplayMetrics().density;
        if (cardList.getWidth() > Math.round((float) 450 * density)) {
            cardList.setNumColumns(Math.round(cardList.getWidth() / (450 * density)));
        } else {
            cardList.setNumColumns(1);
        }
    }

    private void updateEmptyViews() {
        setEmptyViewsVisible(stack.getNumberOfArchivedCards() == 0);
    }

    private void setEmptyViewsVisible(boolean b) {
        emptyImageView.setVisibility(b ? View.VISIBLE : View.GONE);
        emptyTextView.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Restore Card?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stack.restoreArchivedCard(position);
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
    public void eventFired(StackEvent evt) {
        updateEmptyViews();
    }
}
