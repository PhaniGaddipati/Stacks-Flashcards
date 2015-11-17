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

import org.centum.android.model.events.StackEvent;
import org.centum.android.model.events.StackListener;
import org.centum.android.model.play.PlaySession;
import org.centum.android.model.play.PlayStats;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Stack implements Externalizable {

    public static final int EVENT_CARD_ADDED = 0;
    public static final int EVENT_CARD_REMOVED = 1;
    public static final int EVENT_ARCHIVED_CARD_ADDED = 9;
    public static final int EVENT_ARCHIVED_CARD_REMOVED = 10;
    public static final int EVENT_CARD_MOVED = 2;
    public static final int EVENT_NAME_CHANGED = 3;
    public static final int EVENT_DESCRIPTION_CHANGED = 4;
    public static final int EVENT_ICON_CHANGED = 5;
    public static final int EVENT_SELECTION_CHANGED = 6;
    public static final int EVENT_PLAY_SESSION_ADDED = 7;
    public static final int EVENT_PLAY_SESSION_REMOVED = 8;
    public static final int EVENT_CARD_ARCHIVE_STATUS_CHANGED = 11;
    public static final int EVENT_CURRENT = 12;
    public static final String[] ICONS = new String[]{"circle_stack", "circle_bio",
            "circle_book", "circle_chem", "circle_gavel",
            "circle_music", "circle_pencil", "circle_plus"};
    private static final long serialVersionUID = -6819255155280303381L;
    private static final int externalizableVersion = 3;
    private static final String DEBUG_TAG = "Stack";
    private final ArrayList<Card> cards = new ArrayList<Card>();
    private final ArrayList<PlaySession> playSessions = new ArrayList<PlaySession>();
    private final ArrayList<StackListener> listeners = new ArrayList<StackListener>();
    private ArrayList<Card> archivedCards = new ArrayList<Card>();
    private String name = null;
    private String description = "";
    private int icon = 0;
    private boolean selected;
    private boolean currentStack = false;
    private boolean isQuizletStack = false;
    private int quizletID = -1;
    private long sqlID = -1;

    public Stack() {

    }

    public Stack(String name) {
        this.name = name;
    }

    public void addPlaySession(PlaySession playSession) {
        int i = 0;
        String oName = playSession.getName();
        String name = oName;
        while (containsPlaySession(name)) {
            i++;
            name = oName + " (" + i + ")";
        }
        playSession.setName(name);
        playSessions.add(playSession);
        fireEvent(this, playSession, EVENT_PLAY_SESSION_ADDED, 0);
    }

    public void removePlaySession(PlaySession playSession) {
        playSessions.remove(playSession);
        fireEvent(this, playSession, EVENT_PLAY_SESSION_REMOVED, 0);
    }

    public boolean containsPlaySession(String name) {
        for (PlaySession playSession : playSessions) {
            if (name.equals(playSession.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPlaySession(PlaySession playSession) {
        for (PlaySession ps : playSessions) {
            if (playSession == ps) {
                return true;
            }
        }
        return false;
    }

    public int getNumberOfPlaySessions() {
        return playSessions.size();
    }

    public PlaySession getPlaySession(String id) {
        for (PlaySession session : playSessions) {
            if (session.getId().equals(id)) {
                return session;
            }
        }
        return null;
    }

    public PlaySession getPlaySession(int i) {
        return playSessions.get(i);
    }

    public PlaySession[] getPlaySessions() {
        return playSessions.toArray(new PlaySession[playSessions.size()]);
    }

    public PlayStats getPlayStatsWithEnabledSessions() {
        PlayStats playStats = new PlayStats();
        for (PlaySession playSession : playSessions) {
            if (playSession.isEnabled()) {
                playStats.addPlaySession(playSession);
            }
        }
        return playStats;
    }

    public int getNumberOfEnabledSessions() {
        int i = 0;
        for (PlaySession playSession : playSessions) {
            if (playSession.isEnabled()) {
                i++;
            }
        }
        return i;
    }

    public boolean addCard(Card card) {
        return addCard(card, cards.size());
    }

    public void quickAddCard(Card card) {
        cards.add(card);
    }

    public boolean addCard(Card card, int pos) {
        if (!containsCard(card)) {
            cards.add(pos, card);
            fireEvent(this, card, EVENT_CARD_ADDED);
            return true;
        }
        return false;
    }

    public boolean removeCard(Card card) {
        return removeCard(card, true);
    }

    public boolean removeCard(Card card, boolean archive) {
        if (containsCard(card)) {
            cards.remove(card);
            if (!archive) {
                fireEvent(this, card, EVENT_CARD_REMOVED);
            } else {
                archivedCards.add(card);
                fireEvent(this, card, EVENT_CARD_ARCHIVE_STATUS_CHANGED);
            }
            return true;
        }
        return false;
    }

    public int getCardPosition(Card card) {
        return cards.indexOf(card);
    }

    public Card getCard(int index) {
        if (index >= 0 && index < getNumberOfCards()) {
            return cards.get(index);
        }
        return null;
    }

    public boolean containsCard(Card c) {
        return cards.contains(c);
    }

    public int getNumberOfCards() {
        return cards.size();
    }

    public int getNumberOfArchivedCards() {
        if (archivedCards == null) {
            archivedCards = new ArrayList<Card>();
        }
        return archivedCards.size();
    }

    public ArrayList<Card> getArchivedCards() {
        return archivedCards;
    }

    public Card getArchivedCard(int i) {
        return archivedCards.get(i);
    }

    public void restoreArchivedCard(int i) {
        if (archivedCards.size() > i) {
            restoreArchivedCard(archivedCards.get(i));
        }
    }

    public void restoreArchivedCard(Card card) {
        archivedCards.remove(card);
        cards.add(card);
        fireEvent(this, card, EVENT_CARD_ARCHIVE_STATUS_CHANGED);
    }

    public void removeArchivedCard(int i) {
        if (i < archivedCards.size()) {
            removeArchivedCard(archivedCards.get(i));
        }
    }

    public void removeArchivedCard(Card card) {
        archivedCards.remove(card);
        fireEvent(this, card, EVENT_ARCHIVED_CARD_REMOVED);
    }

    public void removeAllArchivedCards() {
        while (archivedCards.size() > 0) {
            removeArchivedCard(0);
        }
    }

    public void restoreAllArchivedCards() {
        while (getNumberOfArchivedCards() > 0) {
            restoreArchivedCard(0);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        fireEvent(this, null, EVENT_NAME_CHANGED);
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
        fireEvent(this, null, EVENT_ICON_CHANGED);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        if (this.description.length() > 500) {
            this.description = this.description.substring(0, 500);
        }
        fireEvent(this, null, EVENT_DESCRIPTION_CHANGED);
    }

    public void moveCard(Card card, int pos) {
        cards.remove(card);
        cards.add(pos, card);
        fireEvent(this, null, EVENT_CARD_MOVED);
    }

    public void undoRemoveCard() {
        if (archivedCards.size() > 0) {
            restoreArchivedCard(archivedCards.size() - 1);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        fireEvent(this, null, EVENT_SELECTION_CHANGED);
    }

    public String toString() {
        return getName();
    }

    public List<Card> getCardList() {
        return cards;
    }

    public void shuffle() {
        Collections.shuffle(cards);
        fireEvent(this, null, EVENT_CARD_MOVED);
    }

    public void sortByWrong() {
        final PlayStats playStats = getPlayStatsWithEnabledSessions();
        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(Card card, Card card2) {
                return Double.compare(playStats.getPercentWrong(card2), playStats.getPercentWrong(card));
            }
        });
        fireEvent(this, null, EVENT_CARD_MOVED);
    }

    public void sortByCorrect() {
        final PlayStats playStats = getPlayStatsWithEnabledSessions();
        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(Card card, Card card2) {
                return Double.compare(playStats.getPercentCorrect(card2), playStats.getPercentCorrect(card));
            }
        });
        fireEvent(this, null, EVENT_CARD_MOVED);
    }

    public boolean isQuizletStack() {
        return isQuizletStack;
    }

    public void setQuizletStack(boolean isQuizletStack) {
        this.isQuizletStack = isQuizletStack;
    }

    public int getQuizletID() {
        return quizletID;
    }

    public void setQuizletID(int quizletID) {
        this.quizletID = quizletID;
    }

    private void disableQuizletSet() {
        isQuizletStack = false;
        quizletID = -1;
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        int version = objectInput.readInt();

        name = objectInput.readUTF();
        description = objectInput.readUTF();
        icon = objectInput.readInt();
        int cards = objectInput.readInt();
        int archived = objectInput.readInt();
        int sessions = objectInput.readInt();

        for (int i = 0; i < cards; i++) {
            this.cards.add((Card) objectInput.readObject());
        }
        for (int i = 0; i < archived; i++) {
            this.archivedCards.add((Card) objectInput.readObject());
        }
        for (int i = 0; i < sessions; i++) {
            this.playSessions.add((PlaySession) objectInput.readObject());
        }

        if (version > 1) {
            isQuizletStack = objectInput.readBoolean();
        }
        if (version > 2) {
            quizletID = objectInput.readInt();
        }
    }

    @Override
    public boolean equals(Object stack) {
        return (stack instanceof Stack && ((Stack) stack).getName().equals(getName()));
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(externalizableVersion);
        objectOutput.writeUTF(name);
        objectOutput.writeUTF(description);
        objectOutput.writeInt(icon);
        objectOutput.writeInt(cards.size());
        objectOutput.writeInt(archivedCards.size());
        objectOutput.writeInt(playSessions.size());

        for (int i = 0; i < cards.size(); i++) {
            objectOutput.writeObject(cards.get(i));
        }
        for (int i = 0; i < archivedCards.size(); i++) {
            objectOutput.writeObject(archivedCards.get(i));
        }
        for (int i = 0; i < playSessions.size(); i++) {
            objectOutput.writeObject(playSessions.get(i));
        }

        objectOutput.writeBoolean(isQuizletStack);
        objectOutput.writeInt(quizletID);
    }

    public void sortByName() {
        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(Card card, Card card2) {
                return card.getTitle().toLowerCase().compareTo(card2.getTitle().toLowerCase());
            }
        });
        fireEvent(this, null, EVENT_CARD_MOVED);
    }

    public long getSqlID() {
        return sqlID;
    }

    public void setSqlID(long sqlID) {
        this.sqlID = sqlID;
    }

    public void addArchivedCard(Card card) {
        archivedCards.add(card);
        fireEvent(this, card, EVENT_ARCHIVED_CARD_ADDED);
    }

    public void quickAddArchivedCard(Card card) {
        archivedCards.add(card);
    }

    public boolean isCurrentStack() {
        return currentStack;
    }

    public void setCurrentStack(boolean currentStack) {
        this.currentStack = currentStack;
        fireEvent(this, null, EVENT_CURRENT);
    }

    private void fireEvent(StackEvent event) {
        for (StackListener listener : listeners) {
            listener.eventFired(event);
        }
    }

    private void fireEvent(Stack stack, Card card, int evt) {
        fireEvent(new StackEvent(stack, card, null, evt));
    }

    private void fireEvent(Stack stack, PlaySession ps, int evt, int r) {
        fireEvent(new StackEvent(stack, null, ps, evt));
    }

    public void addListener(StackListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(StackListener listener) {
        listeners.remove(listener);
    }
}
