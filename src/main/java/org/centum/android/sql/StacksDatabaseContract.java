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

import android.provider.BaseColumns;

/**
 * Created by Phani on 2/26/14.
 */
public final class StacksDatabaseContract {

    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = " INTEGER";
    public static final String NOT_NULL = " NOT NULL";
    public static final String FOREIGN_KEY_OPEN = " FOREIGN KEY (";
    public static final String FOREIGN_KEY_CLOSE = ") REFERENCES ";
    public static final String COMMA_SEP = ",";

    private StacksDatabaseContract() {
    }

    public static abstract class StackEntry implements BaseColumns {
        public static final String TABLE_NAME = "stacks";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_IS_QUIZLET = "isQuizlet";
        public static final String COLUMN_QUIZLET_ID = "quizledID";
        public static final String COLUMN_IS_ARCHIVED = "isArchived";


        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_DESCRIPTION + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_ICON + INT_TYPE + COMMA_SEP +
                        COLUMN_IS_QUIZLET + INT_TYPE + COMMA_SEP +
                        COLUMN_QUIZLET_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_IS_ARCHIVED + INT_TYPE + " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static abstract class CardEntry implements BaseColumns {
        public static final String TABLE_NAME = "cards";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_ATTACHMENT = "attachment";
        public static final String COLUMN_IS_ATTACHMENT_DETAIL = "isAttachmentDetails";
        public static final String COLUMN_IS_ARCHIVED = "isArchived";
        public static final String COLUMN_POSITION = "pos";
        public static final String COLUMN_PARENT_STACK = "parentStack";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_TITLE + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_DETAILS + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_ATTACHMENT + TEXT_TYPE + COMMA_SEP +
                        COLUMN_IS_ATTACHMENT_DETAIL + INT_TYPE + COMMA_SEP +
                        COLUMN_IS_ARCHIVED + INT_TYPE + COMMA_SEP +
                        COLUMN_POSITION + INT_TYPE + COMMA_SEP +
                        COLUMN_PARENT_STACK + INT_TYPE + COMMA_SEP +
                        FOREIGN_KEY_OPEN + COLUMN_PARENT_STACK + FOREIGN_KEY_CLOSE + StackEntry.TABLE_NAME + "(" + StackEntry._ID + ") ON DELETE CASCADE )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class PlaySessionEntry implements BaseColumns {
        public static final String TABLE_NAME = "playsessions";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_ENABLED = "enabled";
        public static final String COLUMN_CARDS = "cards";
        public static final String COLUMN_ANSWERS = "answers";
        public static final String COLUMN_PARENT_STACK = "parentStack";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_TIME + INT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_ENABLED + INT_TYPE + COMMA_SEP +
                        COLUMN_CARDS + TEXT_TYPE + COMMA_SEP +
                        COLUMN_ANSWERS + TEXT_TYPE + COMMA_SEP +
                        COLUMN_PARENT_STACK + INT_TYPE + COMMA_SEP +
                        FOREIGN_KEY_OPEN + COLUMN_PARENT_STACK + FOREIGN_KEY_CLOSE + StackEntry.TABLE_NAME + "(" + StackEntry._ID + ") ON DELETE CASCADE )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
