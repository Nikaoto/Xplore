package com.explorify.xplore.xplore_demo;

import java.util.Date;

/**
 * Created by nikao on 2/21/2017.
 */

public class DateSetup {
    int sYear, sMonth, sDay; //starting
    int eYear, eMonth, eDay; //ending
    boolean choice; // 0 is startDate, 1 is endDate
    boolean confirmedS, confirmedE;
    long start, end; //full ints

    public DateSetup() {
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
    }

    public int getsYear() {
        return sYear;
    }

    public void setsYear(int sYear) {
        this.sYear = sYear;
    }

    public int getsMonth() {
        return sMonth;
    }

    public void setsMonth(int sMonth) {
        this.sMonth = sMonth;
    }

    public int getsDay() {
        return sDay;
    }

    public void setsDay(int sDay) {
        this.sDay = sDay;
    }

    public int geteYear() {
        return eYear;
    }

    public void seteYear(int eYear) {
        this.eYear = eYear;
    }

    public int geteMonth() {
        return eMonth;
    }

    public void seteMonth(int eMonth) {
        this.eMonth = eMonth;
    }

    public int geteDay() {
        return eDay;
    }

    public void seteDay(int eDay) {
        this.eDay = eDay;
    }

    public boolean isConfirmedS() {
        return confirmedS;
    }

    public void setConfirmedS(boolean confirmedS) {
        this.confirmedS = confirmedS;
    }

    public boolean isConfirmedE() {
        return confirmedE;
    }

    public void setConfirmedE(boolean confirmedE) {
        this.confirmedE = confirmedE;
    }
}
