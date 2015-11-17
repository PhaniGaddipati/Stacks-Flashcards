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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

/**
 * Created by Phani on 3/16/14.
 */
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_THEME = "pref_theme";
    public static final String KEY_PREF_STACK_SWIPE_DELETES = "pref_key_stack_swipe_deletes";
    public static final String KEY_PREF_CARD_SWIPE_DELETES = "pref_key_card_swipe_deletes";
    public static final String KEY_PREF_CLEAN_ATTACHMENTS = "pref_clean_attachments";
    public static final String KEY_PREF_RESET = "pref_reset";
    public static final String KEY_PREF_PLAY_AUTO_ADVANCE = "pref_key_play_auto_advance";
    public static final String KEY_PREF_PLAY_SHUFFLE_TEST = "pref_key_play_shuffle_test";
    public static final String KEY_PREF_EXPORT = "pref_key_export";
    public static final String KEY_PREF_IMPORT = "pref_key_import";
    public static final String KEY_PREF_CARD_TAP_PREVIEW = "pref_key_card_tap_preview";
    public static final String KEY_PREF_ANALYTICS_OPTOUT = "pref_key_analytics_optout";
    public static final String KEY_PREF_CLEAN_EMPTY_MEMBERS = "pref_clean_empty_members";
    public static final String KEY_PREF_ADD_SAMPLE_STACK = "pref_add_sample_stack";
    public static final String KEY_PREF_CARD_CROP_DRAWING = "pref_key_card_crop_drawing";
    public static final String KEY_PREF_UPGRADE = "pref_key_card_crop_drawing";
    public static final String KEY_PREF_BACKUP_TIME = "pref_key_backup_time";
    public static final String KEY_PREF_PLAY_MARGIN_ADVANCE = "pref_key_play_margin_advance";
    public static final String KEY_PREF_WRITE_MIN_SIMILARITY = "pref_key_write_min_similarity";
    public static final String KEY_PREF_USE_IMMERSIVE = "pref_key_immersive";
    public static final String KEY_PREF_VIEW_RELEASE_NOTES = "pref_key_release_notes";
    public static final String KEY_PREF_WRITE_IGNORE_CASE = "pref_key_write_ignore_case";

    private boolean restartOnBack = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setTheme(Themes.get(this).getCurrentTheme());
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        restartOnBack = getIntent().getBooleanExtra("themeChanged", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (restartOnBack) {
            restartActivity();
        } else {
            super.onBackPressed();
        }
    }

    private void restartActivity() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_THEME)) {
            finish();
            Intent i = new Intent(this, this.getClass());
            i.putExtra("themeChanged", true);
            startActivity(i);
        }
    }
}
