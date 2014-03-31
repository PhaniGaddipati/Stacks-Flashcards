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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;
import org.centum.android.utils.AttachmentHandler;

import java.io.IOException;

/**
 * Created by Phani on 1/13/14.
 */
public class PlayCardView extends RelativeLayout implements View.OnClickListener {

    private static final float DISABLED_ALPHA = .25f;
    private static final float ENABLED_ALPHA = 1.0f;
    private Card card = null;
    private Stack stack = null;
    private RelativeLayout playRelativeLayout;
    private TextView titleTextView;
    private TextView detailsTextView;
    private ImageView imageView;
    private ImageButton correctBtn;
    private ImageButton wrongBtn;
    private ScrollView detailsScrollView;
    private boolean detailsVisible = false;
    private boolean correctSelected = false;
    private boolean wrongSelected = false;
    private ObjectAnimator detailsAnimator;
    private ObjectAnimator correctAnimator;
    private ObjectAnimator wrongAnimator;
    private PlaySession playSession;
    private int answer = PlaySession.ANS_NONE;
    private AsyncTask<Void, Void, Void> imageLoaderTask;
    private boolean autoAdvance = false;

    public PlayCardView(Context context) {
        super(context);
    }

    public PlayCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        titleTextView = (TextView) findViewById(R.id.title_textView);
        detailsTextView = (TextView) findViewById(R.id.details_textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        correctBtn = (ImageButton) findViewById(R.id.correct_imageButton);
        wrongBtn = (ImageButton) findViewById(R.id.wrong_imageButton);
        playRelativeLayout = (RelativeLayout) findViewById(R.id.play_relativelayout);
        detailsScrollView = (ScrollView) findViewById(R.id.details_scrollView);

        int titleTextSize = 30;
        int detailsTextSize = 22;
        try {
            titleTextSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsActivity.KEY_PREF_PLAY_TITLE_SIZE, "30"));
        } catch (Exception e) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsActivity.KEY_PREF_PLAY_TITLE_SIZE, "30").commit();
        }
        try {
            detailsTextSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsActivity.KEY_PREF_PLAY_DETAILS_SIZE, "22"));
        } catch (Exception e) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsActivity.KEY_PREF_PLAY_DETAILS_SIZE, "22").commit();
        }

        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleTextSize);
        detailsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, detailsTextSize);

        autoAdvance = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(SettingsActivity.KEY_PREF_PLAY_AUTO_ADVANCE, false);

        detailsAnimator = new ObjectAnimator();
        detailsAnimator.setTarget(detailsTextView);
        detailsAnimator.setPropertyName("alpha");
        detailsAnimator.setDuration(100);
        detailsAnimator.setInterpolator(new LinearInterpolator());

        correctAnimator = new ObjectAnimator();
        correctAnimator.setTarget(correctBtn);
        correctAnimator.setPropertyName("alpha");
        correctAnimator.setDuration(100);
        correctAnimator.setInterpolator(new LinearInterpolator());

        wrongAnimator = new ObjectAnimator();
        wrongAnimator.setTarget(wrongBtn);
        wrongAnimator.setPropertyName("alpha");
        wrongAnimator.setDuration(100);
        wrongAnimator.setInterpolator(new LinearInterpolator());

        setThemeParams();

        detailsScrollView.setOnClickListener(this);
        detailsTextView.setOnClickListener(this);
        playRelativeLayout.setOnClickListener(this);
        correctBtn.setOnClickListener(this);
        wrongBtn.setOnClickListener(this);
        imageView.setOnClickListener(this);
        setOnClickListener(this);
    }

    public void setCard(PlaySession playSession, Stack stack, final Card card) {
        this.card = card;
        this.playSession = playSession;
        this.stack = stack;
        init();
        titleTextView.setText(card.getTitle());
        detailsTextView.setText(card.getDetails());
        imageView.setVisibility(GONE);
        loadAndDisplayImage(card);
        if (playSession != null)
            setAnswer(playSession.getSessionStat(card));
    }

    private void loadAndDisplayImage(final Card card) {
        if (imageLoaderTask != null && !imageLoaderTask.isCancelled()) {
            imageLoaderTask.cancel(true);
        }
        if (card.hasAttachment()) {
            imageLoaderTask = new AsyncTask<Void, Void, Void>() {
                private BitmapDrawable bitmapDrawable;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        bitmapDrawable = new BitmapDrawable(getResources(), AttachmentHandler.get(getContext()).getScaledBitmap(card.getAttachment()));
                    } catch (IOException e) {
                        bitmapDrawable = null;
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    imageView.setImageDrawable(bitmapDrawable);
                    if (!card.isAttachmentPartOfDetails()) {
                        imageView.setVisibility(VISIBLE);
                    }
                }

                @Override
                protected void onCancelled(Void result) {
                    imageView.setImageDrawable(null);
                }
            };
            imageLoaderTask.execute();
        }
    }

    private void setThemeParams() {
        if (Themes.get().isThemeDark()) {
            titleTextView.setTextColor(getResources().getColor(android.R.color.white));
            detailsTextView.setTextColor(getResources().getColor(android.R.color.white));
            playRelativeLayout.setBackgroundResource(R.drawable.card_bg_dark);
        }
    }

    public Card getCard() {
        return card;
    }

    @Override
    public void onClick(View view) {
        if (view == correctBtn) {
            if (answer == PlaySession.ANS_CORRECT) {
                setAnswer(PlaySession.ANS_NONE);
            } else {
                setAnswer(PlaySession.ANS_CORRECT);
                if (autoAdvance) {
                    advance();
                }
            }
            setDetailsVisible(true);
        } else if (view == wrongBtn) {
            if (answer == PlaySession.ANS_WRONG) {
                setAnswer(PlaySession.ANS_NONE);
            } else {
                setAnswer(PlaySession.ANS_WRONG);
                if (autoAdvance) {
                    advance();
                }
            }
            setDetailsVisible(true);
        } else if (view == imageView) {
            if (card.hasAttachment()) {
                AttachmentHandler.get(getContext()).showBitmap(card.getAttachment());
            } else {
                toggleDetails();
            }
        } else {
            toggleDetails();
        }
    }

    private void advance() {
        ViewPager viewPager = ((ViewPager) getParent());
        if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }
    }

    private void setAnswer(int sel) {
        answer = sel;
        if (playSession != null)
            playSession.setSessionStat(card, sel);
        boolean oCor = correctSelected;
        boolean oWro = wrongSelected;

        switch (sel) {
            case PlaySession.ANS_NONE:
                correctSelected = false;
                wrongSelected = false;
                break;
            case PlaySession.ANS_CORRECT:
                correctSelected = true;
                wrongSelected = false;
                break;
            case PlaySession.ANS_WRONG:
                correctSelected = false;
                wrongSelected = true;
                break;

        }

        if (correctSelected != oCor) {
            correctAnimator.setFloatValues(correctSelected ? DISABLED_ALPHA : ENABLED_ALPHA,
                    correctSelected ? ENABLED_ALPHA : DISABLED_ALPHA);
            correctAnimator.start();
        }
        if (wrongSelected != oWro) {
            wrongAnimator.setFloatValues(wrongSelected ? DISABLED_ALPHA : ENABLED_ALPHA,
                    wrongSelected ? ENABLED_ALPHA : DISABLED_ALPHA);
            wrongAnimator.start();
        }
    }

    public void setDetailsVisible(boolean visible) {
        if (visible != detailsVisible) {
            toggleDetails();
        }
        detailsVisible = visible;
    }

    public void toggleDetails() {
        detailsAnimator.removeAllListeners();
        if (detailsVisible) {
            detailsAnimator.setFloatValues(1.0f, 0.0f);
            detailsAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    detailsScrollView.setVisibility(GONE);
                    if (card.isAttachmentPartOfDetails()) {
                        imageView.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else {
            detailsScrollView.setVisibility(TextUtils.isEmpty(card.getDetails()) ? GONE : VISIBLE);
            detailsAnimator.setFloatValues(0.0f, 1.0f);
            imageView.setVisibility(card.hasAttachment() ? VISIBLE : GONE);
        }
        detailsAnimator.start();
        detailsVisible = !detailsVisible;
    }

    public void hideCorrectWrongButtons() {
        correctBtn.setVisibility(GONE);
        wrongBtn.setVisibility(GONE);
    }

    public void showCorrectWrongButtons() {
        correctBtn.setVisibility(VISIBLE);
        wrongBtn.setVisibility(VISIBLE);
    }
}
