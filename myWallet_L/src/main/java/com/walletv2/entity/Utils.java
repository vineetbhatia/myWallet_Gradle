package com.walletv2.entity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static String removePayeeFromPayeeList(String payeeList, String payee) {
        if (payeeList.equals(payee)) {
            payeeList = "";
        } else if (payeeList.contains(", " + payee + ", ")) {
            payeeList = payeeList.replace(", " + payee + ", ", ", ");
        } else if (payeeList.startsWith(payee + ", ")) {
            payeeList = payeeList.replaceFirst(payee + ", ", "");
        } else if (payeeList.endsWith(", " + payee)) {
            payeeList = payeeList.substring(0, payeeList.lastIndexOf(','));
        }
        return payeeList;
    }

    public static String replacePayeeFromPayeeList(String payeeList, String newPayee, String oldPayee) {
        if (payeeList.equals(oldPayee)) {
            payeeList = newPayee;
        } else if (payeeList.contains(", " + oldPayee + ", ")) {
            payeeList = payeeList.replace(", " + oldPayee + ", ", ", " + newPayee + ", ");
        } else if (payeeList.startsWith(oldPayee + ", ")) {
            payeeList = payeeList.replaceFirst(oldPayee + ", ", newPayee + ", ");
        } else if (payeeList.endsWith(", " + oldPayee)) {
            payeeList = payeeList.substring(0, payeeList.lastIndexOf(',')) +
                        payeeList.substring(payeeList.lastIndexOf(',')).replaceFirst(", " + oldPayee, ", " + newPayee);
        }

        return payeeList;
    }

    public static int getPayeeCountFromPayees(String payees) {
        String[] strings = payees.split(", ");
        return strings.length;
    }

    public static boolean doesPayeeExistInPayeeList(String payee, String payeeList) {
        if (payeeList.contains(", " + payee + ", ")) {
            return true;
        } else if (payeeList.startsWith(payee + ", ")) {
            return true;
        } else if (payeeList.endsWith(", " + payee)) {
            return true;
        }
        return payeeList.equals(payee);
    }

    public static String getCurrentTimeFormatted() {
        return DateFormat.format(Constants.DATE_FORMAT, System.currentTimeMillis()).toString();
    }

    public static String getMilliesFromDate(String dateString) {
        Date date = null;
        try {
            date = new Date(Long.parseLong(dateString));
        } catch (NumberFormatException e) {
            date = new Date(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf((date != null ? date.getTime() : 0));
    }

    public static String getFormattedDateFromMillies(String millies) {
        return DateFormat.format(Constants.DATE_FORMAT_UI, Long.parseLong(millies.trim())).toString();
    }

    @SuppressLint("SimpleDateFormat")
    public static boolean isFormattedDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        Date testDate;
        try {
            testDate = sdf.parse(date);
        } catch (ParseException e) {
            Log.i("MyWallet", "The date you provided is not formatted");
            return false;
        }
        if (!sdf.format(testDate).equals(date)) {
            Log.i("MyWallet", "The date you provided is not formatted");
            return false;
        }
        Log.i("MyWallet", "The date you provided is formatted");
        return true;
    }

    public static void remindAPayeeAboutAllExpensesViaSms(Activity context, String amount) {
        String smsBody;
        if (Integer.parseInt(amount) > 0)
            smsBody =
                    "Hey, remember I paid *amount* bucks*date**description*. Is there any chance I can have it, I kinda need at the moment.";
        else
            smsBody = "Hey, remember you paid *amount* bucks*date**description*. I will return them soon.";
        smsBody = smsBody.replace("*amount*", amount.replace("-", ""));
        smsBody = smsBody.replace("*date*", "");
        smsBody = smsBody.replace("*description*", " in total");
        smsBody += "\n-shared from MyWallet";
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, smsBody);
        shareIntent.setType("text/plain");
        context.startActivity(shareIntent);
    }

    public static void remindAPayeeAboutOneExpenseViaSms(Activity context, ExpenseDetails expenseDetails) {
        int payeesCount = Utils.getPayeeCountFromPayees(expenseDetails.getPayeesList());
        String smsBody;
        if (Integer.parseInt(expenseDetails.getAmount()) > 0)
            smsBody =
                    "Hey, remember I paid *amount* bucks*date**description*. Is there any chance I can have it, I kinda need at the moment.";
        else
            smsBody = "Hey, remember you paid *amount* bucks*date**description*. I will return them soon.";
        if (payeesCount > 1) {
            smsBody = smsBody.replace("*amount*", Math.abs(Integer.parseInt(expenseDetails.getAmount()) / payeesCount)
                                                  + "");
        } else {
            smsBody = smsBody.replace("*amount*", expenseDetails.getAmount().replace("-", ""));
        }
        smsBody = smsBody.replace("*date*", " on " + Utils.getFormattedDateFromMillies(expenseDetails.getDate()));
        if (expenseDetails.getDescription().equals("")) {
            smsBody = smsBody.replace("*description*", "");
        } else {
            smsBody = smsBody.replace("*description*", " for " + expenseDetails.getDescription());
        }
        smsBody += "\n-shared from MyWallet";
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, smsBody);
        shareIntent.setType("text/plain");
        context.startActivity(shareIntent);
    }

    public static void restartApplication(Activity activity) {
        Intent i = activity.getBaseContext().getPackageManager()
                           .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
    }

    public static int getActionBarHeight(Activity pActivity) {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (pActivity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, pActivity.getResources().getDisplayMetrics());
        }
        return 0;
    }
}
