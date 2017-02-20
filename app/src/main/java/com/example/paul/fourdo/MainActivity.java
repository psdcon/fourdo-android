package com.example.paul.fourdo;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.software.shell.fab.ActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity {

    /* TODO Fix this crasher. All i was doing is rotating with the new task open
    09-03 19:13:19.389 19085-19085/com.example.paul.fourdo E/AndroidRuntime: FATAL EXCEPTION: main
         Process: com.example.paul.fourdo, PID: 19085
         java.lang.NullPointerException: Attempt to invoke virtual method 'void com.example.paul.fourdo.Tab.updateTabViews()' on a null object reference
             at com.example.paul.fourdo.MainActivity$3$1.run(MainActivity.java:120)
             at android.os.Handler.handleCallback(Handler.java:739)
             at android.os.Handler.dispatchMessage(Handler.java:95)
             at android.os.Looper.loop(Looper.java:152)
             at android.app.ActivityThread.main(ActivityThread.java:5507)
             at java.lang.reflect.Method.invoke(Native Method)
             at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:726)
             at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:616)
    * */

    // Data vars
    private TodoListSQLHelper todoListSQLHelper;
    private ArrayList<Task> tasks;

    // View vars
    private int currentTabIndex;
    private ActionButton actionButton;
    private EditText textTask;
    private View action_bar_inputs;

    // Adapter vars
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private TabsPagerAdapter mAdapter;
    private static final int TODAY_TAB = 0;
    private static final int SOMEDAY_TAB = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get data so that when fragment list starts, the data is ready and waiting
        getTasks();

        // Set up Action Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set pagerAdapter for tabs
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Add tabs
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer(){
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mTabs.setViewPager(mPager);
        mTabs.setOnPageChangeListener(scollPositionListener);

        // Set up action button
        actionButton = (ActionButton) findViewById(R.id.action_button);
        actionButton.playShowAnimation();   // plays the show animation


        // Handles 'Finished' button on keyboard
        action_bar_inputs = findViewById(R.id.action_bar_inputs);
        textTask = (EditText) findViewById(R.id.textTask);
        textTask.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Add the task
                    if (addTask()){
                        // If task was added successfully, hide the textView and show action button
                        disableAddTask();
                        return false; // Dismiss the keyboard
                    }
                    else{
                        return true; // Keep the keyboard open
                    }
                }
                return false;
            }
        });

        // Refresh the data every 5 seconds so that time left indicator changes
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        // Update both list views
                        mAdapter.getTabInstance(TODAY_TAB).updateTabViews();
                        mAdapter.getTabInstance(SOMEDAY_TAB).updateTabViews();
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 5001, 30000); // Update every 30 seconds after a delay of 5001

    }

    // Populate tasks ArrayList from database
    public ArrayList<Task> getTasks() {
        if(tasks == null) {
            // Read from the DB
            todoListSQLHelper = new TodoListSQLHelper(MainActivity.this);
            SQLiteDatabase db = todoListSQLHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM "+TodoListSQLHelper.TABLE_NAME+
                    " WHERE "+TodoListSQLHelper.DONE+" < 2 AND "+TodoListSQLHelper.PARENT_TASK_ID+" = 0"+ // Don't select tasks that've been finished
                    " ORDER BY "+TodoListSQLHelper.DATE_COMPLETE_BY+" DESC, "+TodoListSQLHelper.DATE_CREATED+" DESC", null);

            // Populate tasks ArrayList
            tasks = new ArrayList<>();
            while (cursor.moveToNext()) {
                Task newTask = new Task(
                        cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper._ID)),
                        cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.TASK_NAME)),
                        cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.TASK_TAB_ID)),
                        cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.PARENT_TASK_ID)),
                        cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.NUM_SUB_TASKS)),
                        cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.DONE)),
                        cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_CREATED)),
                        cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_COMPLETE_BY)),
                        cursor.getLong(cursor.getColumnIndex(TodoListSQLHelper.DATE_COMPLETED))
                );
                tasks.add(newTask); // Add each task to list
            }
            cursor.close();
            return tasks;
        }
        else
            return tasks;
    }

    /** Action functions **/
    // Hide button, slide in text view with focus, open keyboard
    public void enableAddTask(View view){
        // Slide down the edit text and action button
        actionButton.hide(); // hides the button if it is shown and plays the hide animation if set
        action_bar_inputs.animate().setDuration(300).translationY(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                // Give focus
                textTask.requestFocus();
                // Open keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textTask, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
    // Redisplay floating action button, hide textView. Keyboard already hidden
    public void disableAddTask(){
        // Slide down the edit text
        action_bar_inputs.animate().setDuration(300).translationY(-140).withEndAction(new Runnable() {
            @Override
            public void run() {
                // Slide the floating action button into view
                actionButton.show();
            }
        });
    }
    // Called when keyboard finish button is pressed
    public boolean addTask() {

        String todoTaskInput = textTask.getText().toString().trim();
        Tab currentTab = mAdapter.getTabInstance(currentTabIndex);

        // Check if there is data in the field. If yes, add to db. Else, toast error
        if (!todoTaskInput.matches("")) {

            SQLiteDatabase db = todoListSQLHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();

            // Get time the task is created
            Calendar now = Calendar.getInstance();

            // Put values that will be written to db
            values.put(TodoListSQLHelper.TASK_NAME, todoTaskInput);
            values.put(TodoListSQLHelper.TASK_TAB_ID, currentTabIndex);
            values.put(TodoListSQLHelper.DATE_CREATED, now.getTimeInMillis());

            // Add the values to database
            int databaseId = (int)db.insertWithOnConflict(TodoListSQLHelper.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            Task newTask = new Task(databaseId, todoTaskInput, currentTabIndex, now.getTimeInMillis());
            tasks.add(newTask); // Save to global tasks. Use to recreate state

            // Update UI
            textTask.setText("");
            currentTab.addItem(newTask);

            return true; // Closes the keyboard from setOnEditorActionListener in onCreate
        }
        // Open the keyboard and give the edit text has focus
        else{
            Toast.makeText(MainActivity.this, " Input some text... ", Toast.LENGTH_SHORT).show();
            return false;
        }
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

//        String[] args = new String[]{"user1", "user2"};
//        db.update("YOUR_TABLE", newValues, "name=? OR name=?", args);

    }

    // Set a task as finished. Assigned in .StickyAdapter
    public void checkClickListner(View view, Task clickedTask){
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.taskCheckBox);
        TextView taskNameTextView = (TextView) view.findViewById(R.id.taskNameTextView);
        ImageButton openCalendar = (ImageButton) view.findViewById(R.id.taskActionButton);

        Task thisTask = tasks.get(tasks.indexOf(clickedTask));
        if (checkBox.isChecked()){
            taskNameTextView.setPaintFlags(taskNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskNameTextView.setTextColor(Color.GRAY);
            openCalendar.setImageResource(R.drawable.ic_close);

            // Update database from main activity
            thisTask.done = 1;
            thisTask.dateCompleted = Calendar.getInstance().getTimeInMillis();
            Toast.makeText(MainActivity.this, "You GO Kid!", Toast.LENGTH_SHORT).show();
        }
        else{
            taskNameTextView.setPaintFlags(taskNameTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            taskNameTextView.setTextColor(Color.WHITE);
            if(thisTask.dateCompleteBy > 0)
                openCalendar.setImageResource(R.drawable.ic_calendar_check);
            else
                openCalendar.setImageResource(R.drawable.perm_group_calendar);

            // Update database from main activity to undo changes
            thisTask.done = 0;
            thisTask.dateCompleted = 0;
            Toast.makeText(MainActivity.this, "No worries man. DFTBA!", Toast.LENGTH_SHORT).show();
        }
        // Update the database with new values
        updateDb(thisTask);
    }

    // Called when x is pressed. Assigned in .StickyAdapter
    public void removeTask(Task taskToRemove){
        // Update local task and database
        Task thisTask = tasks.get(tasks.indexOf(taskToRemove));
        thisTask.done = 2;
        updateDb(thisTask);

        // Tell the  Tab instance to remove the task
        Tab currentTab = mAdapter.getTabInstance(currentTabIndex);
        currentTab.removeItem(taskToRemove);
    }

    // Called from assignHeaderId in .Tab when a task should be moved between tabs
    public void moveTaskToTab(int tabIndex, Task taskToMove){
        Tab destinationTab = mAdapter.getTabInstance(tabIndex);
        destinationTab.addItem(taskToMove);

        // Update database
        int taskIndex = tasks.indexOf(taskToMove);
        tasks.get(taskIndex).taskTabIndex = tabIndex;
        updateDb(tasks.get(taskIndex));
    }

    // Used for when .CalendarActibity finishes
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Get vars passed back from activity
            int databaseId = data.getIntExtra("databaseId", 0);
            long dateCompleteBy = data.getLongExtra("dateCompleteBy", 0);

            // Update task array and database
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).databaseId == databaseId){
                    tasks.get(i).dateCompleteBy = dateCompleteBy;
                    updateDb(tasks.get(i));
                    break;
                }
            }

            // Update list view
            Tab currentTab = mAdapter.getTabInstance(currentTabIndex);
            currentTab.updateTabViews();
        }
    }

    // Keep track of which Tab is in view. Necessary to know which tab to add a new task to
    public ViewPager.OnPageChangeListener scollPositionListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { currentTabIndex = position; }

        @Override
        public void onPageSelected(int position) { currentTabIndex = position; }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_view_tasks) {
            MainActivity.this.startActivity(new Intent(MainActivity.this, ViewTasksActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
