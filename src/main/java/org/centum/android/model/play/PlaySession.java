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
import org.centum.android.model.events.PlaySessionEvent;
import org.centum.android.model.events.PlaySessionListener;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Phani on 1/14/14.
 */
public class PlaySession implements Externalizable {

    public static final int EVENT_NAME_CHANGED = 0;
    public static final int EVENT_TIME_CHANGED = 1;
    public static final int EVENT_ENABLED_CHANGED = 2;
    public static final int EVENT_STATS_CHANGED = 3;
    public static final int EVENT_SESSION_SETTINGS_CHANGED = 4;
    public static final int ANS_NONE = 0;
    public static final int ANS_CORRECT = 1;
    public static final int ANS_WRONG = 2;
    private static final long serialVersionUID = 500881258321170619L;
    private static final String DEBUG_TAG = "PlaySession";
    private static final int externalizableVersion = 1;
    private final HashMap<Card, Integer> stats = new HashMap<Card, Integer>();
    private final List<PlaySessionListener> playSessionListeners = new ArrayList<PlaySessionListener>();
    private transient SessionSettings sessionSettings = null;
    private long time = 1;
    private String name;
    private boolean enabled = true;
    private long sqlID = -1;

    public PlaySession() {
        time = System.currentTimeMillis();
        name = "Play Session";
    }

    public PlaySession(String name) {
        this.name = name;
        time = System.currentTimeMillis();
    }

    public boolean hasDefaultName() {
        return name.equals("Play Session");
    }

    public boolean containsSessionStat(Card card) {
        return stats.containsKey(card);
    }

    public int getSessionStat(Card card) {
        if (!containsSessionStat(card)) {
            return ANS_NONE;
        }
        return stats.get(card);
    }

    public void setSessionStat(Card card, int state) {
        this.stats.put(card, state);
        if (state == ANS_NONE) {
            removeCard(card);
        } else {
            fireEvent(card, EVENT_STATS_CHANGED);
        }
    }

    public void removeCard(Card card) {
        stats.remove(card);
        fireEvent(card, EVENT_STATS_CHANGED);
    }

    public boolean isEmpty() {
        for (Card card : stats.keySet()) {
            if (getSessionStat(card) != ANS_NONE) {
                return false;
            }
        }
        return true;
    }

    public Date getDate() {
        return new Date(getTime());
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        fireEvent(null, EVENT_TIME_CHANGED);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        fireEvent(null, EVENT_NAME_CHANGED);
    }

    public Card[] getCards() {
        return stats.keySet().toArray(new Card[stats.keySet().size()]);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        fireEvent(null, EVENT_ENABLED_CHANGED);
    }

    public SessionSettings getSessionSettings() {
        if (sessionSettings == null) {
            setSessionSettings(new SessionSettings());
        }
        return sessionSettings;
    }

    public void setSessionSettings(SessionSettings sessionSettings) {
        this.sessionSettings = sessionSettings;
        fireEvent(null, EVENT_SESSION_SETTINGS_CHANGED);
    }

    public void fireAll() {
        for (Card card : stats.keySet()) {
            fireEvent(card, EVENT_STATS_CHANGED);
        }
    }

    public void addListener(PlaySessionListener playSessionListener) {
        if (!playSessionListeners.contains(playSessionListener))
            playSessionListeners.add(playSessionListener);
    }

    public void removeListener(PlaySessionListener playSessionListener) {
        playSessionListeners.remove(playSessionListener);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getId() {
        return time + name;
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        int version = objectInput.readInt();
        time = objectInput.readLong();
        name = objectInput.readUTF();
        enabled = objectInput.readBoolean();
        int size = objectInput.readInt();

        for (int i = 0; i < size; i++) {
            Card card = (Card) objectInput.readObject();
            int ans = objectInput.readInt();
            stats.put(card, ans);
        }
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(externalizableVersion);
        objectOutput.writeLong(time);
        objectOutput.writeUTF(name);
        objectOutput.writeBoolean(enabled);
        objectOutput.writeInt(stats.size());

        Object cards[] = stats.keySet().toArray();
        for (int i = 0; i < stats.size(); i++) {
            objectOutput.writeObject(cards[i]);
            objectOutput.writeInt(stats.get(cards[i]));
        }
    }

    public long getSqlID() {
        return sqlID;
    }

    public void setSqlID(long sqlID) {
        this.sqlID = sqlID;
    }

    private void fireEvent(Card card, int event) {
        fireEvent(new PlaySessionEvent(this, card, event));
    }

    private void fireEvent(PlaySessionEvent playSessionEvent) {
        for (PlaySessionListener listener : playSessionListeners) {
            listener.eventFired(playSessionEvent);
        }
    }
}
