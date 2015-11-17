package org.centum.android.presentation.mixed;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.centum.android.model.Stack;
import org.centum.android.model.events.StackEvent;
import org.centum.android.model.events.StackListener;
import org.centum.android.model.play.PlaySession;
import org.centum.android.presentation.general.AbstractCardView;
import org.centum.android.presentation.general.PlayLastView;
import org.centum.android.stack.R;

/**
 * Created by Phani on 4/16/2014.
 */
public class MixedPagerAdapter extends PagerAdapter implements StackListener {
    private final Stack stack;
    private final Context context;
    private final PlaySession playSession;
    private final AbstractCardView[] views;
    private final int[] resources = new int[3];
    private boolean includeSimple = true, includeMulti = true, includeWrite = true;
    private int numTypes = 0;

    public MixedPagerAdapter(Context context, Stack stack, PlaySession playSession) {
        this.context = context;
        this.stack = stack;
        this.playSession = playSession;
        views = new AbstractCardView[stack.getNumberOfCards()];

        includeSimple = playSession.getSessionSettings().isIncludeSimple();
        includeMulti = playSession.getSessionSettings().isIncludeMulti();
        includeWrite = playSession.getSessionSettings().isIncludeWriteIn();

        if (!includeSimple && !includeWrite && !includeMulti) {
            includeSimple = true;
            includeMulti = true;
            includeWrite = true;
        }

        if (includeSimple) {
            resources[numTypes] = R.layout.play_card_item;
            numTypes++;
        }
        if (includeMulti) {
            resources[numTypes] = R.layout.multi_choice_card_item;
            numTypes++;
        }
        if (includeWrite) {
            resources[numTypes] = R.layout.write_in_card_item;
            numTypes++;
        }

        stack.addListener(this);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        if (position < views.length) {
            if (views[position] == null) {
                views[position] = getRandomCardView();
                views[position].setCard(playSession, stack, stack.getCard(position));
            }
            container.addView(views[position]);
            return views[position];
        } else {
            PlayLastView view = (PlayLastView) LayoutInflater.from(context).inflate(R.layout.play_last_card, container, false);
            view.setPlaySession(stack, playSession);
            container.addView(view);
            return view;
        }
    }

    private AbstractCardView getRandomCardView() {
        int rand = (int) (Math.random() * numTypes);
        return (AbstractCardView) LayoutInflater.from(context).inflate(resources[rand], null);
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
