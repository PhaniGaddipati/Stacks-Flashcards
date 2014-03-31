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

/**
 * Created by Phani on 2/18/14.
 */
public class GenericSet {

    private String title, description;
    private int id;
    private int term_count;
    private String type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description.length() > 200) {
            this.description = description.substring(0, 200);
        } else {
            this.description = description;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTermCount() {
        return term_count;
    }

    public void setTermCount(int term_count) {
        this.term_count = term_count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object set) {
        if (set instanceof GenericSet) {
            GenericSet qs = (GenericSet) set;
            return qs.getTitle().equals(getTitle()) &&
                    qs.getDescription().equals(getDescription()) &&
                    qs.getId() == getId() &&
                    qs.getTermCount() == getTermCount();
        }
        return false;
    }
}
