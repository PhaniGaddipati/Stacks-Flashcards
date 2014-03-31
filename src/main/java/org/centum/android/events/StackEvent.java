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
package org.centum.android.events;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;

public class StackEvent {

    private final Stack source;
    private final Card target;
    private final PlaySession psTarget;
    private final int event;

    public StackEvent(Stack source, Card target, PlaySession psTarget, int event) {
        this.source = source;
        this.target = target;
        this.psTarget = psTarget;
        this.event = event;
    }

    public PlaySession getPsTarget() {
        return psTarget;
    }

    public Stack getSource() {
        return source;
    }

    public Card getTarget() {
        return target;
    }

    public int getEvent() {
        return event;
    }

}
