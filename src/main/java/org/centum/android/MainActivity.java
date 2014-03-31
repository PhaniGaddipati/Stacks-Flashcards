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
package org.centum.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import org.centum.android.card.CardFragment;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.sql.StacksDatabaseHelper;
import org.centum.android.sql.StacksDatabaseSyncWatcher;
import org.centum.android.stack.R;
import org.centum.android.stack.StackFragment;
import org.centum.android.utils.KEYS;
import org.centum.android.utils.LoadedData;
import org.centum.android.utils.SampleStack;
import org.centum.android.utils.Serializer;

import java.util.List;

/**
 * The base Activity, this loads data on start up, displays relevent
 * start-up messages, and holds StackFragment and CardFragment.
 * <p/>
 * Created by Phani on 1/5/14.
 */
public class MainActivity extends Activity implements SlidingPaneLayout.PanelSlideListener {

    /**
     * Preference Constants
     */
    private static final int VERSION = 2;
    private static final long RATE_TIME_TO_WAIT = 1209600000; //2 weeks
    private static final long UPGRADE_TIME_TO_WAIT = 1814400000; // 2 weeks
    private static final int STATE_NO_PREF_RATE = 0;
    private static final int STATE_NO_RATE = 1;
    private static final int STATE_NO_PREF_UPGRADE = 0;
    private static final int STATE_NO_UPGRADE = 1;
    /**
     * Views amd fragments
     */
    private SlidingPaneLayout slidingPane;
    private FrameLayout adFrameLayout;
    private AdView adView;
    private StackFragment stackFragment;
    private CardFragment cardFragment;
    /**
     * Store whether data has already been loaded, to avoid re-load on rotates
     */
    private boolean dataLoaded = false;
    /**
     * DB classes
     */
    private StacksDatabaseHelper sqlHelper;
    private StacksDatabaseSyncWatcher syncWatcher;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setTheme(Themes.get(this).getCurrentTheme());
        sqlHelper = StacksDatabaseHelper.get(this);
        setContentView(R.layout.main_activity);

        adFrameLayout = (FrameLayout) findViewById(R.id.ad_frameLayout);

        if (!isProVersion()) {
            adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(KEYS.AD_UNIT_ID);
            adFrameLayout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(KEYS.TEST_DEVICE).build();
            adView.loadAd(adRequest);
            adView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            adView.setVisibility(View.VISIBLE);
                        }
                    });
                    adView.setVisibility(View.GONE);
                }
            });
        } else {
            adView = null;
            adFrameLayout.setVisibility(View.GONE);
        }

        slidingPane = (SlidingPaneLayout) findViewById(R.id.slidingPane);
        slidingPane.setCoveredFadeColor(getResources().getColor(android.R.color.transparent));
        slidingPane.setSliderFadeColor(getResources().getColor(android.R.color.transparent));
        slidingPane.setPanelSlideListener(this);

        stackFragment = (StackFragment) getFragmentManager().findFragmentById(R.id.stackFragment);
        cardFragment = (CardFragment) getFragmentManager().findFragmentById(R.id.cardFragment);

        setupUncaughtExceptionHandler();

        if (savedInstanceState != null) {
            dataLoaded = savedInstanceState.getBoolean("dataLoaded", false);

            if (savedInstanceState.containsKey("stack") && StackManager.get().getStack(savedInstanceState.getString("stack")) != null) {
                cardFragment.setStack(StackManager.get().getStack(savedInstanceState.getString("stack")));
            }

            if (savedInstanceState.getBoolean("stacksOpened")) {
                showStacks();
                onPanelOpened(null);
            } else {
                showCards();
                onPanelClosed(null);
            }
        } else {
            showStacks();
            onPanelOpened(null);
        }

        if (!dataLoaded || (StackManager.get().getArchivedStackList().size() == 0 &&
                StackManager.get().getStackList().size() == 0)) {
            loadData();
        }

        getSharedPreferences("properties", MODE_PRIVATE).edit().putInt("version", VERSION).apply();
    }

    /**
     * Check whether this is the Pro version
     *
     * @return
     */
    private boolean isProVersion() {
        //REMOVED
		return false;
    }

    /**
     * Load the stk data if the user is coming from an old version,
     * or load from the SQL db
     */
    private void loadData() {
        if (getSharedPreferences("properties", MODE_PRIVATE).getInt("version", 1) >= 2) {
            loadSQLData();
        } else {
            loadStkData();
        }
    }

    /**
     * Report uncaught exceptions (crashes) to Analytics
     */
    private void setupUncaughtExceptionHandler() {
        ExceptionReporter myHandler = new ExceptionReporter(EasyTracker.getInstance(this),
                GAServiceManager.getInstance(), Thread.getDefaultUncaughtExceptionHandler(), this);

        StandardExceptionParser exceptionParser =
                new StandardExceptionParser(getApplicationContext(), null) {
                    @Override
                    public String getDescription(String threadName, Throwable t) {
                        return "{" + threadName + "} " + "{" + Build.MODEL + "} " + "{" + Build.VERSION.SDK_INT + "} " + Log.getStackTraceString(t);
                    }
                };

        myHandler.setExceptionParser(exceptionParser);
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
    }

    /**
     * Load data from stacks.stk - the default save file in old version
     */
    private void loadStkData() {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;
            LoadedData loadedData;

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Upgrading Data");
                progressDialog.setMessage("Please wait");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
                syncWatcher = new StacksDatabaseSyncWatcher(sqlHelper);
            }

            @Override
            protected Void doInBackground(Void... params) {
                loadedData = Serializer.getInstance(MainActivity.this).loadData("stacks.stk");
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                List<Stack> archivedStacks = loadedData.getLoadedArchivedStacks();
                List<Stack> stacks = loadedData.getLoadedStacks();

                for (Stack stack : stacks) {
                    StackManager.get().addStack(stack);
                }
                for (Stack stack : archivedStacks) {
                    StackManager.get().addArchiveStack(stack);
                }

                stackFragment.updateEmptyViews();
                progressDialog.dismiss();
                dataLoaded = true;

                showTimedMessages();
            }
        }.execute();
    }

    /**
     * Load the SQL db and add it into the working model
     */
    private void loadSQLData() {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;
            LoadedData loadedData;

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Loading Data");
                progressDialog.setMessage("Please wait");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                loadedData = sqlHelper.loadStacks();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                List<Stack> archivedStacks = loadedData.getLoadedArchivedStacks();
                List<Stack> stacks = loadedData.getLoadedStacks();

                for (Stack stack : stacks) {
                    StackManager.get().addStack(stack);
                }
                for (Stack stack : archivedStacks) {
                    StackManager.get().addArchiveStack(stack);
                }

                stackFragment.updateEmptyViews();
                progressDialog.dismiss();
                dataLoaded = true;
                syncWatcher = new StacksDatabaseSyncWatcher(sqlHelper);
                showTimedMessages();
            }
        }.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("dataLoaded", dataLoaded);
        outState.putBoolean("stacksOpened", isStacksShowing());
        if (cardFragment.getStack() != null)
            outState.putString("stack", cardFragment.getStack().getName());
    }

    /**
     * Ask the user to rate the app or upgrade after a certain time,
     * and mark it so the message is never shown again
     */
    private void showTimedMessages() {
        if (getSharedPreferences("first_run", 0).getLong("first_date", 0) < 1) {
            getSharedPreferences("first_run", 0).edit().putLong("first_date", System.currentTimeMillis()).apply();
            onFirstRun();
        } else {
            long first_run = getSharedPreferences("first_run", 0).getLong("first_date", 0);
            int askStateRate = getSharedPreferences("first_run", 0).getInt("askState", STATE_NO_PREF_RATE);
            int askStateUpgrade = getSharedPreferences("first_run", 0).getInt("askStateUpgrade", STATE_NO_PREF_UPGRADE);

            if (askStateRate == STATE_NO_PREF_RATE) {
                if (System.currentTimeMillis() - first_run > RATE_TIME_TO_WAIT) {
                    new AlertDialog.Builder(this).setTitle("Rate").setMessage("Please rate/review this app!")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getSharedPreferences("first_run", 0).edit().putInt("askState", STATE_NO_RATE).apply();
                                    final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                                    final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);
                                    if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
                                        startActivity(rateAppIntent);
                                    }
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("Never", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getSharedPreferences("first_run", 0).edit().putInt("askState", STATE_NO_RATE).apply();
                                    dialogInterface.dismiss();
                                }
                            }).show();

                }
            }
            if (askStateUpgrade == STATE_NO_PREF_UPGRADE) {
                if (System.currentTimeMillis() - first_run > UPGRADE_TIME_TO_WAIT) {
                    new AlertDialog.Builder(this).setTitle("Upgrade").setMessage("Do you want to upgrade to remove all ads and support development?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getSharedPreferences("first_run", 0).edit().putInt("askStateUpgrade", STATE_NO_UPGRADE).apply();
                                    final Uri uri = Uri.parse("market://details?id=org.centum.android.stack.pro");
                                    final Intent upgradeAppIntent = new Intent(Intent.ACTION_VIEW, uri);
                                    if (getPackageManager().queryIntentActivities(upgradeAppIntent, 0).size() > 0) {
                                        startActivity(upgradeAppIntent);
                                    }
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("Never", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getSharedPreferences("first_run", 0).edit().putInt("askStateUpgrade", STATE_NO_UPGRADE).apply();
                                    dialogInterface.dismiss();
                                }
                            }).show();

                }
            }
        }
    }

    /**
     * Called if this app is run for the first time
     */
    private void onFirstRun() {
        if (StackManager.get().getNumberOfStacks() == 0) {
            addSampleStack();
            cardFragment.setStack(StackManager.get().getStack(0));
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
    public void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
        //Debug.stopMethodTracing();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
            adView.resume();
        }
        backupData();
        stackFragment.updateEmptyViews();
        //Debug.startMethodTracing("app");
    }

    /**
     * Check whether if the settings indicates whether data should be backed up,
     * and backs up if enough time has passed
     */
    private void backupData() {
        String backupTimeS = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_PREF_BACKUP_TIME, "12");
        long backupTime;
        try {
            if (Double.parseDouble(backupTimeS) < 0) {
                backupTime = Long.MAX_VALUE;
            } else if (Double.parseDouble(backupTimeS) < 1) {
                backupTime = 3600000;
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(SettingsActivity.KEY_PREF_BACKUP_TIME, "1").commit();
            } else {
                backupTime = (long) (Double.parseDouble(backupTimeS) * 3600000); //hours to ms
            }
        } catch (NumberFormatException ex) {
            backupTime = 12 * 3600000;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(SettingsActivity.KEY_PREF_BACKUP_TIME, "12").commit();
        }
        if ((System.currentTimeMillis() - getSharedPreferences("backup", 0).getLong("lastBackup", 0)) > backupTime) {
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("Backing up");
                    progressDialog.setMessage("Change the backup frequency in Settings");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                }

                @Override
                protected void onPostExecute(Void result) {
                    progressDialog.dismiss();
                    Log.d("MainActivity", "Backed data up");
                    getSharedPreferences("backup", 0).edit().putLong("lastBackup", System.currentTimeMillis()).commit();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    Serializer.getInstance(MainActivity.this).backup();
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public void addSampleStack() {
        if (!StackManager.get().containsStack("Organic Chemistry") && StackManager.get().getNumberOfArchivedStacks() == 0) {
            Stack chemStack = SampleStack.getSampleStack();
            StackManager.get().addStack(chemStack);
        }
    }

    @Override
    public void onPanelSlide(View view, float v) {
        cardFragment.updateColumns();
    }

    @Override
    public void onPanelOpened(View view) {
        stackFragment.setHasOptionsMenu(true);
        cardFragment.setHasOptionsMenu(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        stackFragment.updateEmptyViews();
        setTitle("Stacks");
        //if (adView != null)
        //    adView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPanelClosed(View view) {
        stackFragment.setHasOptionsMenu(false);
        cardFragment.setHasOptionsMenu(true);
        if (cardFragment.getStack() != null) {
            setTitle(cardFragment.getStack().getName());
        }
        cardFragment.updateColumns();
        stackFragment.updateEmptyViews();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //if (adView != null)
        //    adView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showStacks();
            return true;
        }
        return false;
    }

    public void showStacks() {
        slidingPane.openPane();
    }

    public void showCards() {
        slidingPane.closePane();
    }

    public boolean isCardsShowing() {
        return !slidingPane.isOpen();
    }

    public boolean isStacksShowing() {
        return slidingPane.isOpen();
    }

    public StackFragment getStackFragment() {
        return stackFragment;
    }

    public CardFragment getCardFragment() {
        return cardFragment;
    }

    @Override
    public void onBackPressed() {
        if (isCardsShowing()) {
            showStacks();
        } else {
            super.onBackPressed();
        }
    }
}

