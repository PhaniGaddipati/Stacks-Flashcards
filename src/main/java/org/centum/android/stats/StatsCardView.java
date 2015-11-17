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
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.centum.android.model.Card;
import org.centum.android.model.play.PlayStats;
import org.centum.android.settings.Themes;
import org.centum.android.stack.R;

/**
 * Created by Phani on 1/22/14.
 */
public class StatsCardView extends RelativeLayout {

    private TextView mCardLabel, mStatsLabel;
    private FrameLayout mChartLayout;
    private GraphicalView mBarChartView;
    private RelativeLayout statsRelativeLayout;
    private int mCorrectColor, mWrongColor;
    private Card mCard;
    private boolean mInitialized = false;
    private PlayStats mPlayStats;
    private XYSeriesRenderer wrongRenderer;
    private XYSeriesRenderer correctRenderer;
    private XYMultipleSeriesRenderer barRenderer;

    public StatsCardView(Context context) {
        super(context);
    }

    public StatsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatsCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        mCardLabel = (TextView) findViewById(R.id.card_textView);
        mStatsLabel = (TextView) findViewById(R.id.stats_textView);
        mChartLayout = (FrameLayout) findViewById(R.id.chart_frameLayout);
        statsRelativeLayout = (RelativeLayout) findViewById(R.id.stats_relativelayout);

        mCorrectColor = getResources().getColor(R.color.holo_green);
        mWrongColor = getResources().getColor(R.color.holo_red);

        correctRenderer = new XYSeriesRenderer();
        wrongRenderer = new XYSeriesRenderer();

        correctRenderer.setColor(mCorrectColor);
        correctRenderer.setDisplayChartValues(false);

        wrongRenderer.setColor(mWrongColor);
        wrongRenderer.setDisplayChartValues(false);

        barRenderer = new XYMultipleSeriesRenderer();
        barRenderer.addSeriesRenderer(correctRenderer);
        barRenderer.addSeriesRenderer(wrongRenderer);
        barRenderer.setZoomEnabled(false, false);
        barRenderer.setPanEnabled(false);

        barRenderer.setBarSpacing(1d);
        barRenderer.setXAxisMin(.5);
        barRenderer.setYAxisMin(.5);
        barRenderer.setXAxisMax(1.5);
        barRenderer.setBarWidth(dpToPixels(90));
        barRenderer.setLegendTextSize(spToPixels(14));
        barRenderer.setLegendHeight(spToPixels(14));
        barRenderer.setShowLegend(false);
        barRenderer.setApplyBackgroundColor(true);
        barRenderer.setBackgroundColor(Color.TRANSPARENT);
        barRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);
        barRenderer.setMargins(new int[]{0, 0, 0, 0});
        if (!Themes.get(getContext()).isThemeDark()) {
            barRenderer.setMarginsColor(Color.WHITE);
        } else {
            barRenderer.setMarginsColor(getResources().getColor(R.color.dark_card_bg));
        }
        barRenderer.setXLabels(0);
        barRenderer.setYLabels(0);

        setThemeParams();

        mInitialized = true;
    }

    private void setThemeParams() {
        if (Themes.get(getContext()).isThemeDark()) {
            mCardLabel.setTextColor(getResources().getColor(android.R.color.white));
            mStatsLabel.setTextColor(getResources().getColor(android.R.color.white));
            statsRelativeLayout.setBackgroundResource(R.drawable.card_bg_dark);
        }
    }

    public void update() {
        if (!mInitialized) {
            init();
        } else {
            setThemeParams();
        }
        if (getCard() == null) {
            mCardLabel.setText("");
            mChartLayout.removeAllViews();
        } else {
            mCardLabel.setText(getCard().getTitle());
            updateChart();
        }

        invalidate();
    }

    private void updateChart() {
        XYMultipleSeriesDataset data = new XYMultipleSeriesDataset();
        CategorySeries correctSeries = new CategorySeries("Correct");
        CategorySeries wrongSeries = new CategorySeries("Wrong");

        int correct = mPlayStats.getNumberCorrect(mCard);
        int wrong = mPlayStats.getNumberWrong(mCard);

        mStatsLabel.setText(correct + " Correct, " + wrong + " Wrong");

        correctSeries.add("Correct", correct);
        wrongSeries.add("Wrong", wrong);

        data.addSeries(correctSeries.toXYSeries());
        data.addSeries(wrongSeries.toXYSeries());

        barRenderer.setYAxisMax(Math.max(correct, wrong));

        mBarChartView = ChartFactory.getBarChartView(getContext(), data, barRenderer, BarChart.Type.DEFAULT);

        mChartLayout.removeAllViews();
        mChartLayout.addView(mBarChartView);
    }

    public void setCard(PlayStats playStats, Card card) {
        this.mCard = card;
        mPlayStats = playStats;
        update();
    }

    public Card getCard() {
        return mCard;
    }

    private int spToPixels(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    private int dpToPixels(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public PlayStats getStats() {
        return mPlayStats;
    }
}
