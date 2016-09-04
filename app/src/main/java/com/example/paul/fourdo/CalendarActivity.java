package com.example.paul.fourdo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;
import com.software.shell.fab.ActionButton;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Paul on 26/04/2015.
 */
public class CalendarActivity extends ActionBarActivity implements OnDateChangedListener {

    private MaterialCalendarView matCalendar;
    private TimePicker timePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Time picker
        timePicker = (TimePicker)findViewById(R.id.timePicker);
        setTimePickerInterval(timePicker);

        // Calendar view
        matCalendar = (MaterialCalendarView) findViewById(R.id.calendarView);
        matCalendar.setOnDateChangedListener(this);

        // Set time showing on widget
        final Calendar calendar = Calendar.getInstance();
        long prevSelection = getIntent().getExtras().getLong("dateCompleteBy"); // Get time previously selected, if it was bundled
        if (prevSelection > 0) {
            // Set the material Calendars selection to the date previously selected
            Calendar previousSelection = Calendar.getInstance();
            previousSelection.setTimeInMillis(prevSelection);
            matCalendar.setSelectedDate(previousSelection);

            // And don't forget the time picker...
            timePicker.setCurrentHour(previousSelection.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(previousSelection.get(Calendar.MINUTE)/TIME_PICKER_INTERVAL);
        }
        else {
            // Set Material Calendar's selected date to now. Time picker, round up according to nearest interval
            matCalendar.setSelectedDate(calendar);
            timePicker.setCurrentMinute(((int)calendar.get(Calendar.MINUTE)/TIME_PICKER_INTERVAL)+1);
        }
        // TODO Updating a task with previousTime set beyond the limit?
        // Set Material Calendar's max and min
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1); // Limit to the first of this month
        matCalendar.setMinimumDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+12, 31); // Limit to a year from now
        matCalendar.setMaximumDate(calendar.getTime());

        // For buttons TODAY, TOMORROW, IN A WEEK at the top of the calendar
        // Get times tomorrow and a week from now
        final Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1); // Add one day
        final Calendar nextWeek = Calendar.getInstance();
        nextWeek.add(Calendar.DAY_OF_MONTH, 7); // 7 days from now

        // Set click handlers to setSelectedDate on the Material Calendar
        TextView textToday = (TextView) findViewById(R.id.textToday);
        textToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matCalendar.setSelectedDate(Calendar.getInstance());
            }
        });
        TextView textTomorrow = (TextView) findViewById(R.id.textTomorrow);
        textTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matCalendar.setSelectedDate(tomorrow);
            }
        });
        TextView textNextWeek = (TextView) findViewById(R.id.textNextWeek);
        textNextWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matCalendar.setSelectedDate(nextWeek);
            }
        });

        // Display the floating action button using it's animation
        ActionButton actionButton = (ActionButton) findViewById(R.id.cal_action_button);
        actionButton.show();   // plays the show animation
        // TODO This activity loads too slowly for this to be seen
    }

    @Override
    public void onDateChanged(MaterialCalendarView widget, CalendarDay date) {
        Calendar today = Calendar.getInstance();
        if (date.getCalendar().compareTo(today) < 0){
            Toast.makeText(CalendarActivity.this, "Can't select a past date", Toast.LENGTH_SHORT).show();
            matCalendar.setSelectedDate(today);
        }
    }

    public void saveTime(View view){
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        Calendar matCalDate = matCalendar.getSelectedDate().getCalendar();
        Calendar today = Calendar.getInstance();

        // Update Material Calendar date with user's selected hour and minute
        matCalDate.set(Calendar.HOUR, timePicker.getCurrentHour());
        matCalDate.set(Calendar.MINUTE, timePicker.getCurrentMinute()*TIME_PICKER_INTERVAL);

        // Check, with the hour and minute include, that the time is not in the past
        if (matCalDate.compareTo(today) < 0){
            Toast.makeText(CalendarActivity.this, "Please select a time in the future", Toast.LENGTH_SHORT).show();
            matCalendar.setSelectedDate(today);
        }
        else {
            // Return to main with data
            Intent intent = new Intent();
            intent.putExtra("dateCompleteBy", matCalDate.getTimeInMillis());
            // Database id which is passed in from list item so we can update the correct item in Main Activity
            intent.putExtra("databaseId", getIntent().getExtras().getInt("databaseId"));
            setResult(RESULT_OK, intent);
            finish();
        }
//        timePicker.setOnTimeChangedListener(mTimePickerListener);
    }
    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    // Changes the time picker's step to intervals of 5 mins
    private int TIME_PICKER_INTERVAL = 5;
    NumberPicker minutePicker;
    List<String> displayedValues;

    private void setTimePickerInterval(TimePicker timePicker) {
        try {
            Class<?> classForId = Class.forName("com.android.internal.R$id");

            Field field = classForId.getField("minute");
            minutePicker = (NumberPicker) timePicker.findViewById(field.getInt(null));

            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(59/TIME_PICKER_INTERVAL);
            displayedValues = new ArrayList<String>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format("%02d", i));
            }
            minutePicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
