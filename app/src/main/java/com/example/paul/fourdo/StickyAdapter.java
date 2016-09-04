package com.example.paul.fourdo;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Paul on 25/04/2015.
 */
public class StickyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private static final int TODAY_TAB = 0;
    private static final int SOMEDAY_TAB = 1;

    private LayoutInflater inflater;
    private ArrayList<Task> tasks;
    private Context context;

    private static final String[] todayHeaderTitles = {"Are you doing it?", "Less than an hour", "Less than 2 hours", "Less than 4 hours", "Less than 6 hours", "More than 6 hours", "Any ol' time today, mate"};
    private static final String[] somedayHeaderTitles = {"Tomorrow", "In 2 days", "This week", "Next week", "This month", "Next month", "Future you's problem, you relax", "Someday"};
    private String[] headerTitles;
    SimpleDateFormat dayTimeText = new SimpleDateFormat("E H:mm");

    public StickyAdapter(Context context, Tab callingTab, ArrayList<Task> tasks) {
        inflater = LayoutInflater.from(context);

        this.context = context;
        this.tasks = tasks;

        if (callingTab.thisTabIndex == TODAY_TAB) {
            headerTitles = todayHeaderTitles;
        } else {
            headerTitles = somedayHeaderTitles;
        }
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position).taskName;
    }

    @Override
    public long getHeaderId(int position) {
        // Each possible header text has an id
        return tasks.get(position).headerId;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Setup task row in viewholder to minimise calls to findViewById
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.task_list_item, parent, false);
            holder.taskText = (TextView) convertView.findViewById(R.id.taskNameTextView);
            holder.timeLeft = (TextView) convertView.findViewById(R.id.timeLeft);
            holder.timeStamp = (TextView) convertView.findViewById(R.id.timeStamp);
            // Check box click listener
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.taskCheckBox);
            final View finalConvertView = convertView;
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) context).checkClickListner(finalConvertView, tasks.get(position));
                }
            });

            // Calendar or x
            holder.actionButton = (ImageButton) convertView.findViewById(R.id.taskActionButton);
            holder.actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if we are removing the task from view
                    if (tasks.get(position).done == 1) {
                        ((MainActivity) context).removeTask(tasks.get(position));
                    }
                    // Start the calendar activity which will pass the date selected back to main
                    else {
                        Intent myIntent = new Intent((MainActivity) context, CalendarActivity.class);
                        myIntent.putExtra("databaseId", tasks.get(position).databaseId);
                        myIntent.putExtra("dateCompleteBy", tasks.get(position).dateCompleteBy);
                        // Sliding transition
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation((MainActivity) context,
                                R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        ((MainActivity) context).startActivityForResult(myIntent, 1, translateBundle);
                    }
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set task's text
        holder.taskText.setText(tasks.get(position).taskName);
        holder.timeLeft.setText(tasks.get(position).timeUntil);
        if (tasks.get(position).dateCompleteBy > 0) {
            holder.timeStamp.setText(dayTimeText.format(new Date(tasks.get(position).dateCompleteBy)));
        }
        else{
            holder.timeStamp.setText("");
        }


        // Set if task is 'done' or not
        if (tasks.get(position).done == 1) {// Task is finished - check checkbox, add strike, gray out
            holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskText.setTextColor(Color.GRAY);
            holder.checkBox.setChecked(true);
            // Set image beside checkbox to x
            holder.actionButton.setImageResource(R.drawable.ic_close);
        } else {
            holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskText.setTextColor(Color.WHITE);
            holder.checkBox.setChecked(false);

            // Set image beside checkbox to indicate if time is set
            if (tasks.get(position).dateCompleteBy > 0)
                holder.actionButton.setImageResource(R.drawable.ic_calendar_check);
            else
                holder.actionButton.setImageResource(R.drawable.perm_group_calendar);
        }

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.textViewHeader);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        // Set header text
        holder.text.setText(headerTitles[tasks.get(position).headerId]);
        return convertView;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView taskText;
        TextView timeLeft;
        TextView timeStamp;
        CheckBox checkBox;
        ImageButton actionButton;
    }
}
