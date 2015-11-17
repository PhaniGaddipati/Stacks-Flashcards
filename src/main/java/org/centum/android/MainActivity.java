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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import org.centum.android.card.CardFragment;
import org.centum.android.integration.communicators.QuizletCommunicator;
import org.centum.android.integration.communicators.StudyStackCommunicator;
import org.centum.android.model.LoadedData;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.sql.StacksDatabaseHelper;
import org.centum.android.sql.StacksDatabaseLoadAsyncTask;
import org.centum.android.sql.StacksDatabaseSyncWatcher;
import org.centum.android.stack.R;
import org.centum.android.stack.StackFragment;
import org.centum.android.utils.Clipboard;
import org.centum.android.utils.KEYS;
import org.centum.android.utils.SampleStack;

/**
 * The base Activity, this loads data on start up, displays relevent
 * start-up messages, and holds StackFragment and CardFragment.
 * <p/>
 * Created by Phani on 1/5/14.
 */
public class MainActivity extends Activity implements SlidingPaneLayout.PanelSlideListener {

    private static final int VERSION = 2;
    private static final long RATE_TIME_TO_WAIT = 2419200000L; //4 weeks
    private static final long UPGRADE_TIME_TO_WAIT = 3024000000L; // 5 weeks
    private static final int STATE_NO_PREF_RATE = 0;
    private static final int STATE_NO_RATE = 1;
    private static final int STATE_NO_PREF_UPGRADE = 0;
    private static final int STATE_NO_UPGRADE = 1;

    private boolean isMultiPane = false;
    private SlidingPaneLayout slidingPane;
    private FrameLayout adFrameLayout;
    private AdView adView;
    private ProgressBar progressBar;
    private StackFragment stackFragment;
    private CardFragment cardFragment;

    private StacksDatabaseHelper sqlHelper;
    private StacksDatabaseSyncWatcher syncWatcher = null;
    private boolean dataLoaded = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setTheme(Themes.get(this).getCurrentTheme());
        sqlHelper = StacksDatabaseHelper.get(this);
        QuizletCommunicator.init(this);
        StudyStackCommunicator.init(this);

        setContentView(R.layout.main_activity);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        adFrameLayout = (FrameLayout) findViewById(R.id.ad_frameLayout);
        slidingPane = (SlidingPaneLayout) findViewById(R.id.slidingPane);

        if (slidingPane == null) {
            isMultiPane = true;
        } else {
            isMultiPane = false;
            slidingPane.setCoveredFadeColor(getResources().getColor(android.R.color.transparent));
            slidingPane.setSliderFadeColor(getResources().getColor(android.R.color.transparent));
            slidingPane.setPanelSlideListener(this);
        }
        progressBar.setVisibility(View.GONE);

        stackFragment = (StackFragment) getFragmentManager().findFragmentById(R.id.stackFragment);
        cardFragment = (CardFragment) getFragmentManager().findFragmentById(R.id.right_button);

        configureAdView();
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

        if ((StackManager.get().getArchivedStackList().size() == 0 &&
                StackManager.get().getStackList().size() == 0)) {
            loadSQLData();
        }
        //new ReleaseNotes(this).showReleaseNotesWithoutRepeat();
        getSharedPreferences("properties", MODE_PRIVATE).edit().putInt("version", VERSION).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        if (!isMultiPane) {
            return false;
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stack_card_multi, menu);
        stackFragment.setSearchMenuItem(menu.findItem(R.id.action_search_stacks));
        cardFragment.setSearchMenuItem(menu.findItem(R.id.action_search_cards));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isMultiPane) {
            return false;
        }
        if (!Clipboard.get().hasContents()) {
            menu.removeItem(R.id.action_paste_card);
        } else if (menu.findItem(R.id.action_paste_card) == null) {
            menu.add(Menu.NONE, R.id.action_paste_card, Menu.NONE, android.R.string.paste);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showStacks();
            return true;
        }
        if (stackFragment.onOptionsItemSelected(item)) {
            return true;
        }
        if (cardFragment.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureAdView() {
        if (!isProVersion()) {
            adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(KEYS.AD_UNIT_ID);
            adFrameLayout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(KEYS.TEST_DEVICE_1)
                    .addTestDevice(KEYS.TEST_DEVICE_2)
                    .build();
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
    }

    /**
     * Check whether this is the Pro version
     *
     * @return
     */
    private boolean isProVersion() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("org.centum.android.stack.pro", PackageManager.GET_META_DATA);
        } catch (Exception e) {
            return false;
        }
        return true;
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
     * Load the SQL db and add it into the working model
     */
    private void loadSQLData() {
        new StacksDatabaseLoadAsyncTask(this) {

            @Override
            public void onPreExecute() {
                if (!isMultiPane)
                    slidingPane.setVisibility(View.GONE);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPostExecute(LoadedData loadedData) {
                StackManager.get().setData(loadedData);

                stackFragment.updateEmptyViews();
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.GONE);
                if (!isMultiPane)
                    slidingPane.setVisibility(View.VISIBLE);
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
                    //showRateMessage();
                }
            }
            if (askStateUpgrade == STATE_NO_PREF_UPGRADE) {
                if (System.currentTimeMillis() - first_run > UPGRADE_TIME_TO_WAIT) {
                    showUpgradeMessage();

                }
            }
        }
    }

    private void showUpgradeMessage() {
        new AlertDialog.Builder(this).setTitle(R.string.upgrade).setMessage(R.string.upgrade_message)
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
                .setNegativeButton(R.string.never, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSharedPreferences("first_run", 0).edit().putInt("askStateUpgrade", STATE_NO_UPGRADE).apply();
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void showRateMessage() {
        new AlertDialog.Builder(this).setTitle(R.string.rate).setMessage(R.string.rate_message)
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
                .setNegativeButton(R.string.never, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSharedPreferences("first_run", 0).edit().putInt("askState", STATE_NO_RATE).apply();
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    /**
     * Called if this app is run for the first time
     */
    private void onFirstRun() {
        if (StackManager.get().getNumberOfStacks() == 0) {
            addSampleStack();
            //cardFragment.setStack(StackManager.get().getStack(0));
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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
            adView.resume();
        }
        //backupDataIfAutomaticScheduled();
        stackFragment.updateEmptyViews();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public void addSampleStack() {
        Stack sampleStack = SampleStack.getNewSampleStack();
        if (!StackManager.get().containsStack(sampleStack) && StackManager.get().getNumberOfArchivedStacks() == 0) {
            StackManager.get().addStack(sampleStack);
        }
    }

    @Override
    public void onPanelSlide(View view, float v) {
        cardFragment.updateColumns();
    }

    @Override
    public void onPanelOpened(View view) {
        if (!isMultiPane) {
            stackFragment.setHasOptionsMenu(true);
            cardFragment.setHasOptionsMenu(false);
            stackFragment.updateEmptyViews();
            setTitle(R.string.app_name_short);
            if (getActionBar() != null)
                getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onPanelClosed(View view) {
        if (!isMultiPane) {
            stackFragment.setHasOptionsMenu(false);
            cardFragment.setHasOptionsMenu(true);
            if (cardFragment.getStack() != null) {
                setTitle(cardFragment.getStack().getName());
            } else {
                setTitle(R.string.cards);
            }
            cardFragment.updateColumns();
            stackFragment.updateEmptyViews();
            if (getActionBar() != null)
                getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void showStacks() {
        if (!isMultiPane) slidingPane.openPane();
    }

    public void showCards() {
        if (!isMultiPane) slidingPane.closePane();
    }

    public boolean isCardsShowing() {
        return isMultiPane || !slidingPane.isOpen();
    }

    public boolean isStacksShowing() {
        return isMultiPane || slidingPane.isOpen();
    }

    public StackFragment getStackFragment() {
        return stackFragment;
    }

    public CardFragment getCardFragment() {
        return cardFragment;
    }

    @Override
    public void onBackPressed() {
        if (isCardsShowing() && !isMultiPane) {
            showStacks();
        } else {
            super.onBackPressed();
        }
    }
}

