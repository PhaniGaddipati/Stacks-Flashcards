package org.centum.android.presentation.general;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;

/**
 * Created by Phani on 4/16/2014.
 */
public abstract class AbstractCardView extends RelativeLayout {
    public AbstractCardView(Context context) {
        super(context);
    }

    public AbstractCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public abstract void setCard(PlaySession playSession, Stack stack, final Card card);
}
