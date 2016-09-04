package com.example.paul.fourdo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class TodoListSQLHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "fourdo";
    public static final String TABLE_NAME = "items";
    public static final int VERSION = 1;
    public static final String _ID = "_ID";
    public static final String TASK_NAME = "task_name";
    public static final String TASK_TAB_ID = "task_tab_id"; //Today 0, Someday 1
    public static final String PARENT_TASK_ID = "parent_task_id";
    public static final String NUM_SUB_TASKS = "num_sub_tasks";
    public static final String DONE = "done"; // 0 Not complete, 1 Strike through, 2 Hidden
    public static final String DATE_CREATED = "date_created";
    public static final String DATE_COMPLETE_BY = "date_complete_by";
    public static final String DATE_COMPLETED = "date_completed";

    public TodoListSQLHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String createTodoListTable =
                "CREATE TABLE " + TABLE_NAME +
                        " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TASK_NAME + " TEXT," +
                        TASK_TAB_ID + " INTEGER," + // No Default, always set
                        PARENT_TASK_ID + " INTEGER DEFAULT 0," +
                        DONE + " INTEGER DEFAULT 0," +
                        NUM_SUB_TASKS + " INTEGER DEFAULT 0," +
                        DATE_CREATED + " INTEGER," +
                        DATE_COMPLETE_BY + " INTEGER DEFAULT 0," +
                        DATE_COMPLETED + " INTEGER DEFAULT 0)";
        sqlDB.execSQL(createTodoListTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        sqlDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqlDB);
    }
}