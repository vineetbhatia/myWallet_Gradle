package com.walletv2.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.walletv2.entity.Constants;
import com.walletv2.fragment.EventListFragment;
import com.walletv2.fragment.PayeeListFragment;

public class DrawerActivity extends BaseActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FloatingActionButton mAddExpenseButton;
    private boolean isWalletList;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private TextView mProfileName;
    private int lastSelectedNavOption = 0;
    private MenuItem menuItemSelected;
    public LinearLayout mToolbarContainer;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_activity);

        setUpToolbar();
        fragmentManager = getSupportFragmentManager();
        initializeUiElements();
        initializeListeners();
        isWalletList = getIntent().getBooleanExtra(Constants.EXTRA_VALUE_IS_WALLET_LIST, false);
        fragment = new PayeeListFragment();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        if (isWalletList)
            setNavigationDrawer();
        else
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void setUpToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbarContainer = (LinearLayout) findViewById(R.id.toolbar_container);
        mActionBar = getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            findViewById(R.id.toolbar_container).setPadding(0, getStatusBarHeight(), 0, 0);
        if (mActionBar == null)
            return;
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void initializeUiElements() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mAddExpenseButton = (FloatingActionButton) findViewById(R.id.btn_add_expense);
        mProfileName = (TextView) findViewById(R.id.text_view_profile_name);
    }

    private void initializeListeners() {
        mAddExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragment instanceof PayeeListFragment) {
                    Intent addExpenseIntent = new Intent(DrawerActivity.this, AddExpenseActivity.class);
                    if (!isWalletList) {
                        addExpenseIntent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_EXPENSE, false);
                        addExpenseIntent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, getIntent().getStringExtra(Constants.EXTRA_VALUE_EVENT_NAME));
                    }
                    startActivityForResult(addExpenseIntent, Constants.REQUEST_CODE_ADD_EXPENSE_LIST);
                } else if (fragment instanceof EventListFragment) {
                    ((EventListFragment) fragment).showAddEvent();
                }
            }
        });
    }

    public void setNavigationDrawer() {
        mProfileName.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.SHARED_PREFERENCES_PROFILE_NAME, "First Name"));

        mDrawerToggle = new ActionBarDrawerToggle(DrawerActivity.this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (lastSelectedNavOption != menuItem.getItemId())
                    menuItemSelected = menuItem;
                else
                    menuItemSelected = null;
                mDrawerLayout.closeDrawer(Gravity.LEFT);
                return true;
            }
        });

        ImageView profileImage = (ImageView) mNavigationView.findViewById(R.id.img_profile);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile_none);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        roundedBitmapDrawable.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
        profileImage.setImageDrawable(roundedBitmapDrawable);
        profileImage.invalidate();

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},   // pressed
                new int[]{android.R.attr.state_checked}, // checked
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_enabled} // enabled
        };

        int[] colors = new int[]{
                Color.YELLOW,
                getResources().getColor(R.color.action_bar_color),
                Color.BLACK,
                Color.BLACK
        };
        mNavigationView.setItemTextColor(new ColorStateList(states, colors));

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                menuItemSelected = null;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (menuItemSelected != null)
                    selectItem(menuItemSelected);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        // Create a new fragment and specify the planet to show based on position
        if (menuItemId == R.id.action_my_wallet) {
            fragment = new PayeeListFragment();
        } else if (menuItemId == R.id.action_my_event) {
            fragment = new EventListFragment();
        } else if (menuItemId == R.id.action_edit_payee) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            Intent intent = new Intent(DrawerActivity.this, EditPayeeActivity.class);
            startActivity(intent);
            return;
        } else if (menuItemId == R.id.action_settings) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            Intent intent = new Intent(DrawerActivity.this, SettingsActivity.class);
            startActivity(intent);
            return;
        }
        // Highlight the selected item, update the title, and close the drawer
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        lastSelectedNavOption = menuItemId;
        menuItem.setChecked(true);
        // Insert the fragment by replacing any existing fragment
        new Thread(new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        else
            super.onBackPressed();
    }

    @Override
    public void setTitle(CharSequence title) {
        mActionBar.setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fragment.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isWalletList)
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                else
                    mDrawerLayout.openDrawer(Gravity.LEFT);

            else {
                return super.onOptionsItemSelected(item);
            }
        } else
            return super.onOptionsItemSelected(item);
        return true;
    }

}
