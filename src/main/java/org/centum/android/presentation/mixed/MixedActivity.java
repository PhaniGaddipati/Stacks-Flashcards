package org.centum.android.presentation.mixed;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.WindowManager;

import org.centum.android.presentation.general.AbstractPlayActivity;

/**
 * Created by Phani on 4/16/2014.
 */
public class MixedActivity extends AbstractPlayActivity {

    private MixedPagerAdapter pagerAdapter;

    public MixedActivity() {
        setImmersive(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    protected void onStackSet() {
        pagerAdapter = new MixedPagerAdapter(this, getStack(), getPlaySession());
    }

    @Override
    public PagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }
}
