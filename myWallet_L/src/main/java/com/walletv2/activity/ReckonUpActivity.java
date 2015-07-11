package com.walletv2.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.walletv2.entity.Constants;
import com.walletv2.entity.ExpenseDetails;
import com.walletv2.views.SlidingTabLayout;

import java.util.ArrayList;

public class ReckonUpActivity extends BaseActivity {
    private ReckonListPagerAdapter mReckonListPagerAdapter;
    private ViewPager mViewPager;
    private FrameLayout progressBar;
    private ArrayList<ExpenseDetails> eventExpenseList;
    private String eventName;
    private GetAllExpenseListForAnEvent getAllExpenseListForAnEvent;
    private ReckonUpExpandableListAdapter reckonUpListAdapter;
    private ReckonUpExpandableListAdapter debtListAdapter;
    // calculation variables
    private float[] paidList;
    private float[] spentOnList;
    private float[] debtList;
    private int[] sortedIndicesForDebtList;
    private ArrayList<String> payeesList;
    private ShareActionProvider mShareActionProvider;
    private String shareText;
    private ExpandableListView reckonUpListView;
    private ExpandableListView debtListView;
    private SlidingTabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payee_list);
        initializeUiComponents();
        loadInitialData();
        getAllExpenseListForAnEvent = new GetAllExpenseListForAnEvent();
        getAllExpenseListForAnEvent.execute();
    }

    private void initializeUiComponents() {
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabLayout = (SlidingTabLayout) findViewById(R.id.tab_layout);
        progressBar = (FrameLayout) findViewById(R.id.progress_bar);
    }

    @Override
    protected void onStop() {
        if (null != getAllExpenseListForAnEvent)
            getAllExpenseListForAnEvent.cancel(true);
        super.onStop();
    }

    private void loadInitialData() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            eventName = bundle.getString(Constants.EXTRA_VALUE_EVENT_NAME);
            mActionBar.setTitle(eventName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reckon_up, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Log.i("MyWallet", "mShareActionProvider is created");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setShareIntent();
        return super.onPrepareOptionsMenu(menu);
    }

    private void setShareIntent() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(mActivity, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                break;
            case R.id.action_expand:
                BaseExpandableListAdapter listAdapter = mViewPager.getCurrentItem() == 0 ? reckonUpListAdapter : debtListAdapter;
                ExpandableListView listView = mViewPager.getCurrentItem() == 0 ? reckonUpListView : debtListView;
                if (listAdapter != null && listView != null)
                    for (int i = 0; i < listAdapter.getGroupCount(); i++)
                        listView.expandGroup(i, true);
                break;
            case R.id.action_settings:
                Intent preferencesIntent = new Intent(mActivity, SettingsActivity.class);
                startActivity(preferencesIntent);
                break;
            default:
                break;
        }
        return true;
    }

    class GetAllExpenseListForAnEvent extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            eventExpenseList = databaseHandler.getAllEventExpenseForAEvent(eventName);
            ArrayList<String> payersList = new ArrayList<>();
            payeesList = new ArrayList<>();
            for (ExpenseDetails eventExpense : eventExpenseList) {
                String[] payeeStrings = eventExpense.getPayeesList().split(", ");
                for (String payee : payeeStrings) {
                    if (!payeesList.contains(payee)) {
                        payeesList.add(payee);
                    }
                }
                if (!payersList.contains(eventExpense.getPayersList())) {
                    payersList.add(eventExpense.getPayersList());
                }
            }
            // add payer in payees list if doesn't exist
            for (String payerName : payersList) {
                if (!payeesList.contains(payerName)) {
                    payeesList.add(payerName);
                }
            }
            setPaidAndSpentList();
            setAndSortDebtList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            if (null != eventExpenseList && eventExpenseList.size() > 0) {
                initializeReckonUpListAdapter();
                initializeDebtListAdapter();
            }
            if (null == mReckonListPagerAdapter) {
                // Create the adapter that will return a fragment for each of
                // the three
                // primary sections of the app.
                setUpViewPagerAndTabs();
            } else
                mReckonListPagerAdapter.notifyDataSetChanged();
        }
    }

    private void setUpViewPagerAndTabs() {
        mReckonListPagerAdapter = new ReckonListPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mReckonListPagerAdapter);

        mTabLayout.setViewPager(mViewPager);

    }

    private void initializeReckonUpListAdapter() {
        ArrayList<String[]> receiversList = new ArrayList<>();
        ArrayList<ArrayList<String[]>> giversList = new ArrayList<>();
        int i = 0;
        int j = sortedIndicesForDebtList.length - 1;
        float diffAmount = 0.00f;
        while (i < j) {
            float transferAmount = Math.abs(debtList[sortedIndicesForDebtList[i]]) > Math.abs(debtList[sortedIndicesForDebtList[j]]) ? Math
                    .abs(debtList[sortedIndicesForDebtList[j]]) : Math.abs(debtList[sortedIndicesForDebtList[i]]);
            if (diffAmount <= 0 || (i == 0 && j == sortedIndicesForDebtList.length - 1)) {
                receiversList.add(new String[]{payeesList.get(sortedIndicesForDebtList[i]), String.format("%.1f", debtList[sortedIndicesForDebtList[i]])});
                ArrayList<String[]> arrayList = new ArrayList<>();
                arrayList.add(new String[]{payeesList.get(sortedIndicesForDebtList[j]), String.format("%.1f", transferAmount)});
                giversList.add(arrayList);
            } else if (diffAmount > 0) {
                giversList.get(receiversList.size() - 1).add(
                        new String[]{payeesList.get(sortedIndicesForDebtList[j]), String.format("%.1f", transferAmount)});
            }
            diffAmount = debtList[sortedIndicesForDebtList[i]] + debtList[sortedIndicesForDebtList[j]];
            if (diffAmount > 0) {
                debtList[sortedIndicesForDebtList[i]] = diffAmount;
                --j;
            } else if (diffAmount < 0) {
                debtList[sortedIndicesForDebtList[j]] = diffAmount;
                ++i;
            } else {
                debtList[sortedIndicesForDebtList[i]] = diffAmount;
                debtList[sortedIndicesForDebtList[j]] = diffAmount;
                ++i;
                --j;
            }
        }
        reckonUpListAdapter = new ReckonUpExpandableListAdapter(receiversList, giversList);
        shareText = "After reckoning up " + eventName + ":";
        for (int k = 0; k < receiversList.size(); k++) {
            String[] reciever = receiversList.get(k);
            shareText += "\n" + reciever[0] + " will get an amount of " + reciever[1] + " in total.";
            for (String[] giver : giversList.get(k)) {
                shareText += "\n" + giver[0] + " will give " + giver[1] + " to " + reciever[0] + ".";
            }
        }
        shareText += "\n-shared from MyWallet";
        setShareIntent();
    }

    @SuppressWarnings("serial")
    private void initializeDebtListAdapter() {
        ArrayList<String[]> groupList = new ArrayList<>();
        ArrayList<ArrayList<String[]>> childrenList = new ArrayList<>();
        for (int i = 0; i < payeesList.size(); i++) {
            String[] personDetails = new String[2];
            personDetails[0] = payeesList.get(i);
            personDetails[1] = String.format("%.1f", (paidList[i] - spentOnList[i]));
            groupList.add(personDetails);
            final String[] debtDetails = new String[2];
            debtDetails[0] = "Spent on\n" + String.format("%.1f", spentOnList[i]);
            debtDetails[1] = "Paid\n" + String.format("%.1f", paidList[i]);
            childrenList.add(new ArrayList<String[]>() {
                {
                    add(debtDetails);
                }
            });
        }
        debtListAdapter = new ReckonUpExpandableListAdapter(groupList, childrenList);
    }

    private void setPaidAndSpentList() {
        int size = payeesList.size();
        paidList = new float[size];
        spentOnList = new float[size];
        for (ExpenseDetails eventExpense : eventExpenseList) {
            String payer = eventExpense.getPayersList();
            String[] payees = eventExpense.getPayeesList().split(", ");
            int index = payeesList.indexOf(payer);
            paidList[index] = paidList[index] + Float.parseFloat(eventExpense.getAmount());
            float amountSpentOnOne = (Float.parseFloat(eventExpense.getAmount()) / payees.length);
            for (String payee : payees) {
                index = payeesList.indexOf(payee);
                spentOnList[index] = spentOnList[index] + amountSpentOnOne;
            }
        }
    }

    private void setAndSortDebtList() {
        debtList = new float[paidList.length];
        sortedIndicesForDebtList = new int[debtList.length];
        for (int i = 0; i < debtList.length; i++) {
            debtList[i] = paidList[i] - spentOnList[i];
            sortedIndicesForDebtList[i] = i; // set sortedIndicesForDebtList
        }
        quicksort(debtList.clone(), sortedIndicesForDebtList);
        for (int i = 0; i < debtList.length; i++) {
            Log.i("MyWallet", payeesList.get(i) + " , " + debtList[i] + " , " + sortedIndicesForDebtList[i]);
        }
    }

    private void quicksort(float[] main, int[] index) {
        quicksort(main, index, 0, index.length - 1);
    }

    // quicksort a[left] to a[right]
    private void quicksort(float[] a, int[] index, int left, int right) {
        if (right <= left)
            return;
        int i = partition(a, index, left, right);
        quicksort(a, index, left, i - 1);
        quicksort(a, index, i + 1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private int partition(float[] a, int[] index, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (more(a[++i], a[right]))
                // find item on left to swap
                ; // a[right] acts as sentinel
            while (more(a[right], a[--j]))
                // find item on right to swap
                if (j == left)
                    break; // don't go out-of-bounds
            if (i >= j)
                break; // check if pointers cross
            exch(a, index, i, j); // swap two elements into place
        }
        exch(a, index, i, right); // swap with partition element
        return i;
    }

    // is x < y ?
    private boolean more(float x, float y) {
        return (x > y);
    }

    // exchange a[i] and a[j]
    private void exch(float[] a, int[] index, int i, int j) {
        float swap = a[i];
        a[i] = a[j];
        a[j] = swap;
        int b = index[i];
        index[i] = index[j];
        index[j] = b;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ReckonListPagerAdapter extends FragmentPagerAdapter {
        public ReckonListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new ReckonUpListSectionFragment();
            Bundle args = new Bundle();
            args.putInt(ReckonUpListSectionFragment.ARG_SECTION_NUMBER, position + 1);
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
            switch (position) {
                case 0:
                    return getString(R.string.reckon_up_list_title);
                case 1:
                    return getString(R.string.debt_list_title);
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    class ReckonUpListSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = null;
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                if (null != eventExpenseList && eventExpenseList.size() > 0) {
                    rootView = inflater.inflate(R.layout.fragment_section_expandable_list_view, container, false);
                    reckonUpListView = (ExpandableListView) rootView.findViewById(R.id.section_expandable_list_view);
                    reckonUpListView.setAdapter(reckonUpListAdapter);
                } else {
                    rootView = inflater.inflate(R.layout.fragment_no_expense, container, false);
                    TextView textView = (TextView) rootView.findViewById(R.id.section_text_view);
                    textView.setText(R.string.no_event_expense_msg);
                }
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                if (null != eventExpenseList && eventExpenseList.size() > 0) {
                    rootView = inflater.inflate(R.layout.fragment_section_expandable_list_view, container, false);
                    debtListView = (ExpandableListView) rootView.findViewById(R.id.section_expandable_list_view);
                    debtListView.setAdapter(debtListAdapter);
                } else {
                    rootView = inflater.inflate(R.layout.fragment_no_expense, container, false);
                    TextView textView = (TextView) rootView.findViewById(R.id.section_text_view);
                    textView.setText(R.string.no_paid_expense_message);
                }
            }
            return rootView;
        }
    }

    class ReckonUpExpandableListAdapter extends BaseExpandableListAdapter {
        private ArrayList<String[]> groupsList;
        private ArrayList<ArrayList<String[]>> childrenList;
        private LayoutInflater mInflater;

        public ReckonUpExpandableListAdapter(ArrayList<String[]> groupsList, ArrayList<ArrayList<String[]>> childrenList) {
            mInflater = mActivity.getLayoutInflater();
            this.groupsList = groupsList;
            this.childrenList = childrenList;
        }

        @Override
        public String[] getChild(int groupPosition, int childPosition) {
            return childrenList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childrenList.get(groupPosition).size();
        }

        @Override
        public String[] getGroup(int groupPosition) {
            return groupsList.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return groupsList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            HolderChild holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_child_reckon_up_list, null);
                holder = new HolderChild();
                holder.textViewLeft = (TextView) convertView.findViewById(R.id.text_view_payee_name);
                holder.textViewRight = (TextView) convertView.findViewById(R.id.text_view_payee_amount);
                convertView.setTag(holder);
            } else {
                holder = (HolderChild) convertView.getTag();
            }
            String[] strings = getChild(groupPosition, childPosition);
            holder.textViewLeft.setText(strings[0]);
            holder.textViewRight.setText(strings[1]);
            return convertView;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            HolderGroup holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_group_reckon_up_list, null);
                holder = new HolderGroup();
                holder.textViewPersonName = (TextView) convertView.findViewById(R.id.text_view_payee_name);
                holder.textViewAmount = (TextView) convertView.findViewById(R.id.text_view_payee_amount);
                convertView.setTag(holder);
            } else {
                holder = (HolderGroup) convertView.getTag();
            }
            String[] strings = getGroup(groupPosition);
            holder.textViewPersonName.setText(strings[0]);
            holder.textViewAmount.setText(strings[1]);
            return convertView;
        }

        class HolderChild {
            TextView textViewLeft;
            TextView textViewRight;
        }

        class HolderGroup {
            TextView textViewPersonName;
            TextView textViewAmount;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return false;
        }
    }
}