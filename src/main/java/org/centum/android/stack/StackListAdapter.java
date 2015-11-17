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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.events.StackManagerEvent;
import org.centum.android.model.events.StackManagerListener;


public class StackListAdapter extends ArrayAdapter implements StackManagerListener {

    private final StackManager stackManager = StackManager.get();
    private final Context context;
    private final int layoutResourceId;

    public StackListAdapter(Context context, int resource) {
        super(context, resource, StackManager.get().getStackList());
        this.context = context;
        this.layoutResourceId = resource;
        stackManager.addListener(this);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null || ((StackView) view).getStack() != stackManager.getStack(i)) {
            view = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
            ((StackView) view).setStack(context, stackManager.getStack(i));
        }
        return view;
    }

    @Override
    public Stack getItem(int position) {
        return StackManager.get().getStack(position);
    }

    @Override
    public void eventFired(StackManagerEvent evt) {
        notifyDataSetChanged();
    }
}
