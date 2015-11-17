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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Phani on 2/19/14.
 */
public class SetListAdapter extends ArrayAdapter<GenericSet> {

    private final int layoutResourceId;

    public SetListAdapter(Context context, int resource, GenericSet[] sets) {
        super(context, resource, sets);
        this.layoutResourceId = resource;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(layoutResourceId, parent, false);
            ((GenericSetView) view).setGenericSet(getItem(i));
        } else if (((GenericSetView) view).getGenericSet() != getItem(i)) {
            ((GenericSetView) view).setGenericSet(getItem(i));
        }
        return view;
    }
}
