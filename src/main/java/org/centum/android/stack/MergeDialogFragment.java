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
package org.centum.android.stack;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;

import java.util.List;

/**
 * Created by Phani on 1/2/14.
 */
public class MergeDialogFragment extends DialogFragment {

    private List<Stack> stacks;
    private EditText nameEditText;
    private EditText descriptionEditText;
    private CheckBox deleteCheckBox;
    private Button cancelButton;
    private Button mergeButton;

    public MergeDialogFragment(List<Stack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.stack_merge_fragment, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        nameEditText = (EditText) view.findViewById(R.id.name_editText);
        descriptionEditText = (EditText) view.findViewById(R.id.description_editText);
        deleteCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        mergeButton = (Button) view.findViewById(R.id.merge_button);
        mergeButton.setEnabled(false);

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString().trim();
                mergeButton.setEnabled(!StackManager.get().containsStack(s) && !TextUtils.isEmpty(s));

                if (StackManager.get().containsStack(s)) {
                    nameEditText.setError("Stack already exists");
                } else {
                    nameEditText.setError(null);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mergeStacks();
                getDialog().dismiss();
            }
        });

        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    public void mergeStacks() {
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        Stack stack = new Stack(name);
        stack.setDescription(description);

        for (Stack selStack : stacks) {
            for (Card card : selStack.getCards()) {
                stack.addCard(card);
            }
        }
        for (Stack selStack : stacks) {
            for (Card card : selStack.getArchivedCards()) {
                stack.addArchivedCard(card);
            }
        }

        StackManager.get().addStack(stack);

        if (deleteCheckBox.isChecked()) {
            for (Stack selStack : stacks) {
                StackManager.get().removeStack(selStack);
            }
        }
    }
}
