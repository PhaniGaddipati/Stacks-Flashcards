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
package org.centum.android.draw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.centum.android.model.draw.Drawing;
import org.centum.android.model.draw.DrawingHolder;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.stack.R;
import org.centum.android.utils.AttachmentHandler;

import java.io.IOException;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;

/**
 * Created by Phani on 3/10/14.
 */
public class DrawActivity extends Activity implements View.OnClickListener {

    private static final int COLORS[] = new int[]{
            Color.BLACK, Color.WHITE, Color.LTGRAY, Color.rgb(255, 20, 147),
            Color.RED, Color.BLUE, Color.CYAN, Color.GREEN,
            Color.YELLOW, Color.MAGENTA, Color.rgb(255, 165, 0)};
    private static int DP_50_PX, DP_40_PX, DP_30_PX;

    private LinearLayout colorLinearLayout;
    private ImageView[] colorSquareViews = new ImageView[COLORS.length];
    private ImageView barImageView;
    private ImageView addStrokeImageView, removeStrokeImageView;
    private ImageView undoImageView, redoImageView;
    private ImageView strokePreviewImageView;
    private ImageView eraseImageView, customColorImageView;
    private FrameLayout surfaceViewFrameLayout;

    private DrawSurfaceView drawSurfaceView;
    private Bitmap strokeBitmap;
    private Canvas strokeCanvas;

    private Paint strokePaint = new Paint();
    private Drawing drawing = new Drawing();

    private int customColor = Color.BLACK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_activity);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        DP_50_PX = dpToPx(50);
        DP_40_PX = dpToPx(40);
        DP_30_PX = dpToPx(30);

        strokeBitmap = Bitmap.createBitmap(DP_50_PX, DP_50_PX, Bitmap.Config.ARGB_8888);
        strokeCanvas = new Canvas(strokeBitmap);
        strokePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        colorLinearLayout = (LinearLayout) findViewById(R.id.colors_linearLayout);
        barImageView = (ImageView) findViewById(R.id.bar_imageView);
        addStrokeImageView = (ImageView) findViewById(R.id.addstroke_imageview);
        removeStrokeImageView = (ImageView) findViewById(R.id.minusstroke_imageView);
        undoImageView = (ImageView) findViewById(R.id.undo_imageView);
        redoImageView = (ImageView) findViewById(R.id.redo_imageView);
        strokePreviewImageView = (ImageView) findViewById(R.id.stroke_imageView);
        eraseImageView = (ImageView) findViewById(R.id.erase_imageView);
        customColorImageView = (ImageView) findViewById(R.id.customColor_imageView);
        surfaceViewFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        for (int i = 0; i < COLORS.length; i++) {
            colorSquareViews[i] = new ImageView(this);
            colorSquareViews[i].setMinimumWidth(DP_40_PX);
            colorSquareViews[i].setMinimumHeight(DP_40_PX);
            colorSquareViews[i].setBackgroundColor(COLORS[i]);
            colorSquareViews[i].setImageDrawable(new ColorDrawable(COLORS[i]));
            colorSquareViews[i].setOnClickListener(this);
            colorLinearLayout.addView(colorSquareViews[i]);
        }

        addStrokeImageView.setOnClickListener(this);
        removeStrokeImageView.setOnClickListener(this);
        undoImageView.setOnClickListener(this);
        redoImageView.setOnClickListener(this);
        eraseImageView.setOnClickListener(this);
        customColorImageView.setOnClickListener(this);

        customColor = Color.YELLOW;

        if (savedInstanceState != null) {
            drawing = DrawingHolder.get().popDrawing(savedInstanceState.getString("drawing"));
        }
        //showBetaMessage();
    }

    @Override
    public void onStart() {
        super.onStart();
        drawSurfaceView = new DrawSurfaceView(this);
        drawSurfaceView.setDrawingCacheEnabled(true);
        drawSurfaceView.setDrawing(drawing);
        surfaceViewFrameLayout.addView(drawSurfaceView);
        setColor(getSharedPreferences("settings", 0).getInt("color", COLORS[5]));
        drawSurfaceView.setStrokeRadius(getSharedPreferences("settings", 0).getInt("radius", 8));
        renderStroke();
    }

    @Override
    public void onStop() {
        super.onStop();
        surfaceViewFrameLayout.removeAllViews();
        drawSurfaceView.setDrawingCacheEnabled(false);
        drawSurfaceView.destroyDrawingCache();
        drawSurfaceView = null;
    }

    private void showBetaMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("This feature is in beta testing");
        builder.setMessage("Please report any crashes, and contact me for all recommendations and/or bugfixes.");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Send Feedback", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "phanigaddipati@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Stacks Feedback");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
        builder.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("drawing", DrawingHolder.get().holdDrawing(drawing));
    }


    @Override
    public void onResume() {
        super.onResume();
        drawSurfaceView.setNotUpdated();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showConfirmExit();
                return true;
            case R.id.action_save:
                onSave();
                return true;
            case R.id.action_discard:
                showConfirmExit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit?");
        builder.setMessage("You'll lose all of your work");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        showConfirmExit();
    }

    private void onSave() {
        new AsyncTask<Void, Void, Void>() {

            private String result = null;
            private ProgressDialog progressDialog = new ProgressDialog(DrawActivity.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle("Saving");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                int name = 0;
                drawSurfaceView.buildDrawingCache(true);
                Bitmap bitmap = drawSurfaceView.getDrawingCache();
                Bitmap croppedBitmap;
                if (drawing.getNumberOfActions() > 0 &&
                        PreferenceManager.getDefaultSharedPreferences(DrawActivity.this)
                                .getBoolean(SettingsActivity.KEY_PREF_CARD_CROP_DRAWING, true)) {
                    croppedBitmap = autoCropBitmap(bitmap, drawing);
                } else {
                    croppedBitmap = bitmap;
                }
                while (AttachmentHandler.get(DrawActivity.this).cacheDirContains(name + ".png")) {
                    name++;
                }
                try {
                    AttachmentHandler.get(DrawActivity.this).writeBitmapToCacheDir(croppedBitmap, name + ".png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                result = name + ".png";
                croppedBitmap = null;
                bitmap = null;
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                progressDialog.dismiss();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("name", this.result);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }.execute();

    }

    private Bitmap autoCropBitmap(Bitmap bitmap, Drawing drawing) {
        Rect bounds = drawing.getActionBounds();
        int x = Math.max(0, bounds.left - 5);
        int y = Math.max(0, bounds.top - 5);
        int width = Math.min(bounds.width() + 10, bitmap.getWidth() - x);
        int height = Math.min(bounds.height() + 10, bitmap.getHeight() - y);
        if (width * height < bitmap.getWidth() * bitmap.getHeight()) {
            return Bitmap.createBitmap(bitmap, x, y, width, height);
        } else {
            return bitmap;
        }
    }

    private void renderStroke() {
        strokePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokePaint.setColor(drawSurfaceView.getColor());
        strokeCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        strokeCanvas.drawCircle(DP_50_PX / 2, DP_50_PX / 2, drawSurfaceView.getStrokeRadius(), strokePaint);
        strokePreviewImageView.setImageDrawable(new BitmapDrawable(getResources(), strokeBitmap));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(2);
        strokeCanvas.drawCircle(DP_50_PX / 2, DP_50_PX / 2, drawSurfaceView.getStrokeRadius(), strokePaint);
    }

    private void setColor(int color) {
        drawSurfaceView.setColor(color);
        barImageView.setImageDrawable(new ColorDrawable(color));
        getSharedPreferences("settings", 0).edit().putInt("color", color).commit();
        renderStroke();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        for (int i = 0; i < COLORS.length; i++) {
            if (colorSquareViews[i] == v) {
                setColor(COLORS[i]);
                return;
            }
        }
        if (id == customColorImageView.getId()) {
            showColorPicker();
        }
        if (id == eraseImageView.getId()) {
            drawSurfaceView.setEraser(true);
            barImageView.setImageDrawable(null);
            renderStroke();
        }
        if (id == addStrokeImageView.getId()) {
            if (drawSurfaceView.getStrokeRadius() < (DP_50_PX / 2 - 4)) {
                drawSurfaceView.setStrokeRadius(drawSurfaceView.getStrokeRadius() + 4);
                getSharedPreferences("settings", 0).edit().putInt("radius", drawSurfaceView.getStrokeRadius()).commit();
                renderStroke();
            }
            return;
        }
        if (id == removeStrokeImageView.getId()) {
            drawSurfaceView.setStrokeRadius(Math.max(2, drawSurfaceView.getStrokeRadius() - 4));
            getSharedPreferences("settings", 0).edit().putInt("radius", drawSurfaceView.getStrokeRadius()).commit();
            renderStroke();
            return;
        }
        if (id == undoImageView.getId()) {
            drawing.undoLastAction();
            return;
        }
        if (id == redoImageView.getId()) {
            drawing.redoLastAction();
        }
    }

    private void showColorPicker() {
        final ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, customColor);
        colorPickerDialog.setAlphaSliderVisible(true);
        colorPickerDialog.setTitle("Pick Color");
        colorPickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                customColor = colorPickerDialog.getColor();
                setColor(customColor);
            }
        });
        colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                setColor(COLORS[1]);
            }
        });
        colorPickerDialog.show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + .5f);
    }
}
