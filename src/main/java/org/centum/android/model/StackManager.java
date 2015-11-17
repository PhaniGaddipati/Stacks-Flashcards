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

import org.centum.android.model.events.StackManagerEvent;
import org.centum.android.model.events.StackManagerListener;

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
    private final List<StackManagerListener> listeners = new ArrayList<StackManagerListener>();
    private final List<Stack> stacks = new ArrayList<Stack>();
    private final List<Stack> archivedStacks = new ArrayList<Stack>();

    private StackManager() {
    }

    public static StackManager get() {
        instance = instance == null ? new StackManager() : instance;
        return instance;
    }

    public static String getValidName(String name) {
        if (!StackManager.get().containsStack(name)) {
            return name;
        }
        int i = 1;
        while (StackManager.get().containsStack(name + " (" + i + ")")) {
            i++;
        }
        return name + " (" + i + ")";
    }

    public void setData(LoadedData loadedData) {
        List<Stack> archivedStacks = loadedData.getLoadedArchivedStacks();
        List<Stack> stacks = loadedData.getLoadedStacks();

        removeAllArchivedStacks();
        removeAllStacks();

        for (Stack stack : stacks) {
            StackManager.get().addStack(stack);
        }
        for (Stack stack : archivedStacks) {
            StackManager.get().addArchiveStack(stack);
        }
    }

    public void addStack(Stack stack, int pos) {
        if (archivedStacks.contains(stack)) {
            restoreArchivedStack(stack);
            return;
        } else if (containsStack(stack)) {
            stack.setName(getValidName(stack.getName()));
        }
        stacks.add(Math.max(0, Math.min(stacks.size(), pos)), stack);
        fireEvent(stack, EVENT_STACK_ADDED);
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

    public void addStack(Stack stack) {
        addStack(stack, stacks.size());
    }

    public boolean removeStack(Stack stack) {
        return removeStack(stack, true);
    }

    public Stack getStack(int i) {
        return stacks.get(i);
    }

    public boolean containsStack(Stack stack) {
        return containsStack(stack.getName());
    }

    public boolean containsStack(String input) {
        return (getStack(input) != null || getArchivedStack(input) != null);
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

    public void addArchiveStack(Stack stack) {
        if (containsArchivedStack(stack)) {
            stack.setName(getValidName(stack.getName()));
        }
        archivedStacks.add(stack);
        fireEvent(stack, EVENT_ARCHIVED_STACK_ADDED);
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

    public Stack getArchivedStack(int i) {
        return archivedStacks.get(i);
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

    public List<Stack> getArchivedStacksList() {
        return archivedStacks;
    }

    public int getNumberOfArchivedStacks() {
        return archivedStacks.size();
    }

    public void setCurrentStack(Stack stack) {
        for (Stack s : stacks) {
            if (s != stack)
                s.setCurrentStack(false);
        }
        for (Stack s : archivedStacks) {
            if (s != stack)
                s.setCurrentStack(false);
        }
        if (stack != null) {
            stack.setCurrentStack(true);
        }
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
