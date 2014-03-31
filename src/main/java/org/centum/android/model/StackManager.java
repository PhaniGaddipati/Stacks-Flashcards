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
package org.centum.android.model;

import org.centum.android.events.StackManagerEvent;
import org.centum.android.events.StackManagerListener;

import java.util.ArrayList;
import java.util.List;


public class StackManager {

    public static final int EVENT_STACK_ADDED = 0;
    public static final int EVENT_STACK_REMOVED = 1;
    public static final int EVENT_STACK_MOVED = 2;
    public static final int EVENT_ARCHIVED_STACK_ADDED = 3;
    public static final int EVENT_ARCHIVED_STACK_REMOVED = 4;
    public static final int EVENT_STACK_ARCHIVE_STATUS_CHANGED = 5;
    private static final String DEBUG_TAG = "StackManager";
    private static StackManager instance = null;
    private List<StackManagerListener> listeners = new ArrayList<StackManagerListener>();
    private List<Stack> stacks = new ArrayList<Stack>();
    private List<Stack> archivedStacks = new ArrayList<Stack>();

    private StackManager() {
    }

    public static StackManager get() {
        instance = instance == null ? new StackManager() : instance;
        return instance;
    }

    public boolean addStack(Stack stack, int pos) {
        if (containsArchivedStack(stack)) {
            restoreArchivedStack(stack);
            return true;
        } else if (!containsStack(stack)) {
            stacks.add(Math.max(0, Math.min(stacks.size(), pos)), stack);
            fireEvent(stack, EVENT_STACK_ADDED);
            return true;
        }
        return false;
    }

    public boolean removeStack(Stack stack, boolean archive) {
        if (stack != null) {
            stacks.remove(stack);
            if (!archive) {
                fireEvent(stack, EVENT_STACK_REMOVED);
            } else {
                archivedStacks.add(stack);
                fireEvent(stack, EVENT_STACK_ARCHIVE_STATUS_CHANGED);
            }
            return true;
        }
        return false;
    }

    public void moveStack(Stack stack, int pos) {
        if (stack != null) {
            stacks.remove(stack);
            stacks.add(pos, stack);
            fireEvent(stack, EVENT_STACK_MOVED);
        }
    }

    public void undoRemoveStack() {
        if (archivedStacks.size() > 0) {
            restoreArchivedStack(archivedStacks.size() - 1);
        }
    }

    public int getStackPosition(Stack stack) {
        for (int i = 0; i < getNumberOfStacks(); i++) {
            if (stacks.get(i) == stack) {
                return i;
            }
        }
        return -1;
    }

    public int getNumberOfStacks() {
        return stacks.size();
    }

    public Stack getStack(String name) {
        for (Stack stack : stacks) {
            if (stack.getName().equals(name)) {
                return stack;
            }
        }
        return null;
    }

    public boolean addStack(Stack stack) {
        return addStack(stack, stacks.size());
    }

    public boolean removeStack(Stack stack) {
        return removeStack(stack, true);
    }

    public Stack getStack(int i) {
        return stacks.get(i);
    }

    public Stack[] getStacks() {
        return stacks.toArray(new Stack[]{});
    }

    public boolean containsStack(Stack stack) {
        return containsStack(stack.getName());
    }

    public boolean containsStack(String input) {
        return getStack(input) != null;
    }

    public String[] getStackNames() {
        String[] names = new String[getNumberOfStacks()];
        for (int i = 0; i < getNumberOfStacks(); i++) {
            names[i] = stacks.get(i).getName();
        }
        return names;
    }

    public List<Stack> getStackList() {
        return stacks;
    }

    public void removeAllStacks() {
        while (stacks.size() > 0) {
            stacks.remove(0);
        }
    }

    /**
     * ****************************ARCHIVED STACKS******************************
     */

    public boolean addArchiveStack(Stack stack) {
        if (!containsArchivedStack(stack)) {
            archivedStacks.add(stack);
            fireEvent(stack, EVENT_ARCHIVED_STACK_ADDED);
            return true;
        }
        return false;
    }

    public void removeArchivedStack(Stack stack) {
        archivedStacks.remove(stack);
        fireEvent(stack, EVENT_ARCHIVED_STACK_REMOVED);
    }

    public Stack getArchivedStack(String name) {
        for (Stack stack : archivedStacks) {
            if (stack.getName().equals(name)) {
                return stack;
            }
        }
        return null;
    }

    public void removeArchivedStack(int stack) {
        removeArchivedStack(archivedStacks.get(stack));
    }

    public void removeAllArchivedStacks() {
        while (archivedStacks.size() > 0) {
            removeArchivedStack(0);
        }
    }

    public void restoreArchivedStack(Stack stack) {
        archivedStacks.remove(stack);
        String name = stack.getName();
        if (containsStack(name)) {
            int suffix = 1;
            while (getStack(name + " (" + suffix + ")") != null) {
                suffix++;
            }
            stack.setName(name + " (" + suffix + ")");
        }
        stacks.add(stack);
        fireEvent(stack, EVENT_STACK_ARCHIVE_STATUS_CHANGED);
    }

    public void restoreArchivedStack(int i) {
        restoreArchivedStack(archivedStacks.get(i));
    }

    public void restoreAllArchivedStacks() {
        while (getNumberOfArchivedStacks() > 0) {
            restoreArchivedStack(archivedStacks.get(0));
        }
    }

    public boolean containsArchivedStack(Stack stack) {
        return containsArchivedStack(stack.getName());
    }

    public boolean containsArchivedStack(String name) {
        return getArchivedStack(name) != null;
    }

    public Stack[] getArchivedStacks() {
        return archivedStacks.toArray(new Stack[]{});
    }

    public int getNumberOfArchivedStacks() {
        return archivedStacks.size();
    }

    /**
     * ****************************EVENTS******************************
     */

    public void addListener(StackManagerListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(StackManagerListener listener) {
        listeners.remove(listener);
    }

    private void fireEvent(StackManagerEvent evt) {
        for (StackManagerListener listener : listeners) {
            listener.eventFired(evt);
        }
    }

    private void fireEvent(Stack stack, int evt) {
        fireEvent(new StackManagerEvent(stack, evt));
    }

    public List<Stack> getArchivedStackList() {
        return archivedStacks;
    }
}
