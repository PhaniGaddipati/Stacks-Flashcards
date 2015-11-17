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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import org.centum.android.model.Card;
import org.centum.android.model.LoadedData;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.play.PlaySession;
import org.centum.android.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Phani on 2/26/14.
 */
public class StacksDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "stacks.db";
    private static StacksDatabaseHelper instance = null;
    private final Context context;
    private boolean isSaving = false;

    private StacksDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static StacksDatabaseHelper get(Context context) {
        if (instance == null) {
            instance = new StacksDatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        log("onCreate");
        sqLiteDatabase.execSQL(StacksDatabaseContract.StackEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(StacksDatabaseContract.CardEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(StacksDatabaseContract.PlaySessionEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        if (i == 1 && i2 == 2) {
            //Add column for quizlet term id
            log("Upgrading Database");
            sqLiteDatabase.execSQL(StacksDatabaseContract.CardEntry.SQL_ADD_TERM_ID_COLUMN);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    private synchronized boolean isSaving() {
        return isSaving;
    }

    private synchronized void setSaving(boolean saving) {
        isSaving = saving;
    }

    public void executeBulkOperation(Runnable runnable) {
        StacksDatabaseHelper.get(null).getWritableDatabase().beginTransaction();
        runnable.run();
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
    }

    public void saveStacks() {
        if (!isSaving()) {
            setSaving(true);
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    long start = System.currentTimeMillis();
                    getWritableDatabase().beginTransaction();
                    for (Stack stack : StackManager.get().getStackList()) {
                        updateStack(stack);
                    }
                    for (Stack stack : StackManager.get().getArchivedStacksList()) {
                        updateStack(stack);
                    }
                    deleteOrphanedStacksInDB();
                    getWritableDatabase().setTransactionSuccessful();
                    getWritableDatabase().endTransaction();
                    long stop = System.currentTimeMillis();
                    log("Saved SQL DB in " + (stop - start) + "ms");
                    setSaving(false);
                    return null;
                }
            }.execute();
        }
    }

    public LoadedData loadStacks() {
        LoadedData loadedData = new LoadedData();
        long start = System.currentTimeMillis();
        Cursor cursor = getReadableDatabase().query(StacksDatabaseContract.StackEntry.TABLE_NAME,
                null, null, null, null, null, null);
        cursor.moveToFirst();

        long sqlID;
        String name, description;
        int icon, quizletID;
        boolean isQuizlet, isArchived;
        Stack stack;

        while (!cursor.isAfterLast()) {
            sqlID = cursor.getLong(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry._ID));
            name = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry.COLUMN_NAME));
            description = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry.COLUMN_DESCRIPTION));
            icon = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry.COLUMN_ICON));
            isQuizlet = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry.COLUMN_IS_QUIZLET)) == 1;
            quizletID = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry.COLUMN_QUIZLET_ID));
            isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry.COLUMN_IS_ARCHIVED)) == 1;

            stack = new Stack(name);
            stack.setSqlID(sqlID);
            stack.setDescription(description);
            stack.setIcon(icon);
            stack.setQuizletStack(isQuizlet);
            stack.setQuizletID(quizletID);

            List<Card> cardList = loadCardsForStack(sqlID);
            List<Card> archivedCardList = loadArchivedCardsForStack(sqlID);
            for (Card card : cardList) {
                stack.quickAddCard(card);
            }
            for (Card card : archivedCardList) {
                stack.quickAddArchivedCard(card);
            }

            List<PlaySession> playSessions = loadPlaySessionsForStack(stack);
            for (PlaySession playSession : playSessions) {
                stack.addPlaySession(playSession);
            }

            if (isArchived) {
                loadedData.addLoadedArchivedStack(stack);
            } else {
                loadedData.addLoadedStack(stack);
            }

            cursor.move(1);
        }
        cursor.close();
        long stop = System.currentTimeMillis();
        log("Loaded SQL DB in " + (stop - start) + "ms");
        if (EasyTracker.getInstance(context) != null) {
            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.KEY_PREF_ANALYTICS_OPTOUT, false))
                EasyTracker.getInstance(context).send(MapBuilder.createTiming("data", stop - start, "load sql db", null).build());
        }
        return loadedData;
    }

    private void resetDB() {
        getWritableDatabase().execSQL(StacksDatabaseContract.StackEntry.SQL_DELETE_ENTRIES);
        getWritableDatabase().execSQL(StacksDatabaseContract.PlaySessionEntry.SQL_DELETE_ENTRIES);
        getWritableDatabase().execSQL(StacksDatabaseContract.CardEntry.SQL_DELETE_ENTRIES);
        onCreate(getWritableDatabase());
    }

    private void deleteOrphanedStacksInDB() {
        List<Long> stacksInDB = new LinkedList<Long>();

        Cursor cursor = getReadableDatabase().query(StacksDatabaseContract.StackEntry.TABLE_NAME,
                new String[]{StacksDatabaseContract.StackEntry._ID}, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            stacksInDB.add(cursor.getLong(cursor.getColumnIndexOrThrow(StacksDatabaseContract.StackEntry._ID)));
            cursor.move(1);
        }
        cursor.close();

        for (Long stackID : stacksInDB) {
            if (!containsStackSqlID(stackID)) {
                getWritableDatabase().delete(StacksDatabaseContract.StackEntry.TABLE_NAME,
                        StacksDatabaseContract.StackEntry._ID + "=" + stackID, null);
                getWritableDatabase().delete(StacksDatabaseContract.PlaySessionEntry.TABLE_NAME,
                        StacksDatabaseContract.PlaySessionEntry.COLUMN_PARENT_STACK + "=" + stackID, null);
                getWritableDatabase().delete(StacksDatabaseContract.CardEntry.TABLE_NAME,
                        StacksDatabaseContract.CardEntry.COLUMN_PARENT_STACK + "=" + stackID, null);
            }
        }
    }

    private boolean containsStackSqlID(long stackID) {
        for (Stack s : StackManager.get().getStackList()) {
            if (s.getSqlID() == stackID) {
                return true;
            }
        }
        for (Stack s : StackManager.get().getArchivedStacksList()) {
            if (s.getSqlID() == stackID) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCardSqlID(Stack stack, long cardID) {
        for (Card c : stack.getCardList()) {
            if (c.getSqlID() == cardID) {
                return true;
            }
        }
        return false;
    }

    private List<PlaySession> loadPlaySessionsForStack(Stack stack) {
        Cursor cursor = getWritableDatabase().query(StacksDatabaseContract.PlaySessionEntry.TABLE_NAME,
                null, StacksDatabaseContract.PlaySessionEntry.COLUMN_PARENT_STACK + "=" + stack.getSqlID()
                , null, null, null, null);

        List<PlaySession> playSessions = new ArrayList<PlaySession>(cursor.getCount());

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.PlaySessionEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.PlaySessionEntry.COLUMN_NAME));
            long time = cursor.getLong(cursor.getColumnIndexOrThrow(StacksDatabaseContract.PlaySessionEntry.COLUMN_TIME));
            boolean enabled = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.PlaySessionEntry.COLUMN_ENABLED)) == 1;
            String card = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.PlaySessionEntry.COLUMN_CARDS));
            String ans = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.PlaySessionEntry.COLUMN_ANSWERS));
            String cards[] = card.split(",");
            String answers[] = ans.split(",");

            PlaySession playSession = new PlaySession(name);
            playSession.setTime(time);
            playSession.setEnabled(enabled);
            playSession.setSqlID(id);

            for (int i = 0; i < cards.length; i++) {
                try {
                    Card c = getCard(stack, Long.parseLong(cards[i]));
                    if (c != null) {
                        playSession.setSessionStat(c, Integer.parseInt(answers[i]));
                    }
                } catch (Exception e) {
                    if (!(e instanceof NumberFormatException))
                        e.printStackTrace();
                }
            }

            playSessions.add(playSession);
            cursor.move(1);
        }
        cursor.close();
        return playSessions;
    }

    private Card getCard(Stack stack, long sqlID) {
        for (Card c : stack.getCardList()) {
            if (c.getSqlID() == sqlID) {
                return c;
            }
        }
        return null;
    }

    private List<Card> loadArchivedCardsForStack(long sqlID) {
        Cursor cursor = getWritableDatabase().query(StacksDatabaseContract.CardEntry.TABLE_NAME,
                null, StacksDatabaseContract.CardEntry.COLUMN_PARENT_STACK + "=" + sqlID + " AND " +
                        StacksDatabaseContract.CardEntry.COLUMN_IS_ARCHIVED + "=" + 1, null, null, null, null
        );

        return loadCards(cursor);
    }

    private List<Card> loadCardsForStack(long sqlID) {
        Cursor cursor = getWritableDatabase().query(StacksDatabaseContract.CardEntry.TABLE_NAME,
                null, StacksDatabaseContract.CardEntry.COLUMN_PARENT_STACK + "=" + sqlID + " AND " +
                        StacksDatabaseContract.CardEntry.COLUMN_IS_ARCHIVED + "=" + 0, null, null, null, null
        );

        return loadCards(cursor);
    }

    private List<Card> loadCards(Cursor cursor) {
        List<Card> cards = new ArrayList<Card>(cursor.getCount());

        String name, details, attachment;
        int id, pos, quizletTermId;
        boolean isAttachmentDetail;
        Card card;

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.CardEntry._ID));
            name = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.CardEntry.COLUMN_TITLE));
            details = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.CardEntry.COLUMN_DETAILS));
            attachment = cursor.getString(cursor.getColumnIndexOrThrow(StacksDatabaseContract.CardEntry.COLUMN_ATTACHMENT));
            isAttachmentDetail = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.CardEntry.COLUMN_IS_ATTACHMENT_DETAIL)) == 1;
            //pos = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.CardEntry.COLUMN_POSITION));
            quizletTermId = cursor.getInt(cursor.getColumnIndexOrThrow(StacksDatabaseContract.CardEntry.COLUMN_QUIZLET_TERM_ID));

            card = new Card(name, details);
            card.setAttachment(attachment);
            card.setAttachmentPartOfDetails(isAttachmentDetail);
            card.setSqlID(id);
            card.setQuizletTermID(quizletTermId);

            cards.add(card);
            cursor.move(1);
        }
        cursor.close();

        return cards;
    }

    public boolean deletePlaySession(PlaySession playSession) {
        int ret = getWritableDatabase().delete(StacksDatabaseContract.PlaySessionEntry.TABLE_NAME,
                StacksDatabaseContract.PlaySessionEntry._ID + "=?", new String[]{playSession.getSqlID() + ""});
        if (ret > 0) {
            playSession.setSqlID(-1);
        }
        return ret > 0;
    }

    public boolean updatePlaySession(Stack stack, PlaySession playSession) {
        if (playSession.getSqlID() < 0) {
            return insertPlaySession(stack, playSession);
        } else {
            ContentValues contentValues = getPlaySessionContentValues(stack, playSession);
            int ret = getWritableDatabase().update(StacksDatabaseContract.PlaySessionEntry.TABLE_NAME, contentValues,
                    StacksDatabaseContract.PlaySessionEntry._ID + "=?", new String[]{playSession.getSqlID() + ""});
            return ret > 1;
        }
    }

    public boolean insertPlaySession(Stack stack, PlaySession playSession) {
        ContentValues contentValues = getPlaySessionContentValues(stack, playSession);
        long ret = getWritableDatabase().insert(StacksDatabaseContract.PlaySessionEntry.TABLE_NAME, null, contentValues);
        playSession.setSqlID(ret);
        return ret != -1;
    }

    public boolean deleteCard(Card card) {
        int ret = getWritableDatabase().delete(StacksDatabaseContract.CardEntry.TABLE_NAME,
                StacksDatabaseContract.CardEntry._ID + "=?", new String[]{card.getSqlID() + ""});
        if (ret > 0) {
            card.setSqlID(-1);
        }
        return ret > 0;
    }

    public boolean updateCard(Stack stack, Card card) {
        if (card.getSqlID() < 0) {
            return insertCard(stack, card);
        } else {
            ContentValues contentValues = getCardContentValues(stack, card);
            int ret = getWritableDatabase().update(StacksDatabaseContract.CardEntry.TABLE_NAME, contentValues,
                    StacksDatabaseContract.CardEntry._ID + "=?", new String[]{card.getSqlID() + ""});
            return ret > 1;
        }
    }

    public boolean insertCard(Stack stack, Card card) {
        ContentValues contentValues = getCardContentValues(stack, card);
        long ret = getWritableDatabase().insert(StacksDatabaseContract.CardEntry.TABLE_NAME, null, contentValues);
        card.setSqlID(ret);
        return ret != -1;
    }

    public boolean deleteStack(Stack stack) {
        int ret = getWritableDatabase().delete(StacksDatabaseContract.StackEntry.TABLE_NAME,
                StacksDatabaseContract.StackEntry._ID + "=?", new String[]{stack.getSqlID() + ""});
        if (ret > 0) {
            stack.setSqlID(-1);
        }
        return ret > 0;
    }

    public boolean updateStackProperties(Stack stack) {
        ContentValues contentValues = getStackContentValues(stack);
        int ret = getWritableDatabase().update(StacksDatabaseContract.StackEntry.TABLE_NAME, contentValues,
                StacksDatabaseContract.StackEntry._ID + "=?", new String[]{stack.getSqlID() + ""});
        return ret > 1;
    }

    public void updateStack(Stack stack) {
        if (stack.getSqlID() < 0) {
            insertStack(stack);
        } else {
            getWritableDatabase().beginTransaction();
            updateStackProperties(stack);
            for (Card card : stack.getCardList()) {
                updateCard(stack, card);
            }
            for (Card card : stack.getArchivedCards()) {
                updateCard(stack, card);
            }
            for (PlaySession playSession : stack.getPlaySessions()) {
                updatePlaySession(stack, playSession);
            }
            getWritableDatabase().setTransactionSuccessful();
            getWritableDatabase().endTransaction();
        }
    }

    public boolean insertStack(Stack stack) {
        ContentValues contentValues = getStackContentValues(stack);
        getWritableDatabase().beginTransaction();
        long ret = getWritableDatabase().insert(StacksDatabaseContract.StackEntry.TABLE_NAME, null, contentValues);
        stack.setSqlID(ret);
        for (Card card : stack.getCardList()) {
            insertCard(stack, card);
        }
        for (Card card : stack.getArchivedCards()) {
            insertCard(stack, card);
        }
        for (PlaySession playSession : stack.getPlaySessions()) {
            insertPlaySession(stack, playSession);
        }
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
        return (ret != -1);
    }

    private ContentValues getPlaySessionContentValues(Stack stack, PlaySession playSession) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StacksDatabaseContract.PlaySessionEntry.COLUMN_NAME, playSession.getName());
        contentValues.put(StacksDatabaseContract.PlaySessionEntry.COLUMN_TIME, playSession.getTime());
        contentValues.put(StacksDatabaseContract.PlaySessionEntry.COLUMN_ENABLED, playSession.isEnabled() ? 1 : 0);
        contentValues.put(StacksDatabaseContract.PlaySessionEntry.COLUMN_PARENT_STACK, stack == null ? -1 : stack.getSqlID());

        StringBuilder cardsBuilder = new StringBuilder();
        StringBuilder answersBuilder = new StringBuilder();

        for (Card card : playSession.getCards()) {
            cardsBuilder.append(card.getSqlID());
            cardsBuilder.append(",");
            answersBuilder.append(playSession.getSessionStat(card) + ",");
            answersBuilder.append(",");
        }
        String cards = cardsBuilder.toString();
        String ans = answersBuilder.toString();
        if (cards.length() > 1) {
            cards = cards.substring(0, cards.length() - 1);
            ans = ans.substring(0, ans.length() - 1);
        }

        contentValues.put(StacksDatabaseContract.PlaySessionEntry.COLUMN_CARDS, cards);
        contentValues.put(StacksDatabaseContract.PlaySessionEntry.COLUMN_ANSWERS, ans);

        return contentValues;
    }

    private ContentValues getCardContentValues(Stack stack, Card card) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_TITLE, card.getTitle());
        contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_DETAILS, card.getDetails());
        contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_ATTACHMENT, card.getAttachment());
        contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_IS_ATTACHMENT_DETAIL, card.isAttachmentPartOfDetails() ? 1 : 0);
        if (stack != null) {
            contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_IS_ARCHIVED, stack.getArchivedCards().contains(card) ? 1 : 0);
            contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_PARENT_STACK, stack.getSqlID());
            contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_POSITION, stack.getCardPosition(card));
        }
        contentValues.put(StacksDatabaseContract.CardEntry.COLUMN_QUIZLET_TERM_ID, card.getQuizletTermID());
        return contentValues;
    }

    private ContentValues getStackContentValues(Stack stack) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StacksDatabaseContract.StackEntry.COLUMN_NAME, stack.getName());
        contentValues.put(StacksDatabaseContract.StackEntry.COLUMN_DESCRIPTION, stack.getDescription());
        contentValues.put(StacksDatabaseContract.StackEntry.COLUMN_ICON, stack.getIcon());
        contentValues.put(StacksDatabaseContract.StackEntry.COLUMN_IS_QUIZLET, stack.isQuizletStack() ? 1 : 0);
        contentValues.put(StacksDatabaseContract.StackEntry.COLUMN_QUIZLET_ID, stack.getQuizletID());
        contentValues.put(StacksDatabaseContract.StackEntry.COLUMN_IS_ARCHIVED, StackManager.get().getArchivedStack(stack.getName()) == null ? 0 : 1);
        return contentValues;
    }

    private void log(String string) {
        Log.d("StacksDatabaseHelper", string);
    }
}
