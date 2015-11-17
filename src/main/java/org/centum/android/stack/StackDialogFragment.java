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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;

/**
 * Created by Phani on 12/29/13.
 */
public class StackDialogFragment extends DialogFragment {

    private Button createButton, cancelButton;
    private ImageButton iconButton;
    private ImageView rightButton, leftButton;
    private EditText nameEditText, descriptionEditText;
    private TextView titleTextView;
    private int icon = 0;
    private Stack stack = null;

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.stack_dialog_fragment, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        titleTextView = (TextView) view.findViewById(R.id.title_textView);
        createButton = (Button) view.findViewById(R.id.create_stack_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        rightButton = (ImageView) view.findViewById(R.id.right_button);
        leftButton = (ImageView) view.findViewById(R.id.left_button);
        iconButton = (ImageButton) view.findViewById(R.id.icon_imageButton);
        nameEditText = (EditText) view.findViewById(R.id.name_editText);
        descriptionEditText = (EditText) view.findViewById(R.id.description_editText);

        if (stack == null) {
            createButton.setText(getResources().getString(R.string.stack_create));
            titleTextView.setText(getResources().getString(R.string.new_stack));
            createButton.setEnabled(false);
            nameEditText.requestFocus();
        } else {
            createButton.setText(getResources().getString(R.string.stack_edit));
            titleTextView.setText(getResources().getString(R.string.stack_edit));
            createButton.setEnabled(true);
            nameEditText.setText(stack.getName());
            descriptionEditText.setText(stack.getDescription());
            icon = stack.getIcon();
            updateIcon();
        }

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                icon++;
                if (icon == Stack.ICONS.length) {
                    icon = 0;
                }
                updateIcon();
            }
        });
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                icon--;
                if (icon < 0) {
                    icon = Stack.ICONS.length - 1;
                }
                updateIcon();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean valid;
                if (stack == null) {
                    valid = (!TextUtils.isEmpty(editable.toString()) &&
                            !StackManager.get().containsStack(editable.toString().trim()));
                } else {
                    valid = (!TextUtils.isEmpty(editable.toString()) &&
                            !StackManager.get().containsStack(editable.toString().trim()))
                            || editable.toString().equals(stack.getName());
                }
                createButton.setEnabled(valid);
                if (!valid && !TextUtils.isEmpty(editable.toString())) {
                    nameEditText.setError("Stack of this name already exists");
                } else {
                    nameEditText.setError(null);
                }

            }
        });
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isNewStack = stack == null;
                if (isNewStack) {
                    stack = new Stack(nameEditText.getText().toString().trim());
                }
                stack.setName(nameEditText.getText().toString().trim());
                stack.setDescription(descriptionEditText.getText().toString());
                stack.setIcon(icon);
                if (isNewStack) {
                    StackManager.get().addStack(stack);
                }
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private void updateIcon() {
        String uri = "drawable/" + Stack.ICONS[icon];
        int iconResource = getResources().getIdentifier(uri, null, getDialog().getContext().getPackageName());
        Drawable icon = getResources().getDrawable(iconResource);
        iconButton.setImageDrawable(icon);
    }
}
