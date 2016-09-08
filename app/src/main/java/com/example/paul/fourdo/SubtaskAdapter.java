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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Paul on 29/04/2015.
 */
public class SubtaskAdapter extends ArrayAdapter<Task> {
    private ArrayList<Task> tasks;
    private Context context;
    private LayoutInflater inflater;

    public SubtaskAdapter(Context context, ArrayList<Task> tasks) {
        super(context, R.layout.subtask_list_item, tasks);

        this.tasks = tasks;
        this.context = context;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Setup task row in viewholder to minimise calls to findViewById
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.subtask_list_item, parent, false);
            holder.taskText = (TextView) convertView.findViewById(R.id.subtaskNameTextView);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.subtaskCheckBox);
            final View finalConvertView = convertView;
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SubtaskActivity) context).checkClickListner(finalConvertView, tasks.get(position));
                }
            });
            holder.actionButton = (ImageButton) convertView.findViewById(R.id.subtaskActionButton);
            holder.actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if we are removing the task from view
                    ((SubtaskActivity) context).removeItem(tasks.get(position));
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set task's text
        holder.taskText.setText(tasks.get(position).taskName);

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

            // Turn off the image
            holder.actionButton.setImageResource(0);
        }

        return convertView;
    }

    class ViewHolder {
        TextView taskText;
        CheckBox checkBox;
        ImageButton actionButton;
    }
}
