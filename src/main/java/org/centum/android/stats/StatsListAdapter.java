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

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlayStats;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/22/14.
 */
public class StatsListAdapter extends ArrayAdapter {

    private Stack mStack;
    private PlayStats playStats;
    private int mLayoutResourceID;
    private StatsPieView statsPieView;

    public StatsListAdapter(Context context, int resource, Stack stack, PlayStats playStats) {
        super(context, resource);
        this.mStack = stack;
        this.mLayoutResourceID = resource;
        this.playStats = playStats;
    }

    @Override
    public int getCount() {
        return mStack.getNumberOfCards() + 1;
    }

    @Override
    public Card getItem(int position) {
        if (position == 0) {
            return null;
        }
        return mStack.getCard(position - 1);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (i == 0) {
            if (statsPieView == null || !(statsPieView instanceof StatsPieView)) {
                statsPieView = (StatsPieView) LayoutInflater.from(getContext()).inflate(R.layout.stats_pie_item, parent, false);
                statsPieView.setStats(playStats);
            }
            if (statsPieView.getStats() != playStats) {
                statsPieView.setStats(playStats);
            }
            return statsPieView;
        }
        if (view == null || !(view instanceof StatsCardView)) {
            view = LayoutInflater.from(getContext()).inflate(mLayoutResourceID, parent, false);
            ((StatsCardView) view).setCard(playStats, mStack.getCard(i - 1));
        } else if (((StatsCardView) view).getCard() != mStack.getCard(i - 1) || ((StatsCardView) view).getStats() != playStats) {
            ((StatsCardView) view).setCard(playStats, mStack.getCard(i - 1));
        }
        return view;
    }

    public void setPlayStats(PlayStats stats) {
        playStats = stats;
        notifyDataSetChanged();
    }

}
