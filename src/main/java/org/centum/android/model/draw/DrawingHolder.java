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

import java.util.HashMap;

/**
 * Created by Phani on 3/15/14.
 */
public class DrawingHolder {

    private static DrawingHolder instance = null;
    private final HashMap<String, Drawing> drawings = new HashMap<String, Drawing>();

    public static DrawingHolder get() {
        if (instance == null) {
            instance = new DrawingHolder();
        }
        return instance;
    }

    public String holdDrawing(Drawing drawing) {
        String random = (int) (Math.random() * 1000) + "";
        while (drawings.keySet().contains(random)) {
            random = (int) (Math.random() * 1000) + "";
        }
        drawings.put(random, drawing);
        return random;
    }

    public Drawing popDrawing(String key) {
        if (drawings.keySet().contains(key)) {
            return drawings.remove(key);
        }
        return new Drawing();
    }

}
