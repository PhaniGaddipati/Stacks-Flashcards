package org.centum.android.presentation.writein;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
 * Created by Phani on 4/15/2014.
 */
public class WriteInCardView extends AbstractCardView implements View.OnClickListener {

    private static final int COLOR_CORRECT = Color.parseColor("#99CC00");
    private static final int COLOR_WRONG = Color.parseColor("#FF4444");

    private Card card = null;
    private Stack stack = null;
    private PlaySession playSession;

    private ImageView attachmentImageView;
    private TextView titleTextView;
    private EditText answerEditText;
    private Button checkButton, showButton;
    private ProgressBar progressBar;

    private int answer = PlaySession.ANS_NONE;
    private AsyncTask<Void, Void, Void> imageLoaderTask;
    private boolean autoAdvance = false;
    private double minSimilarity = .8;
    private boolean ignoreCase = false;
    private boolean answerShowing = false;


    public WriteInCardView(Context context) {
        super(context);
    }

    public WriteInCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WriteInCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        titleTextView = (TextView) findViewById(R.id.title_textView);
        attachmentImageView = (ImageView) findViewById(R.id.attachment_imageView);
        answerEditText = (EditText) findViewById(R.id.answer_editText);
        checkButton = (Button) findViewById(R.id.check_button);
        showButton = (Button) findViewById(R.id.show_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        checkButton.setOnClickListener(this);
        showButton.setOnClickListener(this);

        autoAdvance = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(SettingsActivity.KEY_PREF_PLAY_AUTO_ADVANCE, false);
        try {
            minSimilarity = (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsActivity.KEY_PREF_WRITE_MIN_SIMILARITY, "80"))) / 100f;
        } catch (Exception e) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsActivity.KEY_PREF_WRITE_MIN_SIMILARITY, "80").commit();
        }
        ignoreCase = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(SettingsActivity.KEY_PREF_WRITE_IGNORE_CASE, false);

        progressBar.setMax(100);
        progressBar.setSecondaryProgress((int) (minSimilarity * 100));

        attachmentImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (card != null && card.hasAttachment() && !card.isAttachmentPartOfDetails()) {
                    AttachmentHandler.get(getContext()).showBitmap(card.getAttachment());
                }
            }
        });
        answerEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validate();
            }
        });
        setThemeParams();
    }

    private void setThemeParams() {
        if (Themes.get(getContext()).isThemeDark()) {
            titleTextView.setTextColor(getResources().getColor(android.R.color.white));
            checkButton.setTextColor(getResources().getColor(android.R.color.black));
        }
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
        answer = playSession.getSessionStat(card);
        if (answer == PlaySession.ANS_CORRECT) {
            checkButton.setText("Correct");
            checkButton.setBackgroundColor(COLOR_CORRECT);
        } else if (answer == PlaySession.ANS_WRONG) {
            checkButton.setText("Wrong");
            checkButton.setBackgroundColor(COLOR_WRONG);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_button:
                onCheckButtonClicked();
                break;
            case R.id.show_button:
                if (answerShowing) {
                    showButton.setText("Show Answer");
                } else {
                    showButton.setText(Html.fromHtml(card.getDetails().replace("\n", "<br>")));
                }
                answerShowing = !answerShowing;
                break;
        }
    }

    private void onCheckButtonClicked() {
        validate();
        if (autoAdvance) {
            advance();
        }
    }

    private void validate() {
        boolean correct;
        String answerString;
        if (answerEditText.getText() == null) {
            answerString = "";
        } else {
            answerString = answerEditText.getText().toString();
        }
        double similarity;
        if (ignoreCase) {
            similarity = LevenshteinDistance.similarity(answerString.toLowerCase(), card.getDetails().toLowerCase());
        } else {
            similarity = LevenshteinDistance.similarity(answerString, card.getDetails());
        }
        progressBar.setProgress((int) (similarity * 100));
        correct = similarity >= minSimilarity;
        answer = correct ? PlaySession.ANS_CORRECT : PlaySession.ANS_WRONG;
        playSession.setSessionStat(card, answer);
        checkButton.setText(correct ? "Correct" : "Wrong");
        checkButton.setBackgroundColor(correct ? COLOR_CORRECT : COLOR_WRONG);
    }

    private void advance() {
        ViewPager viewPager = ((ViewPager) getParent());
        if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }
    }
}
