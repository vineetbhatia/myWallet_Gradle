package com.walletv2.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.walletv2.entity.Constants;
import com.walletv2.filehandler.FileHandler;

import java.io.File;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

public class HomeActivity extends BaseActivity {
    private SharedPreferences sharedPreferences;
    private Editor preferencesEditor;
    private final int CORRECT_DATE_FORMAT_TASK = 10;
    private final int CURRENT_DB_VERSION = 1;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        preferencesEditor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean(getString(R.string.prefs_secure_app_key), false)) {
            if (sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_IS_LOCK_SET, false)) {
                confirmPattern();
            } else {
                createNewPattern();
            }
        } else {
            if (!sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_IS_DB_UPGRADED, false) && onUpgradeToDBVersionOne())
                preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_DB_UPGRADED, true).apply();
            if (!sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_IS_DATE_FORMAT_CORRECTED, false))
                new ChangeDateFormatTask().execute();
            else
                launchNextActivity();
        }
    }

    public boolean onUpgradeToDBVersionOne() {
        boolean success;
        databaseHandler.alterExpenseTable();
        success = databaseHandler.updateExpenseTableForUpgradeToVersionOne();
        return success;
    }

    public void launchNextActivity() {
        if (sharedPreferences.contains(Constants.SHARED_PREFERENCES_IS_INTRODUCTION_COMPLETED) && sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_IS_INTRODUCTION_COMPLETED, false))
            launchWalletPayeeList();
        else {
            Intent intent = new Intent(mActivity, IntroductionActivity.class);
            startActivityForResult(intent, Constants.REQUEST_CODE_WALLET_EXPENSE_LIST);
        }
    }

    private void launchWalletPayeeList() {
        Intent intent = new Intent(mActivity, DrawerActivity.class);
        intent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_LIST, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_WALLET_EXPENSE_LIST);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CORRECT_DATE_FORMAT_TASK:
                pd = new ProgressDialog(mActivity);
                pd.setTitle("Please Wait...");
                pd.setMessage("Performing some housekeeping, Please do not close this application, this may take a minute...");
                pd.setCancelable(false);
                return pd;
            default:
                return super.onCreateDialog(id);
        }
    }

    class ChangeDateFormatTask extends AsyncTask<String, String, String> {
        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(CORRECT_DATE_FORMAT_TASK);
        }

        @Override
        protected String doInBackground(String... arg0) {
            Log.i("MayWallet", "Correcting date format");
            databaseHandler.createAllTables();
            databaseHandler.changeDateToSystemMillies();
            // delete old preference file
            new File(mActivity.getApplicationInfo().dataDir + "/shared_prefs/" + Constants.SHARED_PREFERENCES_NAME + ".xml").delete();
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
                new File(Environment.getExternalStorageDirectory().toString() + "/MyWallet/shared_prefs/" + Constants.SHARED_PREFERENCES_NAME + ".xml")
                        .delete();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd != null && pd.isShowing())
                pd.dismiss();
            Log.i("MyWallet", "Date Format corrected");
            preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_DATE_FORMAT_CORRECTED, true);
            preferencesEditor.commit();
            launchNextActivity();
        }
    }

    /**
     * This function is used to reset the current pattern for pattern lock or to
     * set the new pattern, if the user is using it for the first time.
     */
    private void createNewPattern() {
        Toast.makeText(mActivity,
                "Please enter the combination twice to secure the application with pattern lock. This combination will be used to unlock the application.",
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(mActivity, LockPatternActivity.class);
        intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
        intent.putExtra(LockPatternActivity._AutoSave, true);
        startActivityForResult(intent, Constants._ReqCreatePattern);
    }

    /**
     * This function is used to confirm the pattern of the pattern lock. The
     * combination, which has been set by the user previously,will be used to
     * unlock the application. This function will start a new activity called
     * LockPatternActivity, which requires the previously set combination to
     * unlock.
     */
    private void confirmPattern() {
        // Toast.makeText(
        // mActivity,
        // "Please enter the correct combination to access the secure application.",
        // Toast.LENGTH_LONG).show();
        Intent intent = new Intent(mActivity, LockPatternActivity.class);
        intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.ComparePattern);
        intent.putExtra(LockPatternActivity._AutoSave, true);
        startActivityForResult(intent, Constants._ReqSignIn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants._ReqCreatePattern:
                if (resultCode == RESULT_OK) {
                    // String pattern = data
                    // .getStringExtra(LockPatternActivity._Pattern);
                    Toast.makeText(mActivity, "Passcode successfully set.", Toast.LENGTH_SHORT).show();
                    preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_LOCK_SET, false).commit();
                } else {
                    Toast.makeText(mActivity, "Some problem has occurred while setting the lock. Please try again.", Toast.LENGTH_LONG).show();
                    preferencesEditor.putBoolean(getString(R.string.prefs_secure_app_key), false);
                    preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_LOCK_SET, false);
                    preferencesEditor.commit();
                    launchWalletPayeeList();
                }
                break;
            case Constants._ReqSignIn:
                if (resultCode == RESULT_OK) {
                    if (sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_IS_DATE_FORMAT_CORRECTED, false))
                        launchWalletPayeeList();
                    else
                        new ChangeDateFormatTask().execute();
                } else {
                    Toast.makeText(mActivity, "Sign in failed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case Constants.REQUEST_CODE_WALLET_EXPENSE_LIST:
                if (sharedPreferences.getBoolean(getString(R.string.prefs_automatic_backup_key), false)) {
                    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (new FileHandler(mActivity).exportToSdCard(Integer.parseInt(sharedPreferences.getString(
                                        getString(R.string.prefs_no_of_backups_key), "5")))) {
                                    Toast.makeText(mActivity, "Export to sdCard successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mActivity, "Export to sdCard unsuccessful", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).run();
                    }
                }
                finish();
                break;
        }
    }
}
