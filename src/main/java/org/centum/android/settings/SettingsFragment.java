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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.centum.android.model.Card;
import org.centum.android.model.LoadedData;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.play.PlaySession;
import org.centum.android.stack.R;
import org.centum.android.utils.AttachmentHandler;
import org.centum.android.utils.ReleaseNotes;
import org.centum.android.utils.SampleStack;
import org.centum.android.utils.Serializer;

import java.util.List;

/**
 * Created by Phani on 3/22/2014.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference attachmentsPref = findPreference(SettingsActivity.KEY_PREF_CLEAN_ATTACHMENTS);
        Preference resetPref = findPreference(SettingsActivity.KEY_PREF_RESET);
        Preference importPref = findPreference(SettingsActivity.KEY_PREF_IMPORT);
        Preference exportPref = findPreference(SettingsActivity.KEY_PREF_EXPORT);
        Preference cleanEmptyPref = findPreference(SettingsActivity.KEY_PREF_CLEAN_EMPTY_MEMBERS);
        Preference addSamplePref = findPreference(SettingsActivity.KEY_PREF_ADD_SAMPLE_STACK);
        Preference releaseNotesPref = findPreference(SettingsActivity.KEY_PREF_VIEW_RELEASE_NOTES);

        if (releaseNotesPref != null) {
            releaseNotesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (getActivity() != null) {
                        new ReleaseNotes(getActivity()).showReleaseNotes();
                    }
                    return true;
                }
            });
        }
        if (addSamplePref != null) {
            addSamplePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    StackManager.get().addStack(SampleStack.getNewSampleStack());
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), "Sample stack added", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        if (cleanEmptyPref != null) {
            cleanEmptyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Clean Empty Members");
                    builder.setMessage("This will delete empty Stacks, Cards, and Testing sessions");
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            for (Stack stack : StackManager.get().getStackList()) {
                                cleanStack(stack);
                            }
                            for (Stack stack : StackManager.get().getArchivedStacksList()) {
                                cleanStack(stack);
                            }
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }
        if (importPref != null) {
            importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    importStacks();
                    return true;
                }
            });
        }
        if (exportPref != null) {
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    exportStacks();
                    return true;
                }
            });
        }
        if (resetPref != null) {
            resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Reset Preferences");
                    builder.setMessage("This will reset all default values");
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
                            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, true);
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }
        if (attachmentsPref != null) {
            attachmentsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Clean Attachments");
                    builder.setMessage("This will delete all currently unused attachments. You cannot undo this!");
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AttachmentHandler.get(getActivity()).cleanCacheDir();
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }
    }

    private void cleanStack(Stack stack) {
        if (stack.getNumberOfCards() == 0 && stack.getNumberOfArchivedCards() == 0) {
            if (StackManager.get().containsStack(stack)) {
                StackManager.get().removeStack(stack, false);
            }
            if (StackManager.get().containsArchivedStack(stack)) {
                StackManager.get().removeArchivedStack(stack);
            }
        } else {
            for (Card card : stack.getCardList()) {
                if (TextUtils.isEmpty(card.getTitle()) && TextUtils.isEmpty(card.getDetails()) && card.getAttachment() == null) {
                    stack.removeCard(card, false);
                }
            }
            for (Card card : stack.getArchivedCards()) {
                if (TextUtils.isEmpty(card.getTitle()) && TextUtils.isEmpty(card.getDetails()) && card.getAttachment() == null) {
                    stack.removeArchivedCard(card);
                }
            }
            for (PlaySession playSession : stack.getPlaySessions()) {
                if (playSession.isEmpty()) {
                    stack.removePlaySession(playSession);
                }
            }
        }
    }

    private void importStacks() {
        final EditText editText = new EditText(getActivity());
        editText.setHint("File name");
        new AlertDialog.Builder(getActivity()).setTitle("Export")
                .setMessage("The file should be in the \"sdcard\\Stacks\" folder")
                .setView(editText)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("Load", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (editText.getText().toString() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                    String name = editText.getText().toString();
                    if (!name.endsWith(".stk")) {
                        name = name + ".stk";
                    }
                    LoadedData loadedData = Serializer.getInstance(getActivity()).loadData(name);
                    List<Stack> stacks = loadedData.getLoadedStacks();
                    List<Stack> archivedStacks = loadedData.getLoadedArchivedStacks();

                    for (Stack stack : stacks) {
                        StackManager.get().addStack(stack);
                    }

                    for (Stack stack : archivedStacks) {
                        StackManager.get().addArchiveStack(stack);
                    }

                    Toast.makeText(getActivity(), "Imported Stacks file", Toast.LENGTH_SHORT).show();
                }
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void exportStacks() {
        final EditText editText = new EditText(getActivity());
        editText.setHint("File name");
        new AlertDialog.Builder(getActivity()).setTitle("Export").setView(editText)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (editText.getText().toString() != null && !TextUtils.isEmpty(editText.getText().toString())) {

                    new AsyncTask<Void, Void, Void>() {
                        private ProgressDialog progressDialog;
                        private String name;

                        @Override
                        protected void onPreExecute() {
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setTitle("Exporting Data");
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setIndeterminate(true);
                            progressDialog.show();

                            name = editText.getText().toString();
                            if (!name.endsWith(".stk")) {
                                name = name + ".stk";
                            }
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            progressDialog.dismiss();
                            Log.d("MainActivity", "Backed data up");
                            Toast.makeText(getActivity(), "Exported Stacks file", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            Serializer.getInstance(getActivity()).writeData(name);
                            return null;
                        }
                    }.execute();

                }
                dialogInterface.dismiss();
            }
        }).show();
    }
}
