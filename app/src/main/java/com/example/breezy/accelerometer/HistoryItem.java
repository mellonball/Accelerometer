package com.example.breezy.accelerometer;

import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kristianhfischer on 3/16/15.
 */
public class HistoryItem {

    public static enum UserActivity {
        WALKING ("Walking"),
        SITTING ("Sitting"),
        SLEEPING ("Sleeping");

        private final String ACTIVITY;

        UserActivity(String activity) {
            ACTIVITY = activity;
        }

        @Override
        public String toString() {
            return ACTIVITY;
        }
    }

    private final Drawable mIcon;
    private final UserActivity mActivity;
    private final Date mStartTime, mEndTime;

    public HistoryItem(Drawable icon, UserActivity activity, Date startTime, Date endTime) {
        mIcon = icon;
        mActivity = activity;
        mStartTime = startTime;
        mEndTime = endTime;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public UserActivity getActivity() {
        return mActivity;
    }

    public String getDisplayTimeRange() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        String start = dateFormat.format(mStartTime);
        String end = dateFormat.format(mEndTime);
        return start + " - " + end;
    }
}
