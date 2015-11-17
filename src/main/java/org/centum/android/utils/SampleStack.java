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
package org.centum.android.utils;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;

/**
 * Created by Phani on 3/30/2014.
 */
public class SampleStack {

    private static Stack sampleStack = null;

    /**
     * Creates a new sample stack
     *
     * @return A sample Organic Chemistry stack
     */
    public static Stack getNewSampleStack() {
        if (sampleStack == null) {
            sampleStack = new Stack("Stacks Flashcards");
            sampleStack.setDescription("Tips and tricks");
            sampleStack.addCard(new Card("Creating a Stack", "When on the Stacks screen, tap the \"+\" button to add a Stack"));
            sampleStack.addCard(new Card("Creating a Card", "Select a Stack to edit, and tap the \"+\" button to add a Card"));
            sampleStack.addCard(new Card("Editing a Stack/Card", "Tap the pencil icon on either a Stack or a Card to edit any information"));
            sampleStack.addCard(new Card("Adding an image to a Card", "Edit a Card and tap the image icon to select from camera capture, drawing, or picking from the gallery."));
            sampleStack.addCard(new Card("Learning a Card", "When on the Cards screen, tap the Learn button. The cards will be shuffled and presented for review."));
            sampleStack.addCard(new Card("Testing yourself", "Whe on the Cards screen, tap the Test button. Here you can pick the test options and quiz yourself"));
            sampleStack.addCard(new Card("Question Type: Simple", "View the title and the details, and manually mark whether you got it correct"));
            sampleStack.addCard(new Card("Question Type: Multi Choice", "Pick from a randomly chosen set of answers"));
            sampleStack.addCard(new Card("Question Type: Write-In", "Type the answer in word for word"));
            sampleStack.addCard(new Card("Test Timing", "A test can be timed by either test length of per-card length."));
            sampleStack.addCard(new Card("Swapping Title and Details", "Long press a card to select it, and after selecting any other cards, tap \"Swap Elements\""));
            sampleStack.addCard(new Card("Archiving Cards or Stacks", "Swipe a Stack or a Card to archive it. It will no long be used in learn/test modes nor be displayed"));
            sampleStack.addCard(new Card("Viewing Stats", "Select a stack, and tap \"Stats\""));

            for (int p = 0; p < 10; p++) {
                PlaySession playSession = new PlaySession("Test Session " + p);
                for (int i = 0; i < sampleStack.getNumberOfCards(); i++) {
                    boolean cor = Math.random() > .37;
                    playSession.setSessionStat(sampleStack.getCard(i), cor ? PlaySession.ANS_CORRECT : PlaySession.ANS_WRONG);
                }
                sampleStack.addPlaySession(playSession);
            }
        }
        return sampleStack;
    }

}
