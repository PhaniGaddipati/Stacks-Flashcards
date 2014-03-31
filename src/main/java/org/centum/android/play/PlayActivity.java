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
package org.centum.android.play;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import org.centum.android.events.PlaySessionEvent;
import org.centum.android.events.PlaySessionListener;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.play.PlaySession;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/7/14.
 */
public class PlayActivity extends Activity implements ViewPager.OnPageChangeListener, View.OnClickListener, PlaySessionListener {

    private Stack stack = null;
    private ViewPager viewPager;
    private PlayProgressView progressBar;
    private FrameLayout frameLayout;
    private ImageView closeImageButton;
    private PlayPagerAdapter pagerAdapter;
    private PlaySession playSession;
    private View rightMargin;
    private View leftMargin;
    private TextView timeTextView;
    private long startTime, pauseTime;
    private boolean timePaused = false;
    private Thread timerThread;
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get().getCurrentTheme());
        setContentView(R.layout.play_activity);
        getActionBar().hide();

        leftMargin = findViewById(R.id.left_margin_view);
        rightMargin = findViewById(R.id.right_margin_view);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        closeImageButton = (ImageView) findViewById(R.id.close_imageButton);
        timeTextView = (TextView) findViewById(R.id.time_textView);

        closeImageButton.setOnClickListener(this);
        leftMargin.setOnClickListener(this);
        rightMargin.setOnClickListener(this);
        timeTextView.setOnClickListener(this);

        try {
            stack = StackManager.get().getStack(getIntent().getExtras().getString("stack"));
            String session = getIntent().getExtras().getString("playsession");
            setStack(stack, session);

            if (savedInstanceState != null) {
                if (savedInstanceState.getString("stack").equals(stack.getName())) {
                    viewPager.setCurrentItem(savedInstanceState.getInt("page"));
                    startTime = savedInstanceState.getLong("startTime");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (stack != null) {
            savedInstanceState.putInt("page", viewPager.getCurrentItem());
            savedInstanceState.putString("stack", stack.getName());
            savedInstanceState.putLong("startTime", startTime);
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
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }


    public void setStack(Stack stack, String session) {
        this.stack = stack;
        if (stack != null) {
            playSession = stack.getPlaySession(session);
            progressBar = new PlayProgressView(this, stack.getNumberOfCards());
            progressBar.setPlayProgressListener(new PlayProgressListener() {
                @Override
                public void segmentTapped(int pos) {
                    scrollTo(pos);
                }
            });

            viewPager.setOnPageChangeListener(this);
            pagerAdapter = new PlayPagerAdapter(this, stack, playSession);
            viewPager.setAdapter(pagerAdapter);

            frameLayout.removeAllViews();
            frameLayout.addView(progressBar);

            playSession.addListener(this);

            for (Card c : stack.getCards()) {
                progressBar.setState(stack.getCardPosition(c), playSession.getSessionStat(c));
            }
        }
        startTime = System.currentTimeMillis();
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    if (startTime != 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updatedTimeText();
                            }
                        });
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        //Expected when the activity stops
                    }
                }
            }
        });
        timerThread.start();
    }

    private void updatedTimeText() {
        if (timePaused) {
            timeTextView.setText("Paused");
        } else {
            long time = System.currentTimeMillis();
            int seconds = (int) (time - startTime) / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            String minutesS, secondsS;
            if (minutes < 10) {
                minutesS = "0" + minutes;
            } else {
                minutesS = minutes + "";
            }
            if (seconds < 10) {
                secondsS = "0" + seconds;
            } else {
                secondsS = seconds + "";
            }
            timeTextView.setText(minutesS + ":" + secondsS);
        }
    }

    public void scrollTo(int i) {
        viewPager.setCurrentItem(i, true);
    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        for (int c = 0; c < viewPager.getChildCount(); c++) {
            if (c != i) {
                if (viewPager.getChildAt(c) != null && viewPager.getChildAt(c) instanceof PlayCardView)
                    ((PlayCardView) viewPager.getChildAt(c)).setDetailsVisible(false);
            }
        }
        progressBar.setCurrentSlot(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

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
        switch (view.getId()) {
            case R.id.close_imageButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("End Session");
                builder.setMessage("Are you sure you want to end this session early?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        onBackPressed();
                    }
                });

                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                break;
            case R.id.right_margin_view:
                if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }
                break;
            case R.id.left_margin_view:
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                }
                break;
            case R.id.time_textView:
                if (!timePaused) {
                    pauseTime = System.currentTimeMillis();
                    timePaused = true;
                    updatedTimeText();
                } else {
                    timePaused = false;
                    startTime += (System.currentTimeMillis() - pauseTime);
                    updatedTimeText();
                }
                break;
        }

    }

    @Override
    public void eventFired(PlaySessionEvent evt) {
        if (evt.getTarget() != null)
            progressBar.setState(stack.getCardPosition(evt.getTarget()), evt.getSource().getSessionStat(evt.getTarget()));
    }
}

