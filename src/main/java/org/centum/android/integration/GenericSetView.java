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
package org.centum.android.integration;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 2/18/14.
 */
public class GenericSetView extends RelativeLayout {

    private GenericSet genericSet = null;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView numCardsTextView;
    private boolean initialized = false;

    public GenericSetView(Context context) {
        super(context);
    }

    public GenericSetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GenericSetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        nameTextView = (TextView) findViewById(R.id.set_name_textView);
        descriptionTextView = (TextView) findViewById(R.id.set_description_textView);
        numCardsTextView = (TextView) findViewById(R.id.set_numCards_textView);

        initialized = true;
    }

    private void setThemeParams() {
        if (Themes.get(getContext()).isThemeDark()) {
            nameTextView.setTextColor(getResources().getColor(android.R.color.white));
            descriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    public void setHighlighted(boolean b) {
        if (b) {
            setBackgroundColor(getResources().getColor(R.color.current_stack));
        } else {
            setBackgroundDrawable(null);
        }
    }

    public GenericSet getGenericSet() {
        return genericSet;
    }

    public void setGenericSet(GenericSet genericSet) {
        if (!initialized) {
            init();
        }
        setThemeParams();
        if (this.genericSet != genericSet) {
            this.genericSet = genericSet;
            nameTextView.setText(genericSet.getTitle());
            descriptionTextView.setText(genericSet.getDescription());
            numCardsTextView.setText((genericSet.getTermCount() > -1 ? genericSet.getTermCount() : "") + "");
        }
        setHighlighted(genericSet.isHighlighted());
    }
}
