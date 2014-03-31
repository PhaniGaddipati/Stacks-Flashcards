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
package org.centum.android.play;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.centum.android.events.StackEvent;
import org.centum.android.events.StackListener;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/7/14.
 */
public class PlayPagerAdapter extends PagerAdapter implements StackListener {

    private Stack stack;
    private Context context;
    private PlayCardView[] views;
    private PlaySession playSession;

    public PlayPagerAdapter(Context ctx, Stack stack, PlaySession playSession) {
        this.stack = stack;
        this.context = ctx;
        this.playSession = playSession;
        views = new PlayCardView[stack.getNumberOfCards()];
        stack.addListener(this);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        if (position < views.length) {
            if (views[position] == null) {
                views[position] = (PlayCardView) LayoutInflater.from(context).inflate(R.layout.play_card_item, null);
                views[position].setCard(playSession, stack, stack.getCard(position));
            }
            container.addView(views[position]);
            return views[position];
        } else {
            PlayLastView view = (PlayLastView) LayoutInflater.from(context).inflate(R.layout.play_last_card, null);
            view.setPlaySession(stack, playSession);
            container.addView(view);
            return view;
        }
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return stack.getNumberOfCards() + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public void eventFired(StackEvent evt) {
        switch (evt.getEvent()) {
            case Stack.EVENT_CARD_REMOVED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_CARD_ADDED:
                notifyDataSetChanged();
                break;
            case Stack.EVENT_CARD_ARCHIVE_STATUS_CHANGED:
                notifyDataSetChanged();
                break;
        }
    }
}
