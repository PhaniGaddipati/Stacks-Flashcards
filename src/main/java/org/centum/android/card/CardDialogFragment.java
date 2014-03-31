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
package org.centum.android.card;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.centum.android.draw.DrawActivity;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.stack.R;
import org.centum.android.utils.ArrayAdapterWithIcon;
import org.centum.android.utils.AttachmentHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by Phani on 1/3/14.
 */
public class CardDialogFragment extends DialogFragment {

    private static final int PICK_IMAGE = 1;
    private static final int DRAW_IMAGE = 2;
    private static final int CAMERA_IMAGE = 3;

    private TextView titleTextView;
    private EditText titleEditText;
    private EditText detailsEditText;
    private ImageView attachmentImageView;
    private ImageView deleteAttachmentImageView;
    private CheckBox imageDetailsCheckBox;
    private Button createButton;
    private Button cancelButton;
    private String attachment = null;
    private Stack stack = null;
    private Card card = null;
    private String fileName;

    public CardDialogFragment(Stack stack) {
        this.stack = stack;
    }

    public CardDialogFragment(Stack stack, Card card) {
        this.stack = stack;
        this.card = card;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.card_dialog_fragment, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        titleTextView = (TextView) view.findViewById(R.id.title_textView);
        titleEditText = (EditText) view.findViewById(R.id.title_editText);
        detailsEditText = (EditText) view.findViewById(R.id.details_editText);
        attachmentImageView = (ImageView) view.findViewById(R.id.attachment_imageView);
        deleteAttachmentImageView = (ImageView) view.findViewById(R.id.delete_attachment_imageView);
        createButton = (Button) view.findViewById(R.id.create_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        imageDetailsCheckBox = (CheckBox) view.findViewById(R.id.image_detail_checkBox);

        if (card == null) {
            titleEditText.requestFocus();
        } else {
            titleTextView.setText(getResources().getString(R.id.edit_card));
            createButton.setText(getResources().getString(R.id.edit_card));
            createButton.setEnabled(true);
            titleEditText.setText(card.getTitle());
            detailsEditText.setText(card.getDetails());
            attachment = card.getAttachment();
            imageDetailsCheckBox.setChecked(card.isAttachmentPartOfDetails());
            updateIcon();
        }

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                createButton.setEnabled(!TextUtils.isEmpty(titleEditText.getText().toString()));
            }
        });
        attachmentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAttachmentPicker();
            }
        });
        deleteAttachmentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachment = null;
                updateIcon();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (card == null) {
                    card = new Card(titleEditText.getText().toString().trim());
                    stack.addCard(card);
                }
                card.setTitle(titleEditText.getText().toString().trim());
                card.setDetails(detailsEditText.getText().toString().trim());
                card.setAttachment(attachment);
                card.setAttachmentPartOfDetails(imageDetailsCheckBox.isChecked());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_IMAGE && resultCode == Activity.RESULT_OK) {
            attachment = fileName;
            updateIcon();
        }
        if (requestCode == DRAW_IMAGE && data != null && resultCode == Activity.RESULT_OK) {
            attachment = data.getStringExtra("name");
            updateIcon();
        }
        if (requestCode == PICK_IMAGE && data != null && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Cursor cursor = getActivity().getContentResolver().query(uri,
                    new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            String imageFilePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
            if (imageFilePath != null && !TextUtils.isEmpty(imageFilePath)) {
                final File file = new File(imageFilePath);

                if (!AttachmentHandler.get(getActivity()).cacheContains(file.getName())) {
                    final ProgressDialog progress = new ProgressDialog(getActivity());
                    progress.setCanceledOnTouchOutside(false);
                    progress.setCancelable(false);
                    progress.setTitle("Loading Image");
                    progress.setMessage("Please wait...");
                    progress.show();
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                AttachmentHandler.get(getActivity()).copyToCacheDir(file.getParent(), file.getName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void v) {
                            progress.dismiss();
                            updateIcon();
                        }
                    }.execute();
                }
                attachment = file.getName();
                updateIcon();
            }
        }
    }

    private void updateIcon() {
        if (attachment == null) {
            attachmentImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery));
            deleteAttachmentImageView.setVisibility(View.INVISIBLE);
        } else {
            try {
                attachmentImageView.setImageDrawable(new BitmapDrawable(getResources(), AttachmentHandler.get(getActivity()).getScaledBitmap(attachment)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            deleteAttachmentImageView.setVisibility(View.VISIBLE);
        }
    }


    private void showAttachmentPicker() {
        final String[] items;
        final Integer[] icons;

        if (getActivity() != null && getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            items = new String[]{"From Gallery", "Draw Image", "From Camera"};
            icons = new Integer[]{android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_edit, android.R.drawable.ic_menu_camera};
        } else {
            items = new String[]{"From Gallery", "Draw Image"};
            icons = new Integer[]{android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_edit};
        }
        ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Attachment");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showGalleryImage();
                        break;
                    case 1:
                        showDrawImage();
                        break;
                    case 2:
                        showCameraImage();
                        break;
                }
            }
        });
        builder.show();

    }

    private void showGalleryImage() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE);
    }

    private void showCameraImage() {
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            int num = 0;
            while (AttachmentHandler.get(getActivity()).cacheDirContains("cam_" + num + ".png")) {
                num++;
            }
            fileName = "cam_" + num + ".png";

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri = Uri.fromFile(new File(AttachmentHandler.get(getActivity()).getCacheDir(), fileName));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("return-data", false);
            startActivityForResult(intent, CAMERA_IMAGE);
        }
    }

    private void showDrawImage() {
        startActivityForResult(new Intent(getActivity(), DrawActivity.class), DRAW_IMAGE);
    }


}
