package com.walletv2.dbhandler;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

import com.walletv2.entity.Constants;
import com.walletv2.entity.ExpenseDetails;
import com.walletv2.entity.PayeeDetails;
import com.walletv2.entity.Quicksort;
import com.walletv2.entity.RandomColor;
import com.walletv2.entity.Utils;

import java.util.ArrayList;

public class ExpenseDatabaseHandler {
    private final Context context;
    private SQLiteDatabase database = null;

    public ExpenseDatabaseHandler(Context context) {
        this.context = context;
        database = context.openOrCreateDatabase(Constants.DB_NAME, Context.MODE_PRIVATE, null);
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void createAllTables() {
        createPayeeTable();
        createExpenseTable();
        createEventTable();
        createEventExpenseTable();
    }

    public void createExpenseTable() {
        String createUnPaidExpenseTableQuery = "CREATE TABLE IF NOT EXISTS " + Constants.DB_UNPAID_EXPENSE_TABLE_NAME + " ( " + Constants.DB_EXPENSE_PAYEE_NAME
                + " TEXT, " + Constants.DB_EXPENSE_AMOUNT_NAME + " INTEGER, " + Constants.DB_EXPENSE_DATE_NAME + " TEXT, "
                + Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME + " INTEGER, " + Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME + " TEXT, "
                + Constants.DB_EXPENSE_DESCRIPTION_NAME + " TEXT )";
        String createPaidExpenseTableQuery = "CREATE TABLE IF NOT EXISTS " + Constants.DB_PAID_EXPENSE_TABLE_NAME + " ( " + Constants.DB_EXPENSE_PAYEE_NAME
                + " TEXT, " + Constants.DB_EXPENSE_AMOUNT_NAME + " INTEGER, " + Constants.DB_EXPENSE_DATE_NAME + " TEXT, "
                + Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME + " INTEGER, " + Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME + " TEXT, "
                + Constants.DB_EXPENSE_DESCRIPTION_NAME + " TEXT )";
        try {
            database.execSQL(createUnPaidExpenseTableQuery);
            database.execSQL(createPaidExpenseTableQuery);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void createEventExpenseTable() {
        String createEventTableQuery = "CREATE TABLE IF NOT EXISTS " + Constants.DB_EVENT_EXPENSE_TABLE_NAME + " ( " + Constants.DB_EVENT_EXPENSE_PAYEE_NAME
                + " TEXT, " + Constants.DB_EVENT_EXPENSE_AMOUNT_NAME + " INTEGER," + Constants.DB_EVENT_EXPENSE_DATE_NAME + " TEXT,"
                + Constants.DB_EVENT_EXPENSE_EVENT_NAME + " TEXT," + Constants.DB_EVENT_EXPENSE_PAYER_NAME + " TEXT,"
                + Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME + " TEXT )";
        try {
            database.execSQL(createEventTableQuery);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void createPayeeTable() {
        String payeeCreateTableQuery = "CREATE TABLE IF NOT EXISTS " + Constants.DB_PAYEE_TABLE + " ( " + Constants.DB_PAYEE_PAYEE_NAME + " TEXT PRIMARY KEY)";
        try {
            database.execSQL(payeeCreateTableQuery);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void createEventTable() {
        String eventCreateTableQuery = "CREATE TABLE IF NOT EXISTS " + Constants.DB_EVENT_TABLE + " ( " + Constants.DB_EVENT_EVENT_NAME + " TEXT PRIMARY KEY)";
        try {
            database.execSQL(eventCreateTableQuery);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public boolean alterExpenseTable() {
        String alterUnPaidExpenseTableQueryAmount = "ALTER TABLE " + Constants.DB_UNPAID_EXPENSE_TABLE_NAME + " ADD COLUMN "
                + Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME + " INTEGER DEFAULT 0";
        String alterUnPaidExpenseTableQueryPayee = "ALTER TABLE " + Constants.DB_UNPAID_EXPENSE_TABLE_NAME + " ADD COLUMN "
                + Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME + " TEXT";
        String alterPaidExpenseTableQueryAmount = "ALTER TABLE " + Constants.DB_PAID_EXPENSE_TABLE_NAME + " ADD COLUMN "
                + Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME + " INTEGER DEFAULT 0";
        String alterPaidExpenseTableQueryPayee = "ALTER TABLE " + Constants.DB_PAID_EXPENSE_TABLE_NAME + " ADD COLUMN "
                + Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME + " TEXT";

        try {
            database.execSQL(alterUnPaidExpenseTableQueryAmount);
            database.execSQL(alterUnPaidExpenseTableQueryPayee);
            database.execSQL(alterPaidExpenseTableQueryAmount);
            database.execSQL(alterPaidExpenseTableQueryPayee);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateExpenseTableForUpgradeToVersionOne() {
        String upgradeUnPaidExpenseTableQueryAmount = "UPDATE " + Constants.DB_UNPAID_EXPENSE_TABLE_NAME + " SET "
                + Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME + " = " + Constants.DB_EXPENSE_AMOUNT_NAME;
        String upgradeUnPaidExpenseTableQueryPayee = "UPDATE " + Constants.DB_UNPAID_EXPENSE_TABLE_NAME + " SET "
                + Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME + " = " + Constants.DB_EXPENSE_PAYEE_NAME;
        String upgradePaidExpenseTableQueryAmount = "UPDATE " + Constants.DB_PAID_EXPENSE_TABLE_NAME + " SET "
                + Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME + " = " + Constants.DB_EXPENSE_AMOUNT_NAME;
        String upgradePaidExpenseTableQueryPayee = "UPDATE " + Constants.DB_PAID_EXPENSE_TABLE_NAME + " SET "
                + Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME + " = " + Constants.DB_EXPENSE_PAYEE_NAME;

        try {
            database.execSQL(upgradeUnPaidExpenseTableQueryAmount);
            database.execSQL(upgradeUnPaidExpenseTableQueryPayee);
            database.execSQL(upgradePaidExpenseTableQueryAmount);
            database.execSQL(upgradePaidExpenseTableQueryPayee);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertPayee(String payee) {
        try {
            if (isPayeeExists(payee)) {
                Toast.makeText(context, "The new payee already exists.", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(Constants.DB_PAYEE_PAYEE_NAME, payee);
                database.insert(Constants.DB_PAYEE_TABLE, null, values);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void insertEvent(String event) {
        try {
            if (isEventExists(event)) {
                Toast.makeText(context, "The new event already exists.", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(Constants.DB_EVENT_EVENT_NAME, event);
                database.insert(Constants.DB_EVENT_TABLE, null, values);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void insertExpenseData(ExpenseDetails expenseDetails) {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.DB_EXPENSE_PAYEE_NAME, expenseDetails.getPayeesList());
            values.put(Constants.DB_EXPENSE_AMOUNT_NAME, expenseDetails.getAmount());
            values.put(Constants.DB_EXPENSE_DATE_NAME, expenseDetails.getDate());
            values.put(Constants.DB_EXPENSE_DESCRIPTION_NAME, expenseDetails.getDescription());
            values.put(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, expenseDetails.getPayersList());
            values.put(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME, expenseDetails.getEventName());
            database.insert(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void insertPaidExpense(ExpenseDetails expenseDetails) {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.DB_EXPENSE_PAYEE_NAME, expenseDetails.getPayeesList());
            values.put(Constants.DB_EXPENSE_AMOUNT_NAME, expenseDetails.getAmount());
            values.put(Constants.DB_EXPENSE_DATE_NAME, expenseDetails.getDate());
            values.put(Constants.DB_EXPENSE_DESCRIPTION_NAME, expenseDetails.getDescription());
            values.put(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, expenseDetails.getPayersList());
            values.put(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME, expenseDetails.getEventName());
            database.insert(Constants.DB_PAID_EXPENSE_TABLE_NAME, null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void insertEventExpenseData(ExpenseDetails eventExpenseDetails) {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.DB_EVENT_EXPENSE_PAYEE_NAME, eventExpenseDetails.getPayeesList());
            values.put(Constants.DB_EVENT_EXPENSE_AMOUNT_NAME, eventExpenseDetails.getAmount());
            values.put(Constants.DB_EVENT_EXPENSE_DATE_NAME, eventExpenseDetails.getDate());
            values.put(Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME, eventExpenseDetails.getDescription());
            values.put(Constants.DB_EVENT_EXPENSE_PAYER_NAME, eventExpenseDetails.getPayersList());
            values.put(Constants.DB_EVENT_EXPENSE_EVENT_NAME, eventExpenseDetails.getEventName());
            database.insert(Constants.DB_EVENT_EXPENSE_TABLE_NAME, null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public boolean isPayeeExists(String payee) {
        Cursor cursor = database.query(Constants.DB_PAYEE_TABLE, new String[]{Constants.DB_PAYEE_PAYEE_NAME}, Constants.DB_PAYEE_PAYEE_NAME + " = " + "'"
                + payee.replaceAll("'", "\''") + "'", null, null, null, null, null);
        boolean flag = false;
        if (cursor.getCount() > 0) {
            flag = true;
        }
        cursor.close();
        return flag;
    }

    public boolean isEventExists(String event) {
        Cursor cursor = database.query(Constants.DB_EVENT_TABLE, new String[]{Constants.DB_EVENT_EVENT_NAME}, Constants.DB_EVENT_EVENT_NAME + " = " + "'"
                + event.replaceAll("'", "\''") + "'", null, null, null, null, null);
        boolean flag = false;
        if (cursor.getCount() > 0) {
            flag = true;
        }
        cursor.close();
        return flag;
    }

    public void updatePayee(String newPayee, String oldPayee) {
        if (!newPayee.equalsIgnoreCase(oldPayee)) {
            if (isPayeeExists(newPayee)) {
                Toast.makeText(context, "The new payee already exists.", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(Constants.DB_PAYEE_PAYEE_NAME, newPayee);
                database.update(Constants.DB_PAYEE_TABLE, values, Constants.DB_PAYEE_PAYEE_NAME + " = " + "'" + oldPayee.replaceAll("'", "\''") + "'", null);
                ArrayList<ExpenseDetails> expenseList = getUnPaidExpenseListForAPayee(oldPayee);
                for (ExpenseDetails expenseDetails : expenseList) {
                    String payeeList = expenseDetails.getPayeesList();
                    String dateString = expenseDetails.getDate();
                    if (Utils.getPayeeCountFromPayees(payeeList) > 1) {
                        payeeList = Utils.replacePayeeFromPayeeList(payeeList, newPayee, oldPayee);
                    } else {
                        payeeList = payeeList.replace(oldPayee, newPayee);
                    }
                    values = new ContentValues();
                    values.put(Constants.DB_EXPENSE_PAYEE_NAME, payeeList);
                    String originalPayeeList = expenseDetails.getPayeesList();
                    if (Utils.getPayeeCountFromPayees(payeeList) > 1) {
                        originalPayeeList = Utils.replacePayeeFromPayeeList(originalPayeeList, newPayee, oldPayee);
                    } else {
                        originalPayeeList = originalPayeeList.replace(oldPayee, newPayee);
                    }
                    values = new ContentValues();
                    values.put(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, originalPayeeList);
                    database.update(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, values, Constants.DB_EXPENSE_DATE_NAME + " = " + "'" + dateString + "'", null);
                }
                expenseList = getPaidExpenseListForAPayee(oldPayee);
                for (ExpenseDetails expenseDetails : expenseList) {
                    String payeeList = expenseDetails.getPayeesList();
                    String dateString = expenseDetails.getDate();
                    if (Utils.getPayeeCountFromPayees(payeeList) > 1) {
                        payeeList = Utils.replacePayeeFromPayeeList(payeeList, newPayee, oldPayee);
                    } else {
                        payeeList = payeeList.replace(oldPayee, newPayee);
                    }
                    values = new ContentValues();
                    values.put(Constants.DB_EXPENSE_PAYEE_NAME, payeeList);
                    database.update(Constants.DB_PAID_EXPENSE_TABLE_NAME, values, Constants.DB_EXPENSE_DATE_NAME + " = " + "'" + dateString + "'", null);
                }
                ArrayList<ExpenseDetails> eventExpenseList = getAllEventExpenseList();
                for (ExpenseDetails eventExpense : eventExpenseList) {
                    String payer = eventExpense.getPayersList();
                    String payeeList = eventExpense.getPayeesList();
                    String dateString = eventExpense.getDate();
                    if (Utils.getPayeeCountFromPayees(payeeList) > 1) {
                        payeeList = Utils.replacePayeeFromPayeeList(payeeList, newPayee, oldPayee);
                    } else {
                        payeeList = payeeList.replace(oldPayee, newPayee);
                    }
                    if (payer.equals(oldPayee)) {
                        payer = newPayee;
                    }
                    values = new ContentValues();
                    values.put(Constants.DB_EVENT_EXPENSE_PAYER_NAME, payer);
                    values.put(Constants.DB_EVENT_EXPENSE_PAYEE_NAME, payeeList);
                    database.update(Constants.DB_EVENT_EXPENSE_TABLE_NAME, values, Constants.DB_EVENT_EXPENSE_DATE_NAME + " = " + "'" + dateString + "'", null);
                }
            }
        }
    }

    public void updateEvent(String newEvent, String oldEvent) {
        if (isEventExists(newEvent)) {
            Toast.makeText(context, "The new event already exists.", Toast.LENGTH_SHORT).show();
        } else {
            ContentValues values = new ContentValues();
            values.put(Constants.DB_EVENT_EVENT_NAME, newEvent);
            try {
                database.update(Constants.DB_EVENT_TABLE, values, Constants.DB_EVENT_EVENT_NAME + " = " + "'" + oldEvent.replaceAll("'", "\''") + "'", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            values = new ContentValues();
            values.put(Constants.DB_EVENT_EXPENSE_EVENT_NAME, newEvent);
            try {
                database.update(Constants.DB_EVENT_EXPENSE_TABLE_NAME, values,
                        Constants.DB_EVENT_EXPENSE_EVENT_NAME + " = " + "'" + oldEvent.replaceAll("'", "\''") + "'", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateUnPaidExpenseFromDate(ExpenseDetails expenseDetails, String customDate) {
        ContentValues values = new ContentValues();
        values.put(Constants.DB_EXPENSE_PAYEE_NAME, expenseDetails.getPayeesList());
        values.put(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, expenseDetails.getPayersList());
        values.put(Constants.DB_EXPENSE_AMOUNT_NAME, expenseDetails.getAmount());
        values.put(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME, expenseDetails.getEventName());
        values.put(Constants.DB_EXPENSE_DESCRIPTION_NAME, expenseDetails.getDescription());
        if (!(customDate.equals("") || customDate.equals("0")))
            values.put(Constants.DB_EXPENSE_DATE_NAME, customDate);
        database.update(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, values, Constants.DB_EXPENSE_DATE_NAME + " = " + "'" + expenseDetails.getDate() + "'", null);
    }

    public void updateEventExpenseFromDate(ExpenseDetails eventExpenseDetails, String customDate) {
        ContentValues values = new ContentValues();
        values.put(Constants.DB_EVENT_EXPENSE_PAYEE_NAME, eventExpenseDetails.getPayeesList());
        values.put(Constants.DB_EVENT_EXPENSE_AMOUNT_NAME, eventExpenseDetails.getAmount());
        values.put(Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME, eventExpenseDetails.getDescription());
        values.put(Constants.DB_EVENT_EXPENSE_PAYER_NAME, eventExpenseDetails.getPayersList());
        values.put(Constants.DB_EVENT_EXPENSE_EVENT_NAME, eventExpenseDetails.getEventName());
        if (!(customDate.equals("") || customDate.equals("0")))
            values.put(Constants.DB_EVENT_EXPENSE_DATE_NAME, customDate);
        database.update(Constants.DB_EVENT_EXPENSE_TABLE_NAME, values,
                Constants.DB_EVENT_EXPENSE_DATE_NAME + " = " + "'" + eventExpenseDetails.getDate() + "'", null);
    }

    public ArrayList<ExpenseDetails> getAllUnPaidExpenseList() {
        Cursor cursor = database.query(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EXPENSE_PAYEE_NAME,
                        Constants.DB_EXPENSE_AMOUNT_NAME, Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME, Constants.DB_EXPENSE_DATE_NAME, Constants.DB_EXPENSE_DESCRIPTION_NAME}, null, null, null, null,
                Constants.DB_EXPENSE_DATE_NAME + " DESC");
        ArrayList<ExpenseDetails> expenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails expenseDetails = new ExpenseDetails();
                expenseDetails.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_PAYEE_NAME)));
                expenseDetails.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_AMOUNT_NAME)));
                expenseDetails.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME)));
                expenseDetails.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME)));
                expenseDetails.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DATE_NAME)));
                expenseDetails.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DESCRIPTION_NAME)));
                expenseList.add(expenseDetails);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenseList;
    }

    public ArrayList<ExpenseDetails> getAllPaidExpenseList() {
        Cursor cursor = database.query(Constants.DB_PAID_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EXPENSE_PAYEE_NAME, Constants.DB_EXPENSE_AMOUNT_NAME,
                Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME, Constants.DB_EXPENSE_DATE_NAME, Constants.DB_EXPENSE_DESCRIPTION_NAME}, null, null, null, null, Constants.DB_EXPENSE_DATE_NAME + " DESC");
        ArrayList<ExpenseDetails> expenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails expenseDetails = new ExpenseDetails();
                expenseDetails.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_PAYEE_NAME)));
                expenseDetails.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_AMOUNT_NAME)));
                expenseDetails.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME)));
                expenseDetails.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME)));
                expenseDetails.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DATE_NAME)));
                expenseDetails.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DESCRIPTION_NAME)));
                expenseList.add(expenseDetails);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenseList;
    }

    public ArrayList<ExpenseDetails> getAllEventExpenseList() {
        Cursor cursor = database.query(Constants.DB_EVENT_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EVENT_EXPENSE_AMOUNT_NAME,
                Constants.DB_EVENT_EXPENSE_EVENT_NAME, Constants.DB_EVENT_EXPENSE_DATE_NAME, Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME,
                Constants.DB_EVENT_EXPENSE_PAYER_NAME, Constants.DB_EVENT_EXPENSE_PAYEE_NAME}, null, null, null, null, Constants.DB_EVENT_EXPENSE_DATE_NAME
                + " DESC");
        ArrayList<ExpenseDetails> eventExpenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails eventExpense = new ExpenseDetails();
                eventExpense.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_AMOUNT_NAME)));
                eventExpense.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DATE_NAME)));
                eventExpense.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME)));
                eventExpense.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYER_NAME)));
                eventExpense.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYEE_NAME)));
                eventExpense.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_EVENT_NAME)));
                eventExpenseList.add(eventExpense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventExpenseList;
    }

    public ArrayList<ExpenseDetails> getUnPaidExpenseListForAPayee(String payee) {
        Cursor cursor = database.query(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EXPENSE_PAYEE_NAME,
                Constants.DB_EXPENSE_AMOUNT_NAME, Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME, Constants.DB_EXPENSE_DATE_NAME, Constants.DB_EXPENSE_DESCRIPTION_NAME}, Constants.DB_EXPENSE_PAYEE_NAME
                + " LIKE " + "'%" + payee.replaceAll("'", "\''") + "%'", null, null, null, Constants.DB_EXPENSE_DATE_NAME + " DESC");
        ArrayList<ExpenseDetails> newExpenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails expenseDetails = new ExpenseDetails();
                expenseDetails.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_PAYEE_NAME)));
                expenseDetails.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_AMOUNT_NAME)));
                expenseDetails.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME)));
                expenseDetails.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME)));
                expenseDetails.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DATE_NAME)));
                expenseDetails.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DESCRIPTION_NAME)));
                if (Utils.doesPayeeExistInPayeeList(payee, expenseDetails.getPayeesList())) {
                    newExpenseList.add(expenseDetails);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return newExpenseList;
    }

    public ArrayList<ExpenseDetails> getPaidExpenseListForAPayee(String payee) {
        Cursor cursor = database.query(Constants.DB_PAID_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EXPENSE_PAYEE_NAME, Constants.DB_EXPENSE_AMOUNT_NAME, Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME,
                        Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME, Constants.DB_EXPENSE_DATE_NAME, Constants.DB_EXPENSE_DESCRIPTION_NAME},
                Constants.DB_EXPENSE_PAYEE_NAME + " LIKE " + "'%" + payee.replaceAll("'", "\''") + "%'", null, null, null, Constants.DB_EXPENSE_DATE_NAME
                        + " DESC");
        ArrayList<ExpenseDetails> newExpenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails expenseDetails = new ExpenseDetails();
                expenseDetails.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_PAYEE_NAME)));
                expenseDetails.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_AMOUNT_NAME)));
                expenseDetails.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME)));
                expenseDetails.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME)));
                expenseDetails.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DATE_NAME)));
                expenseDetails.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DESCRIPTION_NAME)));
                if (Utils.doesPayeeExistInPayeeList(payee, expenseDetails.getPayeesList())) {
                    newExpenseList.add(expenseDetails);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return newExpenseList;
    }

    public ArrayList<ExpenseDetails> getEventExpenseListForAPayerForAEvent(String eventName, String payer) {
        Cursor cursor = database.query(Constants.DB_EVENT_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EVENT_EXPENSE_AMOUNT_NAME,
                        Constants.DB_EVENT_EXPENSE_EVENT_NAME, Constants.DB_EVENT_EXPENSE_DATE_NAME, Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME,
                        Constants.DB_EVENT_EXPENSE_PAYER_NAME, Constants.DB_EVENT_EXPENSE_PAYEE_NAME}, Constants.DB_EVENT_EXPENSE_PAYER_NAME + "  LIKE " + "'%"
                        + payer.replaceAll("'", "\''") + "%'" + " AND " + Constants.DB_EVENT_EXPENSE_EVENT_NAME + " = " + "'" + eventName.replaceAll("'", "\''") + "'",
                null, null, null, Constants.DB_EVENT_EXPENSE_DATE_NAME + " DESC");
        ArrayList<ExpenseDetails> eventExpenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails eventExpense = new ExpenseDetails();
                eventExpense.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_AMOUNT_NAME)));
                eventExpense.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DATE_NAME)));
                eventExpense.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME)));
                eventExpense.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYER_NAME)));
                eventExpense.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYEE_NAME)));
                eventExpense.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_EVENT_NAME)));
                eventExpenseList.add(eventExpense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventExpenseList;
    }

    public ArrayList<ExpenseDetails> getEventExpenseListForAPayeeForAEvent(String eventName, String payee) {
        Cursor cursor = database.query(Constants.DB_EVENT_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EVENT_EXPENSE_AMOUNT_NAME,
                        Constants.DB_EVENT_EXPENSE_EVENT_NAME, Constants.DB_EVENT_EXPENSE_DATE_NAME, Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME,
                        Constants.DB_EVENT_EXPENSE_PAYER_NAME, Constants.DB_EVENT_EXPENSE_PAYEE_NAME}, Constants.DB_EVENT_EXPENSE_PAYEE_NAME + "  LIKE " + "'%"
                        + payee.replaceAll("'", "\''") + "%'" + " AND " + Constants.DB_EVENT_EXPENSE_EVENT_NAME + " = " + "'" + eventName.replaceAll("'", "\''") + "'",
                null, null, null, Constants.DB_EVENT_EXPENSE_DATE_NAME + " DESC");
        ArrayList<ExpenseDetails> eventExpenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails eventExpense = new ExpenseDetails();
                eventExpense.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_AMOUNT_NAME)));
                eventExpense.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DATE_NAME)));
                eventExpense.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME)));
                eventExpense.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYER_NAME)));
                eventExpense.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYEE_NAME)));
                eventExpense.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_EVENT_NAME)));
                eventExpenseList.add(eventExpense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventExpenseList;
    }

    public ArrayList<String> getAllPayees() {
        Cursor cursor = database.query(Constants.DB_PAYEE_TABLE, new String[]{Constants.DB_PAYEE_PAYEE_NAME}, null, null, null, null,
                Constants.DB_PAYEE_PAYEE_NAME + " ASC", null);
        ArrayList<String> payeeList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                payeeList.add(cursor.getString(cursor.getColumnIndex(Constants.DB_PAYEE_PAYEE_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return payeeList;
    }

    public ArrayList<String> getAllEvents() {
        Cursor cursor = database.query(Constants.DB_EVENT_TABLE, new String[]{Constants.DB_EVENT_EVENT_NAME}, null, null, null, null,
                Constants.DB_EVENT_EVENT_NAME + " ASC", null);
        ArrayList<String> eventList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                eventList.add(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EVENT_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventList;
    }

    public ExpenseDetails getUnPaidExpenseFromDate(String date) {
        ExpenseDetails expenseDetails = null;
        Cursor cursor = database.query(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EXPENSE_PAYEE_NAME,
                Constants.DB_EXPENSE_AMOUNT_NAME, Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME, Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME,
                Constants.DB_EXPENSE_DATE_NAME, Constants.DB_EXPENSE_DESCRIPTION_NAME}, Constants.DB_EXPENSE_DATE_NAME
                + " = " + "'" + date + "'", null, null, null, Constants.DB_EXPENSE_DATE_NAME + " DESC");
        if (cursor.moveToFirst()) {
            do {
                expenseDetails = new ExpenseDetails();
                expenseDetails.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_PAYEE_NAME)));
                expenseDetails.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_AMOUNT_NAME)));
                expenseDetails.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_PAYEE_NAME)));
                expenseDetails.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_ORIGINAL_AMOUNT_NAME)));
                expenseDetails.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DATE_NAME)));
                expenseDetails.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EXPENSE_DESCRIPTION_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenseDetails;
    }

    public ExpenseDetails getEventExpenseFromDate(String date) {
        ExpenseDetails eventExpense = null;
        Cursor cursor = database.query(Constants.DB_EVENT_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EVENT_EXPENSE_PAYER_NAME,
                Constants.DB_EVENT_EXPENSE_PAYEE_NAME, Constants.DB_EVENT_EXPENSE_AMOUNT_NAME, Constants.DB_EVENT_EXPENSE_DATE_NAME,
                Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME, Constants.DB_EVENT_EXPENSE_EVENT_NAME}, Constants.DB_EVENT_EXPENSE_DATE_NAME + " = " + "'" + date
                + "'", null, null, null, Constants.DB_EVENT_EXPENSE_DATE_NAME + " DESC");
        if (cursor.moveToFirst()) {
            do {
                eventExpense = new ExpenseDetails();
                eventExpense.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYER_NAME)));
                eventExpense.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYEE_NAME)));
                eventExpense.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_AMOUNT_NAME)));
                eventExpense.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DATE_NAME)));
                eventExpense.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME)));
                eventExpense.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_EVENT_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventExpense;
    }

    public ArrayList<PayeeDetails> getPayeeWithNonZeroAmount() {
        ArrayList<String> payeeList = getAllPayees();
        int[] payeeAmountList = new int[payeeList.size()];
        ArrayList<ExpenseDetails> expenseList = getAllUnPaidExpenseList();
        for (ExpenseDetails expense : expenseList) {
            String[] payees = expense.getPayeesList().split(", ");
            int amountForAPayee = Integer.parseInt(expense.getAmount()) / payees.length;
            for (String payee : payees) {
                payeeAmountList[payeeList.indexOf(payee)] = payeeAmountList[payeeList.indexOf(payee)] + amountForAPayee;
            }
        }
        ArrayList<PayeeDetails> payeeDetailsList = new ArrayList<>();
        for (int i = 0; i < payeeAmountList.length; i++) {
            if (payeeAmountList[i] != 0) {
                PayeeDetails payeeDetails = new PayeeDetails();
                payeeDetails.setName(payeeList.get(i));
                payeeDetails.setAmount(payeeAmountList[i] + "");
                payeeDetails.setColorCode(RandomColor.getRandomColor(payeeList.get(i)));
                payeeDetailsList.add(payeeDetails);
            }
        }
        return payeeDetailsList;
    }

    public ArrayList<PayeeDetails> getPayeesWithPaidExpense() {
        ArrayList<String> payeeList = getAllPayees();
        ArrayList<ExpenseDetails> expenseList = getAllPaidExpenseList();
        int[] payeeAmountList = new int[payeeList.size()];
        for (ExpenseDetails expense : expenseList) {
            String[] payees = expense.getPayeesList().split(", ");
            int amountForAPayee = Integer.parseInt(expense.getAmount()) / payees.length;
            for (String payee : payees) {
                payeeAmountList[payeeList.indexOf(payee)] = payeeAmountList[payeeList.indexOf(payee)] + amountForAPayee;
            }
        }
        ArrayList<PayeeDetails> payeeDetailsList = new ArrayList<>();
        for (int i = 0; i < payeeAmountList.length; i++) {
            if (payeeAmountList[i] != 0) {
                PayeeDetails payeeDetails = new PayeeDetails();
                payeeDetails.setName(payeeList.get(i));
                payeeDetails.setAmount(payeeAmountList[i] + "");
                payeeDetails.setColorCode(RandomColor.getRandomColor(payeeList.get(i)));
                payeeDetailsList.add(payeeDetails);
            }
        }
        return payeeDetailsList;
    }

    public ArrayList<PayeeDetails> getPayersWithNonZeroAmountForAnEvent(String eventName) {
        ArrayList<PayeeDetails> nonZeroPayerList = new ArrayList<>();
        ArrayList<String> payerList = getAllPayersForAEvent(eventName);
        ArrayList<ExpenseDetails> eventExpenseList = getAllEventExpenseForAEvent(eventName);
        int[] payerAmountList = new int[payerList.size()];
        for (ExpenseDetails eventExpense : eventExpenseList) {
            payerAmountList[payerList.indexOf(eventExpense.getPayersList())] = payerAmountList[payerList.indexOf(eventExpense.getPayersList())]
                    + Integer.parseInt(eventExpense.getAmount());
        }
        for (int i = 0; i < payerAmountList.length; i++) {
            PayeeDetails payerDetails = new PayeeDetails();
            payerDetails.setName(payerList.get(i));
            payerDetails.setAmount(payerAmountList[i] + "");
            payerDetails.setColorCode(RandomColor.getRandomColor(payerList.get(i)));
            nonZeroPayerList.add(payerDetails);
        }
        return nonZeroPayerList;
    }

    public ArrayList<String> getAllPayersForAEvent(String eventName) {
        Cursor cursor = database.query(true, Constants.DB_EVENT_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EVENT_EXPENSE_PAYER_NAME},
                Constants.DB_EVENT_EXPENSE_EVENT_NAME + " = " + "'" + eventName.replaceAll("'", "\''") + "'", null, null, null, null, null);
        ArrayList<String> payerStrings = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                payerStrings.add(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYER_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return payerStrings;
    }

    public ArrayList<PayeeDetails> getAllPersonsListForAnEvent(String eventName) {
        ArrayList<PayeeDetails> personsList = new ArrayList<>();
        ArrayList<String> personsNameList = new ArrayList<>();
        ArrayList<Integer> personAmountList = new ArrayList<>();
        ArrayList<ExpenseDetails> eventExpenseList = getAllEventExpenseForAEvent(eventName);
        for (ExpenseDetails eventExpense : eventExpenseList) {
            String[] payeeStrings = eventExpense.getPayeesList().split(", ");
            for (String payee : payeeStrings) {
                if (!personsNameList.contains(payee)) {
                    personsNameList.add(payee);
                    personAmountList.add(0);
                }
            }
            String[] payerStrings = eventExpense.getPayersList().split(", ");
            for (String payer : payerStrings) {
                if (!personsNameList.contains(payer)) {
                    personsNameList.add(payer);
                    personAmountList.add(0);
                }
                personAmountList.set(personsNameList.indexOf(payer),
                        personAmountList.get(personsNameList.indexOf(payer)) + Integer.parseInt(eventExpense.getAmount()));
            }
        }
        for (int i = 0; i < personAmountList.size(); i++) {
            PayeeDetails payerDetails = new PayeeDetails();
            payerDetails.setName(personsNameList.get(i));
            payerDetails.setAmount(personAmountList.get(i) + "");
            payerDetails.setColorCode(RandomColor.getRandomColor(personsNameList.get(i)));
            personsList.add(payerDetails);
        }
        if (personsList.size() >= 1) {
            personsList = new Quicksort(personsList).sort();
        }
        return personsList;
    }

    public String getTotalAmountForAPayerForAEvent(String event, String payer) {
        ArrayList<ExpenseDetails> eventExpenseList = getEventExpenseListForAPayerForAEvent(event, payer);
        int amount = 0;
        for (ExpenseDetails eventExpense : eventExpenseList) {
            int payerCount = Utils.getPayeeCountFromPayees(eventExpense.getPayersList());
            if (payerCount <= 1) {
                amount = amount + Integer.parseInt(eventExpense.getAmount());
            } else {
                amount = amount + (Integer.parseInt(eventExpense.getAmount()) / payerCount);
            }
        }
        return amount + "";
    }

    public String getTotalAmountForApayee(String payee) {
        ArrayList<ExpenseDetails> expenseList = getUnPaidExpenseListForAPayee(payee);
        // float amount = 0.00f;
        int amount = 0;
        for (ExpenseDetails expenseDetails : expenseList) {
            String[] strings = expenseDetails.getPayeesList().split(", ");
            if (strings.length <= 1) {
                // amount = amount +
                // Float.parseFloat(expenseDetails.getAmount());
                amount = amount + Integer.parseInt(expenseDetails.getAmount());
            } else {
                amount = amount + (Integer.parseInt(expenseDetails.getAmount()) / strings.length);
                // amount = amount +
                // (Float.parseFloat(expenseDetails.getAmount()) /
                // strings.length);
            }
        }
        return amount + "";
    }

    public ArrayList<String> getAllPayeesForAnEvent(String eventName) {
        ArrayList<ExpenseDetails> eventExpenseList = getAllEventExpenseForAEvent(eventName);
        ArrayList<String> payeeList = new ArrayList<>();
        for (ExpenseDetails eventExpense : eventExpenseList) {
            String[] payeeStrings = eventExpense.getPayeesList().split(", ");
            for (String payee : payeeStrings) {
                if (!payeeList.contains(payee)) {
                    payeeList.add(payee);
                }
            }
        }
        return payeeList;
    }

    public ArrayList<ExpenseDetails> getAllEventExpenseForAEvent(String eventName) {
        Cursor cursor = database.query(Constants.DB_EVENT_EXPENSE_TABLE_NAME, new String[]{Constants.DB_EVENT_EXPENSE_AMOUNT_NAME,
                        Constants.DB_EVENT_EXPENSE_EVENT_NAME, Constants.DB_EVENT_EXPENSE_DATE_NAME, Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME,
                        Constants.DB_EVENT_EXPENSE_PAYER_NAME, Constants.DB_EVENT_EXPENSE_PAYEE_NAME},
                Constants.DB_EVENT_EXPENSE_EVENT_NAME + " = " + "'" + eventName.replaceAll("'", "\''") + "'", null, null, null,
                Constants.DB_EVENT_EXPENSE_DATE_NAME + " DESC");
        ArrayList<ExpenseDetails> eventExpenseList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ExpenseDetails eventExpense = new ExpenseDetails();
                eventExpense.setAmount(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_AMOUNT_NAME)));
                eventExpense.setDate(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DATE_NAME)));
                eventExpense.setDescription(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_DESCRIPTION_NAME)));
                eventExpense.setPayersList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYER_NAME)));
                eventExpense.setPayeesList(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_PAYEE_NAME)));
                eventExpense.setEventName(cursor.getString(cursor.getColumnIndex(Constants.DB_EVENT_EXPENSE_EVENT_NAME)));
                eventExpenseList.add(eventExpense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventExpenseList;
    }

    public String getTotalAmountForAEvent(String eventName) {
        ArrayList<ExpenseDetails> eventExpenseList = getAllEventExpenseForAEvent(eventName);
        int amount = 0;
        for (ExpenseDetails eventExpense : eventExpenseList) {
            amount = amount + Integer.parseInt(eventExpense.getAmount());
        }
        return amount + "";
    }

    public void deleteUnPaidExpense(ExpenseDetails expenseDetails) {
        database.delete(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, Constants.DB_EXPENSE_DATE_NAME + " = " + "'" + expenseDetails.getDate() + "'" + " AND "
                + Constants.DB_EXPENSE_PAYEE_NAME + " = " + "'" + expenseDetails.getPayeesList().replaceAll("'", "\''") + "'", null);
    }

    public boolean deletePayee(String payee) {
        boolean isPayeeInUse = isPayeeInUse(payee);
        Log.i("MyWallet", "isPayeeInUse-" + isPayeeInUse);
        if (!isPayeeInUse) {
            ArrayList<ExpenseDetails> expenseList = getPaidExpenseListForAPayee(payee);
            for (ExpenseDetails expenseDetails : expenseList) {
                if (Utils.getPayeeCountFromPayees(expenseDetails.getPayeesList()) == 1) {
                    deletePaidExpense(expenseDetails);
                }
            }
            database.delete(Constants.DB_PAYEE_TABLE, Constants.DB_PAYEE_PAYEE_NAME + " = " + "'" + payee.replaceAll("'", "\''") + "'", null);
        }
        return !isPayeeInUse;
    }

    private boolean isPayeeInUse(String payee) {
        ArrayList<ExpenseDetails> unPaidExpenseList = getAllUnPaidExpenseList();
        for (ExpenseDetails expense : unPaidExpenseList) {
            if (Utils.doesPayeeExistInPayeeList(payee, expense.getPayeesList())) {
                Log.i("MyWallet", "payee found in unpaid list, returning true");
                return true;
            }
        }
        ArrayList<ExpenseDetails> eventExpenseList = getAllEventExpenseList();
        for (ExpenseDetails expense : eventExpenseList) {
            if (Utils.doesPayeeExistInPayeeList(payee, expense.getPayeesList())) {
                Log.i("MyWallet", "payee found in event list, returning true");
                return true;
            }
        }
        Log.i("MyWallet", "payee not found in any list, returning false");
        return false;
    }

    public void deleteEventName(String eventName) {
        deleteAllExpensesForAEvent(eventName);
        database.delete(Constants.DB_EVENT_TABLE, Constants.DB_EVENT_EVENT_NAME + " = " + "'" + eventName.replaceAll("'", "\''") + "'", null);
    }

    public void deleteAllExpensesForAEvent(String eventName) {
        database.delete(Constants.DB_EVENT_EXPENSE_TABLE_NAME, Constants.DB_EVENT_EXPENSE_EVENT_NAME + " = " + "'" + eventName.replaceAll("'", "\''") + "'",
                null);
    }

    public void deletePaidExpense(ExpenseDetails expenseDetails) {
        database.delete(Constants.DB_PAID_EXPENSE_TABLE_NAME, Constants.DB_EXPENSE_DATE_NAME + " = " + "'" + expenseDetails.getDate() + "'" + " AND "
                + Constants.DB_EXPENSE_PAYEE_NAME + " = " + "'" + expenseDetails.getPayeesList().replaceAll("'", "\''") + "'", null);
    }

    public void deleteEventExpenseForAPayerForAEvent(ExpenseDetails eventExpense) {
        database.delete(Constants.DB_EVENT_EXPENSE_TABLE_NAME, Constants.DB_EVENT_EXPENSE_DATE_NAME + " = " + "'" + eventExpense.getDate() + "'", null);
    }

    public void markExpenseAsPaid(ExpenseDetails expenseDetails) {
        deleteUnPaidExpense(expenseDetails);
        insertPaidExpense(expenseDetails);
    }

    public void markExpenseAsUnPaid(ExpenseDetails expenseDetails) {
        deletePaidExpense(expenseDetails);
        insertExpenseData(expenseDetails);
    }

    public void markAsPaidForOnePayee(ExpenseDetails expenseDetails, String payee) {
        int payeeCount = Utils.getPayeeCountFromPayees(expenseDetails.getPayeesList());
        if (payeeCount > 1) {
            int amount = Integer.parseInt(expenseDetails.getAmount());
            int amountForOnePayee = amount / payeeCount;
            ExpenseDetails expense = new ExpenseDetails();
            expense.setAmount("" + amountForOnePayee);
            expense.setDate(expenseDetails.getDate());
            expense.setDescription(expenseDetails.getDescription());
            expense.setPayeesList(payee);
            expense.setPayersList(expenseDetails.getPayersList());
            expense.setEventName(expenseDetails.getEventName());
            insertPaidExpense(expense);
            expenseDetails.setPayeesList(Utils.removePayeeFromPayeeList(expenseDetails.getPayeesList(), payee));
            expenseDetails.setAmount("" + (amount - amountForOnePayee));
            updateUnPaidExpenseFromDate(expenseDetails, "");
        } else {
            markExpenseAsPaid(expenseDetails);
        }
    }

    public void markAsUnPaidForOnePayee(ExpenseDetails expenseDetails) {
        ExpenseDetails expense = getUnPaidExpenseFromDate(expenseDetails.getDate());
        if (expense == null) {
            markExpenseAsUnPaid(expenseDetails);
        } else {
            expense.setPayeesList(expense.getPayeesList() + ", " + expenseDetails.getPayeesList());
            expense.setAmount("" + (Integer.parseInt(expense.getAmount()) + Integer.parseInt(expenseDetails.getAmount())));
            updateUnPaidExpenseFromDate(expense, "");
            deletePaidExpense(expenseDetails);
        }
    }

    public void markAllAsPaidForOnePayee(String payee) {
        ArrayList<ExpenseDetails> expenseList = getUnPaidExpenseListForAPayee(payee);
        for (ExpenseDetails expense : expenseList) {
            markAsPaidForOnePayee(expense, payee);
        }
    }

    public void markAllAsPaidForAllPayees() {
        ArrayList<String> allPayees = getAllPayees();
        for (String payee : allPayees) {
            ArrayList<ExpenseDetails> expenseList = getUnPaidExpenseListForAPayee(payee);
            for (ExpenseDetails expense : expenseList) {
                markAsPaidForOnePayee(expense, payee);
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void changeDateToSystemMillies() {
        ContentValues values;
        String regex = "\\d+";
        ArrayList<ExpenseDetails> expenseList = getAllUnPaidExpenseList();
        for (ExpenseDetails expenseDetails : expenseList) {
            String dateString = expenseDetails.getDate();
            values = new ContentValues();
            try {
                if (!dateString.matches(regex)) {
                    values.put(Constants.DB_EXPENSE_DATE_NAME, Utils.getMilliesFromDate(dateString));
                    database.update(Constants.DB_UNPAID_EXPENSE_TABLE_NAME, values, Constants.DB_EXPENSE_DATE_NAME + " = " + "'" + dateString + "'", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        expenseList = getAllPaidExpenseList();
        for (ExpenseDetails expenseDetails : expenseList) {
            String dateString = expenseDetails.getDate();
            values = new ContentValues();
            try {
                if (!dateString.matches(regex)) {
                    values.put(Constants.DB_EXPENSE_DATE_NAME, Utils.getMilliesFromDate(dateString));
                    database.update(Constants.DB_PAID_EXPENSE_TABLE_NAME, values, Constants.DB_EXPENSE_DATE_NAME + " = " + "'" + dateString + "'", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ArrayList<ExpenseDetails> eventExpenseList = getAllEventExpenseList();
        for (ExpenseDetails eventExpense : eventExpenseList) {
            String dateString = eventExpense.getDate();
            values = new ContentValues();
            try {
                if (!dateString.matches(regex)) {
                    values.put(Constants.DB_EVENT_EXPENSE_DATE_NAME, Utils.getMilliesFromDate(dateString));
                    database.update(Constants.DB_EVENT_EXPENSE_TABLE_NAME, values, Constants.DB_EVENT_EXPENSE_DATE_NAME + " = " + "'" + dateString + "'", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dropExpenseTable() {
        String scoreDropTableQuery = "DROP TABLE " + Constants.DB_UNPAID_EXPENSE_TABLE_NAME;
        database.execSQL(scoreDropTableQuery);
    }

    public void dropPayeeTable() {
        String scoreDropTableQuery = "DROP TABLE " + Constants.DB_PAYEE_TABLE;
        database.execSQL(scoreDropTableQuery);
    }
}
