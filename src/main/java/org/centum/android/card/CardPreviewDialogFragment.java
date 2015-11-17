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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.centum.android.model.Card;
import org.centum.android.presentation.play.PlayCardView;
import org.centum.android.stack.R;

/**
 * Created by Phani on 3/22/2014.
 */
public class CardPreviewDialogFragment extends DialogFragment {

    private Card card;
    private FrameLayout previewLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.card_preview_dialog_fragment, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        previewLayout = (FrameLayout) view.findViewById(R.id.frameLayout);

        if (card != null) {
            PlayCardView cardView = (PlayCardView) inflater.inflate(R.layout.play_card_item, previewLayout, false);
            cardView.setCard(null, null, card);
            //cardView.toggleDetails();
            previewLayout.addView(cardView);
            cardView.hideCorrectWrongButtons();
        }

        return view;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
