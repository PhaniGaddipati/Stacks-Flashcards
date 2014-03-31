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
    /**
     * Creates a new sample stack
     *
     * @return A sample Organic Chemistry stack
     */
    public static Stack getSampleStack() {
        Stack chemStack = new Stack("Organic Chemistry");
        chemStack.setDescription("A sample Stack. Swipe to archive.");
        chemStack.setIcon(3);
        chemStack.addCard(new Card("Ozonolysis of Alkenes", "Split double bonds making terminal double bonded Os"));
        chemStack.addCard(new Card("Radical Addition", "Addition of a radical halogen via initiation, propagation, and termination"));
        chemStack.addCard(new Card("Initiation", "High energy splits a molecule so each has an extra electron"));
        chemStack.addCard(new Card("Propagation", "A radical reacts to form a product and another radical"));
        chemStack.addCard(new Card("Termination", "A radical reacts, leaving no more radicals"));
        chemStack.addCard(new Card("Electrophillic Addition of Halogens", "Br2, Cl2, etc. Room temperature, anti-addition"));
        chemStack.addCard(new Card("Alcohol to Alkyl Halide", "SOCl2 in Pyridine, Sn1 for tertiary alcohols, Sn2 for primary alcohols"));
        chemStack.addCard(new Card("Williamson Ether Synthesis", "Sn2 Example, oxygen is nucleophile"));
        chemStack.addCard(new Card("Electrophillic Addition of Water to Alkenes", "Acid catalyst, high temperature required, usually reversible. Markovnikov's rule applies."));
        chemStack.addCard(new Card("Hydroxylation of Alkenes", "OsO4/pyridine, redox reaction, syn addition"));
        chemStack.addCard(new Card("Syn Addition", "Both atoms added on same side"));
        chemStack.addCard(new Card("Trans Addition", "Atoms added on opposite sides of molecule"));
        chemStack.addCard(new Card("Hydrogenation of Alkynes", "Lindlar's Catalyst for cis product (alkene). Li in NH3 for trans product (alkene). Heterogenous reaction"));
        chemStack.addCard(new Card("Dies-Alder Reaction", "Pericyclic reaction, requires a diene and a dienophile. Stereochemistry preserved"));
        chemStack.addCard(new Card("Aromatic", "Uninterrupted ring of electrons, odd pairs of electrons"));
        chemStack.addCard(new Card("Anti-Aromatic", "Uninterrupted ring of electrons, even pairs of electrons"));
        chemStack.addCard(new Card("Oxidation of Alcohols", "Pyridium Chlorochromate for oxidation of 1st degree alcohols to aldehydes; Jones's reagent for 1st degree alcohols to carboxylic acids and 2nd degree alcohols to ketones"));

        for (int p = 0; p < 10; p++) {
            PlaySession playSession = new PlaySession("Test Session " + p);
            for (int i = 0; i < chemStack.getNumberOfCards(); i++) {
                boolean cor = Math.random() > .37;
                playSession.setSessionStat(chemStack.getCard(i), cor ? PlaySession.ANS_CORRECT : PlaySession.ANS_WRONG);
            }
            chemStack.addPlaySession(playSession);
        }
        return chemStack;
    }

}
