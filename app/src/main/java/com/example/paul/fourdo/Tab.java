package com.example.paul.fourdo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Paul on 24/04/2015.
 */
public class Tab extends Fragment {

    private static final int TODAY_TAB = 0;
    private static final int SOMEDAY_TAB = 1;

    public int thisTabIndex;
    private StickyAdapter adapter;
    private ArrayList<Task> thisTasks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab, container, false);
        StickyListHeadersListView stickyList = (StickyListHeadersListView) v.findViewById(R.id.list);

        // Add footer to list so that floating action button doesn't cover bottom tasks
        TextView emptyTextView = new TextView(container.getContext());
        emptyTextView.setHeight(240);
        stickyList.addFooterView(emptyTextView, null, false);

        // Get this Tab's index which is bundled in TabsPagerAdapter; 0 today, 1 someday
        Bundle bundle = getArguments();
        thisTabIndex = bundle.getInt("tabIndex");

        // Get ArrayList of tasks from MainActivity
        ArrayList<Task> MainTasks = new ArrayList<>(((MainActivity) getActivity()).getTasks());
        thisTasks = new ArrayList<>();
        // Only keep tasks that are associated with this view
        for (int i = 0; i < MainTasks.size(); i++) {
            if (thisTabIndex == MainTasks.get(i).taskTabIndex) {
                thisTasks.add(MainTasks.get(i));
            }
        }

        // Add tasks to the list view
        adapter = new StickyAdapter(container.getContext(), this, thisTasks);
        stickyList.setAdapter(adapter);

        // Set adapter before sorting because if a task has moved Tab,
        // adapter.notifyDataSetChange is called in updateTabViews which will nullPointerException
        updateTabViews();

        return v;
    }

    // Called from main activity after new task's been added to the db or task is moved
    public void addItem(Task task) {
        thisTasks.add(task);
        updateTabViews();
    }

    // Something changed, eg a time was set by the calendar, task moved
    public void updateTabViews() {
        assignHeaderId();
        Collections.sort(thisTasks, new sortTasks());
        adapter.notifyDataSetChanged();
    }

    // The x was pressed or a task has moved Tab due to time's relentless onslaught
    public void removeItem(Task task) {
        thisTasks.remove(task);
        adapter.notifyDataSetChanged();
    }

    // The 'brain' function. Gives the app it's worth
    // Figures out... everything
    public void assignHeaderId() {
        // Get time now to do all comparisons in loop against this
        Calendar nowCal = Calendar.getInstance();
        long nowSec = nowCal.getTimeInMillis();

        Calendar todayStartCal = Calendar.getInstance();
        todayStartCal.set(Calendar.HOUR_OF_DAY, 0);
        todayStartCal.set(Calendar.MINUTE, 0);
        todayStartCal.set(Calendar.SECOND, 0);
        todayStartCal.set(Calendar.MILLISECOND, 0);

        Calendar tomorrowStartCal = (Calendar) todayStartCal.clone();
        tomorrowStartCal.add(Calendar.DATE, 1);
        double tomorrowHoursUntil = TimeUnit.MILLISECONDS.toHours(tomorrowStartCal.getTimeInMillis() - nowSec);

        Calendar mondayCal = (Calendar) todayStartCal.clone();
        mondayCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        double mondayDaysUntil = TimeUnit.MILLISECONDS.toDays(mondayCal.getTimeInMillis() - nowSec);

        Calendar monthEndCal = (Calendar) todayStartCal.clone();
        monthEndCal.set(Calendar.DAY_OF_MONTH, todayStartCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        double monthEndDaysUntil = TimeUnit.MILLISECONDS.toDays(monthEndCal.getTimeInMillis() - nowSec);

        Calendar nextMonthEnd = (Calendar) monthEndCal.clone();
        nextMonthEnd.add(Calendar.DATE, 1);
        nextMonthEnd.set(Calendar.DAY_OF_MONTH, nextMonthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        double nextMonthEndDaysUntil = TimeUnit.MILLISECONDS.toDays(nextMonthEnd.getTimeInMillis() - nowSec);

        for (int i = 0; i < thisTasks.size(); i++) {
            /* Two things going on here.
                If in the today tab, the comparisons are done using hoursUntil
                If in Someday tab, the comparison is done based on taskDayStart but the times printed are based on hoursUntil
            */

            // Figure out how long till this task's due
            long taskMilliSecUntil = thisTasks.get(i).dateCompleteBy - nowSec;
            double taskHoursUntil = taskMilliSecUntil / 3600000.0; // Number of milliseconds in an hour

//            For printing
            int taskPrintTodayHoursUntil = (int) (((taskHoursUntil/24)%1)*24);
            int taskPrintTodayMinsUntil = (int) ((((taskHoursUntil/24)%1)*24)%1*60);
            int taskPrintWeeksUntil = (int) ((taskMilliSecUntil/86400000.0) / 7);
            int taskPrintDaysUntil = (int) (((taskMilliSecUntil/86400000.0)/7)%1*7);
//            int taskPrintMonthsUntil = (int) ((taskMilliSecUntil/86400000.0) % 30);

            // Create calendar representing task for use in day calculations
            Calendar taskDayStartCal = Calendar.getInstance();
            taskDayStartCal.setTimeInMillis(thisTasks.get(i).dateCompleteBy);
            taskDayStartCal.set(Calendar.HOUR_OF_DAY, 0);
            taskDayStartCal.set(Calendar.MINUTE, 0);
            taskDayStartCal.set(Calendar.SECOND, 0);
            taskDayStartCal.set(Calendar.MILLISECOND, 0);

            long taskMilliSecUntilStarts = taskDayStartCal.getTimeInMillis() - todayStartCal.getTimeInMillis();
            double taskDaysUntil = taskMilliSecUntilStarts / 86400000.0; // Number of milliseconds in a day


            if (thisTabIndex == TODAY_TAB) {
                // If time not set, 'do soon'
                // If time set in the past, umm...
                // If time set, group by hour

                // If completeBy date is not set ( ie task was added on this tab )
                if (thisTasks.get(i).dateCompleteBy == 0) {
                    thisTasks.get(i).headerId = 6; // Anytime today...
                } else {
                    // Set timeUntil text that appears on list item
                    thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dh %dm", taskPrintTodayHoursUntil, taskPrintTodayMinsUntil);
                    if (taskHoursUntil > tomorrowHoursUntil) {
                        // Task has been set to tomorrow, it should be moved to SOMEDAY_TAB
                        ((MainActivity) getActivity()).moveTaskToTab(SOMEDAY_TAB, thisTasks.get(i));
                        removeItem(thisTasks.get(i));
                        break; // Break loop. Tasks get sorted again
                    } else if (taskHoursUntil < 0) {
                        // Are you doing it?
                        thisTasks.get(i).headerId = 0;
                        if (taskHoursUntil > -1) // If task was due an hour ago countdown only in min
                            thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dm", taskPrintTodayMinsUntil);
                    } else if (taskHoursUntil < 1) {
                        // Less than an hour
                        thisTasks.get(i).headerId = 1;
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dm", taskPrintTodayMinsUntil);
                    } else if (taskHoursUntil < 2) {
                        // Less than 2 hours
                        thisTasks.get(i).headerId = 2;
                    } else if (taskHoursUntil < 4) {
                        // Less than 4 hours
                        thisTasks.get(i).headerId = 3;
                    } else if (taskHoursUntil < 6) {
                        // Less than 6 hours
                        thisTasks.get(i).headerId = 4;
                    } else if (taskHoursUntil < 24) {
                        // More than 6 hours
                        thisTasks.get(i).headerId = 5;
                    }
                }
            } else { // Someday Tab

                // If completeBy date is not set, header is someday
                if (thisTasks.get(i).dateCompleteBy == 0) {
                    thisTasks.get(i).headerId = 7; // Someday
                } else {
                    // Days until a task, where task-day is 00:00:00 the day task is due minus 00:00:00 of day
//                    taskDaysUntil = tas

//                    If the number of hours till the task is due is less than the number of hours from now until tomorrow
                    if (taskDaysUntil < 1) {
                        // Need to move to Today tab
                        ((MainActivity) getActivity()).moveTaskToTab(TODAY_TAB, thisTasks.get(i));
                        removeItem(thisTasks.get(i));
                        break; // from loop. Sorting happens again
                    } else if (taskDaysUntil < 2) {
                        // Tomorrow
                        thisTasks.get(i).headerId = 0;
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dh %dm", (int)(taskHoursUntil), taskPrintTodayMinsUntil);
//                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dh %dm", taskPrintTodayHoursUntil, taskPrintTodayMinsUntil);
                    } else if (taskDaysUntil < 3) {
                        // In 2 days
                        thisTasks.get(i).headerId = 1;
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dd %dh", taskPrintDaysUntil, taskPrintTodayHoursUntil);
                    } else if (taskDaysUntil < mondayDaysUntil) {
                        // This week
                        thisTasks.get(i).headerId = 2;
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dd %dh", taskPrintDaysUntil, taskPrintTodayHoursUntil);
                    } else if (taskDaysUntil < 7 + mondayDaysUntil) {
                        // Next week
                        thisTasks.get(i).headerId = 3;
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dd %dh", taskPrintDaysUntil, taskPrintTodayHoursUntil);
                    } else if (taskDaysUntil < monthEndDaysUntil) {
                        // This month
                        thisTasks.get(i).headerId = 4;
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dw %dd %dh", taskPrintWeeksUntil, taskPrintDaysUntil, taskPrintTodayHoursUntil);
                    } else if (taskDaysUntil < nextMonthEndDaysUntil) {
                        // Next month
                        thisTasks.get(i).headerId = 5;
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dw %dd %dh", taskPrintWeeksUntil, taskPrintDaysUntil, taskPrintTodayHoursUntil);
                    } else {
                        // Beyond worrying
                        thisTasks.get(i).headerId = 6;
//                        if (taskDaysUntil < 60)
                        thisTasks.get(i).timeUntil = String.format(Locale.ENGLISH, "%dw %dd %dh", taskPrintWeeksUntil, taskPrintDaysUntil, taskPrintTodayHoursUntil);

//                        else
//                            thisTasks.get(i).timeUntil = Integer.toString((int) (taskDaysUntil / 30)) + "m " +
//                                    Integer.toString((int) (taskDaysUntil / 7)) + "w";
                    }
                }
            }
        }
    }

    // Comparator for Task objects. Sorts tasks within the headers.
    public class sortTasks implements Comparator<Task> {
        @Override
        public int compare(Task t1, Task t2) {

            // If both have dateCompleteBy , set smallest first (more immediate) one first - ASC
            // If one has dateCompleteBy, sort the bigger one first - ASC
            // else sort by dateCreated with most recent added (biggest) DESC

            if (t1.dateCompleteBy > 0 && t2.dateCompleteBy > 0) {
                return Long.compare(t1.dateCompleteBy, t2.dateCompleteBy); // t1 - t2 = ASC
            } else if (t1.dateCompleteBy > 0 || t2.dateCompleteBy > 0) {
                return (t1.dateCompleteBy == 0) ? 1 : -1; // dateCompleteBy not set, move it down = 1.
            } else {
                return Long.compare(t2.dateCreated, t1.dateCreated); // t2 - t1 = DESC
            }
        }
    }

}
