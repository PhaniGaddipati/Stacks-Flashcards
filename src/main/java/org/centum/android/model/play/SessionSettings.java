package org.centum.android.model.play;

/**
 * Created by Phani on 4/2/2014.
 */
public class SessionSettings {

    private boolean countdownCard = true;
    private boolean countdownStack = true;
    private boolean freeNavigation = false;
    private boolean includeSimple = true, includeMulti = true, includeWriteIn = true;
    private int stackSecondsLimit = 300;//60 * 5;
    private int cardSecondsLimit = 5;

    public SessionSettings() {
        //Default settings
    }

    public SessionSettings(boolean countdownCard, boolean countdownStack,
                           boolean freeNavigation, int stackSecondsLimit, int cardSecondsLimit,
                           boolean includeSimple, boolean includeMulti, boolean includeWriteIn) {
        this.countdownCard = countdownCard;
        this.countdownStack = countdownStack;
        this.freeNavigation = freeNavigation;
        this.stackSecondsLimit = stackSecondsLimit;
        this.cardSecondsLimit = cardSecondsLimit;
        this.includeSimple = includeSimple;
        this.includeMulti = includeMulti;
        this.includeWriteIn = includeWriteIn;

        if (cardSecondsLimit < 1) {
            this.cardSecondsLimit = 0;
        }
        if (stackSecondsLimit < 1) {
            this.stackSecondsLimit = 1;
        }

    }

    public int getStackSecondsLimit() {
        return stackSecondsLimit;
    }

    public int getCardSecondsLimit() {
        return cardSecondsLimit;
    }

    public boolean isCountdownCard() {
        return countdownCard;
    }

    public boolean isCountdownStack() {
        return countdownStack;
    }

    public boolean isFreeNavigationEnabled() {
        return freeNavigation;
    }

    public boolean isIncludeSimple() {
        return includeSimple;
    }

    public boolean isIncludeMulti() {
        return includeMulti;
    }

    public boolean isIncludeWriteIn() {
        return includeWriteIn;
    }
}
