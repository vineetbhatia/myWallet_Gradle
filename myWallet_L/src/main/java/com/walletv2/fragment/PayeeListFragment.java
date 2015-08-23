package com.walletv2.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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

import com.walletv2.activity.EditPayeeActivity;
import com.walletv2.activity.ExpenseListActivity;
import com.walletv2.activity.R;
import com.walletv2.activity.ReckonUpActivity;
import com.walletv2.activity.SettingsActivity;
import com.walletv2.entity.AlarmDetails;
import com.walletv2.entity.Constants;
import com.walletv2.entity.PayeeDetails;
import com.walletv2.entity.Utils;
import com.walletv2.services.ScheduleClient;
import com.walletv2.views.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Calendar;

public class PayeeListFragment extends BaseFragment {

    private PayeeListPagerAdapter mPayeeListPagerAdapter;
    private ViewPager mViewPager;
    private FrameLayout progressBar;
    private SlidingTabLayout mTabLayout;
    private ArrayList<PayeeDetails> unPaidPayeeList;
    private ArrayList<PayeeDetails> paidPayeeList;
    private PayeeListAdapter unpaidExpensePayeeListAdapter;
    private PayeeListAdapter paidExpensePayeeListAdapter;
    private boolean isWalletList = true;
    private String eventName;
    private GetAllPayeeWithNonZeroExpenseAmount getAllPayeeWithNonZeroExpenseAmount;
    private PerformItemMenuAction performItemMenuAction;
    // Date Formatting
    private ScheduleClient scheduleClient;
    private AlarmDetails alarmDetails;
    private boolean isUnpaidPayeeListEmpty = false;
    private boolean isPaidPayeeListEmpty = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_payee_list, container, false);
        scheduleClient = new ScheduleClient(mParentActivity);
        scheduleClient.doBindService();
        loadInitialData();
        setActionBar();
        initializeUiComponents(rootView);
        setHasOptionsMenu(true);
        getAllPayeeWithNonZeroExpenseAmount = new GetAllPayeeWithNonZeroExpenseAmount();
        getAllPayeeWithNonZeroExpenseAmount.execute();
        return rootView;
    }

    private void initializeUiComponents(View rootView) {
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.tab_layout);
        progressBar = (FrameLayout) rootView.findViewById(R.id.progress_bar);
    }

    private void setActionBar() {
        if (!isWalletList) {
            mActionBar.setTitle(eventName);
        }
    }

    @Override
    public void onStop() {
        if (getAllPayeeWithNonZeroExpenseAmount != null)
            getAllPayeeWithNonZeroExpenseAmount.cancel(true);
        if (null != performItemMenuAction)
            performItemMenuAction.cancel(true);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        scheduleClient.doUnbindService();
        super.onDestroy();
    }

    private void loadInitialData() {
        Bundle bundle = mParentActivity.getIntent().getExtras();
        if (null != bundle) {
            isWalletList = bundle.getBoolean(Constants.EXTRA_VALUE_IS_WALLET_LIST, true);
            eventName = bundle.getString(Constants.EXTRA_VALUE_EVENT_NAME, "");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.payee_list, menu);
        if (isWalletList)
            menu.removeItem(R.id.action_reckon_up_event);
        else
            menu.removeItem(R.id.action_mark_all_payees_as_paid);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (unPaidPayeeList == null || unPaidPayeeList.size() == 0)
            if (isWalletList)
                menu.removeItem(R.id.action_mark_all_payees_as_paid);
            else
                menu.removeItem(R.id.action_reckon_up_event);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reckon_up_event:
                Intent intent = new Intent(mParentActivity, ReckonUpActivity.class);
                intent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, eventName);
                startActivity(intent);
                break;
            case R.id.action_edit_payee:
                Intent editPayeeIntent = new Intent(mParentActivity, EditPayeeActivity.class);
                startActivity(editPayeeIntent);
                break;
            case R.id.action_mark_all_payees_as_paid:
                AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
                builder.setTitle(R.string.dialog_mark_all_as_paid_for_all_payee_message)
                       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int id) {
                               performItemMenuAction = new PerformItemMenuAction(item.getItemId());
                               performItemMenuAction.execute();
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
                Intent preferencesIntent = new Intent(mParentActivity, SettingsActivity.class);
                startActivity(preferencesIntent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetAllPayeeWithNonZeroExpenseAmount extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (isWalletList) {
                unPaidPayeeList = databaseHandler.getPayeeWithNonZeroAmount();
                paidPayeeList = databaseHandler.getPayeesWithPaidExpense();
            } else
                unPaidPayeeList = databaseHandler.getAllPersonsListForAnEvent(eventName);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (unpaidExpensePayeeListAdapter == null)
                unpaidExpensePayeeListAdapter = new PayeeListAdapter(unPaidPayeeList, true);
            else {
                unpaidExpensePayeeListAdapter.setPayeeList(unPaidPayeeList);
                unpaidExpensePayeeListAdapter.notifyDataSetChanged();
            }

            if (isWalletList)
                if (null == paidExpensePayeeListAdapter)
                    paidExpensePayeeListAdapter = new PayeeListAdapter(paidPayeeList, false);
                else {
                    paidExpensePayeeListAdapter.setPayeeList(paidPayeeList);
                    paidExpensePayeeListAdapter.notifyDataSetChanged();
                }

            // Create the adapter that will return a fragment for each of the
            // three primary sections of the app.
            if (mPayeeListPagerAdapter == null || (isUnpaidPayeeListEmpty && unPaidPayeeList.size() > 0)
                || (!isUnpaidPayeeListEmpty && unPaidPayeeList.size() == 0)
                || (isWalletList && isPaidPayeeListEmpty && paidPayeeList.size() > 0)
                || (isWalletList && !isPaidPayeeListEmpty && paidPayeeList.size() == 0)) {
                setUpViewPagerAndTabs();
            } else {
                mPayeeListPagerAdapter.notifyDataSetChanged();
                mViewPager.requestLayout();
            }
            isUnpaidPayeeListEmpty = unPaidPayeeList.size() == 0;
            if (isWalletList)
                isPaidPayeeListEmpty = paidPayeeList.size() == 0;
            mParentActivity.invalidateOptionsMenu();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setUpViewPagerAndTabs() {
        mPayeeListPagerAdapter = new PayeeListPagerAdapter(mFragmentManager);
        mViewPager.setAdapter(mPayeeListPagerAdapter);

//        mTabLayout.setLayoutMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setViewPager(mViewPager);
        Resources resources = getResources();
        mTabLayout
                .setSelectedIndicatorColors(resources.getColor(R.color.unpaid_tab_color), resources.getColor(R.color.paid_tab_color));

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class PayeeListPagerAdapter extends FragmentStatePagerAdapter {
        public PayeeListPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            PayeeListSectionFragment fragment = new PayeeListSectionFragment();
            Bundle args = new Bundle();
            args.putInt(PayeeListSectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            if (isWalletList)
                return 2;
            else
                return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (isWalletList) {
                switch (position) {
                    case 0:
                        return getString(R.string.unpaid_list);
                    case 1:
                        return getString(R.string.paid_list);
                }
            } else {
                return getString(R.string.payee_list_title);
            }
            return null;
        }
    }

    private void launchExpenseList(String payer, boolean isShowPaid) {
        Intent intent = new Intent(mParentActivity, ExpenseListActivity.class);
        if (isWalletList) {
            intent.putExtra(Constants.EXTRA_VALUE_PAYEE_NAME, payer);
            intent.putExtra(Constants.EXTRA_VALUE_IS_SHOW_PAID, isShowPaid);
        } else {
            intent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_LIST, false);
            intent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, eventName);
            intent.putExtra(Constants.EXTRA_VALUE_PAYER_NAME, payer);
            intent.putExtra(Constants.EXTRA_VALUE_IS_SHOW_PAID, isShowPaid);
        }
        mParentActivity.startActivityForResult(intent, Constants.REQUEST_CODE_WALLET_EXPENSE_LIST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == Constants.REQUEST_CODE_ADD_EXPENSE_LIST || requestCode == Constants.REQUEST_CODE_WALLET_EXPENSE_LIST) &&
            resultCode == Activity.RESULT_OK) {
            getAllPayeeWithNonZeroExpenseAmount = new GetAllPayeeWithNonZeroExpenseAmount();
            getAllPayeeWithNonZeroExpenseAmount.execute();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public class PayeeListSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public PayeeListSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = null;
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                int textResId;
                if (isWalletList)
                    textResId = R.string.no_unpaid_expense_msg;
                else
                    textResId = R.string.no_payer_event_msg;
                rootView = getDefaultListFragmentView(inflater, container, unPaidPayeeList, unpaidExpensePayeeListAdapter, textResId);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                rootView = getDefaultListFragmentView(inflater, container, paidPayeeList, paidExpensePayeeListAdapter,
                                                      R.string.no_paid_expense_message);
            }
            return rootView;
        }

        private View getDefaultListFragmentView(LayoutInflater inflater, ViewGroup container, ArrayList pPayeeList,
                                                PayeeListAdapter pListAdapter, int textResId) {
            View rootView;
            if (pPayeeList != null && pPayeeList.size() > 0) {
                rootView = inflater.inflate(R.layout.fragment_section_list_view, container, false);
                RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.section_list_view);
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                recyclerView.setHasFixedSize(true);

                // use a linear layout manager
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(mParentActivity);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(pListAdapter);
            } else {
                rootView = inflater.inflate(R.layout.fragment_no_expense, container, false);
                TextView textView = (TextView) rootView.findViewById(R.id.section_text_view);
                textView.setText(textResId);
            }
            return rootView;
        }
    }

    public class PayeeListAdapter extends RecyclerView.Adapter<PayeeListAdapter.ViewHolder> {
        private final LayoutInflater mInflater;
        private ArrayList<PayeeDetails> payeeList;
        private final boolean isUnpaidList;

        public PayeeListAdapter(ArrayList<PayeeDetails> payeeList, boolean isUnpaidList) {
            this.payeeList = payeeList;
            this.isUnpaidList = isUnpaidList;
            mInflater = LayoutInflater.from(mParentActivity);
        }

        public void setPayeeList(ArrayList<PayeeDetails> payeeList) {
            this.payeeList = payeeList;
        }

        @Override
        public int getItemCount() {
            return payeeList.size();
        }

        private PayeeDetails getItem(int position) {
            return payeeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View view = mInflater.inflate(R.layout.item_payee_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final PayeeDetails payeeDetails = getItem(position);
            final String payeeName = payeeDetails.getName();
            holder.textViewPayeeName.setText(payeeName);
            holder.textViewPayeeThumbnail.setText(payeeName.substring(0, 1).toUpperCase());
            holder.textViewPayeeThumbnail.setBackgroundResource(payeeDetails.getColorCode());
            holder.textViewAmount.setText(payeeDetails.getAmount());
            holder.parentLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchExpenseList(payeeName, !isUnpaidList);
                }
            });
            if (isWalletList && isUnpaidList) {
                holder.imageButtonItemMenu.setVisibility(View.VISIBLE);
                holder.imageButtonItemMenu.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(v, payeeDetails);
                    }
                });
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout parentLayout;
            TextView textViewPayeeThumbnail;
            TextView textViewPayeeName;
            TextView textViewAmount;
            ImageView imageButtonItemMenu;

            public ViewHolder(View convertView) {
                super(convertView);
                parentLayout = (LinearLayout) convertView.findViewById(R.id.parent_layout);
                textViewPayeeThumbnail = (TextView) convertView.findViewById(R.id.text_view_payee_initial);
                textViewPayeeName = (TextView) convertView.findViewById(R.id.text_view_payee_name);
                textViewAmount = (TextView) convertView.findViewById(R.id.text_view_payee_amount);
                imageButtonItemMenu = (ImageView) convertView.findViewById(R.id.image_button_item_menu);
            }
        }
    }

    public void showPopup(View view, final PayeeDetails payeeDetails) {
        PopupMenu popup = new PopupMenu(mParentActivity, view);
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_mark_all_as_paid:
                        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
                        builder.setTitle(R.string.dialog_mark_all_as_paid_for_one_payee_message)
                               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int id) {
                                       performItemMenuAction = new PerformItemMenuAction(item.getItemId(), payeeDetails.getName());
                                       performItemMenuAction.execute();
                                   }
                               }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        // Create the AlertDialog object and show it
                        builder.create().show();
                        break;
                    case R.id.action_remind_me:
                        setAlarmDetails(payeeDetails);
                        (new CustomDateTimeDialog()).show(mFragmentManager, "set_alarm_reminder");
                        break;
                    case R.id.action_remind_payee:
                        Utils.remindAPayeeAboutAllExpensesViaSms(mParentActivity, payeeDetails.getAmount());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.item_payee_list, popup.getMenu());
        popup.getMenu().findItem(R.id.action_remind_payee).setTitle("Remind " + payeeDetails.getName());
        popup.show();
    }

    public class PerformItemMenuAction extends AsyncTask<Void, Void, Void> {
        private final int actionId;
        private String payeeName;

        public PerformItemMenuAction(int actionId) {
            this.actionId = actionId;
        }

        public PerformItemMenuAction(int actionId, String payeeName) {
            this.actionId = actionId;
            this.payeeName = payeeName;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (actionId) {
                case R.id.action_mark_all_as_paid:
                    databaseHandler.markAllAsPaidForOnePayee(payeeName);
                    break;
                case R.id.action_mark_all_payees_as_paid:
                    databaseHandler.markAllAsPaidForAllPayees();
                    break;
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getAllPayeeWithNonZeroExpenseAmount = new GetAllPayeeWithNonZeroExpenseAmount();
            getAllPayeeWithNonZeroExpenseAmount.execute();
        }
    }

    class CustomDateTimeDialog extends DialogFragment {
        private Calendar customDate;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                            customDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                                           timePicker.getCurrentHour(),
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
        Toast.makeText(mParentActivity, "Notification set for: " + Utils.getFormattedDateFromMillies(date.getTimeInMillis() + ""),
                       Toast.LENGTH_SHORT).show();
    }

    private void setAlarmDetails(PayeeDetails payeeDetails) {
        String notificationContent;
        if (Integer.parseInt(payeeDetails.getAmount()) < 0) {
            notificationContent =
                    "Hey, remember *payee* paid *amount* bucks*tag**date**description*. You might wanna remind your friend that you will give it back.";
            notificationContent = notificationContent.replace("*payee*", payeeDetails.getName());
            notificationContent = notificationContent.replace("*tag*", " for you ");
        } else {
            notificationContent =
                    "Hey, remember *payee* paid *amount* bucks*tag**date**description*. You might wanna remind your friend to give it back.";
            notificationContent = notificationContent.replace("*tag*", " for " + payeeDetails.getName()) + " ";
            notificationContent = notificationContent.replace("*payee*", "you");
        }
        notificationContent = notificationContent.replace("*amount*", payeeDetails.getAmount().replace("-", ""));
        notificationContent = notificationContent.replace("*date*", "");
        notificationContent = notificationContent.replace("*description*", "");
        alarmDetails = new AlarmDetails();
        alarmDetails.setNotificationTitle("It's about money!!!");
        alarmDetails.setNotificationContent(notificationContent);
        alarmDetails.setExpensePayee(payeeDetails.getName());
    }
}
