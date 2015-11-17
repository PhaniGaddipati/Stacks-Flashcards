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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.events.CardEvent;
import org.centum.android.model.events.CardListener;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;
import org.centum.android.utils.AttachmentHandler;

/**
 * Created by Phani on 1/2/14.
 */
public class CardView extends RelativeLayout implements CardListener {

    private static final int COLOR_BAD = Color.parseColor("#CC0000");
    private static final int COLOR_OK = Color.parseColor("#FF8800");
    private static final int COLOR_GOOD = Color.parseColor("#669900");
    private final Handler handler = new Handler();
    private Card card;
    private Stack stack;
    private CardFragment cardFragment;
    private RelativeLayout cardRelativeLayout;
    private TextView titleTextView;
    private TextView detailsTextView;
    private TextView correctTextView;
    private ImageView editImageButton;
    private ImageView attachThumbView;
    private boolean selected = false;
    private boolean initialized = false;
    private AsyncTask<Void, Void, Void> loaderTask;

    public CardView(Context context) {
        super(context);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        titleTextView = (TextView) findViewById(R.id.title_textView);
        detailsTextView = (TextView) findViewById(R.id.details_textView);
        cardRelativeLayout = (RelativeLayout) findViewById(R.id.card_relativelayout);
        editImageButton = (ImageView) findViewById(R.id.edit_imageView);
        attachThumbView = (ImageView) findViewById(R.id.thumb_imageView);
        correctTextView = (TextView) findViewById(R.id.correct_textView);
        correctTextView.setVisibility(INVISIBLE);

        setThemeParams();

        attachThumbView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAttachment();
            }
        });
        editImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CardDialogFragment fragment = new CardDialogFragment();
                fragment.setCard(stack, card);
                fragment.show(cardFragment.getFragmentManager(), "edit_card");
            }
        });

        initialized = true;
    }

    public void setPercentCorrect(int percent) {
        correctTextView.setVisibility(percent >= 0 ? VISIBLE : INVISIBLE);
        if (percent <= 20) {
            correctTextView.setBackgroundColor(COLOR_BAD);
        } else if (percent < 80) {
            correctTextView.setBackgroundColor(COLOR_OK);
        } else {
            correctTextView.setBackgroundColor(COLOR_GOOD);
        }
        if (percent >= 0) {
            correctTextView.setText(percent + "% Correct");
        } else {
            correctTextView.setText("");
        }
    }

    private void setThemeParams() {
        if (Themes.get(getContext()).isThemeDark()) {
            titleTextView.setTextColor(getResources().getColor(android.R.color.white));
            detailsTextView.setTextColor(getResources().getColor(android.R.color.white));
            cardRelativeLayout.setBackgroundResource(R.drawable.card_bg_dark);
        } else {
            titleTextView.setTextColor(getResources().getColor(android.R.color.black));
            detailsTextView.setTextColor(getResources().getColor(android.R.color.black));
            cardRelativeLayout.setBackgroundResource(R.drawable.card_bg);
        }
    }

    private void showAttachment() {
        if (card.hasAttachment())
            AttachmentHandler.get(getContext()).showBitmap(card.getAttachment());
    }

    public void setCard(final CardFragment fragment, Stack mStack, Card mCard) {
        if (card != null) {
            card.removeListener(this);
        }

        this.card = mCard;
        this.stack = mStack;
        this.cardFragment = fragment;

        if (mCard != null) {
            if (!initialized) {
                init();
            } else {
                setThemeParams();
            }

            updateTitle();
            updateDetails();
            updateSelected();
            updateAttachment();

            card.addListener(this);
        } else {
            titleTextView.setText("");
            detailsTextView.setText("");
            setContextSelected(false);
            attachThumbView.setImageDrawable(null);
        }

        invalidate();
    }

    private void updateAttachment() {
        if (loaderTask != null && !loaderTask.isCancelled()) {
            loaderTask.cancel(true);
        }
        if (card.hasAttachment()) {
            loaderTask = new AsyncTask<Void, Void, Void>() {
                Bitmap bitmap;

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        bitmap = AttachmentHandler.get(getContext()).getScaledBitmap(card.getAttachment());
                    } catch (Exception e) {
                        e.printStackTrace();
                        bitmap = null;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (bitmap != null) {
                        attachThumbView.setScaleType(card.isAttachmentPartOfDetails() ? ImageView.ScaleType.FIT_END : ImageView.ScaleType.FIT_START);
                        attachThumbView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                    } else {
                        attachThumbView.setImageDrawable(null);
                    }
                }

                @Override
                protected void onCancelled(Void result) {
                    attachThumbView.setImageDrawable(null);
                }
            };
            loaderTask.execute();
        } else {
            attachThumbView.setImageDrawable(null);
        }
    }

    public boolean isContextSelected() {
        return selected;
    }

    public void setContextSelected(boolean selected) {
        this.selected = selected;
        if (Themes.get(getContext()).isThemeDark()) {
            cardRelativeLayout.setBackgroundResource(selected ? R.drawable.card_sel_bg : R.drawable.card_bg_dark);
        } else {
            cardRelativeLayout.setBackgroundResource(selected ? R.drawable.card_sel_bg : R.drawable.card_bg);
        }
    }

    private void updateTitle() {
        titleTextView.setText(Html.fromHtml(card.getTitle()));
    }

    private void updateDetails() {
        detailsTextView.setText(Html.fromHtml(card.getDetails().replace("\n", "<br>")));
    }

    private void updateSelected() {
        setContextSelected(card.isSelected());
    }

    public Card getCard() {
        return card;
    }

    @Override
    public void eventFired(final CardEvent evt) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                processCardEvent(evt);
            }
        });
    }

    private void processCardEvent(CardEvent evt) {
        switch (evt.getEvent()) {
            case Card.EVENT_SELECTION_CHANGED:
                setContextSelected(evt.getSource().isSelected());
                break;
            case Card.EVENT_ATTACHMENT_CHANGED:
                updateAttachment();
                break;
            case Card.EVENT_ATTACHMENT_IS_DETAILS_CHANGED:
                updateAttachment();
                break;
            case Card.EVENT_DETAILS_CHANGED:
                updateDetails();
                break;
            case Card.EVENT_TITLE_CHANGED:
                updateTitle();
                break;
        }
    }
}
