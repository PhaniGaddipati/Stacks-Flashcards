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
package org.centum.android.integration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.centum.android.HelpActivity;
import org.centum.android.communicators.QuizletCommunicator;
import org.centum.android.communicators.StudyStackCommunicator;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 2/19/14.
 */
public class ImportActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String SOURCE_QUIZLET = "quizlet";
    public static final String SOURCE_STUDY_STACK = "studystack";
    private TextView siteLink, titleTextView;
    private Button importButton, cancelButton;
    private ImageButton helpButton, keywordBtn, usernameBtn;
    private EditText idEditText, usernameEditText, keywordEditText;
    private ProgressBar progressBar;
    private CheckBox imagesDetailsCheckBox;
    private ListView listView;
    private AsyncTask<Void, Void, Void> updateUserSetsTask;
    private AsyncTask<Void, Void, Void> updateKeywordSetsTask;
    private SetListAdapter emptyListAdapter;
    private SetListAdapter listAdapter;
    private Communicator communicator = QuizletCommunicator.getInstance(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Themes.get().getCurrentTheme());
        getActionBar().hide();
        setContentView(R.layout.import_fragment);

        if (getIntent() != null && SOURCE_STUDY_STACK.equals(getIntent().getStringExtra("source"))) {
            communicator = StudyStackCommunicator.getInstance(this);
        }

        siteLink = (TextView) findViewById(R.id.attribution_textview);
        importButton = (Button) findViewById(R.id.import_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        helpButton = (ImageButton) findViewById(R.id.question_imageButton);
        keywordBtn = (ImageButton) findViewById(R.id.keyword_btn);
        usernameBtn = (ImageButton) findViewById(R.id.username_btn);
        idEditText = (EditText) findViewById(R.id.id_edit_text);
        usernameEditText = (EditText) findViewById(R.id.username_editText);
        keywordEditText = (EditText) findViewById(R.id.keyword_editText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imagesDetailsCheckBox = (CheckBox) findViewById(R.id.images_details_checkBox);
        titleTextView = (TextView) findViewById(R.id.title_textView);

        listView = (ListView) findViewById(R.id.setslistView);
        keywordEditText.requestFocus();

        siteLink.setText(communicator.getAttributionText());
        titleTextView.setText(communicator.getName() + " Import");

        GenericSet emptySet = new GenericSet();
        emptySet.setType("No Sets");
        emptySet.setTitle("Search for a user or set");
        emptySet.setDescription("Relevant sets will appear");
        emptySet.setTermCount(0);
        emptySet.setId(-1);
        emptyListAdapter = new SetListAdapter(this, R.layout.generic_set_item, new GenericSet[]{emptySet});
        listView.setAdapter(emptyListAdapter);
        listView.setOnItemClickListener(this);

        siteLink.setOnClickListener(this);
        importButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);
        keywordBtn.setOnClickListener(this);
        usernameBtn.setOnClickListener(this);

        progressBar.setVisibility(View.GONE);

        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                keywordTextChanged(editable);
            }
        });
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                usernameTextChanged(editable);
            }
        });

        usernameEditText.setText(getSharedPreferences("values", 0).getString("username", ""));
        keywordEditText.setText(getSharedPreferences("values", 0).getString("keyword", ""));
        imagesDetailsCheckBox.setChecked(getSharedPreferences("values", 0).getBoolean("imagedetails", false));
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        getSharedPreferences("values", 0).edit().putString("username", usernameEditText.getText().toString()).apply();
        getSharedPreferences("values", 0).edit().putString("keyword", keywordEditText.getText().toString()).apply();
        getSharedPreferences("values", 0).edit().putBoolean("imagedetails", imagesDetailsCheckBox.isChecked()).apply();
    }

    private void keywordTextChanged(Editable editable) {
        final String keyword = editable.toString();
        if (TextUtils.isEmpty(keyword)) {
            listView.setAdapter(emptyListAdapter);
        } else {
            if (updateUserSetsTask != null) {
                updateUserSetsTask.cancel(true);
            }
            if (updateKeywordSetsTask != null) {
                updateKeywordSetsTask.cancel(true);
            }

            updateKeywordSetsTask = new AsyncTask<Void, Void, Void>() {
                GenericSet[] sets;

                @Override
                protected void onPreExecute() {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    listView.setAdapter(null);
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        sets = communicator.getKeywordSets(keyword);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onCancelled() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (sets != null && sets.length > 0) {
                        listAdapter = new SetListAdapter(ImportActivity.this, R.layout.generic_set_item, sets);
                        listView.setAdapter(listAdapter);
                    } else {
                        listView.setAdapter(emptyListAdapter);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            };
            updateKeywordSetsTask.execute();
        }
    }

    private void usernameTextChanged(Editable editable) {
        final String user = editable.toString();
        if (TextUtils.isEmpty(user)) {
            listView.setAdapter(emptyListAdapter);
        } else {
            if (updateUserSetsTask != null) {
                updateUserSetsTask.cancel(true);
            }
            if (updateKeywordSetsTask != null) {
                updateKeywordSetsTask.cancel(true);
            }

            updateUserSetsTask = new AsyncTask<Void, Void, Void>() {
                GenericSet[] sets;

                @Override
                protected void onPreExecute() {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    listView.setAdapter(null);
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        sets = communicator.getUserSets(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onCancelled() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (sets != null && sets.length > 0) {
                        listAdapter = new SetListAdapter(emptyListAdapter.getContext(), R.layout.generic_set_item, sets);
                        listView.setAdapter(listAdapter);
                    } else {
                        listView.setAdapter(emptyListAdapter);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            };
            updateUserSetsTask.execute();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.question_imageButton:
                showHelp();
                break;
            case R.id.attribution_textview:
                showAttribution();
                break;
            case R.id.import_button:
                importSet();
                break;
            case R.id.cancel_button:
                onBackPressed();
                break;
            case R.id.keyword_btn:
                keywordTextChanged(keywordEditText.getText());
                break;
            case R.id.username_btn:
                usernameTextChanged(usernameEditText.getText());
                break;
        }
    }

    private void showHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("page", "file:///android_asset/import.html");
        startActivity(intent);
    }

    private void showAttribution() {
        String _url = communicator.getAttributionURL();
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(_url));
        startActivity(webIntent);
    }

    private void importSet() {

        if (!TextUtils.isEmpty(idEditText.getText().toString())) {
            new AsyncTask<Void, Void, Void>() {

                private Stack stack;
                private ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    progressDialog = new ProgressDialog(ImportActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Importing");
                    progressDialog.setMessage("Please wait");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        stack = communicator.getStack(Integer.parseInt(idEditText.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    progressDialog.dismiss();
                    if (stack != null) {
                        String name = stack.getName();
                        String newName = name;
                        int suffix = 1;

                        while (StackManager.get().containsStack(newName)) {
                            newName = name + " (" + suffix + ")";
                            suffix++;
                        }

                        stack.setName(newName);

                        if (imagesDetailsCheckBox.isChecked()) {
                            for (Card c : stack.getCards()) {
                                c.setAttachmentPartOfDetails(true);
                            }
                        }

                        StackManager.get().addStack(stack);
                        onBackPressed();
                    } else {
                        new AlertDialog.Builder(ImportActivity.this)
                                .setTitle("Error Importing")
                                .setMessage("The set was not able to be imported")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onBackPressed();
                                    }
                                })
                                .show();
                    }
                }
            }.execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Can't Import");
            builder.setMessage("You must either enter a set ID or select a stack to set the ID before importing");
            builder.show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        GenericSetView genericSetView = (GenericSetView) view;
        if (genericSetView.getGenericSet().getId() > -1) {
            idEditText.setText(genericSetView.getGenericSet().getId() + "");
        }
    }


}
