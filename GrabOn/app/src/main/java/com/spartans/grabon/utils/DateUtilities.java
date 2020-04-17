package com.spartans.grabon.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author : Sudha Amarnath on 2020-04-17
 */
public class DateUtilities {

    public String getCurrentTimeInMillis() {

        String currentTime = String.valueOf(System.currentTimeMillis());

        return  currentTime;

    }

    public String getPostTimeInMillis(String currentTime, int days) {

        long postTime = Long.parseLong(currentTime) + days * 86400000L;

        return String.valueOf(postTime);

    }

    public String getDateAndTime (String time) {

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm z");
        String dateString = formatter.format(new Date(Long.parseLong(time)));

        return dateString;
    }

    public boolean orderCanBeCancelled (String buytime) {

        if ((Long.parseLong(getCurrentTimeInMillis()) - Long.parseLong(buytime)) > 30 * 86400000L) {
            return false;
        } else {
            return true;
        }

    }

    public boolean orderExpired (String buytime) {

        if ((Long.parseLong(getCurrentTimeInMillis()) - Long.parseLong(buytime)) > 7 * 86400000L) {
            return true;
        } else {
            return false;
        }

    }

    public boolean returnExpired (String canceltime) {

        if ((Long.parseLong(getCurrentTimeInMillis()) - Long.parseLong(canceltime)) > 15 * 86400000L) {
            return true;
        } else {
            return false;
        }

    }

}
