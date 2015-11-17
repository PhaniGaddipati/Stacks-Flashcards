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
package org.centum.android.model.play;

import org.centum.android.model.Card;

import java.util.HashMap;

/**
 * Created by Phani on 1/14/14.
 */
public class PlayStats {

    private static final String DEBUG_TAG = "PlayStats";

    private final long time;
    private String name;
    private HashMap<Card, Integer> correct = new HashMap<Card, Integer>();
    private HashMap<Card, Integer> wrong = new HashMap<Card, Integer>();
    private int numberOfSessions = 0;

    public PlayStats() {
        time = System.currentTimeMillis();
        name = time + "";
        debug("Play Stats \"" + name + "\" created @ " + time);
    }

    public PlayStats(String name) {
        this.name = name;
        time = System.currentTimeMillis();
        debug("Play Stats \"" + name + "\" created @ " + time);
    }

    public void reset() {
        correct = new HashMap<Card, Integer>();
        wrong = new HashMap<Card, Integer>();
        numberOfSessions = 0;
        debug("Play Stats \"" + name + "\" reset");
    }

    public void addPlaySession(PlaySession session) {
        for (Card c : session.getCards()) {
            if (session.getSessionStat(c) == PlaySession.ANS_CORRECT) {
                addCorrect(c);
            } else if (session.getSessionStat(c) == PlaySession.ANS_WRONG) {
                addWrong(c);
            }
        }
        numberOfSessions++;
        debug("Play Stats \"" + name + "\" added " + session.getName());
    }

    public void removePlaySession(PlaySession session) {
        for (Card c : session.getCards()) {
            if (session.getSessionStat(c) == PlaySession.ANS_CORRECT) {
                removeCorrect(c);
            } else if (session.getSessionStat(c) == PlaySession.ANS_WRONG) {
                removeWrong(c);
            }
        }
        numberOfSessions--;
        debug("Play Stats \"" + name + "\" removed " + session.getName());
    }

    private void addCorrect(Card c) {
        if (correct.containsKey(c)) {
            correct.put(c, correct.get(c) + 1);
        } else {
            correct.put(c, 1);
        }
    }

    private void removeCorrect(Card c) {
        if (correct.containsKey(c)) {
            correct.put(c, correct.get(c) - 1);
        } else {
            correct.put(c, 0);
        }
    }

    private void addWrong(Card c) {
        if (wrong.containsKey(c)) {
            wrong.put(c, wrong.get(c) + 1);
        } else {
            wrong.put(c, 1);
        }
    }

    private void removeWrong(Card c) {
        if (wrong.containsKey(c)) {
            wrong.put(c, wrong.get(c) - 1);
        } else {
            wrong.put(c, 0);
        }
    }

    public double getPercentCorrect(Card c) {
        if (getTotal(c) == 0) {
            return 0;
        }
        return 100d * ((double) getNumberCorrect(c) / (double) getTotal(c));
    }

    public double getPercentWrong(Card c) {
        return 100d - getPercentCorrect(c);
    }

    public int getTotal(Card c) {
        return getNumberCorrect(c) + getNumberWrong(c);
    }

    public int getNumberCorrect(Card c) {
        if (correct.containsKey(c))
            return correct.get(c);
        return 0;
    }

    public int getNumberWrong(Card c) {
        if (wrong.containsKey(c))
            return wrong.get(c);
        return 0;
    }

    public int getTotalCorrect() {
        int tot = 0;
        for (Card c : correct.keySet()) {
            tot += getNumberCorrect(c);
        }
        return tot;
    }

    public int getTotalWrong() {
        int tot = 0;
        for (Card c : wrong.keySet()) {
            tot += getNumberWrong(c);
        }
        return tot;
    }

    public double getTotalPercentCorrect() {
        return 100d * ((double) getTotalCorrect() / (double) (getTotalCorrect() + getTotalWrong()));
    }

    public double getTotalPercentWrong() {
        return 100d - getTotalPercentCorrect();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public String toString() {
        return getName();
    }

    private void debug(String msg) {
        // Log.d(DEBUG_TAG, msg);
    }

    public int getNumberOfSessions() {
        return numberOfSessions;
    }
}
