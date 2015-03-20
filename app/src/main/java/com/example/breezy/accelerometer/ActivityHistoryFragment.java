package com.example.breezy.accelerometer;

import android.app.ListFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;

public class ActivityHistoryFragment extends ListFragment {


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
        mAdapter = new ActivityHistoryAdapter(getActivity(), mHistoryItems);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HistoryItem item = mHistoryItems.get(position);
        Toast.makeText(getActivity(), item.getActivity().toString(), Toast.LENGTH_SHORT).show();
    }

    //Used by MainActivity to update UI
    //Need to get to this function with fragment manager (I think?)
    public void addNewHistoryActivity(HistoryItem item) {
        mHistoryItems.push(item);
        if(mHistoryItems.size() > MAX_HISTORY_ITEMS) {
            mHistoryItems.removeLast();
        }
        mAdapter.notifyDataSetChanged();
    }

}
