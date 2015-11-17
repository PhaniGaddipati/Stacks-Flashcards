package org.centum.android.integration;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.centum.android.HelpActivity;
import org.centum.android.integration.communicators.Communicator;
import org.centum.android.integration.communicators.QuizletCommunicator;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.stack.R;

/**
 * Created by Phani on 9/19/2014.
 */
public class ImportDialogFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

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
    private Communicator communicator = QuizletCommunicator.get();
    private GenericSet selectedSet = null;

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.import_fragment, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        siteLink = (TextView) view.findViewById(R.id.attribution_textview);
        importButton = (Button) view.findViewById(R.id.import_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        helpButton = (ImageButton) view.findViewById(R.id.question_imageButton);
        keywordBtn = (ImageButton) view.findViewById(R.id.keyword_btn);
        usernameBtn = (ImageButton) view.findViewById(R.id.username_btn);
        idEditText = (EditText) view.findViewById(R.id.id_edit_text);
        usernameEditText = (EditText) view.findViewById(R.id.username_editText);
        keywordEditText = (EditText) view.findViewById(R.id.keyword_editText);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        imagesDetailsCheckBox = (CheckBox) view.findViewById(R.id.images_details_checkBox);
        titleTextView = (TextView) view.findViewById(R.id.title_textView);

        listView = (ListView) view.findViewById(R.id.setslistView);
        keywordEditText.requestFocus();

        siteLink.setText(communicator.getAttributionText());
        titleTextView.setText(communicator.getName() + " Import");

        GenericSet emptySet = new GenericSet();
        emptySet.setType("No Sets");
        emptySet.setTitle("Search for a user or set");
        emptySet.setDescription("Relevant sets will appear");
        emptySet.setTermCount(0);
        emptySet.setId(-1);
        emptyListAdapter = new SetListAdapter(getActivity(), R.layout.generic_set_item, new GenericSet[]{emptySet});
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

        usernameEditText.setText(getActivity().getSharedPreferences("values", 0).getString("username", ""));
        keywordEditText.setText(getActivity().getSharedPreferences("values", 0).getString("keyword", ""));
        imagesDetailsCheckBox.setChecked(getActivity().getSharedPreferences("values", 0).getBoolean("imagedetails", false));

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getSharedPreferences("values", 0).edit().putString("username", usernameEditText.getText().toString()).apply();
        getActivity().getSharedPreferences("values", 0).edit().putString("keyword", keywordEditText.getText().toString()).apply();
        getActivity().getSharedPreferences("values", 0).edit().putBoolean("imagedetails", imagesDetailsCheckBox.isChecked()).apply();
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
                    if (getActivity() != null) {
                        if (sets != null && sets.length > 0) {
                            listAdapter = new SetListAdapter(getActivity(), R.layout.generic_set_item, sets);
                            listView.setAdapter(listAdapter);
                        } else {
                            listView.setAdapter(emptyListAdapter);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
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
                dismiss();
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
        Intent intent = new Intent(getActivity(), HelpActivity.class);
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
                    progressDialog = new ProgressDialog(getActivity());
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
                            for (Card c : stack.getCardList()) {
                                c.setAttachmentPartOfDetails(true);
                            }
                        }

                        StackManager.get().addStack(stack);
                        dismiss();
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Error Importing")
                                .setMessage("The set was not able to be imported")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dismiss();
                                    }
                                })
                                .show();
                    }
                }
            }.execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Can't Import");
            builder.setMessage("You must either enter a set ID or select a stack to set the ID before importing");
            builder.show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        GenericSetView genericSetView = (GenericSetView) view;
        if (genericSetView.getGenericSet().getId() > -1) {
            if (selectedSet != null) {
                selectedSet.setHighlighted(false);
            }
            selectedSet = genericSetView.getGenericSet();
            selectedSet.setHighlighted(true);
            idEditText.setText(selectedSet.getId() + "");
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

}
