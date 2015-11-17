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
package org.centum.android.stack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.model.Stack;
import org.centum.android.model.events.StackEvent;
import org.centum.android.model.events.StackListener;
import org.centum.android.settings.Themes;

/**
 * Created by Phani on 1/1/14.
 */
public class StackView extends RelativeLayout {

    private static final int COLOR_BAD = Color.parseColor("#CC0000");
    private static final int COLOR_OK = Color.parseColor("#FF8800");
    private static final int COLOR_GOOD = Color.parseColor("#669900");
    private static final int COLOR_NONE = Color.parseColor("#0099CC");

    private Stack stack;
    private Context context;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView numCardsTextView;
    private ImageView iconImageView;
    private ImageView editImageView;
    private ImageView barImageView;
    private RelativeLayout cardRelativeLayout;
    private StackListener listener;
    private int lastDrawnIcon = -2;
    private boolean initialized = false;

    private boolean selected = false;

    public StackView(Context context) {
        super(context);
    }

    public StackView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        cardRelativeLayout = (RelativeLayout) findViewById(R.id.stack_relativelayout);
        nameTextView = (TextView) findViewById(R.id.stack_name_textView);
        descriptionTextView = (TextView) findViewById(R.id.stack_description_textView);
        numCardsTextView = (TextView) findViewById(R.id.stack_numCards_textView);
        iconImageView = (ImageView) findViewById(R.id.stack_icon_imageView);
        editImageView = (ImageView) findViewById(R.id.stack_edit_imageView);
        barImageView = (ImageView) findViewById(R.id.bar_imageView);

        setThemeParams();

        editImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stack != null) {
                    StackDialogFragment fragment = new StackDialogFragment();
                    fragment.setStack(stack);
                    fragment.show(((Activity) getContext()).getFragmentManager(), "edit_stack");
                }
            }
        });

        initialized = true;
    }

    private void setThemeParams() {
        if (Themes.get(getContext()).isThemeDark()) {
            nameTextView.setTextColor(getResources().getColor(android.R.color.white));
            descriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    public void setStack(final Context context, Stack stack) {
        if (this.stack != null && listener != null) {
            this.stack.removeListener(listener);
        }

        if (stack != null) {
            this.stack = stack;
            this.context = context;

            if (listener == null) {
                listener = new StackListener() {
                    @Override
                    public void eventFired(StackEvent evt) {
                        StackView.this.eventFired(evt);
                    }
                };
            }
            this.stack.addListener(listener);

            if (!initialized) {
                init();
            } else {
                setThemeParams();
            }

            updateNumCards();
            updateDescription();
            updateName();
            updateIcon();
            updateSelected();
            updateColor();
            updateCurrent();

        }
        invalidate();
    }

    public boolean isContextSelected() {
        return selected;
    }

    public void setContextSelected(boolean selected) {
        this.selected = selected;
        updateIcon(true);
    }

    private void updateName() {
        nameTextView.setText(Html.fromHtml(stack.getName()));
    }

    private void updateDescription() {
        descriptionTextView.setText(Html.fromHtml(stack.getDescription()));
    }

    private void updateNumCards() {
        numCardsTextView.setText("(" + stack.getNumberOfCards() + ")");
    }

    private void updateSelected() {
        setContextSelected(stack.isSelected());
    }

    private void updateIcon() {
        updateIcon(false);
    }

    private void updateIcon(boolean force) {
        if (force || stack.getIcon() != lastDrawnIcon) {
            lastDrawnIcon = stack.getIcon();
            String uri = "drawable/" + (isContextSelected() ? "circle_selected" : Stack.ICONS[stack.getIcon()]);
            int iconResource = getResources().getIdentifier(uri, null, getContext().getPackageName());
            final Drawable icon = getResources().getDrawable(iconResource);
            iconImageView.setImageDrawable(icon);
        }
    }

    private void updateColor() {
        if (stack != null && stack.getNumberOfEnabledSessions() > 0) {
            int percent = (int) stack.getPlayStatsWithEnabledSessions().getTotalPercentCorrect();
            if (percent <= 20) {
                barImageView.setBackgroundColor(COLOR_BAD);
            } else if (percent < 80) {
                barImageView.setBackgroundColor(COLOR_OK);
            } else {
                barImageView.setBackgroundColor(COLOR_GOOD);
            }
        } else {
            barImageView.setBackgroundColor(COLOR_NONE);
        }
    }

    private void updateCurrent() {
        if (stack.isCurrentStack()) {
            setBackgroundColor(getResources().getColor(R.color.current_stack));
        } else {
            setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

    public RelativeLayout getCardRelativeLayout() {
        return cardRelativeLayout;
    }

    public Stack getStack() {
        return stack;
    }

    public void eventFired(StackEvent evt) {
        switch (evt.getEvent()) {
            case Stack.EVENT_CARD_ARCHIVE_STATUS_CHANGED:
                updateNumCards();
                break;
            case Stack.EVENT_CARD_REMOVED:
                updateNumCards();
                break;
            case Stack.EVENT_CARD_ADDED:
                updateNumCards();
                break;
            case Stack.EVENT_DESCRIPTION_CHANGED:
                updateDescription();
                break;
            case Stack.EVENT_ICON_CHANGED:
                updateIcon();
                break;
            case Stack.EVENT_NAME_CHANGED:
                updateName();
                break;
            case Stack.EVENT_SELECTION_CHANGED:
                setContextSelected(evt.getSource().isSelected());
                break;
            case Stack.EVENT_PLAY_SESSION_ADDED:
                updateColor();
                break;
            case Stack.EVENT_PLAY_SESSION_REMOVED:
                updateColor();
                break;
            case Stack.EVENT_CURRENT:
                updateCurrent();
                break;
        }
    }
}
