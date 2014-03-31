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
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/18/14.
 */
public class PlayLastView extends RelativeLayout implements View.OnClickListener {

    private PlaySession playSession = null;
    private TextView nameTextView = null;
    private TextView nameLabel = null;

    public PlayLastView(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public PlayLastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public PlayLastView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
    }

    public void setPlaySession(final Stack stack, PlaySession session) {
        this.playSession = session;

        if (playSession != null) {
            if (nameTextView == null) {
                nameTextView = (TextView) findViewById(R.id.session_textview);
                nameLabel = (TextView) findViewById(R.id.name_label);
            }

            nameTextView.setVisibility(View.VISIBLE);
            nameLabel.setVisibility(View.VISIBLE);

            nameTextView.setText(playSession.getName());
        } else {
            nameTextView.setVisibility(View.INVISIBLE);
            nameLabel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).onBackPressed();
        }
    }
}
