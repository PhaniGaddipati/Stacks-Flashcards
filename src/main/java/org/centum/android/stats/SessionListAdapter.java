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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;

/**
 * Created by Phani on 1/23/14.
 */
public class SessionListAdapter extends ArrayAdapter {

    private final Stack stack;
    private final int layoutResourceID;

    public SessionListAdapter(Context context, int resource, Stack stack) {
        super(context, resource);
        this.stack = stack;
        layoutResourceID = resource;
    }

    @Override
    public int getCount() {
        return stack.getNumberOfPlaySessions();
    }

    @Override
    public PlaySession getItem(int position) {
        return stack.getPlaySession(position);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(layoutResourceID, parent, false);
            ((SessionView) view).setPlaySession(stack, getItem(i));
        } else if (((SessionView) view).getPlaySession() != getItem(i)) {
            ((SessionView) view).setPlaySession(stack, getItem(i));
        }
        return view;
    }

}
