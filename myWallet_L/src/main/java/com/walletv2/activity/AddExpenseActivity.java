package com.walletv2.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.walletv2.entity.Constants;
import com.walletv2.entity.ExpenseDetails;
import com.walletv2.entity.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AddExpenseActivity extends BaseActivity {
    private EditText editTextAmount = null;
    private EditText editTextDescription = null;
    private GridView gridViewSelectedPayees = null;
    private GridView gridViewSelectedPayers = null;
    private GridView gridViewAllPayees = null;
    private FrameLayout layoutPayeesParent = null;
    private FrameLayout layoutPayersParent = null;
    private ArrayList<String> allPayeeList = null;
    private ArrayList<String> selectedPayeesList = null;
    private ArrayList<String> selectedPayersList = null;
    private ProgressBar progressBar = null;
    private TextView textViewNoPayee = null;
    private TextView textViewAddPayeeHint = null;
    private TextView textViewAddPayerHint = null;
    private PayeeGridAdapter allPayeeGridAdapter = null;
    private PayeeGridAdapter selectedPayeesGridAdapter = null;
    private PayeeGridAdapter selectedPayersGridAdapter = null;
    private ExpenseDetails expenseDetails = null;
    private GetAllPayees getAllPayees = null;
    private Calendar customDate;
    private boolean isWalletExpense = true;
    private boolean isEditMode = false;
    private boolean hasPayeeListFocus = true;
    private CustomDateTimeDialog customDateTimeDialog = null;
    private FloatingActionButton mAddExpenseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        initializeUiComponents();
        loadInitialData();
        initializeListeners();
        getAllPayees = new GetAllPayees();
        getAllPayees.execute();
    }

    private void initializeUiComponents() {
        editTextAmount = (EditText) findViewById(R.id.edit_text_amount);
        editTextDescription = (EditText) findViewById(R.id.edit_text_description);
        gridViewAllPayees = (GridView) findViewById(R.id.grid_view_all_payees);
        gridViewSelectedPayees = (GridView) findViewById(R.id.grid_view_selected_payees);
        gridViewSelectedPayers = (GridView) findViewById(R.id.grid_view_selected_payers);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        textViewNoPayee = (TextView) findViewById(R.id.text_view_no_payee);
        textViewAddPayeeHint = (TextView) findViewById(R.id.text_view_add_payee_hint);
        textViewAddPayerHint = (TextView) findViewById(R.id.text_view_add_payer_hint);
        layoutPayeesParent = (FrameLayout) findViewById(R.id.layout_payees_parent);
        layoutPayersParent = (FrameLayout) findViewById(R.id.layout_payers_parent);
        mAddExpenseButton = (FloatingActionButton) findViewById(R.id.btn_save_expense);
        customDateTimeDialog = new CustomDateTimeDialog();
    }

    @Override
    protected void onStop() {
        if (null != getAllPayees)
            getAllPayees.cancel(true);
        super.onStop();
    }

    private void loadInitialData() {
        expenseDetails = new ExpenseDetails();
        Bundle bundle = getIntent().getExtras();
        try {
            if (bundle != null) {
                isWalletExpense = bundle.getBoolean(Constants.EXTRA_VALUE_IS_WALLET_EXPENSE, true);
                String expenseDate = bundle.getString(Constants.EXTRA_VALUE_EXPENSE_DATE);
                String payeeName = bundle.getString(Constants.EXTRA_VALUE_PAYEE_NAME);
                String payerName = bundle.getString(Constants.EXTRA_VALUE_PAYER_NAME);
                String eventName = bundle.getString(Constants.EXTRA_VALUE_EVENT_NAME);
                if (null != expenseDate && !expenseDate.equals("")) {
                    isEditMode = true;
                    if (isWalletExpense) {
                        expenseDetails = databaseHandler.getUnPaidExpenseFromDate(expenseDate);
                    } else {
                        expenseDetails = databaseHandler.getEventExpenseFromDate(expenseDate);
                    }
                    selectedPayeesList = new ArrayList<>(Arrays.asList(expenseDetails.getPayeesList().split(", ")));
                    if (!isWalletExpense) {
                        selectedPayersList = new ArrayList<>(Arrays.asList(expenseDetails.getPayersList().split(", ")));
                    }
                    editTextAmount.setText(expenseDetails.getAmount());
                    editTextDescription.setText(expenseDetails.getDescription());
                } else {
                    if (null != payeeName && !payeeName.equals("")) {
                        selectedPayeesList = new ArrayList<>();
                        selectedPayeesList.add(payeeName);
                    }
                    if (!isWalletExpense) {
                        expenseDetails.setEventName(eventName);
                        if (null != payerName && !payerName.equals("")) {
                            selectedPayersList = new ArrayList<>();
                            selectedPayersList.add(payerName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeListeners() {
        gridViewAllPayees.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ToggleButton toggleButton = (ToggleButton) view;
                toggleButton.setChecked(!toggleButton.isChecked());
                if (toggleButton.isChecked()) {
                    if (hasPayeeListFocus) {
                        addAPayeeInSelectedPayeesList(allPayeeList.get(position));
                        layoutPayeesParent.requestFocus();
                    } else {
                        addAPayerInSelectedPayersList(allPayeeList.get(position));
                        layoutPayersParent.requestFocus();
                    }
                } else {
                    if (hasPayeeListFocus) {
                        removeAPayeeFromSelectedPayeesList(allPayeeList.get(position));
                        layoutPayeesParent.requestFocus();
                    } else {
                        removeAPayerFromSelectedPayersList(allPayeeList.get(position));
                        layoutPayersParent.requestFocus();
                    }
                }
            }
        });
        gridViewSelectedPayees.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                removeAPayeeFromSelectedPayeesList(selectedPayeesList.get(position));
            }
        });
        gridViewSelectedPayees.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                layoutPayeesParent.requestFocus();
                return false;
            }
        });
        layoutPayeesParent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    dismissKeyboard(view);
                }
            }
        });
        if (!isWalletExpense) {
            layoutPayersParent.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        hasPayeeListFocus = false;
                        dismissKeyboard(view);
                    } else
                        hasPayeeListFocus = true;
                    if (null != allPayeeGridAdapter)
                        allPayeeGridAdapter.notifyDataSetInvalidated();
                }
            });
            gridViewSelectedPayers.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    removeAPayerFromSelectedPayersList(selectedPayersList.get(position));
                }
            });
            gridViewSelectedPayers.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    layoutPayersParent.requestFocus();
                    return false;
                }
            });
        }
        mAddExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAnExpense();
            }
        });
        textViewNoPayee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, EditPayeeActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_EDIT_PAYEE_LIST);
            }
        });

    }

    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_EDIT_PAYEE_LIST && resultCode == Activity.RESULT_OK) {
            getAllPayees = new GetAllPayees();
            getAllPayees.execute();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    class GetAllPayees extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            gridViewAllPayees.setVisibility(View.GONE);
            gridViewSelectedPayees.setVisibility(View.GONE);
            gridViewSelectedPayers.setVisibility(View.GONE);
            textViewNoPayee.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            textViewAddPayeeHint.setVisibility(View.VISIBLE);
            textViewAddPayerHint.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            allPayeeList = databaseHandler.getAllPayees();
            if (null != selectedPayeesGridAdapter) {
                ArrayList<String> removalPayeeList = new ArrayList<>();
                if (!allPayeeList.containsAll(selectedPayeesList)) {
                    for (String selectedPayee : selectedPayeesList) {
                        if (!allPayeeList.contains(selectedPayee)) {
                            removalPayeeList.add(selectedPayee);
                        }
                    }
                    selectedPayeesList.removeAll(removalPayeeList);
                }
            }
            if (!isWalletExpense && null != selectedPayersGridAdapter) {
                ArrayList<String> removalPayeeList = new ArrayList<>();
                if (!allPayeeList.containsAll(selectedPayersList)) {
                    for (String selectedPayee : selectedPayersList) {
                        if (!allPayeeList.contains(selectedPayee)) {
                            removalPayeeList.add(selectedPayee);
                        }
                    }
                    selectedPayersList.removeAll(removalPayeeList);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            if (null != allPayeeList && allPayeeList.size() > 0) {
                gridViewAllPayees.setVisibility(View.VISIBLE);
                textViewNoPayee.setVisibility(View.GONE);
                if (null == allPayeeGridAdapter) {
                    allPayeeGridAdapter = new PayeeGridAdapter(allPayeeList);
                    gridViewAllPayees.setAdapter(allPayeeGridAdapter);
                } else {
                    allPayeeGridAdapter.setPayeeList(allPayeeList);
                    allPayeeGridAdapter.notifyDataSetChanged();
                }
            } else {
                textViewNoPayee.setVisibility(View.VISIBLE);
            }
            if (null != selectedPayeesList && selectedPayeesList.size() > 0) {
                gridViewSelectedPayees.setVisibility(View.VISIBLE);
                textViewAddPayeeHint.setVisibility(View.GONE);
                if (null == selectedPayeesGridAdapter) {
                    selectedPayeesGridAdapter = new PayeeGridAdapter(selectedPayeesList);
                    gridViewSelectedPayees.setAdapter(selectedPayeesGridAdapter);
                }
            } else {
                textViewAddPayeeHint.setVisibility(View.VISIBLE);
                if (null == selectedPayeesGridAdapter) {
                    selectedPayeesList = new ArrayList<>();
                    selectedPayeesGridAdapter = new PayeeGridAdapter(selectedPayeesList);
                    gridViewSelectedPayees.setAdapter(selectedPayeesGridAdapter);
                }
            }
            selectedPayeesGridAdapter.setPayeeList(selectedPayeesList);
            selectedPayeesGridAdapter.notifyDataSetChanged();
            if (!isWalletExpense) {
                layoutPayersParent.setVisibility(View.VISIBLE);
                if (null != selectedPayersList && selectedPayersList.size() > 0) {
                    gridViewSelectedPayers.setVisibility(View.VISIBLE);
                    textViewAddPayerHint.setVisibility(View.GONE);
                    if (null == selectedPayersGridAdapter) {
                        selectedPayersGridAdapter = new PayeeGridAdapter(selectedPayersList);
                        gridViewSelectedPayers.setAdapter(selectedPayersGridAdapter);
                    }
                } else {
                    textViewAddPayerHint.setVisibility(View.VISIBLE);
                    if (null == selectedPayersGridAdapter) {
                        selectedPayersList = new ArrayList<>();
                        selectedPayersGridAdapter = new PayeeGridAdapter(selectedPayersList);
                        gridViewSelectedPayers.setAdapter(selectedPayersGridAdapter);
                    }
                }
                selectedPayersGridAdapter.setPayeeList(selectedPayersList);
                selectedPayersGridAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_expense, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (null == selectedPayeesList || selectedPayeesList.size() == 0)
            menu.removeItem(R.id.action_clear_all_selected_payee);
        if (isWalletExpense)
            menu.removeItem(R.id.action_add_all_previous_payees);
        if (getSetCustomDate() > 0) {
            menu.findItem(R.id.action_set_custom_date).setVisible(false);
            menu.findItem(R.id.action_remove_custom_date).setVisible(true);
        } else {
            menu.findItem(R.id.action_remove_custom_date).setVisible(false);
            menu.findItem(R.id.action_set_custom_date).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_custom_date:
                customDateTimeDialog.show(getSupportFragmentManager(), "set_custom_date_time");
                break;
            case R.id.action_remove_custom_date:
                customDate.setTimeInMillis(0);
                break;
            case R.id.action_add_new_payee:
                Intent intent = new Intent(mActivity, EditPayeeActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_EDIT_PAYEE_LIST);
                break;
            case R.id.action_clear_all_selected_payee:
                clearSelectedPayees();
                break;
            case R.id.action_add_all_previous_payees:
                for (String payee : databaseHandler.getAllPayeesForAnEvent(expenseDetails.getEventName())) {
                    if (!selectedPayeesList.contains(payee))
                        selectedPayeesList.add(payee);
                }
                if (selectedPayeesList.size() >= 1) {
                    textViewAddPayeeHint.setVisibility(View.GONE);
                    gridViewSelectedPayees.setVisibility(View.VISIBLE);
                }
                selectedPayeesGridAdapter.notifyDataSetChanged();
                allPayeeGridAdapter.notifyDataSetInvalidated();
                break;
            case R.id.action_settings:
                Intent preferencesIntent = new Intent(mActivity, SettingsActivity.class);
                startActivity(preferencesIntent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearSelectedPayees() {
        selectedPayeesList.clear();
        selectedPayeesGridAdapter.notifyDataSetChanged();
        textViewAddPayeeHint.setVisibility(View.VISIBLE);
        gridViewSelectedPayees.setVisibility(View.GONE);
    }

    private void addAPayeeInSelectedPayeesList(String payee) {
        selectedPayeesList.add(payee);
        if (selectedPayeesList.size() == 1) {
            textViewAddPayeeHint.setVisibility(View.GONE);
            gridViewSelectedPayees.setVisibility(View.VISIBLE);
        }
        selectedPayeesGridAdapter.notifyDataSetChanged();
        allPayeeGridAdapter.notifyDataSetInvalidated();
    }

    private void addAPayerInSelectedPayersList(String payer) {
        if (selectedPayersList.size() == 1) {
            Toast.makeText(mActivity, "Only one payer can be added in payers list", Toast.LENGTH_SHORT).show();
            selectedPayersGridAdapter.notifyDataSetChanged();
            allPayeeGridAdapter.notifyDataSetInvalidated();
            return;
        }
        selectedPayersList.add(payer);
        if (selectedPayersList.size() == 1) {
            textViewAddPayerHint.setVisibility(View.GONE);
            gridViewSelectedPayers.setVisibility(View.VISIBLE);
        }
        selectedPayersGridAdapter.notifyDataSetChanged();
        allPayeeGridAdapter.notifyDataSetInvalidated();
    }

    private void removeAPayeeFromSelectedPayeesList(String payee) {
        selectedPayeesList.remove(payee);
        selectedPayeesGridAdapter.notifyDataSetChanged();
        allPayeeGridAdapter.notifyDataSetInvalidated();
        if (selectedPayeesList.size() == 0) {
            textViewAddPayeeHint.setVisibility(View.VISIBLE);
            gridViewSelectedPayees.setVisibility(View.GONE);
        }
    }

    private void removeAPayerFromSelectedPayersList(String payer) {
        selectedPayersList.remove(payer);
        selectedPayersGridAdapter.notifyDataSetChanged();
        allPayeeGridAdapter.notifyDataSetInvalidated();
        if (selectedPayersList.size() == 0) {
            textViewAddPayerHint.setVisibility(View.VISIBLE);
            gridViewSelectedPayers.setVisibility(View.GONE);
        }
    }

    private boolean validateExpenseDetails() {
        String toastString = "";
        boolean flag = true;
        if (expenseDetails.getAmount().equals("")) {
            flag = false;
            toastString = toastString + "Please enter the amount.";
        } else if (Integer.parseInt(expenseDetails.getAmount()) == 0) {
            flag = false;
            toastString = toastString + "Please enter a valid amount.";
        }
        if (expenseDetails.getPayeesList().equals("")) {
            flag = false;
            toastString = toastString + "\nPlease add at least one payee.";
        }
        if (!isWalletExpense && expenseDetails.getPayersList().equals("")) {
            flag = false;
            toastString = toastString + "\nPlease add at least one payer.";
        }
        if (!toastString.trim().equals("")) {
            Toast.makeText(mActivity, toastString.trim(), Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    private void addAnExpense() {
        try {
            expenseDetails.setAmount(editTextAmount.getText().toString());
            expenseDetails.setDescription(editTextDescription.getText().toString().trim());
            if (null != selectedPayeesList && selectedPayeesList.size() > 0) {
                String payeeListString = selectedPayeesList.toString().trim().replace("[", "");
                payeeListString = payeeListString.replace("]", "");
                expenseDetails.setPayeesList(payeeListString);
                expenseDetails.setPayersList(payeeListString);
            } else {
                expenseDetails.setPayeesList("");
                expenseDetails.setPayersList("");
            }
            if (!isWalletExpense)
                if (selectedPayersList != null && selectedPayersList.size() > 0) {
                    String payerListString = selectedPayersList.toString().trim().replace("[", "");
                    payerListString = payerListString.replace("]", "");
                    expenseDetails.setPayersList(payerListString);
                } else {
                    expenseDetails.setPayersList("");
                }
            else
                expenseDetails.setEventName(editTextAmount.getText().toString());
            if (validateExpenseDetails()) {
                if (isEditMode) {
                    if (isWalletExpense)
                        databaseHandler.updateUnPaidExpenseFromDate(expenseDetails, "" + getSetCustomDate());
                    else
                        databaseHandler.updateEventExpenseFromDate(expenseDetails, "" + getSetCustomDate());
                } else {
                    if (getSetCustomDate() == 0)
                        expenseDetails.setDate("" + System.currentTimeMillis());
                    else
                        expenseDetails.setDate("" + getSetCustomDate());
                    if (isWalletExpense)
                        databaseHandler.insertExpenseData(expenseDetails);
                    else
                        databaseHandler.insertEventExpenseData(expenseDetails);
                }
                Log.i("MyWallet",
                        "New Expense Details: " + expenseDetails.toString() + "\nCustomDate: " + Utils.getFormattedDateFromMillies("" + getSetCustomDate()));
                setResult(Activity.RESULT_OK);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getSetCustomDate() {
        try {
            if (null == customDate)
                return 0;
            else
                return customDate.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    class CustomDateTimeDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View inflateView = inflater.inflate(R.layout.dialog_custom_date_time, null);
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog
            // layout
            final DatePicker datePicker = (DatePicker) inflateView.findViewById(R.id.dp_add_expense_custom_date);
            datePicker.setCalendarViewShown(false);
            final TimePicker timePicker = (TimePicker) inflateView.findViewById(R.id.tp_add_expense_custom_time);
            builder.setTitle(R.string.dialog_custom_date_time_title).setView(inflateView)
                    // Add action buttons
                    .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            customDate = Calendar.getInstance();
                            customDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
                                    timePicker.getCurrentMinute(), 0);
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            return builder.create();
        }
    }

    class PayeeGridAdapter extends BaseAdapter {
        private ArrayList<String> payeeList;

        public PayeeGridAdapter(ArrayList<String> payeeList) {
            this.payeeList = payeeList;
        }

        public void setPayeeList(ArrayList<String> payeeList) {
            this.payeeList = payeeList;
        }

        @Override
        public int getCount() {
            return payeeList.size();
        }

        @Override
        public String getItem(int position) {
            return payeeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ToggleButton toggleButton;
            if (convertView == null) {
                toggleButton = new ToggleButton(mActivity);
                toggleButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                toggleButton.setSingleLine();
                toggleButton.setEllipsize(TruncateAt.END);
                toggleButton.setFocusable(false);
                toggleButton.setClickable(false);
                convertView = toggleButton;
            } else {
                toggleButton = (ToggleButton) convertView;
            }
            String tag = getItem(position);
            toggleButton.setText(tag);
            toggleButton.setTextOn(tag);
            toggleButton.setTextOff(tag);
            if (isWalletExpense || hasPayeeListFocus) {
                if (null == selectedPayeesList || selectedPayeesList.size() == 0)
                    toggleButton.setChecked(false);
                else
                    toggleButton.setChecked(selectedPayeesList.contains(getItem(position)));
            } else {
                if (null == selectedPayersList || selectedPayersList.size() == 0)
                    toggleButton.setChecked(false);
                else
                    toggleButton.setChecked(selectedPayersList.contains(getItem(position)));
            }
            return convertView;
        }
    }
}
