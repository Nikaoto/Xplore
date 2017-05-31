package com.explorify.xplore.xplore;

/**
 * Created by nikao on 2/21/2017.
 */

public class DateSetup {
    int sYear, sMonth, sDay; //starting
    int eYear, eMonth, eDay; //ending
    boolean choice; // 0 is startDate, 1 is endDate
    boolean confirmedS, confirmedE;
    long start, end; //full ints

    DateSetup() { }

    long getStart() {
        return start;
    }

    void setStart(long start) {
        this.start = start;
    }

    long getEnd() {
        return end;
    }

    void setEnd(long end) {
        this.end = end;
    }

    boolean isChoice() {
        return choice;
    }

    void setChoice(boolean choice) {
        this.choice = choice;
    }

    int getsYear() {
        return sYear;
    }

    void setsYear(int sYear) {
        this.sYear = sYear;
    }

    int getsMonth() {
        return sMonth;
    }

    void setsMonth(int sMonth) {
        this.sMonth = sMonth;
    }

    int getsDay() {
        return sDay;
    }

    void setsDay(int sDay) {
        this.sDay = sDay;
    }

    int geteYear() {
        return eYear;
    }

    void seteYear(int eYear) {
        this.eYear = eYear;
    }

    int geteMonth() {
        return eMonth;
    }

    void seteMonth(int eMonth) {
        this.eMonth = eMonth;
    }

    int geteDay() {
        return eDay;
    }

    void seteDay(int eDay) {
        this.eDay = eDay;
    }

    boolean isConfirmedS() {
        return confirmedS;
    }

    void setConfirmedS(boolean confirmedS) {
        this.confirmedS = confirmedS;
    }

    boolean isConfirmedE() {
        return confirmedE;
    }

    void setConfirmedE(boolean confirmedE) {
        this.confirmedE = confirmedE;
    }
}
