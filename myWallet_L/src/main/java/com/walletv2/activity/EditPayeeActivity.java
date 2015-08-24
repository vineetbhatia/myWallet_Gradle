package com.walletv2.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EditPayeeActivity extends BaseActivity {
    private ArrayList<String> payeeList = null;
    private ListView listViewPayee = null;
    private FrameLayout progressBar = null;
    private TextView textViewNoPayee = null;
    private EditPayeeListAdapter payeeListAdapter = null;
    private GetAllPayees getAllPayees = null;
    private boolean mDataChanged = false;
    private FloatingActionButton addPayeeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_payee);
        initializeUiComponents();
        setClickListener();
        getAllPayees = new GetAllPayees();
        getAllPayees.execute();
    }

    @Override
    protected void onStop() {
        if (null != getAllPayees)
            getAllPayees.cancel(true);
        super.onStop();
    }

    private void initializeUiComponents() {
        listViewPayee = (ListView) findViewById(R.id.list_view_payee_name);
        progressBar = (FrameLayout) findViewById(R.id.progress_bar);
        addPayeeButton = (FloatingActionButton) findViewById(R.id.btn_add_payee);
        textViewNoPayee = (TextView) findViewById(R.id.text_view_no_payee);
    }

    private void setClickListener() {
        addPayeeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                (new AddOrEditPayeeDialog("", true)).show(getSupportFragmentManager(), "add_payee");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_payee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_payee:
                (new AddOrEditPayeeDialog("", true)).show(getSupportFragmentManager(), "add_payee");
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

    class AddOrEditPayeeDialog extends DialogFragment {
        private final String oldPayeeName;
        private final boolean isNewPayee;

        public AddOrEditPayeeDialog(String oldPayeeName, boolean isNewPayee) {
            this.oldPayeeName = oldPayeeName;
            this.isNewPayee = isNewPayee;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View inflateView = inflater.inflate(R.layout.dialog_add_payee, null);
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog
            // layout
            final EditText editTextPayeeName = (EditText) inflateView.findViewById(R.id.edit_text_payee_name);
            editTextPayeeName.setText(oldPayeeName);
            int dialogTitleResourceId;
            if (isNewPayee)
                dialogTitleResourceId = R.string.dialog_add_payee_title;
            else
                dialogTitleResourceId = R.string.dialog_edit_payee_title;
            builder.setTitle(dialogTitleResourceId).setView(editTextPayeeName)
                    // Add action buttons
                    .setPositiveButton(isNewPayee ? R.string.add : R.string.edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String s = editTextPayeeName.getText().toString().trim();
                            if (!s.equals("") && !s.contains(",")) {
                                if (isNewPayee)
                                    databaseHandler.insertPayee(editTextPayeeName.getText().toString().trim());
                                else
                                    databaseHandler.updatePayee(editTextPayeeName.getText().toString().trim(), oldPayeeName);
                                mDataChanged = true;
                                getAllPayees = new GetAllPayees();
                                getAllPayees.execute();
                            } else
                                Toast.makeText(EditPayeeActivity.this, "Blank & payee name with comma (,) are not allowed.", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            return builder.create();
        }
    }

    class DeletePayeeDialog extends DialogFragment {
        String payeeName;

        public DeletePayeeDialog(String payeeName) {
            this.payeeName = payeeName;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.dialog_delete_payee_message)
                   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                           if (databaseHandler.deletePayee(payeeName)) {
                               new GetAllPayees().execute();
                               mDataChanged = true;
                           } else
                               Toast.makeText(mActivity, R.string.error_payee_cannot_be_deleted, Toast.LENGTH_LONG).show();
                       }
                   }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    class GetAllPayees extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            textViewNoPayee.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            payeeList = databaseHandler.getAllPayees();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            if (null != payeeList && payeeList.size() > 0) {
                listViewPayee.setVisibility(View.VISIBLE);
                if (null == payeeListAdapter) {
                    payeeListAdapter = new EditPayeeListAdapter();
                    listViewPayee.setAdapter(payeeListAdapter);
                } else
                    payeeListAdapter.notifyDataSetChanged();
            } else {
                listViewPayee.setVisibility(View.GONE);
                textViewNoPayee.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDataChanged) {
            setResult(Activity.RESULT_OK);
            finish();
        } else
            super.onBackPressed();
    }

    class EditPayeeListAdapter extends BaseAdapter {
        LayoutInflater inflater = null;

        public EditPayeeListAdapter() {
            inflater = mActivity.getLayoutInflater();
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
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemHolder itemHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_edit_payee_list, null);
                itemHolder = new ItemHolder();
                itemHolder.textViewPayeeName = (TextView) convertView.findViewById(R.id.text_view_payee_name);
                itemHolder.imageButtonEdit = (ImageView) convertView.findViewById(R.id.image_button_edit_payee);
                itemHolder.imageButtonDelete = (ImageView) convertView.findViewById(R.id.image_button_delete_payee);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemHolder) convertView.getTag();
            }
            final String payee = getItem(position);
            itemHolder.imageButtonEdit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AddOrEditPayeeDialog(payee, false).show(getSupportFragmentManager(), "edit_payee");
                }
            });
            itemHolder.imageButtonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DeletePayeeDialog(payee).show(getSupportFragmentManager(), "delete_payee");
                }
            });
            itemHolder.textViewPayeeName.setText(payee);
            return convertView;
        }

        class ItemHolder {
            TextView textViewPayeeName;
            ImageView imageButtonEdit;
            ImageView imageButtonDelete;
        }
    }
}
