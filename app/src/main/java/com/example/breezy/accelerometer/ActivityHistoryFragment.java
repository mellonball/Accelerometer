package com.example.breezy.accelerometer;

import android.app.ListFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class ActivityHistoryFragment extends ListFragment implements MainActivity.IHistoryChanged {


    private LinkedList<HistoryItem> mHistoryItems;
    private ActivityHistoryAdapter mAdapter;

    private final int MAX_HISTORY_ITEMS = 10;

    public ActivityHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        mHistoryItems = new LinkedList<>();
        //Temporary data until we get the actual view working.
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(0),
                getEndDate(0)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_walk),
                HistoryItem.UserActivity.WALKING,
                getStartDate(2),
                getEndDate(2)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_sit),
                HistoryItem.UserActivity.SITTING,
                getStartDate(4),
                getEndDate(4)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(6),
                getEndDate(6)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_walk),
                HistoryItem.UserActivity.WALKING,
                getStartDate(8),
                getEndDate(8)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_sit),
                HistoryItem.UserActivity.SITTING,
                getStartDate(10),
                getEndDate(10)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(12),
                getEndDate(12)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(14),
                getEndDate(14)));
        mHistoryItems.push(new HistoryItem(res.getDrawable(R.drawable.ic_sleep),
                HistoryItem.UserActivity.SLEEPING,
                getStartDate(16),
                getEndDate(16)));
        mAdapter = new ActivityHistoryAdapter(getActivity(), mHistoryItems);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HistoryItem item = mHistoryItems.get(position);
        Toast.makeText(getActivity(), item.getActivity().toString(), Toast.LENGTH_SHORT).show();
    }

    private Date getStartDate(int minutes) {
        return new Date( (System.currentTimeMillis() + minutes * 60000) );
    }

    private Date getEndDate(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date( (System.currentTimeMillis() + minutes * 60000)) );
        cal.add(Calendar.MINUTE, 2 + minutes);
        return cal.getTime();
    }

    @Override
    public void newHistoryActivity(HistoryItem item) {
        mHistoryItems.push(item);
        if(mHistoryItems.size() > MAX_HISTORY_ITEMS) {
            mHistoryItems.removeLast();
        }
        mAdapter.notifyDataSetChanged();
    }
}
