package com.walletv2.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.walletv2.dbhandler.ExpenseDatabaseHandler;

public class BaseActivity extends AppCompatActivity {
    protected ActionBar mActionBar;
    protected AppCompatActivity mActivity;
    protected ExpenseDatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setElevation(0);
        }
        databaseHandler = new ExpenseDatabaseHandler(mActivity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        databaseHandler.getDatabase().close();
        super.onDestroy();
    }
}
