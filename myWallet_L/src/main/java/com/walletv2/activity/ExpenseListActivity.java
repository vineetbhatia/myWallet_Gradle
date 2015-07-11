package com.walletv2.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.walletv2.entity.AlarmDetails;
import com.walletv2.entity.Constants;
import com.walletv2.entity.ExpenseDetails;
import com.walletv2.entity.Utils;
import com.walletv2.services.ScheduleClient;
import com.walletv2.views.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Calendar;

public class ExpenseListActivity extends BaseActivity {
    private ExpenseListPagerAdapter mExpenseListPagerAdapter;
    private ViewPager mViewPager;
    private FrameLayout progressBar;
    private ArrayList<ExpenseDetails> unPaidExpenseList;
    private ArrayList<ExpenseDetails> paidExpenseList;
    private ExpenseListAdapter unpaidExpensePayeeListAdapter;
    private ExpenseListAdapter paidExpensePayeeListAdapter;
    private String payeeName;
    private GetExpenseListForAPayee getExpenseListForAPayee;
    private PerformItemMenuAction performItemMenuAction;
    private boolean isWalletList = true;
    private String eventName;
    private ScheduleClient scheduleClient;
    private AlarmDetails alarmDetails;
    private ShareActionProvider mShareActionProvider;
    private String shareText;
    private boolean mDataChanged;
    private SlidingTabLayout mTabLayout;
    private boolean isUnpaidExpenseListEmpty = false;
    private boolean isPaidExpenseListEmpty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payee_list);
        scheduleClient = new ScheduleClient(mActivity);
        scheduleClient.doBindService();

        initializeUiComponents();
        loadInitialData();

        getExpenseListForAPayee = new GetExpenseListForAPayee();
        getExpenseListForAPayee.execute();
    }

    private void initializeUiComponents() {
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabLayout = (SlidingTabLayout) findViewById(R.id.tab_layout);
        progressBar = (FrameLayout) findViewById(R.id.progress_bar);
    }

    @Override
    protected void onStop() {
        if (null != getExpenseListForAPayee)
            getExpenseListForAPayee.cancel(true);
        if (null != performItemMenuAction)
            performItemMenuAction.cancel(true);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        scheduleClient.doUnbindService();
        super.onDestroy();
    }

    private void loadInitialData() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            isWalletList = bundle.getBoolean(Constants.EXTRA_VALUE_IS_WALLET_LIST, true);
            payeeName = bundle.getString(Constants.EXTRA_VALUE_PAYEE_NAME);
            if (!isWalletList) {
                eventName = bundle.getString(Constants.EXTRA_VALUE_EVENT_NAME);
                payeeName = bundle.getString(Constants.EXTRA_VALUE_PAYER_NAME);
            }
            mActionBar.setTitle(payeeName);
            if (bundle.getBoolean(Constants.EXTRA_VALUE_IS_SHOW_PAID)) {
                mViewPager.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                mViewPager.setCurrentItem(2);
                            }
                        }, 500);
                        mViewPager.removeOnLayoutChangeListener(this);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.expense_list, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Log.i("MyWallet", "mShareActionProvider is created");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isWalletList || null == unPaidExpenseList || unPaidExpenseList.size() == 0)
            menu.removeItem(R.id.action_mark_all_as_paid);
        if (isWalletList || null == unPaidExpenseList || unPaidExpenseList.size() == 0)
            menu.removeItem(R.id.action_reckon_up_event);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_expense:
                Intent addExpenseIntent = new Intent(mActivity, AddExpenseActivity.class);
                if (isWalletList) {
                    addExpenseIntent.putExtra(Constants.EXTRA_VALUE_PAYEE_NAME, payeeName);
                } else {
                    addExpenseIntent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_EXPENSE, false);
                    addExpenseIntent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, eventName);
                    addExpenseIntent.putExtra(Constants.EXTRA_VALUE_PAYER_NAME, payeeName);
                }
                startActivityForResult(addExpenseIntent, Constants.REQUEST_CODE_ADD_EXPENSE_LIST);
                break;
            case R.id.action_reckon_up_event:
                Intent intent = new Intent(mActivity, ReckonUpActivity.class);
                intent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, eventName);
                startActivity(intent);
                break;
            case R.id.action_edit_payee:
                Intent editPayeeIntent = new Intent(mActivity, EditPayeeActivity.class);
                startActivity(editPayeeIntent);
                break;
            case R.id.action_mark_all_as_paid:
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.dialog_mark_all_as_paid_for_one_payee_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                new PerformItemMenuAction(R.id.action_mark_all_as_paid).execute();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                // Create the AlertDialog object and show it
                builder.create().show();
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

    private void setShareIntent() {
        if (isWalletList)
            setWalletShareText();
        else
            setEventShareText();
        Log.i("MyWallet", "setShareIntent is called");
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");
        if (mShareActionProvider != null) {
            Log.i("MyWallet", "mShareActionProvider is not null");
            Log.i("MyWallet", "mShareActionProvider ShareText-\n" + shareText);
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void setWalletShareText() {
        shareText = "After reckoning up:";
        int amount = 0;
        shareText += "\n" + "I will get an amount of Rs xxx in total from " + payeeName + ".";
        for (ExpenseDetails expenseDetails : unPaidExpenseList) {
            int amountForThisExpense = Integer.parseInt(expenseDetails.getAmount()) / Utils.getPayeeCountFromPayees(expenseDetails.getPayeesList());
            if (amountForThisExpense < 0)
                shareText += "\n" + "- I will give " + payeeName + " Rs " + String.valueOf(Math.abs(amountForThisExpense))
                        + " for " + expenseDetails.getDescription() + " spent at " + Utils.getFormattedDateFromMillies(expenseDetails.getDate());
            else
                shareText += "\n" + "- " + payeeName + " will give me Rs " + String.valueOf(Math.abs(amountForThisExpense))
                        + " for " + expenseDetails.getDescription() + " spent at " + Utils.getFormattedDateFromMillies(expenseDetails.getDate());

            amount += amountForThisExpense;
        }
        if (amount < 0)
            shareText = shareText.replaceFirst("I", payeeName);
        shareText = shareText.replaceFirst("xxx", String.valueOf(Math.abs(amount)));

        shareText += "\n-shared from MyWallet";
    }

    public void setEventShareText() {
        shareText = payeeName + "'s expenses for " + eventName + ":";
        int amount = 0;
        shareText += "\n" + payeeName + " spent an amount of Rs xxx in total";
        for (ExpenseDetails expenseDetails : unPaidExpenseList) {
            int amountForThisExpense = Integer.parseInt(expenseDetails.getAmount());
            if (amountForThisExpense < 0)
                shareText += "\n" + "- " + expenseDetails.getPayeesList() + " spent an amount of Rs " + String.valueOf(Math.abs(amountForThisExpense)) + " for " + payeeName
                        + " for " + expenseDetails.getDescription() + " at " + Utils.getFormattedDateFromMillies(expenseDetails.getDate());
            else
                shareText += "\n" + "- " + payeeName + " spent an amount of Rs " + String.valueOf(Math.abs(amountForThisExpense)) + " for " + expenseDetails.getPayeesList()
                        + " for " + expenseDetails.getDescription() + " at " + Utils.getFormattedDateFromMillies(expenseDetails.getDate());

            amount += amountForThisExpense;
        }
        shareText = shareText.replaceFirst("xxx", String.valueOf(amount));

        shareText += "\n\nExpenses spent on " + payeeName + " for " + eventName + ":";
        int amountSpentOn = 0;
        shareText += "\n" + "An amount of Rs xxx was spent in total on " + payeeName;
        for (ExpenseDetails expenseDetails : paidExpenseList) {
            int amountForThisExpense = Integer.parseInt(expenseDetails.getAmount()) / Utils.getPayeeCountFromPayees(expenseDetails.getPayeesList());
            shareText += "\n" + "- " + "An amount of Rs " + String.valueOf(Math.abs(amountForThisExpense)) + " was spent on " + payeeName + " by " + expenseDetails.getPayersList()
                    + " for " + expenseDetails.getDescription() + " at " + Utils.getFormattedDateFromMillies(expenseDetails.getDate());

            amountSpentOn += amountForThisExpense;
        }
        shareText = shareText.replaceFirst("xxx", String.valueOf(amountSpentOn));

        shareText += "\n-shared from MyWallet";
    }

    private void launchEditExpenseActivity(String expenseDate) {
        Intent intent = new Intent(mActivity, AddExpenseActivity.class);
        intent.putExtra(Constants.EXTRA_VALUE_EXPENSE_DATE, expenseDate);
        if (!isWalletList) {
            intent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_EXPENSE, false);
            intent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, eventName);
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_ADD_EXPENSE_LIST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_ADD_EXPENSE_LIST && resultCode == Activity.RESULT_OK) {
            mDataChanged = true;
            getExpenseListForAPayee = new GetExpenseListForAPayee();
            getExpenseListForAPayee.execute();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mDataChanged) {
            setResult(Activity.RESULT_OK);
            finish();
        } else
            super.onBackPressed();
    }

    class GetExpenseListForAPayee extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (isWalletList) {
                unPaidExpenseList = databaseHandler.getUnPaidExpenseListForAPayee(payeeName);
                paidExpenseList = databaseHandler.getPaidExpenseListForAPayee(payeeName);
            } else {
                unPaidExpenseList = databaseHandler.getEventExpenseListForAPayerForAEvent(eventName, payeeName);
                paidExpenseList = databaseHandler.getEventExpenseListForAPayeeForAEvent(eventName, payeeName);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (null == unpaidExpensePayeeListAdapter)
                unpaidExpensePayeeListAdapter = new ExpenseListAdapter(unPaidExpenseList, true);
            else {
                unpaidExpensePayeeListAdapter.setExpenseList(unPaidExpenseList);
                unpaidExpensePayeeListAdapter.notifyDataSetChanged();
            }
            if (null == paidExpensePayeeListAdapter)
                paidExpensePayeeListAdapter = new ExpenseListAdapter(paidExpenseList, false);
            else {
                paidExpensePayeeListAdapter.setExpenseList(paidExpenseList);
                paidExpensePayeeListAdapter.notifyDataSetChanged();
            }
            if (null == mExpenseListPagerAdapter || (isUnpaidExpenseListEmpty && unPaidExpenseList.size() > 0)
                    || (!isUnpaidExpenseListEmpty && unPaidExpenseList.size() == 0)
                    || (isPaidExpenseListEmpty && paidExpenseList.size() > 0)
                    || (!isPaidExpenseListEmpty && paidExpenseList.size() == 0)) {
                setUpViewPagerAndTabs();
            } else
                mExpenseListPagerAdapter.notifyDataSetChanged();
            setShareIntent();
            invalidateOptionsMenu();
            isUnpaidExpenseListEmpty = unPaidExpenseList.size() == 0;
            isPaidExpenseListEmpty = paidExpenseList.size() == 0;
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setUpViewPagerAndTabs() {
        mExpenseListPagerAdapter = new ExpenseListPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mExpenseListPagerAdapter);

        mTabLayout.setViewPager(mViewPager);

    }

    public class ExpenseListPagerAdapter extends FragmentStatePagerAdapter {
        public ExpenseListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new ExpenseListSectionFragment();
            Bundle args = new Bundle();
            args.putInt(ExpenseListSectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (isWalletList) {
                switch (position) {
                    case 0:
                        return mActivity.getString(R.string.unpaid_list);
                    case 1:
                        return mActivity.getString(R.string.paid_list);
                }
            } else {
                switch (position) {
                    case 0:
                        return mActivity.getString(R.string.expense_list_title);
                    case 1:
                        return mActivity.getString(R.string.included_list_title);
                }
            }
            return null;
        }
    }

    public class ExpenseListSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = null;
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                int textResId;
                if (isWalletList)
                    textResId = R.string.no_unpaid_expense_msg;
                else
                    textResId = R.string.no_payer_expense_msg;

                rootView = getDefaultListFragmentView(inflater, container, unPaidExpenseList, unpaidExpensePayeeListAdapter, textResId);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                int textResId;
                if (isWalletList)
                    textResId = R.string.no_paid_expense_message;
                else
                    textResId = R.string.no_included_event_expense_msg;

                rootView = getDefaultListFragmentView(inflater, container, paidExpenseList, paidExpensePayeeListAdapter, textResId);
            }
            return rootView;
        }

        private View getDefaultListFragmentView(LayoutInflater inflater, ViewGroup container, ArrayList pPayeeList,
                                                ExpenseListAdapter pListAdapter, int textResId) {
            View rootView;
            if (pPayeeList != null && pPayeeList.size() > 0) {
                rootView = inflater.inflate(R.layout.fragment_section_list_view, container, false);
                RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.section_list_view);
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                listView.setHasFixedSize(true);

                // use a linear layout manager
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
                listView.setLayoutManager(mLayoutManager);

                listView.setAdapter(pListAdapter);
            } else {
                rootView = inflater.inflate(R.layout.fragment_no_expense, container, false);
                TextView textView = (TextView) rootView.findViewById(R.id.section_text_view);
                textView.setText(textResId);
            }
            return rootView;
        }
    }

    class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ViewHolder> {
        private final LayoutInflater mInflater;
        private ArrayList<ExpenseDetails> expenseList;
        private final boolean isUnpaidList;

        public ExpenseListAdapter(ArrayList<ExpenseDetails> expenseList, boolean isUnpaidList) {
            this.expenseList = expenseList;
            this.isUnpaidList = isUnpaidList;
            mInflater = LayoutInflater.from(mActivity);
        }

        public void setExpenseList(ArrayList<ExpenseDetails> expenseList) {
            this.expenseList = expenseList;
        }

        @Override
        public int getItemCount() {
            return expenseList.size();
        }

        private ExpenseDetails getItem(int position) {
            return expenseList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View view = mInflater.inflate(R.layout.item_expense_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final ExpenseDetails expenseDetails = getItem(position);
            int payeeCount = Utils.getPayeeCountFromPayees(expenseDetails.getPayeesList());
            holder.textViewDate.setText(Utils.getFormattedDateFromMillies(expenseDetails.getDate()));
            holder.textViewDescription.setText(expenseDetails.getDescription());
            if (isWalletList) {
                String amount = expenseDetails.getAmount();
                if (payeeCount > 1)
                    amount = String.valueOf(Integer.parseInt(amount) / payeeCount);
                holder.textViewAmount.setText(amount);
                holder.textViewOriginalPayeeList.setPaintFlags(holder.textViewOriginalPayeeList.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                if ((payeeCount == 1 && expenseDetails.getPayersList() != null && Utils.getPayeeCountFromPayees(expenseDetails.getPayersList()) == 1)
                        || expenseDetails.getPayersList() == null || expenseDetails.getPayersList().equals("")) {
                    holder.linearLayoutOriginal.setVisibility(View.GONE);
                    holder.textViewPayeeList.setVisibility(View.VISIBLE);
                    holder.textViewPayeeList.setText(expenseDetails.getPayeesList());
                } else {
                    if (payeeCount > 1 && expenseDetails.getPayersList().equalsIgnoreCase(expenseDetails.getPayeesList()))
                        holder.textViewPayeeList.setVisibility(View.GONE);
                    else {
                        holder.textViewOriginalPayeeList.setPaintFlags(holder.textViewOriginalPayeeList.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        holder.textViewPayeeList.setVisibility(View.VISIBLE);
                        holder.textViewPayeeList.setText(expenseDetails.getPayeesList());
                    }
                    holder.linearLayoutOriginal.setVisibility(View.VISIBLE);
                    holder.textViewOriginalAmount.setText(expenseDetails.getEventName());
                    holder.textViewOriginalPayeeList.setText(expenseDetails.getPayersList());
                }
            } else {
                holder.textViewAmount.setText(expenseDetails.getAmount());
                holder.linearLayoutOriginal.setVisibility(View.GONE);
                holder.textViewPayeeList.setVisibility(View.VISIBLE);
                holder.textViewPayeeList.setText(expenseDetails.getPayeesList());
            }
            holder.imageButtonItemMenu.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(view, isUnpaidList, getItem(position));
                    Log.i("ExpenseList", "Expense Clicked: " + getItem(position).toString());
                }
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewDate;
            TextView textViewAmount;
            TextView textViewDescription;
            TextView textViewPayeeList;
            TextView textViewOriginalAmount;
            TextView textViewOriginalPayeeList;
            ImageView imageButtonItemMenu;
            LinearLayout linearLayoutOriginal;

            public ViewHolder(View convertView) {
                super(convertView);
                textViewDate = (TextView) convertView.findViewById(R.id.text_view_expense_date);
                textViewAmount = (TextView) convertView.findViewById(R.id.text_view_expense_amount);
                textViewDescription = (TextView) convertView.findViewById(R.id.text_view_expense_description);
                textViewPayeeList = (TextView) convertView.findViewById(R.id.text_view_expense_payee_list);
                textViewOriginalAmount = (TextView) convertView.findViewById(R.id.text_view_expense_original_amount);
                textViewOriginalPayeeList = (TextView) convertView.findViewById(R.id.text_view_expense_original_payees);
                imageButtonItemMenu = (ImageView) convertView.findViewById(R.id.image_button_item_menu);
                linearLayoutOriginal = (LinearLayout) convertView.findViewById(R.id.original_details_layout);
            }
        }
    }

    private void showPopup(View view, final boolean isUnpaidPopUp, final ExpenseDetails expenseDetails) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit_expense:
                        launchEditExpenseActivity(expenseDetails.getDate());
                        break;
                    case R.id.action_mark_as_paid:
                    case R.id.action_mark_as_unpaid:
                        performItemMenuAction = new PerformItemMenuAction(item.getItemId(), expenseDetails, isUnpaidPopUp);
                        performItemMenuAction.execute();
                        break;
                    case R.id.action_delete_expense:
                        new DeleteExpenseDialog(expenseDetails, isUnpaidPopUp).show(getSupportFragmentManager(), "delete_expense");
                        break;
                    case R.id.action_remind_me:
                        setAlarmDetails(expenseDetails, payeeName);
                        (new CustomDateTimeDialog()).show(getSupportFragmentManager(), "set_alarm_reminder");
                        break;
                    case R.id.action_remind_payee:
                        Utils.remindAPayeeAboutOneExpenseViaSms(mActivity, expenseDetails);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        int menuRes;
        if (isWalletList) {
            if (isUnpaidPopUp) {
                menuRes = R.menu.item_unpaid_expense_list;
            } else
                menuRes = R.menu.item_paid_expense_list;
        } else
            menuRes = R.menu.item_event_expense_list;
        inflater.inflate(menuRes, popup.getMenu());
        if (isWalletList && isUnpaidPopUp) {
            popup.getMenu().findItem(R.id.action_remind_payee).setTitle("Remind " + payeeName);
        }
        popup.show();
    }

    private class PerformItemMenuAction extends AsyncTask<Void, Void, Void> {
        private final int actionId;
        private ExpenseDetails expenseDetails;
        private boolean isUnpaid;

        public PerformItemMenuAction(int actionId) {
            this.actionId = actionId;
        }

        public PerformItemMenuAction(int actionId, ExpenseDetails expenseDetails, boolean isUnpaid) {
            this.expenseDetails = expenseDetails;
            this.actionId = actionId;
            this.isUnpaid = isUnpaid;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (actionId) {
                case R.id.action_mark_as_paid:
                    databaseHandler.markAsPaidForOnePayee(expenseDetails, payeeName);
                    mDataChanged = true;
                    break;
                case R.id.action_mark_as_unpaid:
                    databaseHandler.markAsUnPaidForOnePayee(expenseDetails);
                    mDataChanged = true;
                    break;
                case R.id.action_delete_expense:
                    if (isWalletList) {
                        if (isUnpaid)
                            databaseHandler.deleteUnPaidExpense(expenseDetails);
                        else
                            databaseHandler.deletePaidExpense(expenseDetails);
                    } else
                        databaseHandler.deleteEventExpenseForAPayerForAEvent(expenseDetails);
                    mDataChanged = true;
                    break;
                case R.id.action_mark_all_as_paid:
                    databaseHandler.markAllAsPaidForOnePayee(payeeName);
                    mDataChanged = true;
                    break;
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getExpenseListForAPayee = new GetExpenseListForAPayee();
            getExpenseListForAPayee.execute();
        }
    }

    class DeleteExpenseDialog extends DialogFragment {
        private final ExpenseDetails expenseDetails;
        private final boolean isUnpaid;

        public DeleteExpenseDialog(ExpenseDetails expenseDetails, boolean isUnpaid) {
            this.expenseDetails = expenseDetails;
            this.isUnpaid = isUnpaid;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.dialog_delete_expense_message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    performItemMenuAction = new PerformItemMenuAction(R.id.action_delete_expense, expenseDetails, isUnpaid);
                    performItemMenuAction.execute();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    class CustomDateTimeDialog extends DialogFragment {
        private Calendar customDate;

        @NonNull
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
                            setAlarmReminder(customDate);
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

    private void setAlarmReminder(Calendar date) {
        alarmDetails.setCalender(date);
        scheduleClient.setAlarmForNotification(alarmDetails);
        // Notify the user what they just did
        Toast.makeText(mActivity, "Notification set for: " + Utils.getFormattedDateFromMillies(date.getTimeInMillis() + ""), Toast.LENGTH_SHORT).show();
    }

    private void setAlarmDetails(ExpenseDetails expenseDetails, String payee) {
        String notificationContent;
        int payeeCount = Utils.getPayeeCountFromPayees(expenseDetails.getPayeesList());
        if (expenseDetails.getAmount().startsWith("-")) {
            notificationContent = "Hey, remember *payee* paid *amount* bucks*tag**date**description*. You might wanna remind your friend that you will give it back.";
            notificationContent = notificationContent.replace("*payee*", payee);
            notificationContent = notificationContent.replace("*tag*", " for you ");
        } else {
            notificationContent = "Hey, remember *payee* paid *amount* bucks*tag**date**description*. You might wanna remind your friend to give it back.";
            notificationContent = notificationContent.replace("*tag*", " for " + payee) + " ";
            notificationContent = notificationContent.replace("*payee*", "you");
        }
        if (payeeCount > 1) {
            notificationContent = notificationContent.replace("*amount*", Math.abs(Integer.parseInt(expenseDetails.getAmount()) / payeeCount) + "");
        } else {
            notificationContent = notificationContent.replace("*amount*", expenseDetails.getAmount().replace("-", ""));
        }
        notificationContent = notificationContent.replace("*date*", " on " + Utils.getFormattedDateFromMillies(expenseDetails.getDate()));
        notificationContent = notificationContent.replace("*description*", " for " + expenseDetails.getDescription());
        alarmDetails = new AlarmDetails();
        alarmDetails.setNotificationTitle("It's about money!!!");
        alarmDetails.setNotificationContent(notificationContent);
        alarmDetails.setExpensePayee(payee);
    }
}
