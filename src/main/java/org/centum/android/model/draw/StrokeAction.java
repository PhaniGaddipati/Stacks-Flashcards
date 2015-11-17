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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Phani on 3/11/14.
 */
public class StrokeAction implements DrawAction {

    private final Paint paint;
    private Path path = new Path();
    private boolean eraser = false;

    public StrokeAction() {
        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setEraser() {
        eraser = true;
    }

    public boolean isEraser() {
        return eraser;
    }

    public int getRadius() {
        return (int) (paint.getStrokeWidth() / 2);
    }

    public void setRadius(int radius) {
        paint.setStrokeWidth(radius * 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public Rect getBounds() {
        RectF bounds = new RectF();
        int margins = (int) paint.getStrokeWidth();
        path.computeBounds(bounds, true);
        return new Rect((int) bounds.left - margins / 2, (int) bounds.top - margins / 2, (int) bounds.right + margins / 2, (int) bounds.bottom + margins / 2);
    }
}
