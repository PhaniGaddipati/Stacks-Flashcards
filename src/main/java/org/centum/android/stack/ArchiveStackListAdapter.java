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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.centum.android.events.StackManagerEvent;
import org.centum.android.events.StackManagerListener;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.settings.Themes;

/**
 * Created by Phani on 1/24/14.
 */
public class ArchiveStackListAdapter extends ArrayAdapter<Stack> implements StackManagerListener {

    static class ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView numTextView;
    }

    public ArchiveStackListAdapter(Context context) {
        super(context, R.layout.archived_stack_item, R.id.stack_name_textView);
        StackManager.get().addListener(this);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.archived_stack_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.nameTextView = ((TextView) view.findViewById(R.id.stack_name_textView));
            viewHolder.numTextView = ((TextView) view.findViewById(R.id.stack_numCards_textView));
            viewHolder.descriptionTextView = ((TextView) view.findViewById(R.id.stack_description_textView));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.nameTextView.setText(getItem(i).getName());
        viewHolder.numTextView.setText("(" + getItem(i).getNumberOfCards() + ")");
        viewHolder.descriptionTextView.setText(getItem(i).getDescription());

        if (Themes.get().isThemeDark()) {
            viewHolder.nameTextView.setTextColor(getContext().getResources().getColor(android.R.color.white));
            viewHolder.descriptionTextView.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        }

        updateIcon(getItem(i).getIcon(), (ImageView) view.findViewById(R.id.stack_icon_imageView));

        return view;
    }

    private void updateIcon(int icon, ImageView imageView) {
        String uri = "drawable/" + Stack.ICONS[icon];
        int iconResource = getContext().getResources().getIdentifier(uri, null, getContext().getPackageName());
        final Drawable iconI = getContext().getResources().getDrawable(iconResource);
        imageView.setImageDrawable(iconI);
    }

    @Override
    public Stack getItem(int i) {
        return StackManager.get().getArchivedStacks()[i];
    }

    @Override
    public int getCount() {
        return StackManager.get().getNumberOfArchivedStacks();
    }

    @Override
    public void eventFired(StackManagerEvent evt) {
        notifyDataSetChanged();
    }
}
