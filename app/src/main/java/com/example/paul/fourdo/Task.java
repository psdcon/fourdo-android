package com.example.paul.fourdo;

/**
 * Created by Paul on 25/04/2015.
 */
public class Task{

    public int databaseId;
    public String taskName;
    public int taskTabIndex;
    public int parentTaskId;
    public int numSubTasks;
    public int done; // 0 or 1
    public long dateCreated;
    public long dateCompleteBy;
    public long dateCompleted;
    public int headerId; // in list weather title is someday, tomorrow, in 2 days etc. Saves computation
    public String timeUntil;

    // When adding new tasks.
    public Task(int databaseId, String taskName, int taskTabIndex, long dateCreated){
        this.databaseId = databaseId;
        this.taskName = taskName;
        this.taskTabIndex = taskTabIndex;
        this.parentTaskId = 0;
        this.numSubTasks = 0;
        this.done = 0;
        this.dateCreated = dateCreated;
        this.dateCompleteBy = 0;
        this.dateCompleted = 0;
    }

    // When tasks are retrieved from the database
    public Task(int databaseId, String taskName, int taskTabIndex, int parentTaskId, int numSubTasks, int done, long dateCreated, long dateCompleteBy, long dateCompleted){
        this.databaseId = databaseId;
        this.taskName = taskName;
        this.taskTabIndex = taskTabIndex;
        this.parentTaskId = parentTaskId;
        this.numSubTasks = numSubTasks;
        this.done = done;
        this.dateCreated = dateCreated;
        this.dateCompleteBy = dateCompleteBy;
        this.dateCompleted = dateCompleted;
    }

}