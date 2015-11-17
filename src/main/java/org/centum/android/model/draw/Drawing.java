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
package org.centum.android.model.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import org.centum.android.model.events.DrawingEvent;
import org.centum.android.model.events.DrawingListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Phani on 3/12/14.
 */
public class Drawing {

    public static final int EVENT_DRAW_ACTION_ADDED = 0;
    public static final int EVENT_DRAW_ACTION_REMOVED = 1;
    public static final int EVENT_DRAWING_RESIZED = 2;
    public static final int EVENT_DRAWING_INVALIDATED = 3;
    public static final int EVENT_BITMAP_VALIDATED = 4;
    private final List<DrawAction> actions = new LinkedList<DrawAction>();
    private final List<DrawAction> redoActions = new LinkedList<DrawAction>();
    private final List<DrawingListener> listeners = new ArrayList<DrawingListener>();
    private int width = 1000, height = 1000;
    private int backgroundColor = Color.WHITE;

    private Bitmap bitmap;
    private Canvas canvas;

    private boolean valid = false;

    public synchronized void renderDrawing(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(backgroundColor);
        for (DrawAction action : actions) {
            if (action instanceof StrokeAction && ((StrokeAction) action).isEraser()) {
                ((StrokeAction) action).setColor(backgroundColor);
            }
            action.onDraw(canvas);
        }
    }

    public synchronized Bitmap renderBitmap() {
        if (valid) {
            return bitmap;
        }

        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }

        renderDrawing(canvas);

        valid = true;
        fireEvent(null, EVENT_BITMAP_VALIDATED);
        return bitmap;
    }

    public Bitmap getScaledBitmap(float scale) {
        return Bitmap.createScaledBitmap(renderBitmap(), (int) (width * scale), (int) (height * scale), true);
    }

    public synchronized void addDrawAction(DrawAction action) {
        actions.add(action);
        invalidate();
        fireEvent(action, EVENT_DRAW_ACTION_ADDED);
    }

    public synchronized void removeDrawAction(DrawAction action) {
        actions.remove(action);
        redoActions.add(action);
        invalidate();
        fireEvent(action, EVENT_DRAW_ACTION_REMOVED);
    }

    public void undoLastAction() {
        if (actions.size() > 0) {
            removeDrawAction(actions.get(actions.size() - 1));
        }
    }

    public void redoLastAction() {
        if (redoActions.size() > 0) {
            DrawAction action = redoActions.get(redoActions.size() - 1);
            addDrawAction(action);
            redoActions.remove(action);
        }
    }

    public void clear() {
        while (actions.size() > 0) {
            removeDrawAction(actions.get(0));
        }
    }

    public DrawAction getDrawAction(int i) {
        invalidate(); //actions may be modified
        return actions.get(i);
    }

    public DrawAction getLastDrawAction() {
        return getDrawAction(actions.size() - 1);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (this.width != width) {
            this.width = width;
            invalidate();
            fireEvent(null, EVENT_DRAWING_RESIZED);
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (this.height != height) {
            this.height = height;
            invalidate();
            fireEvent(null, EVENT_DRAWING_RESIZED);
        }
    }

    public Rect getActionBounds() {
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;

        Rect bounds;
        for (DrawAction action : actions) {
            bounds = action.getBounds();
            left = Math.min(left, bounds.left);
            top = Math.min(top, bounds.top);
            right = Math.max(right, bounds.right);
            bottom = Math.max(bottom, bounds.bottom);
        }

        return new Rect(left, top, right, bottom);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public void invalidate() {
        valid = false;
        fireEvent(null, EVENT_DRAWING_INVALIDATED);
    }

    private void fireEvent(DrawAction action, int eventDrawActionRemoved) {
        DrawingEvent evt = new DrawingEvent(eventDrawActionRemoved, this, action);
        for (DrawingListener listener : listeners) {
            listener.eventFired(evt);
        }
    }

    public void addDrawingListener(DrawingListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeDrawingListener(DrawingListener listener) {
        listeners.remove(listener);
    }

    public int getNumberOfActions() {
        return actions.size();
    }
}
