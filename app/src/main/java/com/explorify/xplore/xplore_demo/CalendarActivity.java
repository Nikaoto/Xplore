package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import java.util.Calendar;

import static com.explorify.xplore.xplore_demo.CreateGroupFragment.dateSetup;

/**
 * Created by nikao on 2/21/2017.
 */

public class CalendarActivity extends Activity {

    private CalendarView calendarView;
    private Calendar calendar;
    private Button confirmButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.calendar_layout);

        SetUpCalendar();
        SetUpConfirmButton();
    }

    private void SetUpCalendar()
    {
        calendarView = (CalendarView) findViewById(R.id.calendar);

        calendar = Calendar.getInstance();

        if(dateSetup.isChoice()) {
            dateSetup.setStart(General.GetCurrentDate());
            dateSetup.setsYear(calendar.get(Calendar.YEAR));
            dateSetup.setsMonth(calendar.get(Calendar.MONTH)+1);
            dateSetup.setsDay(calendar.get(Calendar.DAY_OF_MONTH));
        }
        else{
            dateSetup.setEnd(General.GetCurrentDate());
            dateSetup.seteYear(calendar.get(Calendar.YEAR));
            dateSetup.seteMonth(calendar.get(Calendar.MONTH)+1);
            dateSetup.seteDay(calendar.get(Calendar.DAY_OF_MONTH));
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int day) {
                if(dateSetup.isChoice()) {
                    dateSetup.setStart(General.GetDateLong(year, month+1, day));
                    dateSetup.setsYear(year);
                    dateSetup.setsMonth(month+1);
                    dateSetup.setsDay(day);
                }
                else{
                    dateSetup.setEnd(General.GetDateLong(year, month+1, day));
                    dateSetup.seteYear(year);
                    dateSetup.seteMonth(month+1);
                    dateSetup.seteDay(day);
                }
            }
        });
    }

    private void SetUpConfirmButton()
    {
        confirmButton = (Button) findViewById(R.id.confirmDate);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dateSetup.isChoice())
                    dateSetup.confirmedS = true;
                else
                    dateSetup.confirmedE = true;
                finish();
            }
        });
    }
}
