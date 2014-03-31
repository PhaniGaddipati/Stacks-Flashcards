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
package org.centum.android.learn;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.play.PlayCardView;
import org.centum.android.stack.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Phani on 2/2/14.
 */
public class LearnPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    private Stack stack;
    private List<Card> cardList = new LinkedList<Card>();
    private Context context;

    public LearnPagerAdapter(ViewPager viewPager, Stack stack) {
        viewPager.setOnPageChangeListener(this);
        this.context = viewPager.getContext();
        this.stack = stack;
        loadMoreCards();
    }

    public Object instantiateItem(ViewGroup container, int position) {
        PlayCardView playCardView = (PlayCardView) LayoutInflater.from(context).inflate(R.layout.play_card_item, null);
        playCardView.setCard(null, stack, cardList.get(position));
        playCardView.hideCorrectWrongButtons();
        container.addView(playCardView);
        return playCardView;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return cardList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        if (i > getCount() - 2) {
            loadMoreCards();
        }
    }

    private void loadMoreCards() {
        stack.shuffle();
        for (int i = 0; i < stack.getNumberOfCards(); i++) {
            cardList.add(stack.getCard(i));
        }
        notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
