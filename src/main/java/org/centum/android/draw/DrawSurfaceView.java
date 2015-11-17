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
package org.centum.android.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.centum.android.model.draw.Drawing;
import org.centum.android.model.draw.StrokeAction;
import org.centum.android.model.events.DrawingEvent;
import org.centum.android.model.events.DrawingListener;

/**
 * Created by Phani on 3/11/14.
 */
public class DrawSurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener, DrawingListener, Runnable {

    private float zoomFactor = 1;

    private boolean surfaceCreated = false;
    private int strokeRadius = 5, color = Color.BLACK;

    private Paint paint = new Paint();
    private Drawing drawing;

    private Thread updateThread;
    private boolean updated = false;

    private boolean eraser = false;

    public DrawSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);
        setWillNotDraw(false);
    }

    public DrawSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);
        setWillNotDraw(false);
    }

    public DrawSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);
        setWillNotDraw(false);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (canvas != null) {
            drawing.renderDrawing(canvas);
            updated = true;
        }
    }


    private void update() {
        if (surfaceCreated) {
            Canvas canvas = getHolder().lockCanvas();
            draw(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(float zoom) {
        zoomFactor = zoom;
        updateDrawingDimensions(getWidth(), getHeight());
        setNotUpdated();
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public int getStrokeRadius() {
        return strokeRadius;
    }

    public void setStrokeRadius(int strokeRadius) {
        this.strokeRadius = strokeRadius;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        eraser = false;
    }

    public void setEraser(boolean eraser) {
        this.eraser = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceCreated = true;
        updateThread = new Thread(this);
        updateThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateDrawingDimensions(width, height);
    }

    private void updateDrawingDimensions(int width, int height) {
        if (width / zoomFactor > drawing.getWidth()) {
            drawing.setWidth((int) (width / zoomFactor));
        }
        if (height / zoomFactor > drawing.getHeight()) {
            drawing.setHeight((int) (height / zoomFactor));
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceCreated = false;
        updateThread.interrupt();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                StrokeAction currentStrokeAction = new StrokeAction();
                currentStrokeAction.setRadius(strokeRadius);
                currentStrokeAction.setColor(color);
                if (eraser) {
                    currentStrokeAction.setEraser();
                }
                currentStrokeAction.getPath().moveTo(event.getX() / zoomFactor, event.getY() / zoomFactor);
                currentStrokeAction.getPath().lineTo(event.getX() / zoomFactor + 1, event.getY() / zoomFactor);
                drawing.addDrawAction(currentStrokeAction);
                setNotUpdated();
                return true;
            case MotionEvent.ACTION_MOVE:
                ((StrokeAction) drawing.getLastDrawAction()).getPath().lineTo(event.getX() / zoomFactor, event.getY() / zoomFactor);
                drawing.invalidate();
                setNotUpdated();
                return true;
            case MotionEvent.ACTION_UP:
                setNotUpdated();
                return true;
        }

        return false;
    }

    public void setDrawing(Drawing newDrawing) {
        if (this.drawing != null) {
            drawing.removeDrawingListener(this);
        }
        drawing = newDrawing;
        drawing.addDrawingListener(this);
        setNotUpdated();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (!isUpdated()) {
                update();
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                //Expected
            }
        }
    }

    public synchronized void setNotUpdated() {
        updated = false;
    }

    public synchronized boolean isUpdated() {
        return updated;
    }

    @Override
    public void eventFired(DrawingEvent evt) {
        setNotUpdated();
    }

}
