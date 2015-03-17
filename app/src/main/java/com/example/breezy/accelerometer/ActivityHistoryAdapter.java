package com.example.breezy.accelerometer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kristianhfischer on 3/16/15.
 */
public class ActivityHistoryAdapter extends ArrayAdapter<HistoryItem> {


    public ActivityHistoryAdapter(Context context, List<HistoryItem> activites) {
        super(context, R.layout.activity_history_item, activites);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.activity_history_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.mStatusIcon = (ImageView) convertView.findViewById(R.id.activity_status_icon);
            //viewHolder.mStatusText = (TextView) convertView.findViewById(R.id.activity_status_description);
            viewHolder.mDateRange = (TextView) convertView.findViewById(R.id.activity_date_range);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        HistoryItem item = getItem(position);
        viewHolder.mStatusIcon.setImageDrawable(item.getIcon());
        //viewHolder.mStatusText.setText(item.getActivity().toString());
        viewHolder.mDateRange.setText(item.getDisplayTimeRange());
        return convertView;
    }

    private static class ViewHolder {
        ImageView mStatusIcon;
        //TextView mStatusText;
        TextView mDateRange;
    }

}
