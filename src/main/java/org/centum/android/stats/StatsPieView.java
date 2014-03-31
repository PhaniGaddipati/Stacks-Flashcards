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
package org.centum.android.stats;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.centum.android.model.play.PlayStats;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/22/14.
 */
public class StatsPieView extends RelativeLayout {

    private PlayStats playStats;
    private boolean initialized;
    private TextView labelTextView;
    private TextView sessionTextView;
    private RelativeLayout statsRelativeLayout;
    private FrameLayout pieLayout;
    private int correctColor, wrongColor;
    private GraphicalView pieChartView;
    private PlayStats stats;

    public StatsPieView(Context context) {
        super(context);
    }

    public StatsPieView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatsPieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        labelTextView = (TextView) findViewById(R.id.label_textView);
        pieLayout = (FrameLayout) findViewById(R.id.pie_frameLayout);
        sessionTextView = (TextView) findViewById(R.id.sessions_textView);
        correctColor = getResources().getColor(R.color.holo_green);
        wrongColor = getResources().getColor(R.color.holo_red);
        statsRelativeLayout = (RelativeLayout) findViewById(R.id.statspie_relativelayout);
        setThemeParams();
        initialized = true;
    }

    private void updatePieChart() {
        DefaultRenderer pieRenderer = new DefaultRenderer();
        SimpleSeriesRenderer correctRenderer = new SimpleSeriesRenderer();
        SimpleSeriesRenderer wrongRenderer = new SimpleSeriesRenderer();

        correctRenderer.setColor(correctColor);
        wrongRenderer.setColor(wrongColor);

        pieRenderer.addSeriesRenderer(correctRenderer);
        pieRenderer.addSeriesRenderer(wrongRenderer);
        if (!Themes.get().isThemeDark()) {
            pieRenderer.setLabelsColor(Color.DKGRAY);
        } else {
            pieRenderer.setLabelsColor(Color.WHITE);
        }
        pieRenderer.setLabelsTextSize(spToPixels(14));
        pieRenderer.setShowLegend(false);
        pieRenderer.setZoomEnabled(false);
        pieRenderer.setPanEnabled(false);
        pieRenderer.setChartTitleTextSize(spToPixels(20));
        pieRenderer.setStartAngle(45);
        pieRenderer.setScale(1.1f);

        CategorySeries data = new CategorySeries("Overview");
        data.add(playStats.getTotalCorrect() + " Correct", playStats.getTotalCorrect());
        data.add(playStats.getTotalWrong() + " Wrong", playStats.getTotalWrong());

        pieChartView = ChartFactory.getPieChartView(getContext(), data, pieRenderer);
        pieLayout.removeAllViews();
        pieLayout.addView(pieChartView);
    }

    private void setThemeParams() {
        if (Themes.get().isThemeDark()) {
            labelTextView.setTextColor(getResources().getColor(android.R.color.white));
            sessionTextView.setTextColor(getResources().getColor(android.R.color.white));
            statsRelativeLayout.setBackgroundResource(R.drawable.card_bg_dark);
        }
    }

    private int dpToPixels(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPixels(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, getResources().getDisplayMetrics());
    }

    private void update() {
        if (playStats != null) {
            updatePieChart();
        }
        sessionTextView.setText(playStats.getNumberOfSessions() + " sessions enabled");
        invalidate();
    }

    public void setStats(PlayStats playStats) {
        this.playStats = playStats;
        if (!initialized) {
            init();
        } else {
            setThemeParams();
        }

        update();
    }

    public PlayStats getStats() {
        return stats;
    }
}
