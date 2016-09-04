package com.example.paul.fourdo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Paul on 26/04/2015.
 */
public class ViewTasksActivity extends ActionBarActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tasks);

        ArrayList<Task> tasks;
        TodoListSQLHelper todoListSQLHelper = new TodoListSQLHelper(ViewTasksActivity.this);
        SQLiteDatabase db = todoListSQLHelper.getReadableDatabase();
        Cursor cursor =  db.rawQuery(
                "SELECT * FROM "+TodoListSQLHelper.TABLE_NAME+" ORDER BY "+TodoListSQLHelper.DATE_CREATED+" DESC", null);

        // Populate ArrayList
        tasks = new ArrayList<>();
        while (cursor.moveToNext()) {
            Task newTask = new Task(
                    cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper._ID)),
                    cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.TASK_NAME)),
                    cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.TASK_TAB_ID)),
                    cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.PARENT_TASK_ID)),
                    cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.DONE)),
                    cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.NUM_SUB_TASKS)),
                    cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_CREATED)),
                    cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_COMPLETE_BY)),
                    cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_COMPLETED))
            );
            tasks.add(newTask);
        }
        cursor.close();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        String taskDetails = "";
        TextView textView = (TextView) findViewById(R.id.textViewTasks);
        for (int i=0;i<tasks.size();i++){
            Task thisTask = tasks.get(i);
            Calendar dateCreatedCal = Calendar.getInstance();
            dateCreatedCal.setTimeInMillis(thisTask.dateCreated);
            String dateCreatedStr = sdf.format(dateCreatedCal.getTime())+" ";

            String dateCompleteByStr = "";
            if (thisTask.dateCompleteBy != 0) {
                Calendar dateCompleteByCal  = Calendar.getInstance();
                dateCompleteByCal.setTimeInMillis(thisTask.dateCompleteBy);
                dateCompleteByStr = sdf.format(dateCompleteByCal.getTime())+" ";
            }

            String dateCompletedStr = "";
            if (thisTask.dateCompleted != 0) {
                Calendar dateCompletedCal = Calendar.getInstance();
                dateCompletedCal.setTimeInMillis(thisTask.dateCompleted);
                dateCompletedStr = sdf.format(dateCompletedCal.getTime()) + " ";
            }

            taskDetails += "databaseId: " + thisTask.databaseId +
                    "\ntaskName: " + thisTask.taskName +
                    "\ntaskTabIndex: " + thisTask.taskTabIndex +
                    "\nparentTaskId: " + thisTask.parentTaskId +
                    "\nnumSubTasks: " + thisTask.numSubTasks +
                    "\ndone: " + thisTask.done +
                    "\ndateCreated: " + dateCreatedStr + thisTask.dateCreated +
                    "\ndateCompleteBy: " + dateCompleteByStr + thisTask.dateCompleteBy +
                    "\ndateCompleted: " + dateCompletedStr + thisTask.dateCompleted +"\n\n";
        }
        textView.setText(taskDetails);
    }
}
