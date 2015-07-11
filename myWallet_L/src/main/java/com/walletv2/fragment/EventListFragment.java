package com.walletv2.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.walletv2.activity.DrawerActivity;
import com.walletv2.activity.EditPayeeActivity;
import com.walletv2.activity.R;
import com.walletv2.activity.ReckonUpActivity;
import com.walletv2.activity.SettingsActivity;
import com.walletv2.entity.Constants;

import java.util.ArrayList;

public class EventListFragment extends BaseFragment {
    private ArrayList<String> eventList = null;
    private ListView listViewEvent = null;
    private FrameLayout progressBar = null;
    private TextView textViewNoEvent = null;
    private EventListAdapter eventListAdapter = null;
    private GetAllEvents getAllEvents = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_edit_payee, container, false);
        initializeUiComponents(rootView);
        listViewEvent.setDivider(null);
        listViewEvent.setDividerHeight(0);
        initializeListeners();
        setHasOptionsMenu(true);
        getAllEvents = new GetAllEvents();
        getAllEvents.execute();
        return rootView;
    }

    @Override
    public void onStop() {
        if (getAllEvents != null)
            getAllEvents.cancel(true);
        super.onStop();
    }

    private void initializeUiComponents(View rootView) {
        listViewEvent = (ListView) rootView.findViewById(R.id.list_view_payee_name);
        progressBar = (FrameLayout) rootView.findViewById(R.id.progress_bar);
        textViewNoEvent = (TextView) rootView.findViewById(R.id.text_view_no_payee);
        textViewNoEvent.setText(R.string.no_event_message);
    }

    private void initializeListeners() {
        listViewEvent.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                launchEventExpenseListActivity(eventList.get(arg2));
            }
        });
    }

    private void launchEventExpenseListActivity(String eventName) {
        Intent intent = new Intent(mParentActivity, DrawerActivity.class);
        intent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_LIST, false);
        intent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, eventName);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.event_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_payee:
                Intent editPayeeIntent = new Intent(mParentActivity, EditPayeeActivity.class);
                startActivity(editPayeeIntent);
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

    public void showAddEvent() {
        (new AddOrEditEventDialog("", true)).show(mFragmentManager, "add_event");
    }

    class AddOrEditEventDialog extends DialogFragment {
        private final String eventName;
        private final boolean isNewEvent;

        public AddOrEditEventDialog(String eventName, boolean isNewEvent) {
            this.eventName = eventName;
            this.isNewEvent = isNewEvent;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            Builder builder = new Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View inflateView = inflater.inflate(R.layout.dialog_add_payee, null);
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog
            // layout
            final EditText editTextEventName = (EditText) inflateView.findViewById(R.id.edit_text_payee_name);
            editTextEventName.setHint(R.string.dialog_add_event_hint);
            editTextEventName.setText(eventName);
            int dialogTitleResourceId;
            if (isNewEvent)
                dialogTitleResourceId = R.string.dialog_add_event_title;
            else
                dialogTitleResourceId = R.string.dialog_edit_event_title;
            builder.setTitle(dialogTitleResourceId).setView(editTextEventName)
                    // Add action buttons
                    .setPositiveButton(isNewEvent ? R.string.add : R.string.edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (!editTextEventName.getText().toString().trim().equals("")) {
                                if (isNewEvent)
                                    databaseHandler.insertEvent(editTextEventName.getText().toString());
                                else
                                    databaseHandler.updateEvent(editTextEventName.getText().toString(), eventName);
                                getAllEvents = new GetAllEvents();
                                getAllEvents.execute();
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            return builder.create();
        }
    }

    class DeleteEventDialog extends DialogFragment {
        String eventName;

        public DeleteEventDialog(String eventName) {
            this.eventName = eventName;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.dialog_delete_event_message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    databaseHandler.deleteEventName(eventName);
                    getAllEvents = new GetAllEvents();
                    getAllEvents.execute();
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

    class GetAllEvents extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            textViewNoEvent.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            eventList = databaseHandler.getAllEvents();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            if (null != eventList && eventList.size() > 0) {
                listViewEvent.setVisibility(View.VISIBLE);
                if (null == eventListAdapter) {
                    eventListAdapter = new EventListAdapter();
                    listViewEvent.setAdapter(eventListAdapter);
                } else
                    eventListAdapter.notifyDataSetChanged();
            } else {
                listViewEvent.setVisibility(View.VISIBLE);
                textViewNoEvent.setVisibility(View.VISIBLE);
            }
        }
    }

    class EventListAdapter extends BaseAdapter {
        LayoutInflater inflater = null;

        public EventListAdapter() {
            inflater = mParentActivity.getLayoutInflater();
        }

        @Override
        public int getCount() {
            return eventList.size();
        }

        @Override
        public String getItem(int position) {
            return eventList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemHolder itemHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_event_list, null);
                itemHolder = new ItemHolder();
                itemHolder.textViewEventName = (TextView) convertView.findViewById(R.id.text_view_event_name);
                itemHolder.imageButtonItemMenu = (ImageView) convertView.findViewById(R.id.image_button_item_menu);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemHolder) convertView.getTag();
            }
            final String event = getItem(position);
            itemHolder.textViewEventName.setText(event);
            itemHolder.imageButtonItemMenu.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(view, event);
                }
            });
            return convertView;
        }

        class ItemHolder {
            TextView textViewEventName;
            ImageView imageButtonItemMenu;
        }
    }

    public void showPopup(View view, final String eventName) {
        PopupMenu popup = new PopupMenu(mParentActivity, view);
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit_event_name:
                        new AddOrEditEventDialog(eventName, false).show(mFragmentManager, "edit_event");
                        break;
                    case R.id.action_delete_event:
                        new DeleteEventDialog(eventName).show(mFragmentManager, "delete_event");
                        break;
                    case R.id.action_reckon_up_event:
                        Intent intent = new Intent(mParentActivity, ReckonUpActivity.class);
                        intent.putExtra(Constants.EXTRA_VALUE_EVENT_NAME, eventName);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.item_event_list, popup.getMenu());
        popup.show();
    }
}
