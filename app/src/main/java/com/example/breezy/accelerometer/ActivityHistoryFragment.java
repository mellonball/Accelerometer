package com.example.breezy.accelerometer;

import android.app.ListFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ActivityHistoryFragment extends ListFragment {


    private List<HistoryItem> mHistoryItems;

    public ActivityHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        mHistoryItems = new ArrayList<>();
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(0),
                getEndDate(0) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_walk),
                HistoryItem.UserActivity.WALKING,
                getStartDate(2),
                getEndDate(2) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_sit),
                HistoryItem.UserActivity.SITTING,
                getStartDate(4),
                getEndDate(4) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(6),
                getEndDate(6) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_walk),
                HistoryItem.UserActivity.WALKING,
                getStartDate(8),
                getEndDate(8) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_sit),
                HistoryItem.UserActivity.SITTING,
                getStartDate(10),
                getEndDate(10) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(12),
                getEndDate(12) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(14),
                getEndDate(14) ));
        mHistoryItems.add(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(16),
                getEndDate(16) ));
        setListAdapter(new ActivityHistoryAdapter(getActivity(), mHistoryItems));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HistoryItem item = mHistoryItems.get(position);
        Toast.makeText(getActivity(), item.getActivity().toString(), Toast.LENGTH_SHORT).show();
    }

    private Date getStartDate(int minutes) {
        return new Date( (System.currentTimeMillis() + minutes * 1000) );
    }

    private Date getEndDate(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date( (System.currentTimeMillis() + minutes * 1000)) );
        cal.add(Calendar.MINUTE, 2 + minutes);
        return cal.getTime();
    }

}
