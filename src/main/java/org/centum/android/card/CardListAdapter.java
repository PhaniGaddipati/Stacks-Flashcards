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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.centum.android.events.StackEvent;
import org.centum.android.events.StackListener;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlayStats;

/**
 * Created by Phani on 1/2/14.
 */
public class CardListAdapter extends ArrayAdapter implements StackListener {

    private Activity context;
    private CardFragment cardFragment;
    private int layoutResourceId;
    private Stack stack;
    private PlayStats playStats;

    public CardListAdapter(CardFragment fragment, Stack stack, int resource) {
        super(fragment.getActivity(), resource, stack.getCardList());
        this.cardFragment = fragment;
        this.layoutResourceId = resource;
        this.stack = stack;
        context = fragment.getActivity();
        stack.addListener(this);
        playStats = stack.getPlayStatsWithEnabledSessions();
    }

    @Override
    public Card getItem(int position) {
        return stack.getCard(position);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
            ((CardView) view).setCard(cardFragment, stack, stack.getCard(i));
            if (playStats.getNumberCorrect(stack.getCard(i)) > 0 || playStats.getNumberWrong(stack.getCard(i)) > 0) {
                ((CardView) view).setPercentCorrect((int) playStats.getPercentCorrect(stack.getCard(i)));
            } else {
                ((CardView) view).setPercentCorrect(-1);
            }
        } else if (((CardView) view).getCard() != stack.getCard(i)) {
            ((CardView) view).setCard(cardFragment, stack, stack.getCard(i));
            if (playStats.getNumberCorrect(stack.getCard(i)) > 0 || playStats.getNumberWrong(stack.getCard(i)) > 0) {
                ((CardView) view).setPercentCorrect((int) playStats.getPercentCorrect(stack.getCard(i)));
            } else {
                ((CardView) view).setPercentCorrect(-1);
            }
        }
        return view;
    }

    @Override
    public void eventFired(StackEvent evt) {
        switch (evt.getEvent()) {
            case Stack.EVENT_CARD_ADDED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_CARD_REMOVED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_CARD_MOVED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_CARD_ARCHIVE_STATUS_CHANGED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_PLAY_SESSION_ADDED:
                updateStats();
                break;
            case Stack.EVENT_PLAY_SESSION_REMOVED:
                updateStats();
                break;
        }
    }

    public void updateStats() {
        if (stack != null) {
            playStats = stack.getPlayStatsWithEnabledSessions();
            notifyDataSetChanged();
        }
    }
}
