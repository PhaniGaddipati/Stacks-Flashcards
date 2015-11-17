package org.centum.android.presentation.general;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import org.centum.android.model.Stack;
import org.centum.android.model.play.PlaySession;
import org.centum.android.model.play.SessionSettings;
import org.centum.android.stack.R;

/**
 * Created by Phani on 4/2/2014.
 */
public class PlaySessionSettingsDialogFragment extends DialogFragment implements View.OnClickListener {

    private Stack stack = null;
    private PlaySession playSession = new PlaySession();
    private EditText nameEditText;
    private EditText stackMinEditText, stackSecEditText;
    private EditText cardMinEditText, cardSecEditText;
    private CheckBox stackLimitCheckBox, cardLimitCheckBox;
    private CheckBox simpleCheckBox, multiCheckBox, writeCheckBox;
    private Button presetTimedButton, presetRapidButton, presetUntimedButton;
    private Button cancelButton, startButton;
    private TextView stackColon, cardColon;
    private DialogInterface.OnClickListener onClickListener;
    private int selectedOption = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.play_settings_dialog_fragment, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        nameEditText = (EditText) view.findViewById(R.id.session_name_editText);
        stackMinEditText = (EditText) view.findViewById(R.id.stack_min_editText);
        stackSecEditText = (EditText) view.findViewById(R.id.stack_sec_editText);
        cardMinEditText = (EditText) view.findViewById(R.id.card_min_editText);
        cardSecEditText = (EditText) view.findViewById(R.id.card_sec_editText);
        stackLimitCheckBox = (CheckBox) view.findViewById(R.id.session_time_limit_checkBox);
        cardLimitCheckBox = (CheckBox) view.findViewById(R.id.card_time_limit_checkBox);
        simpleCheckBox = (CheckBox) view.findViewById(R.id.simple_checkBox);
        multiCheckBox = (CheckBox) view.findViewById(R.id.multi_checkBox);
        writeCheckBox = (CheckBox) view.findViewById(R.id.write_checkBox);
        presetTimedButton = (Button) view.findViewById(R.id.timed_test_button);
        presetRapidButton = (Button) view.findViewById(R.id.rapid_fire_button);
        presetUntimedButton = (Button) view.findViewById(R.id.untimed_button);
        stackColon = (TextView) view.findViewById(R.id.stack_colon_textView);
        cardColon = (TextView) view.findViewById(R.id.card_colon_textView);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        startButton = (Button) view.findViewById(R.id.confirm_button);

        presetRapidButton.setOnClickListener(this);
        presetTimedButton.setOnClickListener(this);
        presetUntimedButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        startButton.setOnClickListener(this);

        cardMinEditText.setText("1");
        stackMinEditText.setText(stack.getNumberOfCards() + "");

        setStackTimeFieldsVisibility(false);
        setCardTimeFieldsVisibility(false);

        stackLimitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setStackTimeFieldsVisibility(isChecked);
            }
        });

        cardLimitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCardTimeFieldsVisibility(isChecked);
            }
        });

        int num = 1;
        while (stack.containsPlaySession("Play Session (" + num + ")")) {
            num++;
        }
        nameEditText.setText("Play Session (" + num + ")");

        return view;
    }

    public PlaySession getPlaySession() {
        return playSession;
    }

    private boolean validate() {
        String name = nameEditText.getText().toString();
        boolean valid = true;
        if (stack != null && stack.containsPlaySession(name) || TextUtils.isEmpty(name)) {
            nameEditText.setError("Invalid name");
            valid = false;
        } else {
            nameEditText.setError(null);
        }
        if (!simpleCheckBox.isChecked() && !multiCheckBox.isChecked() && !writeCheckBox.isChecked()) {
            simpleCheckBox.setChecked(true);
            multiCheckBox.setChecked(true);
            writeCheckBox.setChecked(true);
        }
        if (stackLimitCheckBox.isChecked()) {
            int min = 0, sec = 0;
            try {
                min = Integer.parseInt(stackMinEditText.getText().toString());
                stackMinEditText.setError(null);
            } catch (Exception e) {
                stackMinEditText.setError("Invalid input");
                valid = false;
            }
            try {
                sec = Integer.parseInt(stackSecEditText.getText().toString());
                stackSecEditText.setError(null);
            } catch (Exception e) {
                stackSecEditText.setError("Invalid input");
                valid = false;
            }

            if (valid && (min <= 0 && sec <= 0)) {
                valid = false;
                stackMinEditText.setError("The time limit has to be > 0");
            } else {
                stackMinEditText.setError(null);

                if (min < 0) {
                    stackMinEditText.setError("This can't be less than 0");
                    valid = false;
                } else {
                    stackMinEditText.setError(null);
                }

                if (sec < 0) {
                    stackSecEditText.setError("This can't be less than 0");
                    valid = false;
                } else {
                    stackSecEditText.setError(null);
                }
            }

        } else {
            stackMinEditText.setError(null);
            stackSecEditText.setError(null);
        }
        if (cardLimitCheckBox.isChecked()) {
            int min = 0, sec = 0;
            try {
                min = Integer.parseInt(cardMinEditText.getText().toString());
                cardMinEditText.setError(null);
            } catch (Exception e) {
                cardMinEditText.setError("Invalid input");
                valid = false;
            }
            try {
                sec = Integer.parseInt(cardSecEditText.getText().toString());
                cardSecEditText.setError(null);
            } catch (Exception e) {
                cardSecEditText.setError("Invalid input");
                valid = false;
            }

            if (valid && (min <= 0 && sec <= 0)) {
                valid = false;
                cardMinEditText.setError("The time limit has to be > 0");
            } else {
                cardMinEditText.setError(null);

                if (min < 0) {
                    cardMinEditText.setError("This can't be less than 0");
                    valid = false;
                } else {
                    cardMinEditText.setError(null);
                }

                if (sec < 0) {
                    cardSecEditText.setError("This can't be less than 0");
                    valid = false;
                } else {
                    cardSecEditText.setError(null);
                }
            }

        } else {
            cardSecEditText.setError(null);
            cardMinEditText.setError(null);
        }

        return valid;
    }

    private void onConfirm() {
        if (validate()) {
            nameEditText.setError(null);
            boolean stackLimit = stackLimitCheckBox.isChecked();
            boolean cardLimit = cardLimitCheckBox.isChecked();
            SessionSettings sessionSettings = new SessionSettings(cardLimit, stackLimit,
                    true, getStackSeconds(), getCardSeconds(),
                    simpleCheckBox.isChecked(), multiCheckBox.isChecked(), writeCheckBox.isChecked());
            playSession.setSessionSettings(sessionSettings);

            if (onClickListener != null) {
                onClickListener.onClick(getDialog(), 0);
            }
        }
    }

    private int getStackSeconds() {
        int minutes;
        int seconds;
        try {
            if (TextUtils.isEmpty(stackMinEditText.getText().toString())) {
                minutes = 0;
            } else {
                minutes = Integer.parseInt(stackMinEditText.getText().toString());
            }
            if (TextUtils.isEmpty(stackSecEditText.getText().toString())) {
                seconds = 0;
            } else {
                seconds = Integer.parseInt(stackSecEditText.getText().toString());
            }
            return minutes * 60 + seconds;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private int getCardSeconds() {
        int minutes;
        int seconds;
        try {
            if (TextUtils.isEmpty(cardMinEditText.getText().toString())) {
                minutes = 0;
            } else {
                minutes = Integer.parseInt(cardMinEditText.getText().toString());
            }
            if (TextUtils.isEmpty(cardSecEditText.getText().toString())) {
                seconds = 0;
            } else {
                seconds = Integer.parseInt(cardSecEditText.getText().toString());
            }
            return minutes * 60 + seconds;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                playSession = null;
                dismiss();
                break;
            case R.id.confirm_button:
                onConfirm();
                break;
            case R.id.rapid_fire_button:
                onRapidFireButtonAction();
                break;
            case R.id.timed_test_button:
                onTimedButtonAction();
                break;
            case R.id.untimed_button:
                onUntimedButtonAction();
                break;
        }
    }

    private void onRapidFireButtonAction() {
        stackLimitCheckBox.setChecked(false);
        stackMinEditText.setText((stack.getNumberOfCards()) + "");
        stackSecEditText.setText("0");
        cardLimitCheckBox.setChecked(true);
        cardMinEditText.setText("0");
        cardSecEditText.setText("10");
        selectedOption = 2;
    }

    private void onTimedButtonAction() {
        stackLimitCheckBox.setChecked(true);
        stackMinEditText.setText(stack.getNumberOfCards() + "");
        stackSecEditText.setText("0");
        cardLimitCheckBox.setChecked(true);
        cardMinEditText.setText("1");
        cardSecEditText.setText("0");
        selectedOption = 1;
    }

    private void onUntimedButtonAction() {
        cardLimitCheckBox.setChecked(false);
        stackLimitCheckBox.setChecked(false);
        selectedOption = 0;
    }


    private void setStackTimeFieldsVisibility(boolean visibility) {
        stackColon.setVisibility(visibility ? View.VISIBLE : View.GONE);
        stackMinEditText.setVisibility(visibility ? View.VISIBLE : View.GONE);
        stackSecEditText.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private void setCardTimeFieldsVisibility(boolean visibility) {
        cardColon.setVisibility(visibility ? View.VISIBLE : View.GONE);
        cardMinEditText.setVisibility(visibility ? View.VISIBLE : View.GONE);
        cardSecEditText.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void setOnClickListener(DialogInterface.OnClickListener listener) {
        onClickListener = listener;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            getActivity().getSharedPreferences("session_settings", 0).edit()
                    .putBoolean("stack_limit", stackLimitCheckBox.isChecked())
                    .putBoolean("card_limit", cardLimitCheckBox.isChecked())
                    .putString("stack_min", stackMinEditText.getText().toString())
                    .putString("stack_sec", stackSecEditText.getText().toString())
                    .putString("card_min", cardMinEditText.getText().toString())
                    .putString("card_sec", cardSecEditText.getText().toString())
                    .putBoolean("simple", simpleCheckBox.isChecked())
                    .putBoolean("multi", multiCheckBox.isChecked())
                    .putBoolean("write", writeCheckBox.isChecked())
                    .putInt("preset", selectedOption).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("session_settings", 0);
            stackLimitCheckBox.setChecked(prefs.getBoolean("stack_limit", false));
            cardLimitCheckBox.setChecked(prefs.getBoolean("card_limit", false));
            stackMinEditText.setText(prefs.getString("stack_min", "10"));
            cardMinEditText.setText(prefs.getString("card_min", "0"));
            stackSecEditText.setText(prefs.getString("stack_sec", "0"));
            cardSecEditText.setText(prefs.getString("card_sec", "10"));
            simpleCheckBox.setChecked(prefs.getBoolean("simple", true));
            multiCheckBox.setChecked(prefs.getBoolean("multi", true));
            writeCheckBox.setChecked(prefs.getBoolean("write", true));
            int opt = prefs.getInt("preset", 0);
            switch (opt) {
                case 0:
                    onUntimedButtonAction();
                    break;
                case 1:
                    onTimedButtonAction();
                    break;
                case 2:
                    onRapidFireButtonAction();
                    break;
            }
        }
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }
}
