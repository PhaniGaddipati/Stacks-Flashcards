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

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Phani on 2/20/14.
 */
public class LoadedData {

    private final List<Stack> loadedStacks = new LinkedList<Stack>();
    private final List<Stack> loadedArchivedStacks = new LinkedList<Stack>();

    public void addLoadedStack(Stack stack) {
        loadedStacks.add(stack);
    }

    public void addLoadedArchivedStack(Stack stack) {
        loadedArchivedStacks.add(stack);
    }

    public List<Stack> getLoadedStacks() {
        return loadedStacks;
    }

    public List<Stack> getLoadedArchivedStacks() {
        return loadedArchivedStacks;
    }
}
