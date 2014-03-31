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
package org.centum.android.integration;

import org.centum.android.model.Stack;

/**
 * Created by Phani on 3/27/2014.
 */
public interface Communicator {

    public String getName();

    public String getAttributionText();

    public String getAttributionURL();

    public boolean isStackAccessible(int setID) throws Exception;

    public Stack getStack(int setID) throws Exception;

    public GenericSet[] getKeywordSets(String keyword) throws Exception;

    public GenericSet[] getUserSets(String username) throws Exception;

}
