package org.centum.android.presentation.general;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Phani on 4/2/2014.
 */
public class NonSwipeableViewPager extends ViewPager {

    private boolean swipingEnabled = true;

    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.swipingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.swipingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public boolean isSwipingEnabled() {
        return swipingEnabled;
    }

    public void setPagingEnabled(boolean enabled) {
        this.swipingEnabled = enabled;
    }
}
