package com.walletv2.services;

import java.util.Random;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.walletv2.entity.AlarmDetails;
import com.walletv2.entity.Constants;

/**
 * Set an alarm for the date passed into the constructor When the alarm is raised it will start the NotifyService
 * 
 * This uses the android build in alarm manager *NOTE* if the phone is turned off this alarm will be cancelled
 * 
 * This will run on it's own thread.
 * 
 */
public class AlarmTask implements Runnable {
	// The date selected for the alarm
	private final AlarmDetails alarmDetails;
	// The android system alarm manager
	private final AlarmManager am;
	// Your mActivity to retrieve the alarm manager from
	private final Context context;
	
	public AlarmTask(Context context, AlarmDetails alarmDetails) {
		this.context = context;
		this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.alarmDetails = alarmDetails;
	}
	
	@Override
	public void run() {
		// Request to start are service when the alarm date is upon us
		// We don't start an activity as we just want to pop up a notification into the system bar not a full activity
		Intent intent = new Intent(context, NotifyService.class);
		intent.putExtra(Constants.EXTRA_VALUE_NOTIFICATION_TITLE, alarmDetails.getNotificationTitle());
		intent.putExtra(Constants.EXTRA_VALUE_NOTIFICATION_CONTENT, alarmDetails.getNotificationContent());
		intent.putExtra(Constants.EXTRA_VALUE_NOTIFICATION_EXPENSE_PAYEE, alarmDetails.getExpensePayee());
		intent.putExtra(NotifyService.INTENT_NOTIFY, true);
		Log.i("AlarmTask", alarmDetails.toString());
		PendingIntent pendingIntent = PendingIntent.getService(context, new Random().nextInt(), intent,
				PendingIntent.FLAG_ONE_SHOT);
		// Sets an alarm - note this alarm will be lost if the phone is turned off and on again
		am.set(AlarmManager.RTC, alarmDetails.getCalender().getTimeInMillis(), pendingIntent);
	}
}