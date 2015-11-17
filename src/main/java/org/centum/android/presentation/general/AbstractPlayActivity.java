package org.centum.android.presentation.general;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.events.PlaySessionEvent;
import org.centum.android.model.events.PlaySessionListener;
import org.centum.android.model.play.PlaySession;
import org.centum.android.model.play.SessionSettings;
import org.centum.android.presentation.play.PlayCardView;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 4/2/2014.
 */
public abstract class AbstractPlayActivity extends Activity implements ViewPager.OnPageChangeListener, View.OnClickListener, PlaySessionListener {

    private final Handler handler = new Handler();
    private Stack stack = null;
    private NonSwipeableViewPager viewPager;
    private PlayProgressView progressBar;
    private FrameLayout frameLayout;
    private ImageView closeImageButton;
    private PlaySession playSession;
    private SessionSettings sessionSettings = new SessionSettings();
    private View rightMargin;
    private View leftMargin;
    private TextView timeTextView;
    private long stackStartTime, pauseTime, cardStartTime;
    private boolean timePaused = false;
    private boolean stackEndedShown = false;
    private boolean immersive = true;
    private Thread timerThread;
    private boolean immersivePref = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get(this).getCurrentTheme());
        setContentView(R.layout.play_activity);
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        leftMargin = findViewById(R.id.left_margin_view);
        rightMargin = findViewById(R.id.right_margin_view);
        viewPager = (NonSwipeableViewPager) findViewById(R.id.viewPager);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        closeImageButton = (ImageView) findViewById(R.id.close_imageButton);
        timeTextView = (TextView) findViewById(R.id.time_textView);

        closeImageButton.setOnClickListener(this);
        leftMargin.setOnClickListener(this);
        rightMargin.setOnClickListener(this);
        timeTextView.setOnClickListener(this);

        cardStartTime = System.currentTimeMillis();

        try {
            stack = StackManager.get().getStack(getIntent().getExtras().getString("stack"));
            String session = getIntent().getExtras().getString("playsession");
            setStack(stack, session);

            if (savedInstanceState != null) {
                if (savedInstanceState.getString("stack").equals(stack.getName())) {
                    viewPager.setCurrentItem(savedInstanceState.getInt("page"));
                    stackStartTime = savedInstanceState.getLong("stackStartTime");
                    cardStartTime = savedInstanceState.getLong("cardStartTime");
                    stackEndedShown = savedInstanceState.getBoolean("stackEndedShown");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        immersivePref = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_PREF_USE_IMMERSIVE, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!timePaused) {
            pauseTime = System.currentTimeMillis();
            timePaused = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (timePaused) {
            timePaused = false;
            stackStartTime += (System.currentTimeMillis() - pauseTime);
            cardStartTime += (System.currentTimeMillis() - pauseTime);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (stack != null) {
            savedInstanceState.putInt("page", viewPager.getCurrentItem());
            savedInstanceState.putString("stack", stack.getName());
            savedInstanceState.putLong("stackStartTime", stackStartTime);
            savedInstanceState.putLong("cardStartTime", cardStartTime);
            savedInstanceState.putBoolean("stackEndedShown", stackEndedShown);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_PREF_ANALYTICS_OPTOUT, false))
            EasyTracker.getInstance(this).activityStart(this);
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        if (stackStartTime != 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateTime();
                                }
                            });
                        }

                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    //Expected when the activity stops
                }
            }
        });
        timerThread.start();
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
            sessionSettings = playSession.getSessionSettings();
            onStackSet();
            viewPager.setPagingEnabled(sessionSettings.isFreeNavigationEnabled());
            progressBar = new PlayProgressView(this, stack.getNumberOfCards());
            progressBar.setPlayProgressListener(new PlayProgressListener() {
                @Override
                public void segmentTapped(int pos) {
                    scrollTo(pos);
                }
            });

            viewPager.setOnPageChangeListener(this);
            viewPager.setAdapter(getPagerAdapter());

            frameLayout.removeAllViews();
            frameLayout.addView(progressBar);

            playSession.addListener(this);

            for (Card c : stack.getCardList()) {
                progressBar.setState(stack.getCardPosition(c), playSession.getSessionStat(c));
            }
        }
        stackStartTime = System.currentTimeMillis();
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }

    protected abstract void onStackSet();

    private synchronized void updateTime() {
        if (timePaused) {
            timeTextView.setText("Paused");
        } else {
            long time = System.currentTimeMillis();
            int stackSeconds = (int) (time - stackStartTime) / 1000;
            int cardSeconds = (int) (time - cardStartTime) / 1000;

            if (sessionSettings.isCountdownStack()) {
                stackSeconds = sessionSettings.getStackSecondsLimit() - stackSeconds;
                if (stackSeconds <= 0) {
                    onStackTimeLimitReached();
                }
            }
            if (sessionSettings.isCountdownCard()) {
                cardSeconds = sessionSettings.getCardSecondsLimit() - cardSeconds;
                if (cardSeconds <= 0) {
                    onCardTimeLimitReached();
                }
            }

            timeTextView.setText((sessionSettings.isCountdownCard() ? "Stack:  " : "") + getFormattedSeconds(stackSeconds)
                    + (sessionSettings.isCountdownCard() ? ("\t \t \t \t \t \t \tCard:  " + getFormattedSeconds(cardSeconds)) : ""));
        }
    }

    private String getFormattedSeconds(int seconds) {
        boolean negative = false;
        if (seconds < 0) {
            negative = true;
            seconds = -1 * seconds;
        }
        int minutes = seconds / 60;
        seconds = seconds % 60;
        String minutesS, secondsS;
        minutesS = minutes + "";
        if (seconds < 10) {
            secondsS = "0" + seconds;
        } else {
            secondsS = seconds + "";
        }
        return ((negative ? "-" : "") + minutesS + ":" + secondsS);
    }

    private void onStackTimeLimitReached() {
        if (!stackEndedShown) {
            stackEndedShown = true;
            pauseTime = System.currentTimeMillis();
            timePaused = true;
            new AlertDialog.Builder(this).setTitle("Time up!").setMessage("Continue anyway?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            timePaused = false;
                            stackStartTime += (System.currentTimeMillis() - pauseTime);
                            cardStartTime += (System.currentTimeMillis() - pauseTime);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            timePaused = false;
                            stackStartTime += (System.currentTimeMillis() - pauseTime);
                            cardStartTime += (System.currentTimeMillis() - pauseTime);
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }
    }

    private void onCardTimeLimitReached() {
        if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }
    }

    public void scrollTo(int i) {
        if (viewPager.isSwipingEnabled())
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
        cardStartTime = System.currentTimeMillis();
        updateTime();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 16 && immersive && immersivePref) {
            hideSystemUI();
        }
    }

    protected void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= 16) {
            viewPager.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
            );
        } else {
            viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
                        finish();
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
                if (sessionSettings.isFreeNavigationEnabled()
                        && PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(SettingsActivity.KEY_PREF_PLAY_MARGIN_ADVANCE, true))
                    if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    }
                break;
            case R.id.left_margin_view:
                if (sessionSettings.isFreeNavigationEnabled()
                        && PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(SettingsActivity.KEY_PREF_PLAY_MARGIN_ADVANCE, true))
                    if (viewPager.getCurrentItem() > 0) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                    }
                break;
            case R.id.time_textView:
                if (!timePaused) {
                    pauseTime = System.currentTimeMillis();
                    timePaused = true;
                    updateTime();
                } else {
                    timePaused = false;
                    stackStartTime += (System.currentTimeMillis() - pauseTime);
                    cardStartTime += (System.currentTimeMillis() - pauseTime);
                    updateTime();
                }
                break;
        }

    }

    @Override
    public void eventFired(PlaySessionEvent evt) {
        if (evt.getTarget() != null)
            progressBar.setState(stack.getCardPosition(evt.getTarget()), evt.getSource().getSessionStat(evt.getTarget()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public Stack getStack() {
        return stack;
    }

    public PlaySession getPlaySession() {
        return playSession;
    }

    public SessionSettings getSessionSettings() {
        return sessionSettings;
    }

    public abstract PagerAdapter getPagerAdapter();

    public void setImmersive(boolean immersive) {
        this.immersive = immersive;
    }
}
