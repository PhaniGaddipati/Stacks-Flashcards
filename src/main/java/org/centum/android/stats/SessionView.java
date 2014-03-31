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
package org.centum.android.stats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

import java.text.SimpleDateFormat;

/**
 * Created by Phani on 1/23/14.
 */
public class SessionView extends RelativeLayout implements View.OnClickListener {

    private ImageView selected;
    private TextView nameTextView;
    private TextView dateTextView;
    private TextView emptyTextView;
    private ImageView editImageView;
    private RelativeLayout sessionRelativeLayout;

    private boolean initialized = false;
    private PlaySession playSession;
    private Stack stack;

    public SessionView(Context context) {
        super(context);
    }

    public SessionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SessionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        selected = (ImageView) findViewById(R.id.imageView);
        nameTextView = (TextView) findViewById(R.id.name_textView);
        dateTextView = (TextView) findViewById(R.id.date_textView);
        editImageView = (ImageView) findViewById(R.id.edit_imageView);
        emptyTextView = (TextView) findViewById(R.id.session_empty_textView);
        sessionRelativeLayout = (RelativeLayout) findViewById(R.id.session_relativelayout);

        editImageView.setOnClickListener(this);

        setThemeParams();

        initialized = true;
    }

    private void setThemeParams() {
        if (Themes.get().isThemeDark()) {
            nameTextView.setTextColor(getResources().getColor(android.R.color.white));
            dateTextView.setTextColor(getResources().getColor(android.R.color.white));
            sessionRelativeLayout.setBackgroundResource(R.drawable.card_bg_dark);
        }
    }

    public void update() {
        if (!initialized) {
            init();
        } else {
            setThemeParams();
        }
        if (playSession != null) {
            nameTextView.setText(playSession.getName());
            String when = new SimpleDateFormat("EEE MMM d, h:mm:ss a").format(playSession.getDate());
            dateTextView.setText(when);
            emptyTextView.setVisibility(playSession.isEmpty() ? VISIBLE : GONE);
            setChecked(playSession.isEnabled());
        }
    }

    public void setChecked(boolean sel) {
        selected.setImageDrawable(getResources().getDrawable(
                sel ? android.R.drawable.presence_online : android.R.drawable.presence_offline));
        playSession.setEnabled(sel);
    }

    public boolean isChecked() {
        return playSession.isEnabled();
    }

    public void setPlaySession(Stack stack, PlaySession playSession) {
        this.stack = stack;
        this.playSession = playSession;
        update();
    }

    public PlaySession getPlaySession() {
        return playSession;
    }

    @Override
    public void onClick(View view) {
        if (view == nameTextView || view == dateTextView) {
            setChecked(!isChecked());
        } else if (view == editImageView) {
            final EditText input = new EditText(getContext());
            input.setText(playSession.getName());

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Rename");
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    playSession.setName(input.getText().toString().trim());
                    update();
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();

        }
    }
}
