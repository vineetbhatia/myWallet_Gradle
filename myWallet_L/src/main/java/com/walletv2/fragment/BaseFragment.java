package com.walletv2.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.walletv2.dbhandler.ExpenseDatabaseHandler;

public class BaseFragment extends Fragment {
    protected AppCompatActivity mParentActivity;
    protected ActionBar mActionBar;
    protected FragmentManager mFragmentManager;
    protected ExpenseDatabaseHandler databaseHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mParentActivity = (AppCompatActivity) getActivity();
        mActionBar = mParentActivity.getSupportActionBar();
        mFragmentManager = mParentActivity.getSupportFragmentManager();
        databaseHandler = new ExpenseDatabaseHandler(mParentActivity);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        databaseHandler.getDatabase().close();
        super.onDestroy();
    }
}
