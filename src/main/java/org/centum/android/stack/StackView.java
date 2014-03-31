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
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.events.StackEvent;
import org.centum.android.events.StackListener;
import org.centum.android.model.Stack;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.settings.Themes;

/**
 * Created by Phani on 1/1/14.
 */
public class StackView extends RelativeLayout implements StackListener {

    private Stack stack;
    private StackFragment stackFragment;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView numCardsTextView;
    private ImageView iconImageView;
    private ImageView editImageView;
    private RelativeLayout cardRelativeLayout;
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

        setThemeParams();

        editImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stack != null)
                    new StackDialogFragment(stack).show(((Activity) getContext()).getFragmentManager(), "edit_stack");
            }
        });

        int titleTextSize = 20;
        int descriptionTextSize = 12;
        try {
            titleTextSize = Integer.parseInt(
                    PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsActivity.KEY_PREF_STACK_TITLE_SIZE, "20"));
        } catch (Exception e) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsActivity.KEY_PREF_STACK_TITLE_SIZE, "20").commit();
        }
        try {
            descriptionTextSize = Integer.parseInt(
                    PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsActivity.KEY_PREF_STACK_DESCRIPTION_SIZE, "12"));
        } catch (Exception e) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsActivity.KEY_PREF_STACK_DESCRIPTION_SIZE, "12").commit();
        }

        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleTextSize);
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, descriptionTextSize);

        initialized = true;
    }

    private void setThemeParams() {
        if (Themes.get().isThemeDark()) {
            nameTextView.setTextColor(getResources().getColor(android.R.color.white));
            descriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    public void setStack(final StackFragment stackFragment, Stack stack) {
        if (this.stack != null) {
            this.stack.removeListener(this);
        }

        if (stack != null) {
            this.stack = stack;
            this.stackFragment = stackFragment;

            this.stack.addListener(this);

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

    public RelativeLayout getCardRelativeLayout() {
        return cardRelativeLayout;
    }

    public Stack getStack() {
        return stack;
    }

    @Override
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
        }
    }
}
