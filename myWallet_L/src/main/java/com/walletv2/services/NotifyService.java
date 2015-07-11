package com.walletv2.services;

import java.util.Random;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.walletv2.activity.ExpenseListActivity;
import com.walletv2.activity.R;
import com.walletv2.entity.Constants;

/**
 * This service is started when an Alarm has been raised
 * 
 * We pop a notification into the status bar for the user to click on When the
 * user clicks the notification a new activity is opened
 * 
 * @author paul.blundell
 */
public class NotifyService extends Service {
	/**
	 * Class for clients to access
	 */
	public class ServiceBinder extends Binder {
		NotifyService getService() {
			return NotifyService.this;
		}
	}

	// Name of an intent extra we can use to identify if this service was
	// started to create a notification
	public static final String INTENT_NOTIFY = "com.walletv2.services.INTENT_NOTIFY";
	// The system notification manager
	private NotificationManager mNM;

	@Override
	public void onCreate() {
		Log.i("NotifyService", "onCreate()");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// If this service was started by out AlarmTask intent then we want to
		// show our notification
		if (intent.getBooleanExtra(INTENT_NOTIFY, false))
			showNotification(intent.getStringExtra(Constants.EXTRA_VALUE_NOTIFICATION_TITLE),
					intent.getStringExtra(Constants.EXTRA_VALUE_NOTIFICATION_CONTENT), intent.getStringExtra(Constants.EXTRA_VALUE_NOTIFICATION_EXPENSE_PAYEE));
		// We don't care if this service is stopped as we have already delivered
		// our notification
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients
	private final IBinder mBinder = new ServiceBinder();

	/**
	 * Creates a notification and shows it in the OS drag-down status bar
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showNotification(String title, String content, String payee) {
		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent intent = new Intent(this, ExpenseListActivity.class);
		intent.putExtra(Constants.EXTRA_VALUE_PAYEE_NAME, payee);
		intent.putExtra(Constants.EXTRA_VALUE_IS_WALLET_LIST, true);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		Notification notification;
		notification = new Notification.Builder(this).setContentTitle(title).setContentText(content).setSmallIcon(R.drawable.ic_lock_idle_alarm_saver)
				.setContentIntent(contentIntent).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.remind_thumbnail)).setAutoCancel(true)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setStyle(new Notification.BigTextStyle().bigText(content)).build();
		// Send the notification to the system.
		mNM.notify(new Random().nextInt(), notification);
		// Stop the service when we are finished
		stopSelf();
	}
}