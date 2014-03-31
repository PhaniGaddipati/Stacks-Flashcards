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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.centum.android.model.play.PlaySession;
import org.centum.android.stack.R;

import java.text.SimpleDateFormat;

/**
 * Created by Phani on 1/24/14.
 */
public class SessionSpinnerAdapter extends ArrayAdapter<PlaySession> {

    public SessionSpinnerAdapter(Context context, PlaySession[] objects) {
        super(context, R.layout.spinner_dropdown, R.id.title_textView, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown, parent, false);
        }
        ((TextView) view.findViewById(R.id.title_textView)).setText(getItem(position).getName());
        String when = new SimpleDateFormat("EEE MMM d, h:mm:ss a").format(getItem(position).getDate());
        ((TextView) view.findViewById(R.id.subtitle_textView)).setText(when);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}
