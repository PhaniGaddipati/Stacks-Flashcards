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
package org.centum.android.presentation.general;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/18/14.
 */
public class PlayProgressView extends View implements View.OnTouchListener {

    public static final int STATE_NONE = 0;
    public static final int STATE_CORRECT = 1;
    public static final int STATE_WRONG = 2;
    private final float MAX_SLOP = 16;
    private final long MAX_CLICK_TIME = 150;
    private int correctColor;
    private int wrongColor;
    private int noneColorLight = Color.LTGRAY;
    private int noneColor = noneColorLight;
    private int noneColorDark = Color.DKGRAY;
    private PlayProgressListener playProgressListener;
    private Paint paint;
    private float xPad, yPad;
    private float wWithPad, hWithPad;
    private int slots;
    private float slotWidth;
    private int[] states;
    private int currentSlot = 0;
    private float downX, downY, upX, upY;
    private long downTime, upTime;
    private RectF rect;
    private LinearGradient linearGradient;

    public PlayProgressView(Context context, int slots) {
        super(context);
        correctColor = getResources().getColor(R.color.holo_green);
        wrongColor = getResources().getColor(R.color.holo_red);

        this.slots = slots;
        states = new int[slots];
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        rect = new RectF();

        setOnTouchListener(this);
    }

    public PlayProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PlayProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayProgressView(Context context) {
        super(context);
    }

    public void setState(int slot, int state) {
        if (slot < states.length) {
            states[slot] = state;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        noneColor = Themes.get(getContext()).isThemeDark() ? noneColorDark : noneColorLight;
        paint.setColor(noneColor);
        rect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        canvas.drawRect(rect, paint);
        if (states != null) {
            for (int i = 0; i < slots; i++) {
                switch (states[i]) {
                    case STATE_NONE:
                        paint.setColor(noneColor);
                        break;
                    case STATE_CORRECT:
                        paint.setColor(correctColor);
                        break;
                    case STATE_WRONG:
                        paint.setColor(wrongColor);
                        break;
                }

                canvas.drawRect(i * slotWidth + getPaddingLeft(), getPaddingTop(), (i + 1) * slotWidth + getPaddingLeft(), getHeight() - getPaddingBottom(), paint);
            }
        }

        if (currentSlot >= 0 && currentSlot < slots) {
            paint.setARGB(120, 255, 255, 255);
            canvas.drawRect(currentSlot * slotWidth + getPaddingLeft() + slotWidth / 3, getPaddingTop() + getHeight() / 3,
                    (currentSlot + 1) * slotWidth + getPaddingLeft() - slotWidth / 3, getHeight() - getPaddingBottom() - getHeight() / 3, paint);
        }

        if (slotWidth > 5) {
            paint.setColor(Color.BLACK);
            for (int i = 1; i < slots; i++) {
                canvas.drawLine((float) i * slotWidth + getPaddingLeft(),
                        (float) getPaddingTop(),
                        (float) i * slotWidth + getPaddingLeft(),
                        (float) getHeight() - getPaddingBottom(), paint);
            }
        }

        //paint.setShader(linearGradient);
        //canvas.drawPaint(paint);
        paint.setShader(null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        xPad = (float) (getPaddingLeft() + getPaddingRight());
        yPad = (float) (getPaddingTop() + getPaddingBottom());

        wWithPad = w - xPad;
        hWithPad = h - yPad;

        slotWidth = wWithPad / slots;

        linearGradient = new LinearGradient(getWidth() / 2, 0, getWidth() / 2, getHeight() / 5, Color.DKGRAY, Color.TRANSPARENT, Shader.TileMode.CLAMP);
    }

    public PlayProgressListener getPlayProgressListener() {
        return playProgressListener;
    }

    public void setPlayProgressListener(PlayProgressListener playProgressListener) {
        this.playProgressListener = playProgressListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = motionEvent.getX();
                downY = motionEvent.getY();
                downTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                upX = motionEvent.getX();
                upY = motionEvent.getY();
                upTime = System.currentTimeMillis();

                if ((upTime - downTime) <= MAX_CLICK_TIME && distance(downX, downY, upX, upY) <= MAX_SLOP) {
                    if (playProgressListener != null) {
                        playProgressListener.segmentTapped((int) ((upX - getPaddingLeft() - getPaddingRight()) / slotWidth));
                    }
                    return true;
                }

                break;
        }
        return false;
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
        invalidate();
    }

    private double distance(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
}
