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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.centum.android.events.StackManagerEvent;
import org.centum.android.events.StackManagerListener;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;


public class StackListAdapter extends ArrayAdapter implements StackManagerListener {

    private StackManager stackManager = StackManager.get();
    private Activity context;
    private StackFragment stackFragment;
    private int layoutResourceId;

    public StackListAdapter(StackFragment fragment, int resource) {
        super(fragment.getActivity(), resource, StackManager.get().getStackList());
        this.stackFragment = fragment;
        this.context = fragment.getActivity();
        this.layoutResourceId = resource;
        stackManager.addListener(this);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null || ((StackView) view).getStack() != stackManager.getStack(i)) {
            view = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
            ((StackView) view).setStack(stackFragment, stackManager.getStack(i));
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
