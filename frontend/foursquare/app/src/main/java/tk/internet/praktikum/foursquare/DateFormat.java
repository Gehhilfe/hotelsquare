package tk.internet.praktikum.foursquare;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;

import tk.internet.praktikum.foursquare.storage.LocalStorage;

/**
 * Created by gehhi on 13.08.2017.
 */

public class DateFormat {
    public static String getFriendlyTime(Context context, Date dateTime) {
        StringBuffer sb = new StringBuffer();
        Date current = Calendar.getInstance().getTime();
        long diffInSeconds = (current.getTime() - dateTime.getTime()) / 1000;

    /*long diff[] = new long[]{0, 0, 0, 0};
    /* sec *  diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
    /* min *  diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
    /* hours *  diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
    /* days * diff[0] = (diffInSeconds = (diffInSeconds / 24));
     */
        long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = (diffInSeconds = (diffInSeconds / 12));
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(context);
        String language=sharedPreferences.getString("LANGUAGE","de");
        if(language.equals("de"))
            sb.append("vor ");
        if (years > 0) {
            if (years == 1) {
                sb.append(context.getResources().getString(R.string.a_year));
            } else {
                sb.append(years + " "+context.getResources().getString(R.string.years));
            }
            /*if (years <= 6 && months > 0) {
                if (months == 1) {
                    sb.append(" and a month");
                } else {
                    sb.append(" and " + months + " months");
                }
            }*/
        } else if (months > 0) {
            if (months == 1) {
                sb.append(context.getResources().getString(R.string.a_month));
            } else {
                sb.append(months + " "+context.getResources().getString(R.string.months));
            }
            /*if (months <= 6 && days > 0) {
                if (days == 1) {
                    sb.append(" and a day");
                } else {
                    sb.append(" and " + days + " days");
                }
            }*/
        } else if (days > 0) {
            if (days == 1) {
                sb.append(context.getResources().getString(R.string.a_day));
            } else {
                sb.append(days + " "+context.getResources().getString(R.string.days));
            }
            /*if (days <= 3 && hrs > 0) {
                if (hrs == 1) {
                    sb.append(" and an hour");
                } else {
                    sb.append(" and " + hrs + " hours");
                }
            }*/
        } else if (hrs > 0) {
            if (hrs == 1) {
                sb.append(context.getResources().getString(R.string.an_hour));
            } else {
                sb.append(hrs + " "+context.getResources().getString(R.string.hours));
            }
            /*if (min > 1) {
                sb.append(" and " + min + " minutes");
            }*/
        } else if (min > 0) {
            if (min == 1) {
                sb.append(context.getResources().getString(R.string.a_minute));
            } else {
                sb.append(min + " "+context.getResources().getString(R.string.minutes));
            }
            /*if (sec > 1) {
                sb.append(" and " + sec + " seconds");
            }*/
        } else {
            if (sec <= 1) {
                sb.append(context.getResources().getString(R.string.about)+" "+context.getResources().getString(R.string.a_second));
            } else {
                sb.append(context.getResources().getString(R.string.about)+" " + sec + " "+context.getResources().getString(R.string.seconds));
            }
        }
        if(language.equals("en"))
           sb.append(" ago");


    /*String result = new String(String.format(
    "%d day%s, %d hour%s, %d minute%s, %d second%s ago",
    diff[0],
    diff[0] > 1 ? "s" : "",
    diff[1],
    diff[1] > 1 ? "s" : "",
    diff[2],
    diff[2] > 1 ? "s" : "",
    diff[3],
    diff[3] > 1 ? "s" : ""));*/
        return sb.toString();
    }
}
