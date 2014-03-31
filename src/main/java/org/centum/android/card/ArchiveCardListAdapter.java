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
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.centum.android.events.StackEvent;
import org.centum.android.events.StackListener;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/24/14.
 */
public class ArchiveCardListAdapter extends ArrayAdapter<Card> implements StackListener {

    private Stack stack;

    public ArchiveCardListAdapter(Context context, Stack stack) {
        super(context, R.layout.archived_card_item);
        this.stack = stack;
        stack.addListener(this);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.archived_card_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.detailsTextView = ((TextView) view.findViewById(R.id.details_textView));
            viewHolder.titleTextView = ((TextView) view.findViewById(R.id.title_textView));
            viewHolder.cardRelativeLayout = (RelativeLayout) view.findViewById(R.id.card_relativelayout);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.titleTextView.setText(getItem(i).getTitle());
        viewHolder.detailsTextView.setText(getItem(i).getDetails());

        if (Themes.get().isThemeDark()) {
            viewHolder.titleTextView.setTextColor(getContext().getResources().getColor(android.R.color.white));
            viewHolder.detailsTextView.setTextColor(getContext().getResources().getColor(android.R.color.white));
            viewHolder.cardRelativeLayout.setBackgroundResource(R.drawable.card_bg_dark);
        }

        return view;
    }

    @Override
    public Card getItem(int i) {
        return stack.getArchivedCards().get(i);
    }

    @Override
    public int getCount() {
        return stack.getNumberOfArchivedCards();
    }

    @Override
    public void eventFired(StackEvent evt) {
        switch (evt.getEvent()) {
            case Stack.EVENT_ARCHIVED_CARD_ADDED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_ARCHIVED_CARD_REMOVED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_CARD_ARCHIVE_STATUS_CHANGED:
                notifyDataSetChanged();
                break;
        }
    }

    static class ViewHolder {
        TextView titleTextView;
        TextView detailsTextView;
        RelativeLayout cardRelativeLayout;
        int pos;
    }
}
