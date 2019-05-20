package edu.nyu.cs9233.callnotes.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.nyu.cs9233.callnotes.authentication.PreAuthActivity;

/**
 * Created by ujjwalchadha8 on 1/22/2018.
 */

public class Utils {
    public static ArrayList<String> stringList(String... strings){
        return new ArrayList<>(Arrays.asList(strings));
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Storage","Permission is granted");
                return true;
            } else {
                Log.v("Storage","Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Storage","Permission is granted");
            return true;
        }
    }

    public static boolean isPermissionGranted(String permission, Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Permission",permission+": Permission is granted");
                return true;
            } else {
                Log.v("Permission",permission+": Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Permission","Permission is granted");
            return true;
        }
    }

    public static int getAge(Date dateOfBirth) {

        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        int age = 0;

        birthDate.setTime(dateOfBirth);
        if (birthDate.after(today)) {
            throw new IllegalArgumentException("Can't be born in the future");
        }

        age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If birth date is greater than todays date (after 2 days adjustment of leap year) then decrement age one year
        if ( (birthDate.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) > 3) ||
                (birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH ))){
            age--;
        // If birth date and todays date are of same month and birth day of month is greater than todays day of month then decrement age
        } else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH )) &&
                (birthDate.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH ))){
            age--;
        }

        return age;
    }

    public static String getAgoTimestamp(Date d){
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(d.getTime());

        Calendar now = Calendar.getInstance();

        long secondsDifference = (now.getTimeInMillis() - date.getTimeInMillis())/1000;
        long minutesDifference = secondsDifference/60;
        long hoursDifference = minutesDifference/60;
        long daysDifference = hoursDifference/24;

        if(minutesDifference == 0) return secondsDifference + "s";
        if(hoursDifference == 0) return minutesDifference + "m";
        if(daysDifference == 0) return hoursDifference + "h";
        if(daysDifference > 0 && daysDifference < 8) return daysDifference + "d";
        return new SimpleDateFormat("dd-MMM-yy", Locale.getDefault()).format(date.getTime());
    }

    public static <T> HashMap<String, T> listToMap(List<T> list){
        HashMap<String, T> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String key = String.valueOf(i);
            T value = list.get(i);
            map.put(key, value);
        }
        return map;
    }

    public static Double asDouble(Object obj){
        return obj instanceof Double ? (Double) obj
                        : obj instanceof Integer ? (int) obj
                        : obj instanceof Float ? (float) obj
                        : null;
    }

    public static boolean isValidEmail(String email) {
        return email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:" +
                "[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?" +
                "[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f" +
                "\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    }

    public static void showErrorAlert(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
