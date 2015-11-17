package org.centum.android.presentation.multiplechoice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;
import org.centum.android.presentation.general.AbstractCardView;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;
import org.centum.android.utils.AttachmentHandler;

import java.io.IOException;

/**
 * Created by Phani on 3/31/2014.
 */
public class MultipleChoiceCardView extends AbstractCardView implements View.OnClickListener {

    private final ChoiceView[] choices = new ChoiceView[4];
    private Card card = null;
    private Stack stack = null;
    private RelativeLayout playRelativeLayout;
    private TextView titleTextView;
    private LinearLayout choicesLinearLayout;
    private PlaySession playSession;
    private ImageView attachmentImageView;
    private int answer = PlaySession.ANS_NONE;
    private boolean autoAdvance = false;

    public MultipleChoiceCardView(Context context) {
        super(context);
    }

    public MultipleChoiceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultipleChoiceCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        titleTextView = (TextView) findViewById(R.id.title_textView);
        playRelativeLayout = (RelativeLayout) findViewById(R.id.play_relativelayout);
        choicesLinearLayout = (LinearLayout) findViewById(R.id.choices_linear_layout);
        attachmentImageView = (ImageView) findViewById(R.id.attachment_imageView);

        autoAdvance = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(SettingsActivity.KEY_PREF_PLAY_AUTO_ADVANCE, false);

        attachmentImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (card != null && card.hasAttachment() && !card.isAttachmentPartOfDetails()) {
                    AttachmentHandler.get(getContext()).showBitmap(card.getAttachment());
                }
            }
        });
        setThemeParams();
    }

    public void setCard(PlaySession playSession, Stack stack, final Card card) {
        this.card = card;
        this.playSession = playSession;
        this.stack = stack;
        init();
        titleTextView.setText(Html.fromHtml(card.getTitle().replace("\n", "<br>")));
        if (card.hasAttachment() && !card.isAttachmentPartOfDetails()) {
            attachmentImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery));
            new AsyncTask<Void, Void, Void>() {

                private Bitmap bitmap;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        bitmap = AttachmentHandler.get(getContext()).getScaledBitmap(card.getAttachment());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (bitmap != null) {
                        attachmentImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                        attachmentImageView.setVisibility(VISIBLE);
                    }
                }
            }.execute();
        } else {
            attachmentImageView.setVisibility(GONE);
        }
        generateChoices();
        answer = playSession.getSessionStat(card);
        if (answer == PlaySession.ANS_CORRECT) {
            for (ChoiceView choiceView : choices) {
                if (choiceView.getCard() == card) {
                    choiceView.setAnswer(PlaySession.ANS_CORRECT);
                }
            }
        } else if (answer == PlaySession.ANS_WRONG) {
            for (ChoiceView choiceView : choices) {
                if (choiceView.getCard() != card) {
                    choiceView.setAnswer(PlaySession.ANS_WRONG);
                }
            }
        }
    }

    private void generateChoices() {
        int correctCard = (int) (Math.random() * 4);
        for (int i = 0; i < 4; i++) {
            choices[i] = (ChoiceView) LayoutInflater.from(getContext()).inflate(R.layout.choice_view, choicesLinearLayout, false);
            if (i == correctCard) {
                choices[i].setCard(card);
            } else {
                Card randCard;
                do {
                    randCard = stack.getCard((int) (Math.random() * stack.getNumberOfCards()));
                } while (choicesContains(randCard) || randCard == card);
                choices[i].setCard(randCard);
            }
        }
        for (ChoiceView choiceView : choices) {
            choicesLinearLayout.addView(choiceView, 0);
            choiceView.setOnClickListener(this);
        }
    }

    private boolean choicesContains(Card card) {
        for (int i = 0; i < 4; i++) {
            if (choices[i] != null && choices[i].getCard() == card) {
                return true;
            }
        }
        return false;
    }

    private void setThemeParams() {
        if (Themes.get(getContext()).isThemeDark()) {
            titleTextView.setTextColor(getResources().getColor(android.R.color.white));
            //playRelativeLayout.setBackgroundResource(R.drawable.card_bg_dark);
        }
    }

    public Card getCard() {
        return card;
    }

    @Override
    public void onClick(View view) {
        if (answer == PlaySession.ANS_NONE) {
            for (ChoiceView choiceView : choices) {
                if (choiceView.getCard() == card) {
                    choiceView.setAnswer(PlaySession.ANS_CORRECT);
                }
                if (choiceView == view) {
                    if (choiceView.getCard() == card) {
                        choiceView.setAnswer(PlaySession.ANS_CORRECT);
                        answer = PlaySession.ANS_CORRECT;
                    } else {
                        choiceView.setAnswer(PlaySession.ANS_WRONG);
                        answer = PlaySession.ANS_WRONG;
                    }
                }
            }
            playSession.setSessionStat(card, answer);
            if (autoAdvance) {
                advance();
            }
        }
    }

    private void advance() {
        ViewPager viewPager = ((ViewPager) getParent());
        if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }
    }

}

