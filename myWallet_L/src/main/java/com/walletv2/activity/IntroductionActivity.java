package com.walletv2.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.walletv2.entity.Constants;

import java.util.ArrayList;

/**
 * Created by wInY on 021, 21 Mar 15.
 */
public class IntroductionActivity extends BaseActivity {
    private EditText mFirstName, mLastName;
    private AutoCompleteTextView mTagName;
    private CheckBox mAlreadyHaveTag;
    private Button mContinue;
    private ArrayList mTagList;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction_activity);

        fetchAllTags();
        initializeUiElements();
        setUiListener();
    }

    private void initializeUiElements() {
        mFirstName = (EditText) findViewById(R.id.edit_text_first);
        mLastName = (EditText) findViewById(R.id.edit_text_last);
        mTagName = (AutoCompleteTextView) findViewById(R.id.edit_text_tag);
        mAlreadyHaveTag = (CheckBox) findViewById(R.id.check_box_already_exists);
        mContinue = (Button) findViewById(R.id.btn_continue);
    }

    private void setUiListener() {
        mAlreadyHaveTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTagName.setHint(R.string.already_have_tag_hint);
                    mTagName.setAdapter(
                            new ArrayAdapter<>(IntroductionActivity.this, android.R.layout.simple_dropdown_item_1line, mTagList));
                    mTagName.setThreshold(1);
                } else {
                    mTagName.setHint(R.string.new_tag_hint);
                    mTagName.setAdapter(null);
                }
            }
        });
        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFirstName.getText().toString().trim().length() > 0 && mLastName.getText().toString().trim().length() > 0 &&
                    mTagName.getText().toString().trim().length() > 0)
                    if (mTagList == null || mTagList.size() == 0)
                        savePersonalDetails(true);
                    else if (mTagList.contains(mTagName.getText().toString().trim()))
                        savePersonalDetails(false);
                    else
                        showCreateNewTagAlert();
            }
        });
    }

    public void loadSharedPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();
        if (mSharedPreferences.contains(Constants.SHARED_PREFERENCES_PROFILE_NAME)) {
            String profileName = mSharedPreferences.getString(Constants.SHARED_PREFERENCES_PROFILE_NAME, "First Name");
            mFirstName.setText(profileName.substring(0, profileName.lastIndexOf(' ')));
            mLastName.setText(profileName.substring(profileName.lastIndexOf(' '), profileName.length()));
            if (mTagList != null && mSharedPreferences.contains(Constants.SHARED_PREFERENCES_TAG_NAME) &&
                mTagList.contains(mSharedPreferences.getString(Constants.SHARED_PREFERENCES_TAG_NAME, ""))) {
                mTagName.setText(mSharedPreferences.getString(Constants.SHARED_PREFERENCES_TAG_NAME, ""));
            }
        }
    }

    private void savePersonalDetails(boolean createTag) {
        if (createTag)
            if (!databaseHandler.isPayeeExists(mTagName.getText().toString().trim()))
                databaseHandler.insertPayee(mTagName.getText().toString().trim());
            else {
                Toast.makeText(this, "This payee already exists, kindly check the tick box and select your tag from the list",
                               Toast.LENGTH_SHORT).show();
                return;
            }

        mEditor.putString(Constants.SHARED_PREFERENCES_PROFILE_NAME,
                          mFirstName.getText().toString().trim() + " " + mLastName.getText().toString().trim());
        mEditor.putString(Constants.SHARED_PREFERENCES_TAG_NAME, mTagName.getText().toString().trim());
        mEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_INTRODUCTION_COMPLETED, true);
        mEditor.commit();
        launchWalletPayeeList();
        finish();
    }

    private void launchWalletPayeeList() {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_LIST, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_WALLET_EXPENSE_LIST);
    }

    private void showCreateNewTagAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_create_new_tag_message)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       savePersonalDetails(true);
                   }
               }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    private void fetchAllTags() {
        new GetAllTags().execute();
    }

    class GetAllTags extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mTagList = databaseHandler.getAllPayees();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mTagList != null && mTagList.size() > 0) {
                mAlreadyHaveTag.setVisibility(View.VISIBLE);
            }
            loadSharedPreferences();
        }
    }
}
