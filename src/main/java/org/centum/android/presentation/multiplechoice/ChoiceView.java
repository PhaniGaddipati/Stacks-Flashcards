package org.centum.android.presentation.multiplechoice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.centum.android.model.Card;
import org.centum.android.model.play.PlaySession;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;
import org.centum.android.utils.AttachmentHandler;

import java.io.IOException;

/**
 * Created by Phani on 3/31/2014.
 */
public class ChoiceView extends RelativeLayout {

    private static final int COLOR_CORRECT = Color.parseColor("#99CC00");
    private static final int COLOR_WRONG = Color.parseColor("#FF4444");

    private TextView text;
    private ImageView image;
    private ScrollView scrollView;
    private boolean initialized = false;
    private Card card = null;

    public ChoiceView(Context context) {
        super(context);
    }

    public ChoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChoiceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        if (!initialized) {
            initialized = true;

            text = (TextView) findViewById(R.id.details_textView);
            image = (ImageView) findViewById(R.id.attachment_imageView);
            scrollView = (ScrollView) findViewById(R.id.scrollView);

            if (Themes.get(getContext()).isThemeDark()) {
                setBackgroundResource(R.drawable.card_bg_dark);
            }

            int detailsTextSize = 14;
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, detailsTextSize);

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (card.hasAttachment() && card.isAttachmentPartOfDetails()) {
                        AttachmentHandler.get(getContext()).showBitmap(card.getAttachment());
                    } else {
                        ChoiceView.this.callOnClick();
                    }
                }
            });
            text.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChoiceView.this.callOnClick();
                }
            });
        }
    }

    private void update() {
        if (card.hasAttachment() && card.isAttachmentPartOfDetails()) {
            image.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery));
            new AsyncTask<Void, Void, Void>() {

                private Bitmap bitmap;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        bitmap = AttachmentHandler.get(null).getScaledBitmap(card.getAttachment());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (bitmap != null) {
                        image.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                    }
                }
            }.execute();
        } else {
            image.setImageDrawable(getResources().getDrawable(R.drawable.ic_right));
        }

        text.setText(Html.fromHtml(card.getDetails().replace("\n", "<br>")));
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        init();
        this.card = card;
        update();
    }

    public void setAnswer(int ans) {
        switch (ans) {
            case PlaySession.ANS_CORRECT:
                setBackgroundColor(COLOR_CORRECT);
                break;
            case PlaySession.ANS_WRONG:
                setBackgroundColor(COLOR_WRONG);
                break;
        }
        invalidate();
    }

}
