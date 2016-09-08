package com.example.paul.fourdo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Paul on 28/04/2015.
 */
public class SubtaskActivity extends ActionBarActivity {

    private TodoListSQLHelper todoListSQLHelper;
    private ArrayList<Task> subTasks;
    int parentDatabaseId;
    public TextView subtaskEditText;
    public ListView subtaskList;
    public static ListAdapter subtaskListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtask);

        // Get info passed from main
        parentDatabaseId = getIntent().getExtras().getInt("databaseId");

        // Get parent task and any existing sub tasks
        todoListSQLHelper = new TodoListSQLHelper(SubtaskActivity.this);
        SQLiteDatabase db = todoListSQLHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TodoListSQLHelper.TABLE_NAME +
                        " WHERE " + TodoListSQLHelper._ID + "=" + parentDatabaseId + " OR " + TodoListSQLHelper.PARENT_TASK_ID + "=" + parentDatabaseId +
                        " ORDER BY " + TodoListSQLHelper.DATE_CREATED + " ASC", null);

        // Populate ArrayList
        subTasks = new ArrayList<>();
        while (cursor.moveToNext()) {
            subTasks.add(
                    new Task(
                            cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper._ID)),
                            cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.TASK_NAME)),
                            cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.TASK_TAB_ID)),
                            cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.PARENT_TASK_ID)),
                            cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.DONE)),
                            cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.NUM_SUB_TASKS)),
                            cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_CREATED)),
                            cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_COMPLETE_BY)),
                            cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_COMPLETED))
                    )
            );
        }
        cursor.close();
        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the title for the activity then remove task so it's not shown in list
        setTitle(subTasks.get(0).taskName);
        subTasks.remove(0);

        // Set list adapter
        subtaskListAdapter = new SubtaskAdapter(this, subTasks);
        subtaskList = (ListView) findViewById(R.id.subtaskList);
        subtaskList.setAdapter(subtaskListAdapter);

        subtaskEditText = (EditText) findViewById(R.id.subtaskEditText);
        subtaskEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Add the task and disable the keyboard if task was empty
                    return !addTask();
                }
                return false;
            }
        });

    }

    // Called when keyboard finish button is pressed
    public boolean addTask() {

        String todoTaskInput = subtaskEditText.getText().toString().trim();

        // Check if there is data in the field. If yes, add to db. Else, toast error
        if (!todoTaskInput.matches("")) {

            SQLiteDatabase db = todoListSQLHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();

            // Get time the task is created
            Calendar now = Calendar.getInstance();

            // Put values that will be written to db
            values.put(TodoListSQLHelper.TASK_NAME, todoTaskInput);
            values.put(TodoListSQLHelper.TASK_TAB_ID, 0);
            values.put(TodoListSQLHelper.PARENT_TASK_ID, parentDatabaseId);
            values.put(TodoListSQLHelper.DATE_CREATED, now.getTimeInMillis());

            // Add the values to database
            int databaseId = (int)db.insertWithOnConflict(TodoListSQLHelper.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            Task newTask = new Task(databaseId, todoTaskInput, 0, now.getTimeInMillis());
            newTask.parentTaskId = parentDatabaseId;

            // TODO Incriment parent task count

            // Update UI
            subtaskEditText.setText("");
            subTasks.add(newTask); // Update list with new item

            return true; // Closes the keyboard from setOnEditorActionListener in onCreate
        }
        // Open the keyboard and give the edit text has focus
        else{
            Toast.makeText(SubtaskActivity.this, " Input some text... ", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    // The x was pressed or a task has moved Tab due to time's relentless onslaught
    public void removeItem(Task taskToRemove) {
        // Update local task and database
        Task thisTask = subTasks.get(subTasks.indexOf(taskToRemove));
        thisTask.done = 2;
        updateDb(thisTask);

        subTasks.remove(taskToRemove);
    }
    public void checkClickListner(View view, Task clickedTask){
        TextView subtaskNameTextView = (TextView) view.findViewById(R.id.subtaskNameTextView);
        CheckBox subtaskCheckBox = (CheckBox) view.findViewById(R.id.subtaskCheckBox);
        ImageButton subtaskActionButton = (ImageButton) view.findViewById(R.id.subtaskActionButton);

        Task thisTask = subTasks.get(subTasks.indexOf(clickedTask));
        if (subtaskCheckBox.isChecked()){
            subtaskNameTextView.setPaintFlags(subtaskNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            subtaskNameTextView.setTextColor(Color.GRAY);
            subtaskActionButton.setImageResource(R.drawable.ic_close);

            // Update database from main activity
            thisTask.done = 1;
            thisTask.dateCompleted = Calendar.getInstance().getTimeInMillis();
            Toast.makeText(SubtaskActivity.this, "At a boy!", Toast.LENGTH_SHORT).show();
        }
        else{
            subtaskNameTextView.setPaintFlags(subtaskNameTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            subtaskNameTextView.setTextColor(Color.WHITE);
            if(thisTask.dateCompleteBy > 0)
                subtaskActionButton.setImageResource(R.drawable.ic_calendar_check);
            else
                subtaskActionButton.setImageResource(R.drawable.perm_group_calendar);

            // Update database from main activity to undo changes
            thisTask.done = 0;
            thisTask.dateCompleted = 0;
            Toast.makeText(SubtaskActivity.this, "That's okay...", Toast.LENGTH_SHORT).show();
        }
        // Update the database with new values
        updateDb(thisTask);
    }

    public void updateDb(Task task){

        SQLiteDatabase db = todoListSQLHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();

        newValues.put(TodoListSQLHelper.TASK_NAME, task.taskName);
        newValues.put(TodoListSQLHelper.TASK_TAB_ID, task.taskTabIndex);
        newValues.put(TodoListSQLHelper.PARENT_TASK_ID, task.parentTaskId);
        newValues.put(TodoListSQLHelper.DONE, task.done);
        newValues.put(TodoListSQLHelper.DATE_CREATED, task.dateCreated);
        newValues.put(TodoListSQLHelper.DATE_COMPLETE_BY, task.dateCompleteBy);
        newValues.put(TodoListSQLHelper.DATE_COMPLETED, task.dateCompleted);

        // Update all columns in the row with the values in the the object
        db.update(TodoListSQLHelper.TABLE_NAME, newValues, TodoListSQLHelper._ID+"="+task.databaseId, null);
        Toast.makeText(SubtaskActivity.this, "db update", Toast.LENGTH_SHORT).show();

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
