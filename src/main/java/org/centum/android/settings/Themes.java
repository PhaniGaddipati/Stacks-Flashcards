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
package org.centum.android.settings;

import android.content.Context;
import android.preference.PreferenceManager;

import org.centum.android.stack.R;

/**
 * Created by Phani on 3/16/14.
 */
public class Themes {

    private static Themes instance = null;

    public static final int THEME_LIGHT = R.style.AppTheme;
    public static final int THEME_DARK = R.style.AppThemeDark;
    public static final int THEME_PITCH = R.style.AppThemePitch;

    private final int themeArray[] = new int[]{THEME_LIGHT, THEME_DARK, THEME_PITCH};
    private final Context context;

    public Themes(Context context) {
        this.context = context;
    }

    public int getCurrentTheme() {
        try {
            return themeArray[Integer.parseInt(
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(SettingsActivity.KEY_PREF_THEME, "0")
            )];
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return themeArray[0];
        }
    }

    public boolean isThemeDark() {
        return getCurrentTheme() == R.style.AppThemeDark || getCurrentTheme() == R.style.AppThemePitch;
    }

    public static Themes get(Context context) {
        if (instance == null) {
            instance = new Themes(context);
        }
        return instance;
    }

    public static Themes get() {
        return instance;
    }

}
