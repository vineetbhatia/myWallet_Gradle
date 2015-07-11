package com.walletv2.activity;

import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;

public class AddShortcutActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// create shortcut if requested
		ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.add_expense_icon);
		Intent launchIntent = new Intent(this, AddExpenseActivity.class);
		launchIntent.setAction(Intent.ACTION_MAIN);
		launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Add Expense");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		setResult(RESULT_OK, intent);
		finish();
	}
}
