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
package org.centum.android.sql;

import org.centum.android.events.CardEvent;
import org.centum.android.events.CardListener;
import org.centum.android.events.PlaySessionEvent;
import org.centum.android.events.PlaySessionListener;
import org.centum.android.events.StackEvent;
import org.centum.android.events.StackListener;
import org.centum.android.events.StackManagerEvent;
import org.centum.android.events.StackManagerListener;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.play.PlaySession;

/**
 * Created by Phani on 3/9/14.
 */
public class StacksDatabaseSyncWatcher implements StackManagerListener, StackListener, CardListener, PlaySessionListener {

    private StacksDatabaseHelper dbHelper;

    public StacksDatabaseSyncWatcher(StacksDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        StackManager.get().addListener(this);
        for (Stack stack : StackManager.get().getStackList()) {
            addListenersToStack(stack);
        }
        for (Stack stack : StackManager.get().getArchivedStackList()) {
            addListenersToStack(stack);
        }
    }

    @Override
    public void eventFired(StackManagerEvent evt) {
        switch (evt.getEvent()) {
            case StackManager.EVENT_STACK_ADDED:
                addListenersToStack(evt.getTarget());
                dbHelper.updateStack(evt.getTarget());
                log("Inserted stack " + evt.getTarget().getName());
                break;
            case StackManager.EVENT_STACK_REMOVED:
                removeListenersFromStack(evt.getTarget());
                dbHelper.deleteStack(evt.getTarget());
                log("Deleted stack " + evt.getTarget().getName());
                break;
            case StackManager.EVENT_ARCHIVED_STACK_ADDED:
                addListenersToStack(evt.getTarget());
                dbHelper.updateStack(evt.getTarget());
                log("Inserted archived stack " + evt.getTarget().getName());
                break;
            case StackManager.EVENT_ARCHIVED_STACK_REMOVED:
                removeListenersFromStack(evt.getTarget());
                dbHelper.deleteStack(evt.getTarget());
                log("Deleted archived stack " + evt.getTarget().getName());
                break;
            case StackManager.EVENT_STACK_ARCHIVE_STATUS_CHANGED:
                dbHelper.updateStackProperties(evt.getTarget());
                log("Updated stack properties (archive status) :" + evt.getTarget().getName());
        }
    }

    @Override
    public void eventFired(StackEvent evt) {
        switch (evt.getEvent()) {
            case Stack.EVENT_CARD_ADDED:
                evt.getTarget().addListener(this);
                dbHelper.updateCard(evt.getSource(), evt.getTarget());
                log("Inserted card " + evt.getTarget().getTitle());
                break;
            case Stack.EVENT_CARD_REMOVED:
                evt.getTarget().removeListener(this);
                dbHelper.deleteCard(evt.getTarget());
                log("Deleted card " + evt.getTarget().getTitle());
                break;
            case Stack.EVENT_PLAY_SESSION_ADDED:
                evt.getPsTarget().addListener(this);
                dbHelper.updatePlaySession(evt.getSource(), evt.getPsTarget());
                log("Updated playsession " + evt.getPsTarget().getName());
                break;
            case Stack.EVENT_PLAY_SESSION_REMOVED:
                evt.getPsTarget().removeListener(this);
                dbHelper.deletePlaySession(evt.getPsTarget());
                log("Deleted playsession " + evt.getPsTarget().getName());
                break;
            case Stack.EVENT_ARCHIVED_CARD_ADDED:
                evt.getTarget().addListener(this);
                dbHelper.updateCard(evt.getSource(), evt.getTarget());
                log("Updated archived card " + evt.getTarget().getTitle());
                break;
            case Stack.EVENT_ARCHIVED_CARD_REMOVED:
                evt.getTarget().removeListener(this);
                dbHelper.deleteCard(evt.getTarget());
                log("Deleted archived card " + evt.getTarget().getTitle());
                break;
            case Stack.EVENT_CARD_ARCHIVE_STATUS_CHANGED:
                dbHelper.updateCard(evt.getSource(), evt.getTarget());
                log("Updated card (archive status) " + evt.getTarget().getTitle());
                break;
            case Stack.EVENT_SELECTION_CHANGED:
                //Selection isn't persisted
                break;
            default:
                dbHelper.updateStackProperties(evt.getSource());
                log("Updated properties stack " + evt.getSource().getName());
                break;
        }
    }

    @Override
    public void eventFired(CardEvent evt) {
        if (evt.getEvent() != Card.EVENT_SELECTION_CHANGED) {
            for (Stack stack : StackManager.get().getStackList()) {
                if (stack.containsCard(evt.getSource())) {
                    dbHelper.updateCard(stack, evt.getSource());
                    log("Updated card " + evt.getSource().getTitle());
                    return;
                }
            }
            for (Stack stack : StackManager.get().getArchivedStackList()) {
                if (stack.containsCard(evt.getSource())) {
                    dbHelper.updateCard(stack, evt.getSource());
                    log("Updated card " + evt.getSource().getTitle());
                    return;
                }
            }

            log("Unexpected condition! Card is not a part of any stack");
        }
    }

    @Override
    public void eventFired(PlaySessionEvent evt) {
        for (Stack stack : StackManager.get().getStackList()) {
            if (stack.containsPlaySession(evt.getSource())) {
                dbHelper.updatePlaySession(stack, evt.getSource());
                log("Updated playsession " + evt.getSource().getName());
                return;
            }
        }
        for (Stack stack : StackManager.get().getArchivedStackList()) {
            if (stack.containsPlaySession(evt.getSource())) {
                dbHelper.updatePlaySession(stack, evt.getSource());
                log("Updated playsession " + evt.getSource().getName());
                return;
            }
        }

        log("Unexpected condition! PlaySession is not a part of any stack");
    }

    private void addListenersToStack(Stack stack) {
        stack.addListener(this);
        for (Card card : stack.getCards()) {
            card.addListener(this);
        }
        for (Card card : stack.getArchivedCards()) {
            card.addListener(this);
        }
        for (PlaySession playSession : stack.getPlaySessions()) {
            playSession.addListener(this);
        }
    }

    private void removeListenersFromStack(Stack stack) {
        stack.removeListener(this);
        for (Card card : stack.getCards()) {
            card.removeListener(this);
        }
        for (Card card : stack.getArchivedCards()) {
            card.removeListener(this);
        }
        for (PlaySession playSession : stack.getPlaySessions()) {
            playSession.removeListener(this);
        }
    }

    private void log(String msg) {
        //Log.d("StacksDatabaseSyncWatcher", msg);
    }
}
