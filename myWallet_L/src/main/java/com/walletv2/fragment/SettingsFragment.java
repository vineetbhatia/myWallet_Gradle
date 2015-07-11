package com.walletv2.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.walletv2.activity.IntroductionActivity;
import com.walletv2.activity.R;
import com.walletv2.entity.Constants;
import com.walletv2.entity.Utils;
import com.walletv2.filehandler.FileHandler;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

public class SettingsFragment extends PreferenceFragment {
	private Activity context = null;
	private SwitchPreference switchPreferenceSecureApp = null;
	private Preference preferenceExport = null;
	private Preference preferenceImport = null;
    private Preference preferenceProfileInfo = null;
	private SharedPreferences sharedPreferences;
	private Editor preferencesEditor;
	// Export/Import
	private String[] mFileList;
	private String[] mDisplayList;
	private int no_of_backup_files = 0;
	private final String FTYPE = "expense_database";
	private final int DIALOG_LOAD_FILE = 1000;

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        context = getActivity();
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.global_preferences);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferencesEditor = sharedPreferences.edit();
		initializePreferencesComponents();
		initializePreferencesListeners();
	}

	private void initializePreferencesComponents() {
		PreferenceScreen preferenceScreen = getPreferenceScreen();
		switchPreferenceSecureApp = (SwitchPreference) preferenceScreen.findPreference(getString(R.string.prefs_secure_app_key));
		preferenceExport = preferenceScreen.findPreference(getString(R.string.prefs_export_key));
		preferenceImport = preferenceScreen.findPreference(getString(R.string.prefs_import_key));
        preferenceProfileInfo = preferenceScreen.findPreference(getString(R.string.prefs_profile_info_key));
	}

	private void initializePreferencesListeners() {
		switchPreferenceSecureApp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ((Boolean) newValue)
					createNewPattern();
				return true;
			}
		});
		preferenceExport.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					okAlertDialog(R.string.export_warning_message, Constants.ACTION_ID_EXPORT_TO_SDCARD);
				} else
					Toast.makeText(context, "Please insert/mount your sdCard to take backup", Toast.LENGTH_LONG).show();
				return false;
			}
		});
		preferenceImport.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					okAlertDialog(R.string.import_warning_message, Constants.ACTION_ID_IMPORT_FROM_SDCARD);
				} else
					Toast.makeText(context, "Please insert/mount your sdCard to take backup", Toast.LENGTH_LONG).show();
				return false;
			}
		});
        preferenceProfileInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context, IntroductionActivity.class);
                startActivity(intent);
                return false;
            }
        });
	}

	public void okAlertDialog(int alertStringId, final int actionID) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(alertStringId).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				if (actionID == Constants.ACTION_ID_EXPORT_TO_SDCARD) {
					FileHandler fileHandler = new FileHandler(context);
					int noOfBackup = Integer.parseInt(sharedPreferences.getString(getString(R.string.prefs_no_of_backups_key), "5"));
					if (fileHandler.exportToSdCard(noOfBackup)) {
						fileHandler.exportPreferencesTosdCard();
						Toast.makeText(context, "Export to sdCard successful", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(context, "Export to sdCard unsuccessful", Toast.LENGTH_SHORT).show();
					}
				} else if (actionID == Constants.ACTION_ID_IMPORT_FROM_SDCARD) {
					loadFileList();
					if (mDisplayList != null && mDisplayList.length > 0) {
						Log.i("FileExplorer", "No of existing back-ups files: " + mDisplayList.length);
						createFilePickerDialog(DIALOG_LOAD_FILE);
					} else {
						Toast.makeText(context, "No Backup File Exists", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private Dialog createFilePickerDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new Builder(getActivity());
		switch (id) {
		case DIALOG_LOAD_FILE:
			builder.setTitle("Choose your file");
			Log.i("FileExplorer", "Title set");
			if (mDisplayList == null) {
				Log.e("FileExplorer", "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(mDisplayList, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int itemClicked = no_of_backup_files - which - 1;
					Log.i("FileExplorer", itemClicked + " item Clicked");
					FileHandler fileHandler = new FileHandler(context);
					if (fileHandler.importFromSdCard(mFileList[itemClicked])) {
						fileHandler.importPreferencesTosdCard();
						sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
						preferencesEditor = sharedPreferences.edit();
						preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_DATE_FORMAT_CORRECTED, false);
                        preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_DB_UPGRADED, false);
						preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_LOCK_SET, false);
						preferencesEditor.apply();
						Toast.makeText(context, "Import from sdCard successful", Toast.LENGTH_SHORT).show();
						Utils.restartApplication(context);
						// Now Restart the application
					} else {
						Toast.makeText(context, "Import to sdCard unsuccessful", Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		}
		dialog = builder.show();
		return dialog;
	}

	private void loadFileList() {
		File mPath = new File(Constants.DATABASE_BACKUP_PATH);
		try {
			mPath.mkdirs();
			Log.i("FileExplorer", "making directories");
		} catch (SecurityException e) {
			Log.e("FileExplorer", "unable to write on the sd card " + e.toString());
		}
		if (mPath.exists()) {
			Log.i("FileExplorer", "directory exists");
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.contains(FTYPE);
				}
			};
			mFileList = mPath.list(filter);
		} else {
			mFileList = new String[0];
		}
		Arrays.sort(mFileList);
		no_of_backup_files = mFileList.length;
		mDisplayList = new String[no_of_backup_files];
		for (int i = 0; i < mFileList.length; i++) {
			String fileName = mFileList[i];
			// Log.i("FileExplorer", "Previous File name: "+ fileName);
			String prefix = Constants.DB_NAME + "_";
			if (fileName.startsWith(prefix)) {
				fileName = fileName.replace(prefix, "");
				// Log.i("FileExplorer", "Long File name: "+ fileName);
				// hh.mm on dd.MMM.yyyy
				try {
					fileName = DateFormat.format("hh.mm on dd.MMM.yyyy", Long.parseLong(fileName)).toString();
				} catch (NumberFormatException e) {
					e.printStackTrace();
					Toast.makeText(context, "Something went wrong while fetching backup date and time!", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, "Something went wrong while fetching backup date and time!", Toast.LENGTH_SHORT).show();
				}
				// Log.i("FileExplorer", "Formatted File name"+ fileName);
				mDisplayList[no_of_backup_files - i - 1] = fileName;
				// Log.i("FileExplorer", "Formatted File name in dialplay list"+
				// mDisplayList[i]);
			}
		}
	}

	/**
	 * This function is used to reset the current pattern for pattern lock or to
	 * set the new pattern, if the user is using it for the first time.
	 */
	private void createNewPattern() {
		Toast.makeText(context,
				"Please enter the combination twice to secure the application with pattern lock. This combination will be used to unlock the application.",
				Toast.LENGTH_LONG).show();
		Intent intent = new Intent(context, LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
		intent.putExtra(LockPatternActivity._AutoSave, true);
		startActivityForResult(intent, Constants._ReqCreatePattern);
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants._ReqCreatePattern:
			if (resultCode == Activity.RESULT_OK) {
				// String pattern = data
				// .getStringExtra(LockPatternActivity._Pattern);
				Toast.makeText(context, "Passcode successfully set.", Toast.LENGTH_SHORT).show();
				preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_LOCK_SET, true).commit();
			} else {
				preferencesEditor.putBoolean(Constants.SHARED_PREFERENCES_IS_LOCK_SET, false).commit();
				switchPreferenceSecureApp.setChecked(false);
			}
			break;
		}
	}
}
