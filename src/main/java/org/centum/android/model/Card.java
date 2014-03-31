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

import org.centum.android.events.CardEvent;
import org.centum.android.events.CardListener;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public class Card implements Externalizable {

    public static final int EVENT_TITLE_CHANGED = 0;
    public static final int EVENT_DETAILS_CHANGED = 1;
    public static final int EVENT_ATTACHMENT_IS_DETAILS_CHANGED = 2;
    public static final int EVENT_ATTACHMENT_CHANGED = 3;
    public static final int EVENT_SELECTION_CHANGED = 4;

    private static final long serialVersionUID = -982362622290325719L;

    private static final String DEBUG_TAG = "Card";
    private static final int externalizableVersion = 2;
    private List<CardListener> listeners = new ArrayList<CardListener>();

    private String title = "";
    private String details = "";
    private String attachment = null;
    private boolean selected = false;
    private boolean attachmentPartOfDetails = false;
    private long sqlID = -1;

    public Card() {

    }

    public Card(String title) {
        this.title = title;
    }

    public Card(String title, String details) {
        this.title = title;
        this.details = details;
        if (details.length() > 1000) {
            this.details = details.substring(0, 1000);
        }
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
        if (details.length() > 1000) {
            this.details = details.substring(0, 1000);
        }
        fireEvent(this, EVENT_DETAILS_CHANGED);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        fireEvent(this, EVENT_TITLE_CHANGED);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        fireEvent(this, EVENT_SELECTION_CHANGED);
    }

    public boolean hasAttachment() {
        return getAttachment() != null;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
        fireEvent(this, EVENT_SELECTION_CHANGED);
    }

    public boolean isAttachmentPartOfDetails() {
        return attachmentPartOfDetails;
    }

    public void setAttachmentPartOfDetails(boolean attachmentPartOfDetails) {
        this.attachmentPartOfDetails = attachmentPartOfDetails;
        fireEvent(this, EVENT_ATTACHMENT_IS_DETAILS_CHANGED);
    }

    public String toString() {
        return getTitle();
    }

    public void addListener(CardListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CardListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        int version = objectInput.readInt();
        title = objectInput.readUTF();
        details = objectInput.readUTF();
        if (objectInput.readBoolean())
            attachment = objectInput.readUTF();
        if (version > 1) {
            attachmentPartOfDetails = objectInput.readBoolean();
        }
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(externalizableVersion);
        objectOutput.writeUTF(title);
        objectOutput.writeUTF(details);
        objectOutput.writeBoolean(hasAttachment());
        if (hasAttachment())
            objectOutput.writeUTF(attachment);
        objectOutput.writeBoolean(attachmentPartOfDetails);
    }

    @Override
    public Card clone() {
        Card card = new Card(getTitle(), getDetails());
        card.setAttachment(getAttachment());
        card.setAttachmentPartOfDetails(isAttachmentPartOfDetails());
        return card;
    }


    public long getSqlID() {
        return sqlID;
    }

    public void setSqlID(long sqlID) {
        this.sqlID = sqlID;
    }

    private void fireEvent(Card card, int event) {
        fireEvent(new CardEvent(card, event));
    }

    private void fireEvent(CardEvent event) {
        for (CardListener listener : listeners) {
            listener.eventFired(event);
        }
    }
}